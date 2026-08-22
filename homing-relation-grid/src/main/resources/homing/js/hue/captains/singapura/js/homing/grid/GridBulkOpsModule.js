// =============================================================================
// GridBulkOpsModule — RFC 0050's bulk operations: the selection materialised
// to IDENTITY sets, then copy and delete over them. The domain never sees
// (i, j): the view maps order the output, but what leaves this module is
// (PK, columnName) — a TSV of raw values, a PK list for the adapter.
//
//   · copyTsv(): the ACTIVE range (the last one; the cursor cell when no
//     range is up) as tab-separated rows in view order, each value the
//     cell's getValueToCopy() — the RAW form, never the display ("$12.50"
//     copies as "12.5"). Drains the rAF batch first: a read-path must never
//     see stale cell state (the hidden-pane lesson).
//   · selectedRowPks(): the distinct rows any range or the cursor touches,
//     in view order.
//   · deleteSelectedRows(): hands the PK set to adapter.deleteRows, then
//     retires the rows (cells die — the only death) in ONE refresh.
//
// No DOM here — values come from cell objects, structure from the maps.
// =============================================================================

class GridBulkOps {

    /** deps: { cells, maps, selection, adapter, host } — host is the facade
     *  (flushNow / _refresh / _squelch), used only for drain + batch. */
    constructor(deps) {
        deps = deps || {};
        if (!deps.cells || !deps.maps || !deps.selection || !deps.adapter || !deps.host)
            throw new Error("[GridBulkOps] cells, maps, selection, adapter, host are required");
        this._cells = deps.cells;
        this._maps = deps.maps;
        this._selection = deps.selection;
        this._adapter = deps.adapter;
        this._host = deps.host;
    }

    /**
     * The ACTIVE rect in view coordinates: the last range, else the cursor
     * as a 1x1. Null when nothing is selectable (empty view).
     */
    _activeRect() {
        var m = this._maps;
        var ranges = this._selection.rangeList();
        if (ranges.length) {
            var r = ranges[ranges.length - 1];
            var a = m.locate(r.anchor.pk, r.anchor.column);
            var f = m.locate(r.focus.pk, r.focus.column);
            if (a && f) return { i0: Math.min(a.i, f.i), i1: Math.max(a.i, f.i),
                                 j0: Math.min(a.j, f.j), j1: Math.max(a.j, f.j) };
        }
        var cur = this._selection.cursorId();
        var at = cur ? m.locate(cur.pk, cur.column) : null;
        return at ? { i0: at.i, i1: at.i, j0: at.j, j1: at.j } : null;
    }

    /** One cell's clipboard form — the cell's raw value, adapter fallback. */
    _valueToCopy(pk, col) {
        var entry = this._cells.get(pk, col);
        if (entry && typeof entry.cell.getValueToCopy === "function")
            return entry.cell.getValueToCopy();
        var v = this._adapter.get(pk, col);
        return (v == null) ? "" : String(v);
    }

    /** The active rect as TSV, raw values, view order. "" when empty. */
    copyTsv() {
        this._host.flushNow();                       // a read-path drains first
        var rect = this._activeRect();
        if (!rect) return "";
        var m = this._maps, rows = [];
        for (var i = rect.i0; i <= rect.i1; i++) {
            var cols = [];
            for (var j = rect.j0; j <= rect.j1; j++) {
                var id = m.resolve(i, j);
                cols.push(id ? this._valueToCopy(id.pk, id.column) : "");
            }
            rows.push(cols.join("\t"));
        }
        return rows.join("\n");
    }

    /** Distinct PKs of every row a range or the cursor touches, view order. */
    selectedRowPks() {
        var m = this._maps, hit = new Set();
        this._selection.rangeList().forEach(function (r) {
            var a = m.locate(r.anchor.pk, r.anchor.column);
            var f = m.locate(r.focus.pk, r.focus.column);
            if (!a || !f) return;
            for (var i = Math.min(a.i, f.i); i <= Math.max(a.i, f.i); i++) hit.add(i);
        });
        var cur = this._selection.cursorId();
        var at = cur ? m.locate(cur.pk, cur.column) : null;
        if (at) hit.add(at.i);
        return Array.from(hit).sort(function (x, y) { return x - y; })
                .map(function (i) { return m.pkAt(i); });
    }

    /**
     * Delete the selected rows: the ADAPTER gets the identity set first (the
     * domain owns the data), then the rows retire — cells die (the only
     * death), base and views shrink — in a single squelched batch + refresh.
     * Selection self-heals through the recompute. Returns the deleted PKs.
     */
    deleteSelectedRows() {
        var pks = this.selectedRowPks();
        if (!pks.length) return pks;
        if (typeof this._adapter.deleteRows === "function") this._adapter.deleteRows(pks);
        var self = this;
        this._host._squelch = true;
        try {
            pks.forEach(function (pk) {
                self._cells.disposeRow(pk);
                self._maps.removePk(pk);
            });
        } finally { this._host._squelch = false; }
        this._host._refresh("base");
        return pks;
    }
}
