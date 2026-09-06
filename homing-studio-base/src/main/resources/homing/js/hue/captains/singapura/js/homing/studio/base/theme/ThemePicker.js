// ThemePicker.js
//
// The theme picker, in two shapes from one implementation:
//
//   mountThemePickerButton(host, opts)  header trigger + dialog — the chrome
//   mountThemePickerTree(host, opts)    the bare tree — the themes app
//
// Borrows rather than builds. The dialog is SystemDialog (RFC 0057), which
// brings scrim, inert, keyboard ownership, glow, golden sizing and the action
// row with it. The rows are TreeRenderer, the framework's
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
function _buildPanes(branch, wrap, themes, active, seq, onSwitch, onPick) {
    // The same MasterDetail the catalogue listing uses. What differs is only
    // what the body draws, which is this module's business and stays here.
    var update = null;
    var picked = active;
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
            var slug = slugOfSelection(sel);
            var t = themeBySlug(themes, slug);
            if (!t) return;                  // a group row selects nothing
            picked = slug;
            if (update) update(t, active);
            if (onPick) onPick(slug);
        },
        onActivate:  function (sel) {
            var slug = slugOfSelection(sel);
            if (slug) onSwitch(slug);
        }
    });

    update = _previewPane(branch, pair.bodyEl, seq);
    update(themeBySlug(themes, active) || themes[0], active);

    return { treeHost: pair.navEl, renderer: pair.renderer,
             selected: function () { return picked; } };
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
 * Header trigger plus dialog. The dialog is SystemDialog — scrim, inert,
 * keyboard ownership, glow, golden sizing and the action row all arrive with
 * it — so this function is only about what the picker PUTS in the dialog and
 * what its three actions mean.
 *
 * Switching a theme still NAVIGATES — live swap is Wish 0018 — and that is what
 * separates the two confirming actions. Apply reloads with the dialog flagged to
 * reopen, so you land back here and can try the next one; OK reloads without the
 * flag and the dialog is simply gone. Cancel never navigates at all. Applying
 * the theme already in use would spend a page load to arrive exactly where you
 * are, so Apply switches off and OK becomes Close in that case.
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

        var dialog = null;   // the SystemDialog handle, while open

        function close() { if (dialog) dialog.close(); }

        function open() {
            if (dialog) { close(); return; }
            var panes = null;

            function applyPicked(keepOpen) {
                var slug = panes && panes.selected();
                if (!slug || slug === active) { if (!keepOpen) close(); return; }
                // The flag is the whole difference between Apply and OK: it
                // survives the reload and tells the far side whether to reopen.
                rememberPickerOpen(!!keepOpen);
                switchToTheme(slug);
            }

            // `live` is "the selection would actually change something".
            function refresh(live) {
                if (!dialog) return;
                dialog.setAction("apply", { enabled: live });
                dialog.setAction("ok",    { label: live ? "OK" : "Close" });
            }

            dialog = openSystemDialog({
                branch:         branch,
                title:          "Theme",
                modal:          true,
                restoreFocusTo: btn,
                content: function (pb, bodyEl) {
                    panes = _buildPanes(pb, bodyEl, themes, active, mySeq,
                        // Enter on a row is the primary action, so it means OK.
                        function () { applyPicked(false); },
                        function (slug) { refresh(slug !== active); });
                    return { onKeydown: function (ev) { return panes.renderer.handleKeydown(ev); },
                             focusEl:   panes.treeHost };
                },
                actions: [
                    { id: "cancel", label: "Cancel",                onClick: function () { close(); } },
                    { id: "apply",  label: "Apply",                 onClick: function () { applyPicked(true);  } },
                    { id: "ok",     label: "OK",     primary: true, onClick: function () { applyPicked(false); } }
                ],
                // Every close path lands here exactly once — Escape, ✕, scrim,
                // Cancel. A deliberate close clears the flag: the user is done
                // trying themes, so the next page must NOT reopen. Apply and OK
                // navigate WITHOUT closing, so they never reach this and the flag
                // they set survives.
                onClose: function () {
                    dialog = null;
                    rememberPickerOpen(false);
                }
            });
            refresh(panes.selected() !== active);
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
