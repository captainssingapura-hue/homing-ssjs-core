// =============================================================================
// ToggleWidget — on or off. new ToggleWidget(branch, params) → root, dispose
//   params: { name, label, summary?, default?, on?, off? }
// The store holds "true" or "false"; the switch says which with aria-checked,
// which is what the knob's design word moves on.
// =============================================================================

const _toggleOwner = Object.freeze({ toString: () => "toggleWidget" });

class ToggleWidget {
    constructor(branch, params) {
        branch.activate(_toggleOwner);
        var self = this;
        this._params = params || {};
        this._field = new PreferenceField(branch.createBranch("field"), params, { kicker: "toggle", onValue: function (v) { self._draw(v); } });

        var sw = branch.createElement("switch", "button");
        sw.type = "button";
        css.addClass(sw, pv_switch);
        sw.setAttribute("role", "switch");
        var track = branch.createElement("track", "span");
        css.addClass(track, pv_switch_track);
        var knob = branch.createElement("knob", "span");
        css.addClass(knob, pv_switch_knob);
        track.appendChild(knob);
        this._word = branch.createElement("word", "span");
        sw.appendChild(track);
        sw.appendChild(this._word);
        sw.addEventListener("click", function () { self._field.set(self._field.value() === "true" ? "false" : "true"); });
        this._field.body.appendChild(sw);
        this._switch = sw;
        this.root = this._field.root;
        this._draw(this._field.value());
    }

    _draw(v) {
        if (!this._switch) return;      // the field draws once while it is built, before the control exists
        var on = v === "true";
        this._switch.setAttribute("aria-checked", on ? "true" : "false");
        this._word.textContent = on ? (this._params.on || "On") : (this._params.off || "Off");
    }

    dispose() { this._field.dispose(); }
}
