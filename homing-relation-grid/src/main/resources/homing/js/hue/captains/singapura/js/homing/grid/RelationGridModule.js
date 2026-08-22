// =============================================================================
// RelationGridModule — RFC 0050's facade: composes the grid family and is the
// ONLY place the two branches meet. Orchestration only — it holds no map
// logic (GridViewMaps), builds no chrome (GridLayout), owns no cell
// (GridCells); it reads the adapter, threads the pieces, and re-places cell
// content into freshly-minted slots whenever the view changes.
//
//   new RelationGrid({
//       container,          // where the layout mounts
//       branch,             // the CELLS branch (DomOpsParty) — handed in
//       adapter,            // RelationAdapterContract shape (JS): pks(),
//                           //   columns(), get(pk, col), subscribe(fn), …
//       cellFactory?,       // (column, value, meta) → cell; default:
//                           //   number → NumberCell, else TextCell
//       onViewChanged?      // ('rows' | 'columns' | 'base') — after re-place
//   });
//
// Phase 3 surface: static render + view commands (sortBy / filterRows /
// clearFilter / hideColumn / showColumn / reorderColumn — all pure remaps
// recomputed from held command state) + the rAF-batched direct update path.
// Selection, editing, and bulk ops land in Phases 4–6 per the journey.
// =============================================================================

class RelationGrid {

    constructor(opts) {
        opts = opts || {};
        if (!opts.container) throw new Error("[RelationGrid] opts.container is required");
        if (!opts.branch)    throw new Error("[RelationGrid] opts.branch is required");
        if (!opts.adapter)   throw new Error("[RelationGrid] opts.adapter is required");
        this._adapter = opts.adapter;
        this._cbViewChanged = opts.onViewChanged || null;
        this._factory = opts.cellFactory || function (column, value) {
            return (typeof value === "number") ? new NumberCell() : new TextCell();
        };

        var self = this;
        this._squelch = false;   // suppresses interim refreshes inside atomic commands
        this._maps = new GridViewMaps({
            pks:     this._adapter.pks(),
            columns: this._adapter.columns(),
            onViewChanged: function (kind) { if (!self._squelch) self._refresh(kind); }
        });
        this._layout = new GridLayout({ container: opts.container });
        this._cells  = new GridCells({ branch: opts.branch });

        // View-command state — the views are always RECOMPUTED from this (the
        // RFC 0049 lesson applied to remaps: held intent, derived view).
        this._sort        = null;         // { column, direction: 'asc'|'desc' }
        this._filterPred  = null;         // (pk, get) → boolean
        this._hidden      = new Set();    // hidden column names
        this._columnOrder = null;         // full column order incl. hidden, or null = base

        // rAF batch for the direct update path (last write per cell wins).
        this._pendingUpdates = new Map(); // key → { pk, col, value }
        this._flushScheduled = false;

        // The domain's push channel — identity-addressed, straight to the cell.
        this._onCellChanged = function (pk, col, newValue) { self.updateCell(pk, col, newValue); };
        if (typeof this._adapter.subscribe === "function") this._adapter.subscribe(this._onCellChanged);

        this._refresh("base");
    }

    // ── the composition step: structure, then re-place content ──────────────

    _refresh(kind) {
        var maps = this._maps;
        var headers = [];
        for (var j0 = 0; j0 < maps.cols(); j0++) headers.push(maps.columnAt(j0));
        this._layout.render({ headers: headers, rows: maps.rows() });

        // Re-place every visible cell into its (possibly new) slot. ensure() is
        // idempotent — existing cells keep their element + state; only cells
        // never seen before are minted. Filtered-out cells simply are not
        // placed (their elements stay detached, alive, in the cells branch).
        for (var i = 0; i < maps.rows(); i++) {
            for (var j = 0; j < maps.cols(); j++) {
                var id = maps.resolve(i, j);
                this._cells.ensure(id.pk, id.column, this._factory,
                                   this._adapter.get(id.pk, id.column));
                this._cells.place(id.pk, id.column, this._layout.slotAt(i, j));
            }
        }
        // Filter DETACHES, never destroys: whatever the view no longer shows
        // leaves the tree with element, instance, and state intact.
        this._cells.detachInvisible(function (pk, col) { return maps.locate(pk, col) !== null; });
        if (this._cbViewChanged) {
            try { this._cbViewChanged(kind); }
            catch (e) { console.error("[RelationGrid] onViewChanged threw:", e); }
        }
    }

    // ── view commands: held intent → recomputed views (pure remaps) ─────────

    /**
     * Recompute the row view from base + filter + sort. Filter first (a
     * subset), then a STABLE sort over the survivors — equal keys keep their
     * base order, and the tiebreak is never direction-flipped.
     */
    _applyRowView() {
        var self = this;
        var pks = this._maps.basePks().slice();
        if (this._filterPred) {
            pks = pks.filter(function (pk) {
                return !!self._filterPred(pk, function (col) { return self._adapter.get(pk, col); });
            });
        }
        if (this._sort) {
            var col = this._sort.column, desc = this._sort.direction === "desc";
            var baseIdx = new Map();
            pks.forEach(function (pk, i) { baseIdx.set(pk, i); });
            pks.sort(function (a, b) {
                var va = self._adapter.get(a, col), vb = self._adapter.get(b, col);
                if (va === vb) return baseIdx.get(a) - baseIdx.get(b);
                var c = (va < vb) ? -1 : 1;
                return desc ? -c : c;
            });
        }
        this._maps.setRowView(pks);
        return this;
    }

    /** Recompute the column view: the held order (or base), minus hidden. */
    _applyColumnView() {
        var self = this;
        var order = this._columnOrder || this._maps.baseColumns().slice();
        this._maps.setColumnView(order.filter(function (c) { return !self._hidden.has(c); }));
        return this;
    }

    /** Sort by a column ('asc' | 'desc'); sortBy(null) restores base order. */
    sortBy(column, direction) {
        this._sort = column ? { column: column, direction: direction || "asc" } : null;
        return this._applyRowView();
    }

    /** Filter to rows where predicate(pk, get) is truthy — cells detach, never die. */
    filterRows(predicate) {
        if (typeof predicate !== "function") throw new Error("[RelationGrid] filterRows needs a function");
        this._filterPred = predicate;
        return this._applyRowView();
    }

    clearFilter() {
        this._filterPred = null;
        return this._applyRowView();
    }

    hideColumn(column) {
        this._hidden.add(column);
        return this._applyColumnView();
    }

    /** Un-hide: the column returns at its place in the HELD order. */
    showColumn(column) {
        this._hidden.delete(column);
        return this._applyColumnView();
    }

    /** Move a column to toIndex within the full (hidden-inclusive) order. */
    reorderColumn(column, toIndex) {
        var order = (this._columnOrder || this._maps.baseColumns().slice()).slice();
        var from = order.indexOf(column);
        if (from < 0) throw new Error("[RelationGrid] unknown column: " + column);
        order.splice(from, 1);
        order.splice(toIndex, 0, column);
        this._columnOrder = order;
        return this._applyColumnView();
    }

    // ── the direct update path (domain → cell; no layout, no lookup) ────────

    /**
     * Batched per animation frame, LAST WRITE PER CELL WINS — a hot feed (the
     * popularity tick) collapses to one cell.update() per frame. Without a
     * requestAnimationFrame (headless), the flush runs synchronously.
     */
    updateCell(pk, col, newValue) {
        if (!this._cells.get(pk, col)) return false;
        this._pendingUpdates.set(pk + " " + col, { pk: pk, col: col, value: newValue });
        this._scheduleFlush();
        return true;
    }

    _scheduleFlush() {
        if (this._flushScheduled) return;
        this._flushScheduled = true;
        var self = this;
        var raf = (typeof requestAnimationFrame === "function")
                ? requestAnimationFrame
                : function (fn) { fn(); };
        raf(function () { self._flushScheduled = false; self._flushUpdates(); });
    }

    _flushUpdates() {
        var self = this;
        var pending = this._pendingUpdates;
        this._pendingUpdates = new Map();
        pending.forEach(function (u) { self._cells.update(u.pk, u.col, u.value); });
        return this;
    }

    // ── access for the phases above (commands land in Phase 3+) ─────────────

    /** The identity/position seam — Phase 3's view commands drive it. */
    viewMaps() { return this._maps; }

    /** The row left the Relation: base out, view out, cells DIE (the only death). */
    removeRow(pk) {
        this._cells.disposeRow(pk);
        this._maps.removePk(pk);
        return this;
    }

    /**
     * A row entered the Relation — ATOMICALLY obeying any held filter/sort:
     * the interim state where the base append reaches the view unfiltered is
     * squelched, so a row the filter rejects is never rendered (nor minted).
     */
    addRow(pk) {
        this._squelch = true;
        try { this._maps.addPk(pk); } finally { this._squelch = false; }
        if (this._sort || this._filterPred) this._applyRowView();
        else this._refresh("base");
        return this;
    }

    destroy() {
        if (typeof this._adapter.unsubscribe === "function") {
            try { this._adapter.unsubscribe(this._onCellChanged); } catch (e) {}
        }
        this._cells.destroy();
        this._layout.destroy();
    }
}
