// ThemePicker.js
//
// The theme picker, in two shapes from one implementation:
//
//   mountThemePickerButton(host, opts)  header trigger + Modal — the chrome
//   mountThemePickerTree(host, opts)    the bare tree — the themes app
//
// Borrows rather than builds. The dialog is Modal, the same primitive the
// workspace-control modal uses. The rows are TreeRenderer, the framework's
// shared tree, which brings the keyboard model with it: ArrowUp/Down through
// visible rows, ArrowRight/Left to fold a group, Enter to activate.
//
// Master/detail, side by side. The tree is a column of NAMES ONLY, sized to
// its content — max-content between a floor and a ceiling, so it is as wide as
// it needs and no wider. Everything about the selected theme — the in-use
// marker, the inspiration line, the palette — lives in the content pane on the
// right. That division is what keeps the whole thing compact: a row carries one
// string, so nothing competes for its width.
//
// Selecting only browses. Enter switches, which navigates - live theme swap is
// not supported yet. A session-local flag carries "the picker was open" across
// the reload, so the dialog is rebuilt on the far side and the user never has
// to reopen it between tries.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java ThemePicker declaration — do not add import/export lines here.

// Module-scoped, frozen owners. DomOpsParty tracks an owner by WeakRef, and an
// MPA page has no `this` to hand in — a function-local owner would be
// collectible while its elements were still on the page.
const _btnOwner  = Object.freeze({ toString: () => "themePickerButton" });
const _treeOwner = Object.freeze({ toString: () => "themePickerTree" });

var _seq = 0;


/**
 * The content pane: everything about the SELECTED theme — its name, whether it
 * is the one in use, what it was drawn from, and its palette.
 *
 * This is what lets the tree be a narrow column of names. A row that carries
 * only its label needs no description column and no badge before the name, so
 * nothing competes for the row's width and every name starts at the same x.
 *
 * Rebuilt on every selection, which is cheap — a heading, a line and a strip.
 */
function _preview(branch, host, theme, activeSlug) {
    while (host.firstChild) host.removeChild(host.firstChild);
    if (!theme) return;

    var name = branch.createElement("pvn" + (++_seq), "div");
    css.addClass(name, tp_preview_name);
    var mySeq = _seq;

    var nameText = branch.createElement("pvnt" + mySeq, "span");
    nameText.textContent = theme.label || theme.slug;
    name.appendChild(nameText);

    if (theme.slug === activeSlug) {
        var chip = branch.createElement("pvc" + mySeq, "span");
        css.addClass(chip, tp_current);
        chip.textContent = "in use";
        name.appendChild(chip);
    }
    host.appendChild(name);

    if (theme.inspiration) {
        var note = branch.createElement("pvi" + mySeq, "div");
        css.addClass(note, tp_preview_note);
        note.textContent = theme.inspiration;
        host.appendChild(note);
    }

    var strip = branch.createElement("pvs" + _seq, "div");
    css.addClass(strip, tp_swatches);

    var palette = theme.palette || {};
    var keys = Object.keys(palette);
    for (var i = 0; i < keys.length; i++) {
        var v = palette[keys[i]];
        if (!v) continue;
        var sw = branch.createElement("sw" + _seq + "_" + i, "div");
        css.addClass(sw, tp_sw);
        // setProperty, not a .style.x write — the route RFC 0044 sanctions for
        // a value that is DATA. The typed class consumes it.
        sw.style.setProperty("--tp-sw", v);
        sw.setAttribute("title", keys[i] + ": " + v);
        strip.appendChild(sw);
    }
    host.appendChild(strip);
}

/**
 * Build the tree into `container`, with the preview wired to its selection.
 * Returns the renderer so the caller can forward keydown — the host owns WHEN
 * keys flow, the renderer owns what they mean.
 */
function _buildTree(branch, container, previewHost, themes, active, onSwitch) {
    var renderer = new TreeRenderer({
        branch:      branch,
        container:   container,
        expandDepth: 2,
        // Neither. A row is its name and nothing else — the description and the
        // in-use marker moved to the content pane, which is what keeps the tree
        // narrow and every name starting at the same x.
        showBadge:   false,
        showNote:    false,
        showRoot:    false,
        onSelect:    function (sel) {
            var t = themeBySlug(themes, slugOfSelection(sel));
            if (t) _preview(branch, previewHost, t, active);
        },
        onActivate:  function (sel) {
            var slug = slugOfSelection(sel);
            if (slug) onSwitch(slug);
        }
    });
    renderer.setData(themeTreeData(themes, active));
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

    return fetchThemes().then(function (themes) {
        var active = activeThemeSlug();

        var wrap = branch.createElement("wrap" + mySeq, "div");
        if (boxed) css.addClass(wrap, tp_inline);

        if (opts && opts.heading) {
            var h = branch.createElement("ihd" + mySeq, "div");
            css.addClass(h, tp_inline_head);
            h.textContent = opts.heading;
            wrap.appendChild(h);
        }

        var treeHost = branch.createElement("thost" + mySeq, "div");
        css.addClass(treeHost, tp_tree_host);
        wrap.appendChild(treeHost);

        var previewHost = branch.createElement("pv" + mySeq, "div");
        css.addClass(previewHost, tp_preview);
        wrap.appendChild(previewHost);

        host.appendChild(wrap);

        var renderer = _buildTree(branch, treeHost, previewHost, themes, active,
            function (slug) { switchToTheme(slug); });

        _preview(branch, previewHost, themeBySlug(themes, active) || themes[0], active);

        treeHost.setAttribute("tabindex", "0");
        treeHost.addEventListener("keydown", function (ev) {
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

    return fetchThemes().then(function (themes) {
        if (!themes || themes.length < 2) return null;   // nothing to switch between
        var active = activeThemeSlug();
        if (!themeBySlug(themes, active)) active = themes[0].slug;

        var btn = branch.createElement("btn" + mySeq, "button");
        btn.type = "button";
        css.addClass(btn, tp_btn);
        btn.setAttribute("aria-haspopup", "dialog");

        var cap = branch.createElement("cap" + mySeq, "span");
        css.addClass(cap, tp_btn_label);
        cap.textContent = (opts && opts.label) || "Theme:";
        btn.appendChild(cap);

        var now = branch.createElement("now" + mySeq, "span");
        var activeTheme = themeBySlug(themes, active);
        now.textContent = activeTheme ? (activeTheme.label || activeTheme.slug) : "";
        btn.appendChild(now);

        var caret = branch.createElement("bcrt" + mySeq, "span");
        caret.textContent = "▾";
        caret.setAttribute("aria-hidden", "true");
        btn.appendChild(caret);

        var modal = null;
        var keyHandler = null;

        function close() {
            // A deliberate close clears the flag: the user is done trying themes,
            // so the next page must NOT reopen. Only a theme switch sets it.
            rememberPickerOpen(false);
            if (keyHandler) { document.removeEventListener("keydown", keyHandler, true); keyHandler = null; }
            if (modal && modal.destroy) { try { modal.destroy(); } catch (e) {} }
            modal = null;
            if (btn.focus) btn.focus();
        }

        function open() {
            if (modal) { close(); return; }

            var body = branch.createElement("mbody" + mySeq + "_" + (++_seq), "div");
            css.addClass(body, tp_body);
            var bodySeq = _seq;

            var treeHost = branch.createElement("mtree" + bodySeq, "div");
            css.addClass(treeHost, tp_tree_host);
            body.appendChild(treeHost);

            var previewHost = branch.createElement("mpv" + bodySeq, "div");
            css.addClass(previewHost, tp_preview);
            body.appendChild(previewHost);

            // Compact: the tree sizes itself, so this only has to leave the
            // content pane enough room for a palette strip and two lines of prose.
            var w = 520;
            var h = 400;
            modal = new Modal({
                container: document.body,
                title:     "Theme",
                content:   body,
                x:         Math.max(20, (window.innerWidth  - w) / 2),
                y:         Math.max(20, (window.innerHeight - h) / 3),
                width:     w,
                height:    h,
                onClose:   function () { modal = null; }
            });

            var renderer = _buildTree(branch, treeHost, previewHost, themes, active,
                function (slug) {
                    // Switching still navigates — live theme swap is not supported
                    // yet. So before leaving, leave a note for the page that comes
                    // back saying the picker was open; pickerReopenWanted() reads it on
                    // mount and reopens. From the user's side the dialog persists
                    // across a try, which is the behaviour that matters.
                    rememberPickerOpen(true);
                    switchToTheme(slug);
                });

            _preview(branch, previewHost, themeBySlug(themes, active), active);

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
            treeHost.setAttribute("tabindex", "0");
            if (treeHost.focus) treeHost.focus();
        }

        btn.addEventListener("click", open);
        host.appendChild(btn);

        // We arrived here from a theme switch with the picker open, so put it
        // back. The user sees a dialog that stayed put across the reload; what
        // actually happened is that it was rebuilt on the far side.
        if (pickerReopenWanted()) open();

        return { branch: branch, open: open, close: close,
                 dissolve: function () { close(); branch.dissolve(); } };
    });
}
