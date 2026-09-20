// =============================================================================
// PreferenceField — what every setting's widget has in common. A branch
// component: the widget makes a sub-branch for it and hands it in.
//
//   new PreferenceField(branch, params, opts)
//
//   params   { name, label, summary?, default? }   name is the steward's name
//   opts     { kicker?, onValue(value) }           onValue draws the control for a
//                                                  value; called now and on every change
//   the instance
//            root          the element the widget holds as its own root
//            body          where the widget puts its control
//            value()       the value in force: address, then store, then default
//            set(value)    a pick: remember(name, value); everyone follows, this included
//            dispose()     stops following the store
//
// The note under the control says where the value came from: pinned by the
// address, chosen here, or the site's default. The reset button forgets the
// pick and is off when there is nothing to forget. Nothing here is a
// channel: the steward is the party, and the field is one of its members.
// =============================================================================

const _fieldOwner = Object.freeze({ toString: () => "preferenceField" });

class PreferenceField {
    constructor(branch, params, opts) {
        if (!branch) throw new Error("[PreferenceField] a branch of its own is required");
        if (!params || !params.name) throw new Error("[PreferenceField] params.name is required");
        var self = this;
        branch.activate(_fieldOwner);
        this._name = params.name;
        this._fallback = params.default == null ? null : String(params.default);
        this._onValue = opts && typeof opts.onValue === "function" ? opts.onValue : null;

        var root = branch.createElement("field", "div");
        var kicker = branch.createElement("kicker", "div");
        css.addClass(kicker, pv_kicker);
        kicker.textContent = (opts && opts.kicker) || params.name;
        root.appendChild(kicker);
        var title = branch.createElement("title", "h2");
        css.addClass(title, pv_title);
        title.textContent = params.label || params.name;
        root.appendChild(title);
        if (params.summary) {
            var summary = branch.createElement("summary", "p");
            css.addClass(summary, pv_summary);
            summary.textContent = params.summary;
            root.appendChild(summary);
        }
        var body = branch.createElement("body", "div");
        root.appendChild(body);
        this._note = branch.createElement("note", "div");
        css.addClass(this._note, pv_note);
        root.appendChild(this._note);
        var actions = branch.createElement("actions", "div");
        css.addClass(actions, pv_actions);
        var reset = new ButtonBuilder().label("Use the site's default").plain()
                .onClick(function () { PreferenceStewardInstance.forget(self._name); });
        this._reset = reset.build(branch.createElement("reset", reset.tag));
        actions.appendChild(this._reset.el);
        root.appendChild(actions);
        this.root = root;
        this.body = body;

        this._stop = PreferenceViewInstance.onChange(function () { self._draw(); });
        this._draw();
    }

    value() { return PreferenceViewInstance.resolve(this._name, this._fallback); }

    set(v) { PreferenceStewardInstance.remember(this._name, v); }

    _draw() {
        var v = this.value();
        var pinned = PreferenceViewInstance.override(this._name);
        var stored = PreferenceViewInstance.preferred(this._name);
        this._note.textContent = pinned ? "Pinned by this page's address: " + pinned + ". A pick here is kept, but this page shows the address's."
                               : stored ? "Chosen: " + stored + "."
                               : "The site's default" + (this._fallback === null ? "." : ": " + this._fallback + ".");
        this._reset.setOn(!!stored);
        if (this._onValue) this._onValue(v);
    }

    dispose() { this._stop(); }
}
