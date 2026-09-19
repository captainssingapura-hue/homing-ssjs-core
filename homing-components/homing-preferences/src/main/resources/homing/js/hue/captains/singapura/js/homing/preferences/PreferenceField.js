// =============================================================================
// PreferenceField — what every setting's widget has in common.
//
//   preferenceField(branch, params, opts) → field
//
//   params   { name, label, summary?, default? }   name is the steward's name
//   opts     { kicker?, onValue(value) }           onValue draws the control for a
//                                                  value; called now and on every change
//   field    root          the element the widget returns as its own root
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

function preferenceField(branch, params, opts) {
    if (!params || !params.name) throw new Error("preferenceField: params.name is required");
    var name = params.name, fallback = params.default == null ? null : String(params.default);
    var o = opts || {};

    var root = branch.createElement("field", "div");
    var kicker = branch.createElement("kicker", "div");
    css.addClass(kicker, pv_kicker);
    kicker.textContent = o.kicker || name;
    root.appendChild(kicker);
    var title = branch.createElement("title", "h2");
    css.addClass(title, pv_title);
    title.textContent = params.label || name;
    root.appendChild(title);
    if (params.summary) {
        var summary = branch.createElement("summary", "p");
        css.addClass(summary, pv_summary);
        summary.textContent = params.summary;
        root.appendChild(summary);
    }
    var body = branch.createElement("body", "div");
    root.appendChild(body);
    var note = branch.createElement("note", "div");
    css.addClass(note, pv_note);
    root.appendChild(note);
    var actions = branch.createElement("actions", "div");
    css.addClass(actions, pv_actions);
    var reset = Button(branch, "reset", { label: "Use the site's default", kind: "plain",
                                          onClick: function () { PreferenceStewardInstance.forget(name); } });
    actions.appendChild(reset);
    root.appendChild(actions);

    function value() { return PreferenceViewInstance.resolve(name, fallback); }

    function draw() {
        var v = value();
        var pinned = PreferenceViewInstance.override(name);
        var stored = PreferenceViewInstance.preferred(name);
        note.textContent = pinned ? "Pinned by this page's address: " + pinned + ". A pick here is kept, but this page shows the address's."
                         : stored ? "Chosen: " + stored + "."
                         : "The site's default" + (fallback === null ? "." : ": " + fallback + ".");
        setButtonOn(reset, !!stored);
        if (typeof o.onValue === "function") o.onValue(v);
    }

    var stop = PreferenceViewInstance.onChange(draw);
    draw();

    return Object.freeze({
        root: root,
        body: body,
        value: value,
        set: function (v) { PreferenceStewardInstance.remember(name, v); },
        dispose: function () { stop(); }
    });
}
