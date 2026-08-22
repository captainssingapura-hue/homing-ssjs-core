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
        this._sort = null;            // { column, direction } | null
        this._criteria = new Map();   // column → { op, operand }  (declarative)
        this._rawFilter = null;       // (pk, get) → bool — EPHEMERAL, never saved
        this._hidden = new Set();
        this._order = null;           // full order incl. hidden, null = base
        this._widths = new Map();     // columnName → px
    }

    // ── the recomputed views (moved here from the facade — same intent) ──────

    hasRowIntent() { return this._sort !== null || this._rawFilter !== null || this._criteria.size > 0; }

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

    /** The held order (or base), minus hidden. */
    applyColumnView() {
        var self = this;
        var order = this._order || this._maps.baseColumns().slice();
        this._maps.setColumnView(order.filter(function (c) { return !self._hidden.has(c); }));
        return this;
    }

    // ── the intent mutators (each recomputes) ───────────────────────────────

    sortBy(column, direction) {
        this._sort = column ? { column: column, direction: direction || "asc" } : null;
        return this.applyRowView();
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
            sort: this._sort ? [{ column: this._sort.column, direction: this._sort.direction }] : [],
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
        var s = (vs.sort || [])[0];
        this._sort = (s && known.has(s.column)) ? { column: s.column, direction: s.direction || "asc" } : null;
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
