// =============================================================================
// ChoiceWidget — one of a list. construct(branch, params) → { root, dispose }
//   params: { name, label, summary?, default?, options: [{ value, label, note? }] }
// The options are a listbox; the one in force is aria-selected; a click or
// Enter/Space on an option writes it through the steward, and the field
// redraws when the store changes, wherever the change came from.
// =============================================================================

const _choiceOwner = Object.freeze({ toString: () => "choiceWidget" });

function construct(branch, params) {
    branch.activate(_choiceOwner);
    var options = (params && params.options) || [];
    var rows = [];
    var field = preferenceField(branch, params, { kicker: "choice", onValue: mark });

    var list = branch.createElement("options", "div");
    css.addClass(list, pv_options);
    list.setAttribute("role", "listbox");
    list.setAttribute("aria-label", params.label || params.name);
    options.forEach(function (opt, i) {
        var row = branch.createElement("opt-" + i, "button");
        row.type = "button";
        css.addClass(row, pv_option);
        row.setAttribute("role", "option");
        var text = branch.createElement("opt-" + i + "-label", "span");
        text.textContent = opt.label == null ? String(opt.value) : String(opt.label);
        row.appendChild(text);
        if (opt.note) {
            var note = branch.createElement("opt-" + i + "-note", "span");
            css.addClass(note, pv_option_note);
            note.textContent = String(opt.note);
            row.appendChild(note);
        }
        row.addEventListener("click", function () { field.set(String(opt.value)); });
        list.appendChild(row);
        rows.push({ value: String(opt.value), el: row });
    });
    field.body.appendChild(list);

    function mark(v) {
        rows.forEach(function (r) { r.el.setAttribute("aria-selected", r.value === v ? "true" : "false"); });
    }
    mark(field.value());

    return { root: field.root, dispose: field.dispose };
}
