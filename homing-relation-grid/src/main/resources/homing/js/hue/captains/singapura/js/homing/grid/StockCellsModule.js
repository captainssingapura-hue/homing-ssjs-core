// =============================================================================
// StockCellsModule — RFC 0050's stock cell renderers: TextCell, NumberCell,
// EnumCell. Each implements the GridCellContract's required set plus the edit
// trio (beginEdit / commitEdit / cancelEdit): a cell renders its OWN editor
// inside its own element (the cells branch owns content — the editor is a
// sub-life of the cell, never the layout's business). onSelect stays PURE
// lifecycle — it never focuses.
//
// The three value forms (NumberCell): raw 12.5 (getValue) vs the display
// "$12.50" (what renders) vs the clipboard "12.5" (getValueToCopy) — a copied
// price must paste as a number, not as its formatting.
//
// While an editor is open, update() records the pushed value but leaves the
// editor alone (the direct path must never clobber a user's keystroke); the
// recorded value shows the moment the edit ends.
// =============================================================================

class TextCell {

    constructor() { this._el = null; this._value = null; this._editing = false; this._input = null; }

    _paint() { if (this._el) this._el.textContent = (this._value == null) ? "" : String(this._value); }

    render(host, value) { this._el = host; this._value = value; this._paint(); return this; }

    update(value) {
        this._value = value;
        if (!this._editing) this._paint();
        return this;
    }

    onSelect(mode) { /* pure lifecycle — visuals are the layout's, focus is placed for us */ }

    beginEdit(current) {
        if (!this._el || this._editing) return;
        this._editing = true;
        var input = document.createElement("input");
        input.value = (current == null) ? "" : String(current);
        this._input = input;
        this._el.textContent = "";
        this._el.appendChild(input);
        if (input.focus) input.focus();
    }

    commitEdit() {
        var v = this._input ? this._input.value : this._value;
        this._teardown();
        this._value = v;
        this._paint();
        return v;
    }

    cancelEdit() { this._teardown(); this._paint(); }

    _teardown() {
        if (this._input && this._input.parentNode) this._input.parentNode.removeChild(this._input);
        this._input = null;
        this._editing = false;
    }

    getValue()       { return this._value; }
    getValueToCopy() { return (this._value == null) ? "" : String(this._value); }
    getEditValue()   { return this._value; }

    dispose() { this._el = null; this._input = null; }
}

class NumberCell {

    /** opts.format — optional (rawNumber) → display string. */
    constructor(opts) {
        this._format = (opts && typeof opts.format === "function") ? opts.format : null;
        this._el = null; this._value = null; this._editing = false; this._input = null;
    }

    _display(value) {
        if (value == null) return "";
        return this._format ? this._format(value) : String(value);
    }

    _paint() { if (this._el) this._el.textContent = this._display(this._value); }

    render(host, value) { this._el = host; this._value = value; this._paint(); return this; }

    update(value) {
        this._value = value;
        if (!this._editing) this._paint();
        return this;
    }

    onSelect(mode) { /* pure lifecycle */ }

    /** The editor shows the RAW number (getEditValue form), not the display. */
    beginEdit(current) {
        if (!this._el || this._editing) return;
        this._editing = true;
        var input = document.createElement("input");
        input.value = (current == null) ? "" : String(current);
        this._input = input;
        this._el.textContent = "";
        this._el.appendChild(input);
        if (input.focus) input.focus();
    }

    /** A non-numeric entry commits as a no-change (the old value stands). */
    commitEdit() {
        var raw = this._input ? this._input.value : null;
        this._teardown();
        var n = Number(raw);
        if (raw !== null && raw !== "" && !isNaN(n)) this._value = n;
        this._paint();
        return this._value;
    }

    cancelEdit() { this._teardown(); this._paint(); }

    _teardown() {
        if (this._input && this._input.parentNode) this._input.parentNode.removeChild(this._input);
        this._input = null;
        this._editing = false;
    }

    getValue()       { return this._value; }
    /** Clipboard form is the RAW number — never the formatted display. */
    getValueToCopy() { return (this._value == null) ? "" : String(this._value); }
    getEditValue()   { return this._value; }

    dispose() { this._el = null; this._input = null; }
}

class EnumCell {

    /** opts.options — the closed value set; the editor is a <select> over it. */
    constructor(opts) {
        this._options = (opts && opts.options) || [];
        this._el = null; this._value = null; this._editing = false; this._select = null;
    }

    _paint() { if (this._el) this._el.textContent = (this._value == null) ? "" : String(this._value); }

    render(host, value) { this._el = host; this._value = value; this._paint(); return this; }

    update(value) {
        this._value = value;
        if (!this._editing) this._paint();
        return this;
    }

    onSelect(mode) { /* pure lifecycle */ }

    beginEdit(current) {
        if (!this._el || this._editing) return;
        this._editing = true;
        var sel = document.createElement("select");
        for (var k = 0; k < this._options.length; k++) {
            var o = document.createElement("option");
            o.value = this._options[k];
            o.textContent = this._options[k];
            sel.appendChild(o);
        }
        sel.value = (current == null) ? this._value : current;
        this._select = sel;
        this._el.textContent = "";
        this._el.appendChild(sel);
        if (sel.focus) sel.focus();
    }

    commitEdit() {
        var v = this._select ? this._select.value : this._value;
        this._teardown();
        this._value = v;
        this._paint();
        return v;
    }

    cancelEdit() { this._teardown(); this._paint(); }

    _teardown() {
        if (this._select && this._select.parentNode) this._select.parentNode.removeChild(this._select);
        this._select = null;
        this._editing = false;
    }

    getValue()       { return this._value; }
    getValueToCopy() { return (this._value == null) ? "" : String(this._value); }
    getEditValue()   { return this._value; }

    dispose() { this._el = null; this._select = null; }
}
