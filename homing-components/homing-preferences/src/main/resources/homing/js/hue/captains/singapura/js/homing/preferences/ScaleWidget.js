// =============================================================================
// ScaleWidget — a number in a range. construct(branch, params) → { root, dispose }
//   params: { name, label, summary?, default?, min, max, step?, unit? }
// The store holds the number as a string; the readout shows it with its unit.
// =============================================================================

const _scaleOwner = Object.freeze({ toString: () => "scaleWidget" });

function construct(branch, params) {
    branch.activate(_scaleOwner);
    var unit = params.unit || "";
    var field = preferenceField(branch, params, { kicker: "scale", onValue: draw });

    var readout = branch.createElement("readout", "div");
    css.addClass(readout, pv_readout);
    var range = branch.createElement("range", "input");
    range.type = "range";
    css.addClass(range, pv_range);
    range.min = params.min; range.max = params.max; range.step = params.step || 1;
    range.setAttribute("aria-label", params.label || params.name);
    range.addEventListener("input", function () { readout.textContent = range.value + unit; });
    range.addEventListener("change", function () { field.set(String(range.value)); });
    field.body.appendChild(range);
    field.body.appendChild(readout);

    function draw(v) {
        if (!range) return;   // the field draws once while it is built, before the control exists
        if (v !== null && v !== undefined && String(range.value) !== String(v)) range.value = v;
        readout.textContent = (v == null ? range.value : v) + unit;
    }
    draw(field.value());

    return { root: field.root, dispose: field.dispose };
}
