// =============================================================================
// GridViewStateModule — RFC 0050-ext2: the VIRTUAL LAYER as first-class,
// serializable state. Owns every piece of "what the user arranged" — column
// order, hidden set, widths, sort, filters — as HELD INTENT (the Phase 3
// rule), and recomputes the view maps from it. Nothing here is DOM and
// nothing is a result: a snapshot records criteria, never the permutation
// they produced, so a restore re-derives against current data.
//
//   · FILTERS ARE DECLARATIVE: {column, op, operand} criteria, compiled to a
//     predicate at apply time — because a function cannot be serialized. The
//     raw-predicate escape hatch remains, marked EPHEMERAL: it works, and it
//     never appears in a snapshot nor survives a restore.
//   · SORT NEEDS A COMPARATOR from the Relation's meta —
//     adapter.columnMeta(column).compare — and there is no fallback to raw
//     '<' (ext6): sortBy() refuses a column without one, apply() drops such a
//     sort as drift. Absent values (null / undefined) never reach the
//     comparator: they go LAST, in base order, in BOTH directions — per key.
//     Ties keep base order (stable). Direction is applied here, never by the
//     comparator.
//   · SORT IS A LIST of keys [{column, direction, pinned}] and RANK IS POSITION
//     (Excel's "Sort by, then by"). The invariant every verb keeps: PINNED keys
//     form the prefix, in pinning order; at most one UNPINNED key exists and it
//     is last — the FREE key, which a plain sortBy(column) replaces while the
//     pinned keys survive. That one rule is how the caret tier gets multi-key
//     without a modifier. apply() normalises, so any saved shape restores sane.
//   · WIDTHS KEY ON columnName, so they follow their column through hide /
//     show / reorder for free (clamped to [MIN, MAX]).
//   · snapshot() / apply(vs): the round-trip. apply() is drift-tolerant —
//     unknown columns dropped, missing ones defaulted, a sort or filter
//     naming a vanished column discarded rather than fatal.
// =============================================================================

var _HGR_MIN_W = 40, _HGR_MAX_W = 2000;

/** One criterion compiled to a value test. Unknown op → throws (caller's bug). */
function _hgrCompileOp(op, operand) {
    switch (op) {
        case "eq":  return function (v) { return v === operand; };
        case "ne":  return function (v) { return v !== operand; };
        case "contains":   return function (v) { return String(v).toLowerCase().indexOf(String(operand).toLowerCase()) >= 0; };
        case "beginsWith": return function (v) { return String(v).toLowerCase().indexOf(String(operand).toLowerCase()) === 0; };
        case "endsWith":   return function (v) { var s = String(v).toLowerCase(), o = String(operand).toLowerCase(); return s.length >= o.length && s.lastIndexOf(o) === s.length - o.length; };
        case "gt":  return function (v) { return Number(v) >  Number(operand); };
        case "gte": return function (v) { return Number(v) >= Number(operand); };
        case "lt":  return function (v) { return Number(v) <  Number(operand); };
        case "lte": return function (v) { return Number(v) <= Number(operand); };
        case "between": return function (v) { var n = Number(v); return n >= Number(operand[0]) && n <= Number(operand[1]); };
        case "oneOf":   return function (v) { return operand.indexOf(v) >= 0; };
        default: throw new Error("[GridViewState] unknown filter op: " + op);
    }
}

class GridViewState {

    /** deps: { maps, adapter, host } — host only for the apply() batch. */
    constructor(deps) {
        deps = deps || {};
        if (!deps.maps || !deps.adapter) throw new Error("[GridViewState] maps, adapter are required");
        this._maps = deps.maps;
        this._adapter = deps.adapter;
        this._host = deps.host || null;
        this._sort = [];              // [{ column, direction, pinned }] — pinned prefix + one free key
        this._criteria = new Map();   // column → { op, operand }  (declarative)
        this._rawFilter = null;       // (pk, get) → bool — EPHEMERAL, never saved
        this._hidden = new Set();
        this._order = null;           // full order incl. hidden, null = base
        this._widths = new Map();     // columnName → px
    }

    // ── the recomputed views (moved here from the facade — same intent) ──────

    hasRowIntent() { return this._sort.length > 0 || this._rawFilter !== null || this._criteria.size > 0; }

    _compiledFilter() {
        if (!this._rawFilter && this._criteria.size === 0) return null;
        var tests = [];
        this._criteria.forEach(function (c, column) {
            var t = _hgrCompileOp(c.op, c.operand);
            tests.push(function (get) { return t(get(column)); });
        });
        var raw = this._rawFilter;
        return function (pk, get) {
            for (var k = 0; k < tests.length; k++) if (!tests[k](get)) return false;
            return raw ? !!raw(pk, get) : true;
        };
    }

    /** Filter first (a subset), then a STABLE sort over the survivors. */
    applyRowView() {
        var self = this;
        var pks = this._maps.basePks().slice();
        var filter = this._compiledFilter();
        if (filter) pks = pks.filter(function (pk) {
            return filter(pk, function (col) { return self._adapter.get(pk, col); });
        });
        if (this._sort.length) {
            var keys = this._sort.map(function (k) {
                return { column: k.column, cmp: self._comparatorFor(k.column),
                         desc: k.direction === "desc", vals: new Map() };
            });
            // Values are read ONCE per key — a live relation must not move
            // under the sort — and compared key by key, rank order.
            var baseIdx = new Map();
            pks.forEach(function (pk, i) {
                baseIdx.set(pk, i);
                keys.forEach(function (k) { k.vals.set(pk, self._adapter.get(pk, k.column)); });
            });
            pks.sort(function (a, b) {
                for (var j = 0; j < keys.length; j++) {
                    var k = keys[j], va = k.vals.get(a), vb = k.vals.get(b);
                    var an = va == null, bn = vb == null;
                    if (an || bn) {                                 // absent LAST, both ways, PER KEY
                        if (an && bn) continue;                     // both absent: the next key decides
                        return an ? 1 : -1;
                    }
                    var c = k.cmp(va, vb);
                    if (c) return k.desc ? -c : c;
                }
                return baseIdx.get(a) - baseIdx.get(b);             // stable: ties keep base order
            });
        }
        this._maps.setRowView(pks);
        return this;
    }

    /** The held order (or base), minus hidden. */
    applyColumnView() {
        var self = this;
        var order = this._order || this._maps.baseColumns().slice();
        this._maps.setColumnView(order.filter(function (c) { return !self._hidden.has(c); }));
        return this;
    }

    // ── the intent mutators (each recomputes) ───────────────────────────────

    /**
     * sortBy(column, direction) — the FREE-key sort: a PINNED column flips its
     *     direction in place (rank kept); any other column REPLACES the free
     *     key, and the pinned keys survive. Refuses a column with no
     *     comparator — no silent fallback.
     * sortBy(null)              — clears every key, pinned or not.
     * sortBy([{column, direction, pinned}, …]) — the list setter: absolute,
     *     normalised (see _normalizeKeys). How a "Sort by / Then by" dialog,
     *     or a Move Up / Move Down, lands.
     */
    sortBy(column, direction) {
        if (column === null || column === undefined) { this._sort = []; return this.applyRowView(); }
        if (Array.isArray(column)) { this._sort = this._normalizeKeys(column); return this.applyRowView(); }
        this._comparatorFor(column);                  // validate eagerly — no silent fallback
        var dir = direction === "desc" ? "desc" : "asc", keys = this._sort.slice();
        var at = this._keyIndex(column);
        if (at >= 0 && keys[at].pinned) {
            keys[at] = { column: column, direction: dir, pinned: true };      // in place, rank kept
        } else {
            if (keys.length && !keys[keys.length - 1].pinned) keys.pop();     // the old free key goes
            keys.push({ column: column, direction: dir, pinned: false });     // the new one is last
        }
        this._sort = keys;
        return this.applyRowView();
    }

    /**
     * Pin a sorted column so it SURVIVES the next free-key sort — it becomes
     * the newest, lowest-precedence pin, which is where it already sat. Unpin
     * the only key and it is simply the free key again; unpin one of several
     * and it leaves the list: "pinned" meant "survives the next click", and a
     * click has already happened. Demoting it and evicting the current free
     * key instead would discard the newest action for an older one.
     */
    pinSortKey(column, pinned) {
        var at = this._keyIndex(column);
        if (at < 0) throw new Error("[GridViewState] '" + column + "' is not a sort key — sort by it first");
        var keys = this._sort.slice(), k = keys[at];
        if (pinned !== false)        keys[at] = { column: k.column, direction: k.direction, pinned: true };
        else if (keys.length === 1)  keys[0]  = { column: k.column, direction: k.direction, pinned: false };
        else                         keys.splice(at, 1);
        this._sort = this._normalizeKeys(keys);
        return this.applyRowView();
    }

    /** Drop one key, pinned or free; the others keep their order (ranks close up). */
    removeSortKey(column) {
        var at = this._keyIndex(column);
        if (at < 0) return this;
        var keys = this._sort.slice(); keys.splice(at, 1);
        this._sort = this._normalizeKeys(keys);
        return this.applyRowView();
    }

    _keyIndex(column) {
        for (var i = 0; i < this._sort.length; i++) if (this._sort[i].column === column) return i;
        return -1;
    }

    // ── what the header may read (ext6 column ops) ──────────────────────────

    /** The column's key as { direction, rank, pinned }, or null. */
    sortKey(column) {
        var at = this._keyIndex(column);
        return at < 0 ? null : { direction: this._sort[at].direction, rank: at, pinned: this._sort[at].pinned };
    }
    /** Does the Relation's meta order this column? The header's 'sortable'. */
    canSort(column) { try { this._comparatorFor(column); return true; } catch (e) { return false; } }
    keyCount() { return this._sort.length; }

    /**
     * A key list brought to the invariant: a column sorts once (the first
     * mention wins), every key has a comparator (the setter throws, a lenient
     * restore drops), and every NON-LAST key is pinned — so a list saved by a
     * dialog-shaped tier, or by an older grid that knew no pins, comes back
     * with the prefix pinned and at most the last key free.
     */
    _normalizeKeys(keys, lenient) {
        var out = [], seen = new Set(), self = this;
        (keys || []).forEach(function (k) {
            if (!k || !k.column || seen.has(k.column)) return;
            if (lenient) { try { self._comparatorFor(k.column); } catch (e) { return; } }
            else self._comparatorFor(k.column);
            seen.add(k.column);
            out.push({ column: k.column, direction: k.direction === "desc" ? "desc" : "asc", pinned: !!k.pinned });
        });
        for (var i = 0; i < out.length - 1; i++) out[i].pinned = true;
        return out;
    }

    /** The column's comparator from the Relation's meta — MANDATORY to sort.
     *  Throws with the remedy in the message; there is deliberately no '<'. */
    _comparatorFor(column) {
        var a = this._adapter;
        var meta = (typeof a.columnMeta === "function") ? a.columnMeta(column) : null;
        var cmp = meta ? meta.compare : null;
        if (typeof cmp !== "function")
            throw new Error("[GridViewState] no comparator for column '" + column
                          + "' — the adapter's columnMeta(column) must supply { compare }");
        return cmp;
    }
    setRawFilter(predicate) { this._rawFilter = predicate; return this.applyRowView(); }
    setCriterion(column, op, operand) {
        _hgrCompileOp(op, operand);   // validate the op eagerly
        this._criteria.set(column, { op: op, operand: operand });
        return this.applyRowView();
    }
    clearCriterion(column) { this._criteria.delete(column); return this.applyRowView(); }
    clearAllFilters() { this._rawFilter = null; this._criteria.clear(); return this.applyRowView(); }
    hide(column) { this._hidden.add(column); return this.applyColumnView(); }
    show(column) { this._hidden.delete(column); return this.applyColumnView(); }
    reorder(column, toIndex) {
        var order = (this._order || this._maps.baseColumns().slice()).slice();
        var from = order.indexOf(column);
        if (from < 0) throw new Error("[GridViewState] unknown column: " + column);
        order.splice(from, 1);
        order.splice(toIndex, 0, column);
        this._order = order;
        return this.applyColumnView();
    }

    /**
     * ext1 drag-reorder's seam: move a column so it lands BEFORE the column
     * currently at VISIBLE index t (t == cols() → after the last visible).
     * The held order includes hidden columns, so the visible index is
     * translated through an anchor column — hidden neighbours keep their
     * places relative to the anchor.
     */
    reorderVisible(column, visIndex) {
        var m = this._maps;
        var anchor = (visIndex < m.cols()) ? m.columnAt(visIndex) : null;
        if (anchor === column) return this;               // dropped onto itself
        var order = (this._order || m.baseColumns().slice()).slice();
        var from = order.indexOf(column);
        if (from < 0) return this;
        order.splice(from, 1);
        var at = (anchor === null) ? order.length : order.indexOf(anchor);
        order.splice(at, 0, column);
        this._order = order;
        return this.applyColumnView();
    }

    // ── widths (identity-keyed; the layout consumes the positional form) ────

    setWidth(column, px) {
        this._widths.set(column, Math.max(_HGR_MIN_W, Math.min(_HGR_MAX_W, px)));
        return this;
    }
    widthOr(column, fallback) {
        return this._widths.has(column) ? this._widths.get(column) : fallback;
    }
    /** px | null per VISIBLE column position — the layout's shape. */
    widthsPositional() {
        var out = [], m = this._maps;
        for (var j = 0; j < m.cols(); j++) {
            var c = m.columnAt(j);
            out.push(this._widths.has(c) ? this._widths.get(c) : null);
        }
        return out;
    }

    // ── the round-trip ──────────────────────────────────────────────────────

    /** Everything the user arranged, as a plain JSON-able object. The raw
     *  filter is deliberately ABSENT — ephemeral by contract. */
    snapshot() {
        var widths = {};
        this._widths.forEach(function (px, c) { widths[c] = px; });
        var filters = [];
        this._criteria.forEach(function (c, column) {
            filters.push({ column: column, op: c.op, operand: c.operand });
        });
        return {
            columns: { order: (this._order || this._maps.baseColumns()).slice(),
                       widths: widths, hidden: Array.from(this._hidden) },
            sort: this._sort.map(function (k) {
                return { column: k.column, direction: k.direction, pinned: k.pinned }; }),
            filters: filters
        };
    }

    /**
     * Drift-tolerant restore: intent in, views re-derived against CURRENT
     * data. Unknown columns dropped; missing ones appended in base order; a
     * sort/filter naming a vanished column discarded. Clears the ephemeral
     * raw filter — it does not survive by contract.
     */
    apply(vs) {
        vs = vs || {};
        var base = this._maps.baseColumns(), known = new Set(base);
        var cols = vs.columns || {};
        var order = (cols.order || []).filter(function (c) { return known.has(c); });
        base.forEach(function (c) { if (order.indexOf(c) < 0) order.push(c); });
        this._order = order;
        this._hidden = new Set((cols.hidden || []).filter(function (c) { return known.has(c); }));
        this._widths = new Map();
        var self = this;
        Object.keys(cols.widths || {}).forEach(function (c) {
            if (known.has(c)) self.setWidth(c, cols.widths[c]);
        });
        // Every key that still names a known, comparable column restores at
        // its saved rank; the rest are drift — a vanished column, a lost
        // comparator — dropped one by one, as a filter with an unknown op is.
        // Then the pinned-prefix invariant is re-imposed.
        this._sort = this._normalizeKeys((vs.sort || []).filter(function (k) {
            return k && known.has(k.column); }), true);
        this._criteria = new Map();
        (vs.filters || []).forEach(function (f) {
            if (!known.has(f.column)) return;
            try { _hgrCompileOp(f.op, f.operand); self._criteria.set(f.column, { op: f.op, operand: f.operand }); } catch (e) { /* unknown op: dropped */ }
        });
        this._rawFilter = null;
        if (this._host) this._host._squelch = true;
        try { this.applyColumnView(); this.applyRowView(); }
        finally { if (this._host) this._host._squelch = false; }
        return this;
    }
}
