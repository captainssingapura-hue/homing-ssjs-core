// =============================================================================
// GridViewMapsModule — RFC 0050's identity/position seam. PURE logic: no DOM,
// no events beyond one optional callback, no knowledge of cells or slots.
//
// This class is THE ONLY place (i, j) meets (PK, columnName) — everything
// identity-side (cells, adapter, selection intent) never sees coordinates;
// everything layout-side never sees keys. Two maps:
//
//   i → PK          the row view: order + subset of the base row keys
//   j → columnName  the column view: order + visibility of the base columns
//
// View operations are pure remaps of these arrays — sort/filter reassign the
// row view, hide/reorder reassign the column view; the Relation itself never
// moves. Base mutations (a row entering/leaving the Relation) are the only
// operations that change what EXISTS; a removed PK falls out of the view too.
//
// PKs and column names are opaque strings here (the TYPED key lives in the
// Java contract; by the time keys reach JS they are their stable string
// forms). Uniqueness is enforced on construction and on every remap.
//
//   new GridViewMaps({ pks, columns, onViewChanged? })
// =============================================================================

class GridViewMaps {

    constructor(opts) {
        opts = opts || {};
        this._basePks     = this._checkUnique(opts.pks || [], "pks");
        this._baseColumns = this._checkUnique(opts.columns || [], "columns");
        // onViewChanged(kind) — kind: 'rows' | 'columns' | 'base'. The layout
        // subscribes to re-place; this class only reports, never renders.
        this._onViewChanged = opts.onViewChanged || null;
        this._rowView = this._basePks.slice();
        this._colView = this._baseColumns.slice();
        this._rowIndex = null;   // pk  → i cache (rebuilt lazily)
        this._colIndex = null;   // col → j cache
    }

    // ── the base Relation axes (identity — what exists) ──────────────────────

    basePks()     { return this._basePks.slice(); }
    baseColumns() { return this._baseColumns.slice(); }

    /** A row entered the Relation: appended to the base AND to the row view. */
    addPk(pk) {
        if (this._basePks.indexOf(pk) >= 0) throw new Error("[GridViewMaps] duplicate pk: " + pk);
        this._basePks.push(pk);
        this._rowView.push(pk);
        this._rowIndex = null;
        this._fire("base");
        return this;
    }

    /** A row left the Relation: gone from the base and from any view of it. */
    removePk(pk) {
        this._basePks = this._basePks.filter(function (p) { return p !== pk; });
        this._rowView = this._rowView.filter(function (p) { return p !== pk; });
        this._rowIndex = null;
        this._fire("base");
        return this;
    }

    // ── the view (position — what shows, where) ──────────────────────────────

    rows() { return this._rowView.length; }
    cols() { return this._colView.length; }

    pkAt(i)     { return (i >= 0 && i < this._rowView.length) ? this._rowView[i] : null; }
    columnAt(j) { return (j >= 0 && j < this._colView.length) ? this._colView[j] : null; }

    /** The view row of a PK, or -1 (filtered out / unknown). */
    rowOf(pk)   { return this._index(this._rowView, "_rowIndex", pk); }
    /** The view column of a name, or -1 (hidden / unknown). */
    colOf(name) { return this._index(this._colView, "_colIndex", name); }

    /** (i, j) → { pk, column }, or null outside the view. */
    resolve(i, j) {
        var pk = this.pkAt(i), col = this.columnAt(j);
        return (pk != null && col != null) ? { pk: pk, column: col } : null;
    }

    /** (pk, column) → { i, j }, or null when either is not in view. */
    locate(pk, column) {
        var i = this.rowOf(pk), j = this.colOf(column);
        return (i >= 0 && j >= 0) ? { i: i, j: j } : null;
    }

    // ── remaps (sort / filter / hide / reorder — pure reassignments) ─────────

    /**
     * Reassign the row view: an ordered subset of the base PKs (a sort is a
     * permutation, a filter a subset, a filtered sort both). Unknown or
     * duplicated PKs are rejected — the view can only show what exists, once.
     */
    setRowView(pks) {
        this._rowView = this._checkSubset(pks, this._basePks, "row view");
        this._rowIndex = null;
        this._fire("rows");
        return this;
    }

    /** Restore the base row order, unfiltered. */
    resetRowView() { return this.setRowView(this._basePks.slice()); }

    /**
     * Reassign the column view: an ordered subset of the base columns (hide =
     * omit, reorder = permute).
     */
    setColumnView(names) {
        this._colView = this._checkSubset(names, this._baseColumns, "column view");
        this._colIndex = null;
        this._fire("columns");
        return this;
    }

    /** Restore the base column order, all visible. */
    resetColumnView() { return this.setColumnView(this._baseColumns.slice()); }

    // ── internals ────────────────────────────────────────────────────────────

    _index(view, cacheField, key) {
        if (this[cacheField] == null) {
            var m = new Map();
            for (var k = 0; k < view.length; k++) m.set(view[k], k);
            this[cacheField] = m;
        }
        var i = this[cacheField].get(key);
        return (i === undefined) ? -1 : i;
    }

    _checkUnique(arr, what) {
        var seen = new Map();
        for (var k = 0; k < arr.length; k++) {
            if (seen.has(arr[k])) throw new Error("[GridViewMaps] duplicate in " + what + ": " + arr[k]);
            seen.set(arr[k], true);
        }
        return arr.slice();
    }

    _checkSubset(arr, base, what) {
        var out = this._checkUnique(arr, what);
        var baseSet = new Map();
        for (var k = 0; k < base.length; k++) baseSet.set(base[k], true);
        for (var m = 0; m < out.length; m++) {
            if (!baseSet.has(out[m])) throw new Error("[GridViewMaps] " + what + " references unknown key: " + out[m]);
        }
        return out;
    }

    _fire(kind) {
        if (!this._onViewChanged) return;
        try { this._onViewChanged(kind); }
        catch (e) { console.error("[GridViewMaps] onViewChanged threw:", e); }
    }
}
