// =============================================================================
// PreferencesButton — the button on the bar, and the dialog it opens. A
// branch component: the chrome makes a sub-branch for it and hands it in.
//
//   new PreferencesButton(branch, bar, chrome, keyboard)
//     keyboard  the page's KeyboardSteward, the chrome's: the dialog claims the keys
//               on open and hands them to the view, which hands them to the master
//               or the detail widget by where the focus is
//     .el      the button's wrap, appended to the bar
//     .open()  what the button does
//
// chrome.preferences names the site's registry module: { module }. On the
// first open it is imported through the serving context — so it joins the
// page's chain — and its PREFERENCES constant is handed to the view, which
// loads each node's widget as the node is chosen. The dialog's content is
// the view; closing the dialog disposes the view first, then the dialog's
// branch, and the next open builds it again from what the registry says.
//
// Done is a plain action, not the primary, so Enter stays the tree's and
// the widgets'; Escape closes. Nothing is written here: every widget writes
// through the steward, and the page follows the store as it always has, so
// a theme picked in the dialog is worn behind it as it is picked.
// =============================================================================

const _prefsOwner = Object.freeze({ toString: () => "preferencesButton" });
var _registry = null;   // Promise<PREFERENCES>, once per page

function _loadRegistry(entry) {
    if (!_registry) {
        _registry = import(withServingContext(entry.module)).then(function (m) {
            if (!m.PREFERENCES) throw new Error("[preferences] " + entry.module + " exports no PREFERENCES");
            return m.PREFERENCES;
        });
    }
    return _registry;
}

class PreferencesButton {
    constructor(branch, bar, chrome, keyboard) {
        if (!branch) throw new Error("[PreferencesButton] a branch of its own is required");
        this._keyboard = keyboard || null;
        var self = this;
        branch.activate(_prefsOwner);
        this._branch = branch;
        this._open = null;    // the dialog while it is up
        this._opens = 0;

        var wrap = branch.createElement("prefs", "div");
        css.addClass(wrap, mpa_prefs);
        var btn = branch.createElement("prefsBtn", "button");
        btn.type = "button";
        css.addClass(btn, mpa_prefs_btn);
        btn.setAttribute("aria-haspopup", "dialog");
        var mark = branch.createElement("prefsBtnMark", "span");
        css.addClass(mark, mpa_prefs_btn_label);
        mark.textContent = "⚙";
        mark.setAttribute("aria-hidden", "true");
        var word = branch.createElement("prefsBtnWord", "span");
        word.textContent = "Preferences";
        btn.appendChild(mark);
        btn.appendChild(word);
        wrap.appendChild(btn);
        bar.appendChild(wrap);
        this.el = wrap;
        this._btn = btn;

        this._entry = chrome && chrome.preferences;
        if (!this._entry || !this._entry.module) { btn.disabled = true; return; }
        btn.addEventListener("click", function () { self.open(); });
    }

    open() {
        if (this._open || !this._entry) return;
        var self = this, view = null;
        this._open = new Dialog(this._branch.createBranch("dialog" + (++this._opens)), {
            title: "Preferences",
            size: { w: 880, h: 580 },
            keyboard: this._keyboard, keyboardId: "preferences",
            content: function (b, body) {
                _loadRegistry(self._entry).then(function (reg) {
                    if (!self._open) return null;
                    view = new PreferencesView(b.createBranch("view"), body, reg);
                    return view.ready;
                }).then(function (master) {
                    if (master && typeof master.setActive === "function") master.setActive(true);
                }).catch(function (e) { console.error("[preferences] could not open", e); });
                return { onKeydown: function (ev) { return !!(view && view.key(ev)); }, dispose: function () { if (view) view.dispose(); view = null; } };
            },
            actions: [{ id: "done", label: "Done", onClick: function (d) { d.close(); } }],
            onClose: function () { self._open = null; self._btn.focus(); }
        });
    }
}
