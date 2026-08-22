// =============================================================================
// GridSelectionModule — RFC 0050's selection: the RFC 0049 pipeline one level
// down. Intent is held in IDENTITY space (cursor {pk, column}, ranges as
// anchor/focus identity pairs); a TOTAL resolver recomputes the view-space
// answer after every event; a reconciler hands the resolved paint set to the
// layout (this class NEVER touches DOM — the module-map boundary rule).
//
// The invariant, one level down: exactly one cursor whenever the view is
// non-empty; ranges are all shallow; at most one editing (deep) cell arrives
// in Phase 5, and deep ⇒ cell.
//
// D5 (settled here, v1 lean): a view REMAP resets the ranges; only the cursor
// survives — by identity when its cell is still visible, else by clamping its
// last resolved position into the new view (total: some cell is always the
// cursor while any cell exists). Base changes re-resolve without resetting.
//
//   new GridSelection({ maps, onPaint, onCursorMoved, onSelectionChanged });
// =============================================================================

class GridSelection {

    constructor(opts) {
        opts = opts || {};
        if (!opts.maps) throw new Error("[GridSelection] opts.maps is required");
        this._maps = opts.maps;
        this._cbPaint     = opts.onPaint || null;
        this._cbCursor    = opts.onCursorMoved || null;
        this._cbSelection = opts.onSelectionChanged || null;

        // Intent — identity space only.
        this._cursor = null;        // { pk, column } | null
        this._ranges = [];          // [{ anchor: {pk, column}, focus: {pk, column} }]

        // Resolver memos: the clamp fallback + the change-detection baselines.
        this._lastCursorIJ = null;  // { i, j } of the last successful resolve
        this._lastFired = { cursor: "null", selection: "[]" };
    }

    // ── intent operations (all end in recompute) ────────────────────────────

    /** Plain select: cursor here, ranges collapse. */
    setCursor(pk, column) {
        this._cursor = { pk: pk, column: column };
        this._ranges = [];
        return this.recompute();
    }

    /** Shift-select: the active range's focus moves; the cursor is its anchor. */
    extendTo(pk, column) {
        if (!this._cursor) return this.setCursor(pk, column);
        if (!this._ranges.length) {
            this._ranges = [{ anchor: { pk: this._cursor.pk, column: this._cursor.column },
                              focus:  { pk: pk, column: column } }];
        } else {
            this._ranges[this._ranges.length - 1].focus = { pk: pk, column: column };
        }
        return this.recompute();
    }

    /** Ctrl-select: a new 1x1 range joins the list; the cursor moves to it. */
    addRange(pk, column) {
        if (!this._ranges.length && this._cursor) {
            this._ranges.push({ anchor: { pk: this._cursor.pk, column: this._cursor.column },
                                focus:  { pk: this._cursor.pk, column: this._cursor.column } });
        }
        this._ranges.push({ anchor: { pk: pk, column: column }, focus: { pk: pk, column: column } });
        this._cursor = { pk: pk, column: column };
        return this.recompute();
    }

    /** One range over the whole visible view. */
    selectAll() {
        var m = this._maps;
        if (!m.rows() || !m.cols()) return this;
        var a = m.resolve(0, 0), f = m.resolve(m.rows() - 1, m.cols() - 1);
        this._ranges = [{ anchor: { pk: a.pk, column: a.column },
                          focus:  { pk: f.pk, column: f.column } }];
        if (!this._cursor) this._cursor = { pk: a.pk, column: a.column };
        return this.recompute();
    }

    /**
     * Relative cursor move with edge confinement; extend=true moves the
     * active range's focus instead (the cursor stays, Excel-style).
     */
    move(di, dj, extend) {
        var m = this._maps;
        if (!m.rows() || !m.cols()) return this;
        var from = extend && this._ranges.length
                 ? m.locate(this._ranges[this._ranges.length - 1].focus.pk,
                            this._ranges[this._ranges.length - 1].focus.column)
                 : (this._cursor ? m.locate(this._cursor.pk, this._cursor.column) : null);
        if (!from) from = this._lastCursorIJ || { i: 0, j: 0 };
        var i = Math.max(0, Math.min(m.rows() - 1, from.i + di));
        var j = Math.max(0, Math.min(m.cols() - 1, from.j + dj));
        var id = m.resolve(i, j);
        return extend ? this.extendTo(id.pk, id.column) : this.setCursor(id.pk, id.column);
    }

    /** The facade reports a view change. D5: remaps reset ranges. */
    onViewChanged(kind) {
        if (kind !== "base") this._ranges = [];
        return this.recompute();
    }

    // ── the total resolver + reconciler ─────────────────────────────────────

    /**
     * TOTAL: always yields the definitive answer for the current view — the
     * cursor self-heals by identity, then by position clamp, then to (0,0);
     * a range whose corner left the view drops. Fires paint always (the
     * layout diffs) and the typed events only on actual change.
     */
    recompute() {
        var m = this._maps, resolved = this._resolve(m);
        this._lastCursorIJ = resolved.cursorIJ || this._lastCursorIJ;
        if (this._cbPaint) this._cbPaint(resolved);
        var cursorKey = JSON.stringify(this._cursor);
        var selKey = JSON.stringify(resolved.ranges);
        if (cursorKey !== this._lastFired.cursor && this._cbCursor) {
            this._cbCursor(this._cursor ? this._cursor.pk : null,
                           this._cursor ? this._cursor.column : null);
        }
        if (selKey !== this._lastFired.selection && this._cbSelection) {
            this._cbSelection(resolved.ranges);
        }
        this._lastFired = { cursor: cursorKey, selection: selKey };
        return this;
    }

    _resolve(m) {
        if (!m.rows() || !m.cols()) {
            this._cursor = null;
            return { cursorIJ: null, cells: [], ranges: [] };
        }
        var at = this._cursor ? m.locate(this._cursor.pk, this._cursor.column) : null;
        if (!at) {
            var clamp = this._lastCursorIJ || { i: 0, j: 0 };
            at = { i: Math.max(0, Math.min(m.rows() - 1, clamp.i)),
                   j: Math.max(0, Math.min(m.cols() - 1, clamp.j)) };
            var id = m.resolve(at.i, at.j);
            this._cursor = { pk: id.pk, column: id.column };   // identity self-heal
        }
        var cells = [], seen = new Set(), ranges = [];
        var mark = function (i, j) {
            var k = i + "," + j;
            if (!seen.has(k)) { seen.add(k); cells.push({ i: i, j: j }); }
        };
        this._ranges = this._ranges.filter(function (r) {
            var a = m.locate(r.anchor.pk, r.anchor.column);
            var f = m.locate(r.focus.pk, r.focus.column);
            if (!a || !f) return false;                        // a corner left the view
            for (var i = Math.min(a.i, f.i); i <= Math.max(a.i, f.i); i++)
                for (var j = Math.min(a.j, f.j); j <= Math.max(a.j, f.j); j++) mark(i, j);
            ranges.push({ anchor: r.anchor, focus: r.focus });
            return true;
        });
        mark(at.i, at.j);
        return { cursorIJ: at, cells: cells, ranges: ranges };
    }

    // ── read access ─────────────────────────────────────────────────────────

    cursorId() { return this._cursor ? { pk: this._cursor.pk, column: this._cursor.column } : null; }
    rangeList() { return this._ranges.map(function (r) { return { anchor: r.anchor, focus: r.focus }; }); }
}
