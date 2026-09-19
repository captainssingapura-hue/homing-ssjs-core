// =============================================================================
// ListMasterWidget — the rigid tree as a flat listbox, depth as indentation.
//
//   construct(branch, params) → { root, onSelect(fn), select(path), setActive(on), dispose() }
//   params: { tree: <canonical rigid tree JSON>, labels: { path: label } }
//
// One row per node in tree order; the row in force is aria-selected, and
// selecting is choosing. ArrowUp/Down, Home and End move; a click chooses
// where it lands. The depth is DATA on the row, through the runtime var
// the row's class indents by.
// =============================================================================

const _owner = Object.freeze({ toString: () => "preferencesList" });

function construct(branch, params) {
    branch.activate(_owner);
    var rows = [], listeners = [], current = -1;

    var list = branch.createElement("list", "div");
    css.addClass(list, pv_list);
    list.setAttribute("role", "listbox");
    list.setAttribute("aria-label", "Preferences");
    list.setAttribute("tabindex", "0");

    (function walk(node, path, depth) {
        var key = path ? path + "/" + node.segment : node.segment;
        var label = (params.labels && params.labels[key]) || node.segment;
        var row = branch.createElement("row-" + rows.length, "div");
        css.addClass(row, pv_list_row);
        row.setAttribute("role", "option");
        row.style.setProperty("--pv-depth", String(depth));   // DATA, via the runtime var
        row.textContent = label;
        var index = rows.length;
        row.addEventListener("click", function () { choose(index); });
        list.appendChild(row);
        rows.push({ key: key, el: row });
        (node.children || []).forEach(function (c) { walk(c, key, depth + 1); });
    })(params.tree, "", 0);

    function choose(i) {
        if (i < 0 || i >= rows.length || i === current) return;
        current = i;
        rows.forEach(function (r, k) { r.el.setAttribute("aria-selected", k === i ? "true" : "false"); });
        list.setAttribute("aria-activedescendant", rows[i].el.id || "");
        listeners.forEach(function (fn) { fn(rows[i].key); });
    }

    list.addEventListener("keydown", function (ev) {
        var next = null;
        if (ev.key === "ArrowDown") next = Math.min(rows.length - 1, current + 1);
        else if (ev.key === "ArrowUp") next = Math.max(0, current - 1);
        else if (ev.key === "Home") next = 0;
        else if (ev.key === "End") next = rows.length - 1;
        if (next === null) return;
        ev.preventDefault();
        choose(next);
    });

    return {
        root: list,
        onSelect: function (fn) { listeners.push(fn); },
        select: function (path) { var i = rows.findIndex(function (r) { return r.key === path; }); if (i >= 0) choose(i); },
        setActive: function (on) { if (on) list.focus(); },
        dispose: function () {}
    };
}
