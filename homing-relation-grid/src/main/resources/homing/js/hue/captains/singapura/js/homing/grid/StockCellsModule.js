// =============================================================================
// StockCellsModule — RFC 0050's stock cell renderers: the plain cells most
// columns use. Each implements the GridCellContract's REQUIRED set (render /
// update / onSelect / getValue / getValueToCopy / dispose); the edit trio and
// onAction arrive with Phase 5. onSelect is PURE lifecycle — it never focuses
// (the layout places focus; the RFC 0049 caveat-1 lesson, contract-level).
//
// The three value forms in action (NumberCell): raw 12.5 (getValue) vs the
// display "12.50" (what renders) vs the clipboard "12.5" (getValueToCopy) —
// a copied price must paste as a number, not as its formatting.
// =============================================================================

class TextCell {

    constructor() { this._el = null; this._value = null; }

    render(host, value) {
        this._el = host;
        this._value = value;
        host.textContent = (value == null) ? "" : String(value);
        return this;
    }

    update(value) {
        this._value = value;
        if (this._el) this._el.textContent = (value == null) ? "" : String(value);
        return this;
    }

    onSelect(mode) { /* pure lifecycle — visuals are the layout's, focus is placed for us */ }

    getValue()       { return this._value; }
    getValueToCopy() { return (this._value == null) ? "" : String(this._value); }

    dispose() { this._el = null; }
}

class NumberCell {

    /** opts.format — optional (rawNumber) → display string. */
    constructor(opts) {
        this._format = (opts && typeof opts.format === "function") ? opts.format : null;
        this._el = null;
        this._value = null;
    }

    _display(value) {
        if (value == null) return "";
        return this._format ? this._format(value) : String(value);
    }

    render(host, value) {
        this._el = host;
        this._value = value;
        host.textContent = this._display(value);
        return this;
    }

    update(value) {
        this._value = value;
        if (this._el) this._el.textContent = this._display(value);
        return this;
    }

    onSelect(mode) { /* pure lifecycle */ }

    getValue()       { return this._value; }
    /** Clipboard form is the RAW number — never the formatted display. */
    getValueToCopy() { return (this._value == null) ? "" : String(this._value); }

    dispose() { this._el = null; }
}
