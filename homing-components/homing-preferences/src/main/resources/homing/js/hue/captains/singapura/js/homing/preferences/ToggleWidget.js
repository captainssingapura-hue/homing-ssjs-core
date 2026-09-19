// =============================================================================
// ToggleWidget — on or off. construct(branch, params) → { root, dispose }
//   params: { name, label, summary?, default?, on?, off? }
// The store holds "true" or "false"; the switch says which with aria-checked,
// which is what the knob's design word moves on.
// =============================================================================

const _toggleOwner = Object.freeze({ toString: () => "toggleWidget" });

function construct(branch, params) {
    branch.activate(_toggleOwner);
    var field = preferenceField(branch, params, { kicker: "toggle", onValue: draw });

    var sw = branch.createElement("switch", "button");
    sw.type = "button";
    css.addClass(sw, pv_switch);
    sw.setAttribute("role", "switch");
    var track = branch.createElement("track", "span");
    css.addClass(track, pv_switch_track);
    var knob = branch.createElement("knob", "span");
    css.addClass(knob, pv_switch_knob);
    track.appendChild(knob);
    var word = branch.createElement("word", "span");
    sw.appendChild(track);
    sw.appendChild(word);
    sw.addEventListener("click", function () { field.set(field.value() === "true" ? "false" : "true"); });
    field.body.appendChild(sw);

    function draw(v) {
        if (!sw) return;      // the field draws once while it is built, before the control exists
        var on = v === "true";
        sw.setAttribute("aria-checked", on ? "true" : "false");
        word.textContent = on ? (params.on || "On") : (params.off || "Off");
    }
    draw(field.value());

    return { root: field.root, dispose: field.dispose };
}
