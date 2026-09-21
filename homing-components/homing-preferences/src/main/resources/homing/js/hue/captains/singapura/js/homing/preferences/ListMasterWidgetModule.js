// =============================================================================
// ListMasterWidget — the rigid tree as a flat listbox, depth as indentation.
//
//   new ListMasterWidget(branch, params) → root, onSelect(fn), select(path), key(ev), setActive(on), dispose()
//   params: { tree: <canonical rigid tree JSON>, labels: { path: label } }
//
// One row per node in tree order; the row in force is aria-selected, and
// selecting is choosing. ArrowUp/Down, Home and End move, by key(ev) from
// whoever holds the keys for the list — the view, in the dialog; no keydown
// listener of its own — and a click chooses where it lands. The depth is DATA on the row, through the runtime var
// the row's class indents by.
// =============================================================================

const _owner = Object.freeze({ toString: () => "preferencesList" });

class ListMasterWidget {
    constructor(branch, params) {
        branch.activate(_owner);
        var self = this;
        this._rows = [];
        this._listeners = [];
        this._current = -1;

        var list = branch.createElement("list", "div");
        css.addClass(list, pv_list);
        list.setAttribute("role", "listbox");
        list.setAttribute("aria-label", "Preferences");
        list.setAttribute("tabindex", "0");
        this._list = list;

        (function walk(node, path, depth) {
            var key = path ? path + "/" + node.segment : node.segment;
            var label = (params.labels && params.labels[key]) || node.segment;
            var row = branch.createElement("row-" + self._rows.length, "div");
            css.addClass(row, pv_list_row);
            row.setAttribute("role", "option");
            row.style.setProperty("--pv-depth", String(depth));   // DATA, via the runtime var
            row.textContent = label;
            var index = self._rows.length;
            row.addEventListener("click", function () { self._choose(index); });
            list.appendChild(row);
            self._rows.push({ key: key, el: row });
            (node.children || []).forEach(function (c) { walk(c, key, depth + 1); });
        })(params.tree, "", 0);

        this.root = list;
    }

    /** A keydown from whoever holds the keys for the list: the arrows, Home and End move the row in force; true when taken. */
    key(ev) {
        var next = null, rows = this._rows, current = this._current;
        if (!ev) return false;
        if (ev.key === "ArrowDown") next = Math.min(rows.length - 1, current + 1);
        else if (ev.key === "ArrowUp") next = Math.max(0, current - 1);
        else if (ev.key === "Home") next = 0;
        else if (ev.key === "End") next = rows.length - 1;
        if (next === null) return false;
        this._choose(next);
        return true;
    }

    _choose(i) {
        var rows = this._rows;
        if (i < 0 || i >= rows.length || i === this._current) return;
        this._current = i;
        rows.forEach(function (r, k) { r.el.setAttribute("aria-selected", k === i ? "true" : "false"); });
        this._list.setAttribute("aria-activedescendant", rows[i].el.id || "");
        this._listeners.forEach(function (fn) { fn(rows[i].key); });
    }

    onSelect(fn) { this._listeners.push(fn); }
    select(path) { var i = this._rows.findIndex(function (r) { return r.key === path; }); if (i >= 0) this._choose(i); }
    setActive(on) { if (on) this._list.focus(); }
    dispose() {}
}
