// ThemePicker.js
//
// The theme picker, in two shapes from one implementation:
//
//   mountThemePickerButton(host, opts)  header trigger + Modal — the chrome
//   mountThemePickerTree(host, opts)    the bare tree — the themes app
//
// Borrows rather than builds. The dialog is Modal, the same primitive the
// workspace-control modal uses, so the two read as one product. The rows are
// TreeRenderer, the framework's shared tree — which brings the keyboard model
// with it: ArrowUp/Down through visible rows, ArrowRight/Left to expand and
// fold a group, Enter to activate.
//
// onSelect only browses; onActivate switches. Without that split, arrowing down
// the list would reload the page on every keystroke.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java ThemePicker declaration — do not add import/export lines here.

// Module-scoped, frozen owners. DomOpsParty tracks an owner by WeakRef, and an
// MPA page has no `this` to hand in — a function-local owner would be
// collectible while its elements were still on the page.
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

function _slugify(s) {
    return String(s).toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-+)|(-+$)/g, "");
}

/**
 * Shape the theme registry into the tree payload TreeRenderer consumes:
 * { level, segment, display: { label, badge, note, kind }, children }.
 *
 * Registry order decides both group order and order within a group, so the
 * server stays the single place that decides presentation order.
 */
function _treeData(themes, active) {
    var order = [];
    var byName = {};
    for (var i = 0; i < themes.length; i++) {
        var t = themes[i];
        var g = t.group || "Themes";
        if (!byName[g]) { byName[g] = []; order.push(g); }
        byName[g].push(t);
    }

    var groups = order.map(function (name) {
        var kids = byName[name].map(function (t) {
            return {
                level:   "L2",
                segment: t.slug,
                display: {
                    label: t.label || t.slug,
                    // The badge is the only mark the active theme needs — the
                    // tree already draws selection, and a second highlight for
                    // "current" would compete with it.
                    badge: (t.slug === active) ? "ACTIVE" : "",
                    note:  "",
                    kind:  "theme"
                },
                children: []
            };
        });
        return {
            level:   "L1",
            segment: _slugify(name),
            display: { label: name, badge: String(kids.length), note: "", kind: "group" },
            children: kids
        };
    });

    return {
        level:   "L0",
        segment: "themes",
        display: { label: "Themes", badge: "", note: "", kind: "root" },
        children: groups
    };
}

function _pick(slug) {
    if (!slug) return;
    HrefManagerInstance.navigate(HrefManagerInstance.withParam("theme", slug));
}

/** A leaf's slug is the last segment of its name-path. */
function _slugOf(sel) {
    if (!sel || sel.hasChildren) return null;
    var np = sel.namePath || "";
    var i = np.lastIndexOf("/");
    return i < 0 ? np : np.slice(i + 1);
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
 * Build the tree into `container`. Returns the renderer so the caller can
 * forward keydown — the host owns WHEN keys flow, the renderer owns what they
 * mean.
 */
function _buildTree(branch, container, themes, active) {
    var renderer = new TreeRenderer({
        branch:      branch,
        container:   container,
        data:        _treeData(themes, active),
        // Groups open: ten themes in three groups all fit, and a picker that
        // opens folded makes the reader work before it helps them.
        expandDepth: 2,
        showBadge:   true,
        // The root is the word "Themes" above a list of themes.
        showRoot:    false,
        onActivate:  function (sel) { _pick(_slugOf(sel)); }
    });
    renderer.setData(_treeData(themes, active));
    return renderer;
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
        var wrap = branch.createElement("wrap" + mySeq, "div");
        if (boxed) css.addClass(wrap, tp_inline);

        if (opts && opts.heading) {
            var h = branch.createElement("ihd" + mySeq, "div");
            css.addClass(h, tp_inline_head);
            h.textContent = opts.heading;
            wrap.appendChild(h);
        }

        var host2 = branch.createElement("thost" + mySeq, "div");
        css.addClass(host2, tp_tree_host);
        wrap.appendChild(host2);
        host.appendChild(wrap);

        var renderer = _buildTree(branch, host2, themes, _activeTheme());

        // Keys flow only while the pointer-independent focus is inside this
        // tree, so an inline picker never steals the page's arrow keys.
        host2.setAttribute("tabindex", "0");
        host2.addEventListener("keydown", function (ev) {
            if (renderer.handleKeydown(ev)) ev.preventDefault();
        });

        return { branch: branch, renderer: renderer,
                 dissolve: function () { branch.dissolve(); } };
    });
}

/**
 * Header trigger plus Modal — the same primitive the workspace-control modal
 * uses, so the two look like one product.
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

        var caret = branch.createElement("bcrt" + mySeq, "span");
        caret.textContent = "▾";
        caret.setAttribute("aria-hidden", "true");
        btn.appendChild(caret);

        var modal = null;
        var keyHandler = null;

        function close() {
            if (keyHandler) { document.removeEventListener("keydown", keyHandler, true); keyHandler = null; }
            if (modal && modal.destroy) { try { modal.destroy(); } catch (e) {} }
            modal = null;
            if (btn.focus) btn.focus();
        }

        function open() {
            if (modal) { close(); return; }

            var content = branch.createElement("mhost" + mySeq + "_" + (++_seq), "div");
            css.addClass(content, tp_tree_host);

            var w = 320;
            var h = 420;
            modal = new Modal({
                container: document.body,
                title:     "Theme",
                content:   content,
                x:         Math.max(20, (window.innerWidth  - w) / 2),
                y:         Math.max(20, (window.innerHeight - h) / 3),
                width:     w,
                height:    h,
                onClose:   function () { modal = null; }
            });

            var renderer = _buildTree(branch, content, themes, active);

            // CAPTURE phase, and stopPropagation on anything we take. The page
            // behind has its own TreeRenderer listening on document; without
            // this, one ArrowDown walks both trees and the page navigates under
            // the open dialog. A modal owns the keyboard while it is up.
            keyHandler = function (ev) {
                if (ev.key === "Escape") {
                    ev.preventDefault(); ev.stopPropagation(); close(); return;
                }
                if (renderer.handleKeydown(ev)) {
                    ev.preventDefault(); ev.stopPropagation();
                }
            };
            document.addEventListener("keydown", keyHandler, true);

            if (modal.open) modal.open();
            content.setAttribute("tabindex", "0");
            if (content.focus) content.focus();
        }

        btn.addEventListener("click", open);
        host.appendChild(btn);

        return { branch: branch, open: open, close: close,
                 dissolve: function () { close(); branch.dissolve(); } };
    });
}
