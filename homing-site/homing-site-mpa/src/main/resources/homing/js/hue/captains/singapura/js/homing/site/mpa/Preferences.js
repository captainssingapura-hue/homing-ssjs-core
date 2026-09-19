// =============================================================================
// Preferences — the button on the bar, and the dialog it opens.
//
//   mountPreferences(branch, bar, chrome) → the button, appended to the bar
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

function mountPreferences(branch, bar, chrome) {
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

    var entry = chrome && chrome.preferences;
    if (!entry || !entry.module) { btn.disabled = true; return wrap; }

    var open = null;   // the dialog handle while it is up
    btn.addEventListener("click", function () {
        if (open) return;
        var view = null;
        open = openDialog({
            branch: branch,
            title: "Preferences",
            size: { w: 880, h: 580 },
            content: function (b, body) {
                _loadRegistry(entry).then(function (reg) {
                    if (!open) return null;
                    view = mountPreferencesView(b, body, reg);
                    return view.ready;
                }).then(function (master) {
                    if (master && typeof master.setActive === "function") master.setActive(true);
                }).catch(function (e) { console.error("[preferences] could not open", e); });
                return { dispose: function () { if (view) view.dispose(); view = null; } };
            },
            actions: [{ id: "done", label: "Done", onClick: function (h) { h.close(); } }],
            onClose: function () { open = null; btn.focus(); }
        });
    });
    return wrap;
}
