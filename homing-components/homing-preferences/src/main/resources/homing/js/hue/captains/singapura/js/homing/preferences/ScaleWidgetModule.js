// =============================================================================
// ScaleWidget — a number in a range. new ScaleWidget(branch, params) → root, dispose
//   params: { name, label, summary?, default?, min, max, step?, unit? }
// The store holds the number as a string; the readout shows it with its unit.
// =============================================================================

const _scaleOwner = Object.freeze({ toString: () => "scaleWidget" });

class ScaleWidget {
    constructor(branch, params) {
        branch.activate(_scaleOwner);
        var self = this;
        this._unit = params.unit || "";
        this._field = new PreferenceField(branch.createBranch("field"), params, { kicker: "scale", onValue: function (v) { self._draw(v); } });

        this._readout = branch.createElement("readout", "div");
        css.addClass(this._readout, pv_readout);
        var range = branch.createElement("range", "input");
        range.type = "range";
        css.addClass(range, pv_range);
        range.min = params.min; range.max = params.max; range.step = params.step || 1;
        range.setAttribute("aria-label", params.label || params.name);
        range.addEventListener("input", function () { self._readout.textContent = range.value + self._unit; });
        range.addEventListener("change", function () { self._field.set(String(range.value)); });
        this._field.body.appendChild(range);
        this._field.body.appendChild(this._readout);
        this._range = range;
        this.root = this._field.root;
        this._draw(this._field.value());
    }

    _draw(v) {
        var range = this._range;
        if (!range) return;   // the field draws once while it is built, before the control exists
        if (v !== null && v !== undefined && String(range.value) !== String(v)) range.value = v;
        this._readout.textContent = (v == null ? range.value : v) + this._unit;
    }

    dispose() { this._field.dispose(); }
}
