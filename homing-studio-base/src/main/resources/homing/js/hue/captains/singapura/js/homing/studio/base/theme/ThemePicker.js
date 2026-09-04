// ThemePicker.js
//
// The theme picker, in two shapes from one implementation:
//
//   mountThemePickerButton(host, opts)  header trigger + modal — the chrome
//   mountThemePickerTree(host, opts)    the bare tree — the themes app
//
// Both draw the same grouped tree, so the two hosts cannot drift apart.
//
// Owns its own DomOpsParty branch and its own typed CSS group, so a host lends
// it neither. Every colour is a theme token: the picker is what you use to
// change the theme, so it must look right in the theme you are leaving.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java ThemePicker declaration — do not add import/export lines here.

// Module-scoped, frozen owners for the two branches. DomOpsParty tracks an
// owner by WeakRef, and an MPA page has no `this` to hand in — a function-local
// owner would be collectible while its elements were still on the page.
const _btnOwner  = Object.freeze({ toString: () => "themePickerButton" });
const _treeOwner = Object.freeze({ toString: () => "themePickerTree" });

var _seq = 0;

/** Read the active theme from the current URL, via the sanctioned href API. */
function _activeTheme() {
    var url = HrefManagerInstance.current();
    var q = url.indexOf("?");
    if (q < 0) return null;
    return new URLSearchParams(url.slice(q + 1)).get("theme");
}

function _groupOf(theme) {
    return (theme && theme.group) ? theme.group : "Themes";
}

/** Registry order decides both group order and order within a group. */
function _group(themes) {
    var order = [];
    var byName = {};
    for (var i = 0; i < themes.length; i++) {
        var g = _groupOf(themes[i]);
        if (!byName[g]) { byName[g] = []; order.push(g); }
        byName[g].push(themes[i]);
    }
    return order.map(function (name) { return { name: name, themes: byName[name] }; });
}

/**
 * The tree itself. Returns the root element; the caller decides where it goes.
 *
 * `out` collects OWNED REFERENCES the caller needs later — the active row and
 * the first row, for focus. That is the Owned References doctrine doing real
 * work: a caller that wants a row is handed it at build time rather than going
 * looking for it afterwards.
 */
function _tree(branch, themes, active, onPick, out) {
    var root = branch.createElement("tree" + (++_seq), "div");
    css.addClass(root, tp_tree);

    var groups = _group(themes);
    for (var gi = 0; gi < groups.length; gi++) {
        var g = groups[gi];
        var box = branch.createElement("grp" + _seq + "_" + gi, "div");

        var head = branch.createElement("grph" + _seq + "_" + gi, "button");
        head.type = "button";
        css.addClass(head, tp_grp_head);
        head.setAttribute("aria-expanded", "true");

        var caret = branch.createElement("crt" + _seq + "_" + gi, "span");
        css.addClass(caret, tp_caret);
        caret.textContent = "▾";
        head.appendChild(caret);

        var name = branch.createElement("gnm" + _seq + "_" + gi, "span");
        name.textContent = g.name;
        head.appendChild(name);

        var count = branch.createElement("cnt" + _seq + "_" + gi, "span");
        css.addClass(count, tp_count);
        count.textContent = String(g.themes.length);
        head.appendChild(count);

        var kids = branch.createElement("kids" + _seq + "_" + gi, "div");
        css.addClass(kids, tp_kids);

        for (var ti = 0; ti < g.themes.length; ti++) {
            var t = g.themes[ti];
            var isOn = (t.slug === active);

            var row = branch.createElement("thm" + _seq + "_" + gi + "_" + ti, "button");
            row.type = "button";
            css.addClass(row, tp_thm);
            if (isOn) {
                css.addClass(row, tp_on);
                row.setAttribute("aria-current", "true");
            }
            if (out) {
                if (isOn) { out.activeRow = row; }
                if (!out.firstRow) { out.firstRow = row; }
            }

            var dot = branch.createElement("dot" + _seq + "_" + gi + "_" + ti, "span");
            css.addClass(dot, tp_dot);
            if (isOn) css.addClass(dot, tp_dot_on);
            row.appendChild(dot);

            var lbl = branch.createElement("lbl" + _seq + "_" + gi + "_" + ti, "span");
            lbl.textContent = t.label || t.slug;
            row.appendChild(lbl);

            row.addEventListener("click", (function (slug) {
                return function () { onPick(slug); };
            })(t.slug));

            kids.appendChild(row);
        }

        head.addEventListener("click", (function (boxEl, kidsEl, caretEl, headEl) {
            return function () {
                var shut = css.hasClass(boxEl, tp_shut);
                css.toggleClass(boxEl, tp_shut, !shut);
                css.toggleClass(kidsEl, tp_kids_shut, !shut);
                css.toggleClass(caretEl, tp_caret_shut, !shut);
                headEl.setAttribute("aria-expanded", shut ? "true" : "false");
            };
        })(box, kids, caret, head));

        box.appendChild(head);
        box.appendChild(kids);
        root.appendChild(box);
    }
    return root;
}

function _pick(slug) {
    HrefManagerInstance.navigate(HrefManagerInstance.withParam("theme", slug));
}

function _themes() {
    return fetch("/themes").then(function (r) {
        if (!r.ok) throw new Error("/themes HTTP " + r.status);
        return r.json().then(function (j) {
            // /themes answers an object with a themes array, not a bare array.
            return (j && j.themes) ? j.themes : (Array.isArray(j) ? j : []);
        });
    });
}

/**
 * The bare tree, mounted into a host the caller already sized. Used by the
 * themes app, which has a page to put it on and wants no modal.
 */
function mountThemePickerTree(host, opts) {
    if (!host) throw new Error("mountThemePickerTree: host element required");
    var branch = domOpsParty.createBranch("themePickerTree" + (++_seq));
    branch.activate(_treeOwner);
    var mySeq = _seq;
    var boxed = !opts || opts.boxed !== false;

    return _themes().then(function (themes) {
        var active = _activeTheme();
        var wrap = branch.createElement("wrap" + mySeq, "div");
        if (boxed) css.addClass(wrap, tp_inline);
        if (opts && opts.heading) {
            var h = branch.createElement("ihd" + mySeq, "div");
            css.addClass(h, tp_head);
            h.textContent = opts.heading;
            wrap.appendChild(h);
        }
        wrap.appendChild(_tree(branch, themes, active, _pick, {}));
        host.appendChild(wrap);
        return { branch: branch, dissolve: function () { branch.dissolve(); } };
    });
}

/**
 * Header trigger plus modal. Focus moves to the active theme on open and back
 * to the trigger on close; Escape and a scrim click both close. Rows are real
 * buttons, so Tab and Enter need no help.
 */
function mountThemePickerButton(host, opts) {
    if (!host) throw new Error("mountThemePickerButton: host element required");
    var branch = domOpsParty.createBranch("themePickerBtn" + (++_seq));
    branch.activate(_btnOwner);
    var mySeq = _seq;

    return _themes().then(function (themes) {
        if (!themes || themes.length < 2) return null;   // nothing to switch between
        var active = _activeTheme();
        var activeLabel = "";
        for (var i = 0; i < themes.length; i++) {
            if (themes[i].slug === active) activeLabel = themes[i].label || themes[i].slug;
        }
        if (!activeLabel) activeLabel = themes[0].label || themes[0].slug;

        var btn = branch.createElement("btn" + mySeq, "button");
        btn.type = "button";
        css.addClass(btn, tp_btn);
        btn.setAttribute("aria-haspopup", "dialog");

        var cap = branch.createElement("cap" + mySeq, "span");
        css.addClass(cap, tp_btn_label);
        cap.textContent = (opts && opts.label) || "Theme:";
        btn.appendChild(cap);

        var now = branch.createElement("now" + mySeq, "span");
        now.textContent = activeLabel;
        btn.appendChild(now);

        var bcaret = branch.createElement("bcrt" + mySeq, "span");
        bcaret.textContent = "▾";
        bcaret.setAttribute("aria-hidden", "true");
        btn.appendChild(bcaret);

        var scrim = branch.createElement("scrim" + mySeq, "div");
        css.addClass(scrim, tp_scrim);
        scrim.hidden = true;
        scrim.setAttribute("role", "dialog");
        scrim.setAttribute("aria-modal", "true");
        scrim.setAttribute("aria-label", "Choose a theme");

        var panel = branch.createElement("panel" + mySeq, "div");
        css.addClass(panel, tp_panel);

        var head = branch.createElement("head" + mySeq, "div");
        css.addClass(head, tp_head);
        var title = branch.createElement("ttl" + mySeq, "span");
        title.textContent = "Choose a theme";
        head.appendChild(title);
        var close = branch.createElement("cls" + mySeq, "button");
        close.type = "button";
        css.addClass(close, tp_close);
        close.textContent = "✕";
        close.setAttribute("aria-label", "Close");
        head.appendChild(close);

        panel.appendChild(head);
        var owned = {};
        panel.appendChild(_tree(branch, themes, active, _pick, owned));
        scrim.appendChild(panel);

        var restoreTo = null;
        function open() {
            restoreTo = document.activeElement;
            scrim.hidden = false;
            css.addClass(scrim, tp_scrim_open);
            var target = owned.activeRow || owned.firstRow;
            if (target) target.focus();
        }
        function shut() {
            scrim.hidden = true;
            css.removeClass(scrim, tp_scrim_open);
            if (restoreTo && restoreTo.focus) restoreTo.focus();
        }

        btn.addEventListener("click", open);
        close.addEventListener("click", shut);
        scrim.addEventListener("click", function (e) { if (e.target === scrim) shut(); });
        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape" && !scrim.hidden) { e.preventDefault(); shut(); }
        });

        host.appendChild(btn);
        document.body.appendChild(scrim);
        return { branch: branch, open: open, close: shut,
                 dissolve: function () { branch.dissolve(); } };
    });
}
