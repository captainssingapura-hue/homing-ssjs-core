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
// A FRESH BRANCH PER OPEN, dissolved on close. DomOpsParty names elements and
// refuses a duplicate name, and TreeRenderer numbers its rows from zero on every
// setData — so a second build on the SAME branch asks for "tn0" twice and throws
// mid-render, leaving an empty panel. Reusing one detached body avoided that but
// bought a worse problem: a dialog that is closed but alive still answers
// keystrokes, and the page's own tree never sees them. A branch per open, named
// by the clock so no two can collide, keeps each dialog's namespace and its
// lifetime to itself.
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
const _dialogOwner = Object.freeze({ toString: () => "themePickerDialog" });

var _seq = 0;
var _dlgSeq = 0;

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
 * The action row, built once and refreshed as the selection moves.
 *
 * Switching a theme still NAVIGATES — live swap is Wish 0018 — and that is what
 * separates the two confirming actions. Apply reloads with the dialog flagged to
 * reopen, so you land back here and can try the next one; OK reloads without the
 * flag and the dialog is simply gone. Cancel never navigates at all.
 *
 * Applying the theme already in use would spend a whole page load to arrive
 * exactly where you are, so both are turned off in that case rather than
 * pretending to do something.
 */
function _actions(branch, host, seq, apply, ok, cancel) {
    var row = branch.createElement("act" + seq, "div");
    css.addClass(row, tp_actions);

    function button(name, label, onClick, primary) {
        var b = branch.createElement(name + seq, "button");
        b.type = "button";
        b.textContent = label;
        css.addClass(b, tp_action);
        if (primary) css.addClass(b, tp_action_primary);
        b.addEventListener("click", onClick);
        row.appendChild(b);
        return b;
    }

    var bCancel = button("acx", "Cancel", cancel, false);
    var bApply  = button("apy", "Apply",  apply,  false);
    var bOk     = button("aok", "OK",     ok,     true);

    host.appendChild(row);

    return {
        row: row,
        // `live` is "the selection would actually change something".
        refresh: function (live) {
            css.toggleClass(bApply, tp_action_off, !live);
            bApply.disabled = !live;
            bOk.textContent = live ? "OK" : "Close";
        }
    };
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

        var modal  = null;
        var keys   = null;   // the capture-phase keydown listener, while open
        var dialog = null;   // { branch, renderer, treeHost }, while open
        var inerted = [];    // what the firewall switched off, to switch back on

        // Everything the open dialog owns, released in one place. Both close
        // paths run this — the ones we drive (Escape, the trigger button) and
        // the one Modal drives (its own ✕). Missing the second is what left a
        // dead listener eating the page's arrow keys after the dialog was gone.
        function teardown() {
            for (var i = 0; i < inerted.length; i++) inerted[i].inert = false;
            inerted = [];
            if (keys) { document.removeEventListener("keydown", keys, true); keys = null; }
            if (modal && modal.destroy) { try { modal.destroy(); } catch (e) {} }
            modal = null;
            if (dialog) { try { dialog.branch.dissolve(); } catch (e) {} dialog = null; }
        }

        function close() {
            // A deliberate close clears the flag: the user is done trying themes,
            // so the next page must NOT reopen. Only a theme switch sets it.
            rememberPickerOpen(false);
            teardown();
            if (btn.focus) btn.focus();
        }

        // φ. The dialog takes 1/φ of each viewport axis, so it is the larger
        // part of the golden cut on a small screen and still leaves the page
        // framing it on a large one. Measured at open rather than tracked: the
        // Modal is absolutely positioned in pixels, and a dialog that resized
        // under the pointer while you were reading it would be worse than one
        // that is simply right for the viewport you opened it in.
        var PHI = 1.6180339887;

        function open() {
            if (modal) { close(); return; }

            // Named by the clock, and by a counter for the two opens that land in
            // the same millisecond. createBranch refuses a name already taken at
            // this level, and dissolving a child does not always give the name
            // back — so never ask for the same one twice.
            var pb = branch.createBranch("tpDlg" + Date.now() + "_" + (++_dlgSeq));
            pb.activate(_dialogOwner);

            var body = pb.createElement("mbody", "div");
            css.addClass(body, tp_body);

            var acts = null;

            function applyPicked(keepOpen) {
                var slug = dialog && dialog.selected();
                if (!slug || slug === active) { if (!keepOpen) close(); return; }
                // The flag is the whole difference between the two: it survives
                // the reload and tells the far side whether to reopen.
                rememberPickerOpen(!!keepOpen);
                switchToTheme(slug);
            }

            var panes = _buildPanes(pb, body, themes, active, mySeq,
                // Enter on a row is the primary action, so it means OK.
                function () { applyPicked(false); },
                function (slug) { if (acts) acts.refresh(slug !== active); });

            dialog = { branch: pb, renderer: panes.renderer,
                       treeHost: panes.treeHost, selected: panes.selected };

            acts = _actions(pb, body, mySeq,
                function () { applyPicked(true);  },
                function () { applyPicked(false); },
                function () { close(); });
            acts.refresh(dialog.selected() !== active);

            // 1/φ of each viewport axis, then clamped. Modal applies minWidth
            // only in resize(), never in its constructor, so an unclamped value
            // reaches the DOM verbatim — a hidden or not-yet-laid-out pane
            // reports innerWidth 0 and the dialog arrives 2px wide, borders
            // only. The ceiling is the other end of the same argument: past
            // about a thousand pixels the tree is still one column of names and
            // the extra width is spent on nothing.
            var vw = window.innerWidth  || 1024;
            var vh = window.innerHeight || 768;
            var w  = Math.min(980, Math.max(460, Math.round(vw / PHI)));
            var h  = Math.min(720, Math.max(320, Math.round(vh / PHI)));
            modal = new Modal({
                container: document.body,
                title:     "Theme",
                content:   body,
                x:         Math.max(8, Math.round((vw - w) / 2)),
                y:         Math.max(8, Math.round((vh - h) / 2)),
                width:     w,
                height:    h,
                minWidth:  420,
                minHeight: 300,
                // The rest of the page goes inert behind a scrim, and the panel
                // takes an accent glow — this dialog is the thing you are using.
                scrim:     true,
                glow:      true,
                // Modal's own ✕ ends the dialog as surely as Escape does, so it
                // gets the same teardown. It must not be `modal = null` alone:
                // that leaves the branch mounted and the keydown listener
                // installed, and the listener answers for a dialog the user has
                // already dismissed.
                onClose:   close
            });

            // MODALITY. The scrim is the visible half and inert is the real half:
            // without inert, Tab walks straight behind the scrim and a screen
            // reader reads through it. Everything already inert for its own
            // reasons is left alone, and only what we switched off is restored.
            var scrim = pb.createElement("scrim", "div");
            css.addClass(scrim, tp_scrim);
            scrim.addEventListener("mousedown", function () { close(); });
            document.body.appendChild(scrim);
            css.addClass(modal.el, tp_glow);

            var kids = document.body.children;
            for (var i = 0; i < kids.length; i++) {
                var k = kids[i];
                if (k === scrim || k === modal.el || k.inert) continue;
                k.inert = true;
                inerted.push(k);
            }

            // CAPTURE phase, and stopPropagation on anything we take. The page
            // behind has its own TreeRenderer listening on document; without
            // this, one ArrowDown walks both trees and the page navigates under
            // the open dialog. A modal owns the keyboard while it is up — and
            // only while it is up, which is teardown's job.
            keys = function (ev) {
                if (ev.key === "Escape") {
                    ev.preventDefault(); ev.stopPropagation(); close(); return;
                }
                if (dialog && dialog.renderer.handleKeydown(ev)) {
                    ev.preventDefault(); ev.stopPropagation();
                }
            };
            document.addEventListener("keydown", keys, true);

            if (modal.open) modal.open();
            if (dialog.treeHost.focus) dialog.treeHost.focus();
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
