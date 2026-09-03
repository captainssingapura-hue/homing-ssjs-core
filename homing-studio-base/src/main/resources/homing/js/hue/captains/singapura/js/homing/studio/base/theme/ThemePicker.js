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
// Master/detail, side by side. The tree is a column of NAMES ONLY, sized to its
// content — max-content between a floor and a ceiling, so it is as wide as it
// needs and no wider. Everything about the selected theme — the in-use marker,
// the inspiration line, the palette — lives in the content pane on the right.
// That division is what keeps the whole thing compact: a row carries one string,
// so nothing competes for its width.
//
// BUILT ONCE, REUSED. Closing the dialog detaches its content; it does not
// destroy it. The branch owns those elements for the life of the mount, and
// reopening hands the same body to a new Modal. This is not an optimisation —
// DomOpsParty names elements and REFUSES a duplicate name with a RangeError, so
// a second build on the same branch would ask TreeRenderer for "tn0" again and
// throw mid-render, leaving an empty panel. Detached-but-alive is the model's
// own answer, and the relation grid keeps its unplaced cells the same way.
//
// Selecting only browses. Enter switches, which navigates — live theme swap is
// not supported yet. A session-local flag carries "the picker was open" across
// the reload, so the dialog is rebuilt on the far side and the user never has to
// reopen it between tries.
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
 * Build the content pane once and return an UPDATE function.
 *
 * Every theme reports the same palette keys, so the pane's shape never changes
 * between selections — only its text and the swatch colours do. Rebuilding it
 * per selection would mint new elements on every arrow-press, and the branch
 * would hold each one for the life of the page.
 */
function _previewPane(branch, host, seq) {
    var name = branch.createElement("pvn" + seq, "div");
    css.addClass(name, tp_preview_name);

    var nameText = branch.createElement("pvnt" + seq, "span");
    name.appendChild(nameText);

    var chip = branch.createElement("pvc" + seq, "span");
    css.addClass(chip, tp_current);
    chip.textContent = "in use";
    name.appendChild(chip);
    host.appendChild(name);

    var note = branch.createElement("pvi" + seq, "div");
    css.addClass(note, tp_preview_note);
    host.appendChild(note);

    var strip = branch.createElement("pvs" + seq, "div");
    css.addClass(strip, tp_swatches);
    host.appendChild(strip);

    var swatches = [];

    return function (theme, activeSlug) {
        if (!theme) return;
        nameText.textContent = theme.label || theme.slug;
        chip.hidden = (theme.slug !== activeSlug);
        note.textContent = theme.inspiration || "";

        var palette = theme.palette || {};
        var keys = Object.keys(palette);
        for (var i = 0; i < keys.length; i++) {
            if (!swatches[i]) {
                swatches[i] = branch.createElement("sw" + seq + "_" + i, "div");
                css.addClass(swatches[i], tp_sw);
                strip.appendChild(swatches[i]);
            }
            // setProperty, not a .style.x write — the route RFC 0044 sanctions
            // for a value that is DATA. The typed class consumes it.
            swatches[i].style.setProperty("--tp-sw", palette[keys[i]] || "transparent");
            swatches[i].setAttribute("title", keys[i] + ": " + palette[keys[i]]);
        }
    };
}

/**
 * Tree plus content pane, wired together, built into `wrap`. Returns the pieces
 * the caller needs afterwards: the renderer (to forward keydown) and the tree
 * host (to focus).
 */
function _buildPanes(branch, wrap, themes, active, seq, onSwitch) {
    // The same MasterDetail the catalogue listing uses. What differs is only
    // what the body draws, which is this module's business and stays here.
    var update = null;
    var pair = mountMasterDetail({
        branch:      branch,
        host:        wrap,
        data:        themeTreeData(themes, active),
        expandDepth: 2,
        // Neither. A row is its name and nothing else — the description and the
        // in-use marker live in the content pane, which is what keeps the tree
        // narrow and every name starting at the same x.
        showBadge:   false,
        showNote:    false,
        showRoot:    false,
        onSelect:    function (bodyEl, sel) {
            var t = themeBySlug(themes, slugOfSelection(sel));
            if (t && update) update(t, active);
        },
        onActivate:  function (sel) {
            var slug = slugOfSelection(sel);
            if (slug) onSwitch(slug);
        }
    });

    update = _previewPane(branch, pair.bodyEl, seq);
    update(themeBySlug(themes, active) || themes[0], active);

    return { treeHost: pair.navEl, renderer: pair.renderer };
}

/**
 * The bare tree plus pane, mounted into a host the caller already sized. Used by
 * the themes app, which has a page to put it on and wants no modal.
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

        var body = branch.createElement("ibody" + mySeq, "div");
        css.addClass(body, tp_body);
        wrap.appendChild(body);
        host.appendChild(wrap);

        var panes = _buildPanes(branch, body, themes, active, mySeq,
            function (slug) { switchToTheme(slug); });

        panes.treeHost.addEventListener("keydown", function (ev) {
            if (panes.renderer.handleKeydown(ev)) ev.preventDefault();
        });

        return { branch: branch, renderer: panes.renderer,
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
        var ui = null;          // built on first open, reused for every later one

        function close() {
            // A deliberate close clears the flag: the user is done trying themes,
            // so the next page must NOT reopen. Only a theme switch sets it.
            rememberPickerOpen(false);
            if (keyHandler) { document.removeEventListener("keydown", keyHandler, true); keyHandler = null; }
            // destroy() removes the Modal's own root, taking our body with it —
            // detached, but alive and still owned by the branch, ready to be
            // handed to the next Modal.
            if (modal && modal.destroy) { try { modal.destroy(); } catch (e) {} }
            modal = null;
            if (btn.focus) btn.focus();
        }

        function open() {
            if (modal) { close(); return; }

            if (!ui) {
                var body = branch.createElement("mbody" + mySeq, "div");
                css.addClass(body, tp_body);
                var panes = _buildPanes(branch, body, themes, active, mySeq,
                    function (slug) {
                        // Switching still navigates — live theme swap is not
                        // supported yet. Leave a note for the page that comes
                        // back, which reopens the dialog on the far side.
                        rememberPickerOpen(true);
                        switchToTheme(slug);
                    });
                ui = { body: body, treeHost: panes.treeHost, renderer: panes.renderer };
            }

            // Compact: the tree sizes itself, so this only has to leave the
            // content pane enough room for a palette strip and two lines of prose.
            var w = 520;
            var h = 400;
            modal = new Modal({
                container: document.body,
                title:     "Theme",
                content:   ui.body,
                x:         Math.max(20, (window.innerWidth  - w) / 2),
                y:         Math.max(20, (window.innerHeight - h) / 3),
                width:     w,
                height:    h,
                onClose:   function () { modal = null; }
            });

            // CAPTURE phase, and stopPropagation on anything we take. The page
            // behind has its own TreeRenderer listening on document; without
            // this, one ArrowDown walks both trees and the page navigates under
            // the open dialog. A modal owns the keyboard while it is up.
            keyHandler = function (ev) {
                if (ev.key === "Escape") {
                    ev.preventDefault(); ev.stopPropagation(); close(); return;
                }
                if (ui.renderer.handleKeydown(ev)) {
                    ev.preventDefault(); ev.stopPropagation();
                }
            };
            document.addEventListener("keydown", keyHandler, true);

            if (modal.open) modal.open();
            if (ui.treeHost.focus) ui.treeHost.focus();
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
