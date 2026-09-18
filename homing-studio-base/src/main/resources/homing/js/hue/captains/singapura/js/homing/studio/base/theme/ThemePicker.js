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
// the inspiration line, the live preview — lives in the content pane on the right.
// That division is what keeps the whole thing compact: a row carries one string,
// so nothing competes for its width.
//
// Selecting only browses; the preview frame shows the selection. Enter, Apply
// and OK switch — live, on this page (RFC 0064): the CSS manager swaps every
// sheet in dependency waves and the dialog is the same element afterwards.
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
 * Build the content pane once and return { update, colours }.
 *
 * The pane is a name, an inspiration line and a FRAME showing the preview page
 * under the selected theme. The frame is one element whose address changes;
 * rebuilding it per selection would mint a new element on every arrow-press,
 * and the branch would hold each one for the life of the page. Reloading it is
 * what makes the preview honest: the page comes back wearing the theme,
 * palette and all, exactly as a navigation would deliver it.
 *
 * The colours are the other axis: the tree picks the base, a menu floating
 * over the frame picks the palette — only the ones the registry offers for
 * that base, so a design is shown in colours crafted for it or vouched for
 * it, and the pill is not there at all when there is nothing to choose.
 */
function _previewPane(branch, host, seq, reg, onPickColours) {
    // A column filling the pane: name and note take their height, the frame
    // takes the rest. Inline, where the pane has no height of its own, the
    // frame falls back to its minimum and the column is as tall as that.
    var pane = branch.createElement("pvp" + seq, "div");
    css.addClass(pane, tp_preview_pane);
    host.appendChild(pane);

    var name = branch.createElement("pvn" + seq, "div");
    css.addClass(name, tp_preview_name);

    var nameText = branch.createElement("pvnt" + seq, "span");
    name.appendChild(nameText);

    var chip = branch.createElement("pvc" + seq, "span");
    css.addClass(chip, tp_current);
    chip.textContent = "in use";
    name.appendChild(chip);
    pane.appendChild(name);

    var note = branch.createElement("pvi" + seq, "div");
    css.addClass(note, tp_preview_note);
    pane.appendChild(note);

    // The frame, its loading banner and the colour menu share a positioned
    // wrapper: the banner covers the frame while a page is on its way and
    // fades once it lands; the menu floats over both.
    var wrap = branch.createElement("pvw" + seq, "div");
    css.addClass(wrap, tp_preview_wrap);
    pane.appendChild(wrap);

    var frame = branch.createElement("pvf" + seq, "iframe");
    css.addClass(frame, tp_preview_frame);
    frame.setAttribute("title", "Theme preview");
    wrap.appendChild(frame);

    var banner = branch.createElement("pvl" + seq, "div");
    css.addClass(banner, tp_preview_loading);
    wrap.appendChild(banner);

    var colours = mountColourMenu(branch, wrap, seq, reg.palettes, onPickColours);

    // `load` fires for every page the frame finishes, error pages included, so
    // the banner cannot get stuck; a slug selected mid-load simply re-shows it.
    frame.addEventListener("load", function () { css.removeClass(banner, tp_preview_loading_on); });

    var shown = null;   // the slug the frame is showing — re-selecting it is free

    // `palette` null is the base's own colours; the slug shown and switched to
    // is the registry's entry for the pair, so the frame previews exactly what
    // OK would apply.
    function update(theme, palette, activeSlug) {
        if (!theme) return;
        var way = colourwayOf(theme, palette) || colourwayOf(theme, null);
        var slug = way ? way.slug : theme.slug;
        nameText.textContent = wornLabel(theme, palette, reg.palettes);
        chip.hidden = (slug !== activeSlug);
        note.textContent = theme.inspiration || "";
        colours.update(theme, palette, reg.themes);
        if (slug === shown) return;
        shown = slug;
        banner.textContent = "Loading " + nameText.textContent + "…";
        css.addClass(banner, tp_preview_loading_on);
        frame.src = previewUrl(slug);
    }
    return { update: update, colours: colours };
}

/** "Neo-Brutalism", or "Neo-Brutalism in Forest" when worn in another's colours. */
function wornLabel(theme, palette, palettes) {
    var base = theme.label || theme.slug;
    if (!palette) return base;
    for (var i = 0; i < palettes.length; i++) if (palettes[i].slug === palette) return base + " in " + (palettes[i].label || palette);
    return base + " in " + palette;
}

/**
 * Tree plus content pane, wired together, built into `wrap`. Returns the pieces
 * the caller needs afterwards: the renderer (to forward keydown) and the tree
 * host (to focus).
 */
function _buildPanes(branch, wrap, reg, active, seq, onSwitch, onPick) {
    // The same MasterDetail the catalogue listing uses. What differs is only
    // what the body draws, which is this module's business and stays here.
    var themes = reg.themes;
    var pane = null;
    // The pick is a pair: the base from the tree, the palette from the menu
    // (null: the base's own). The slug is the registry's entry for the pair.
    // The picker opens on the pair the page wears, cross included; moving the
    // tree resets the colours to the new base's own — the palette it was
    // crafted for — and the menu offers the rest.
    var at = decompose(themes, active) || { theme: themes[0], palette: null };
    var base = at.theme, palette = at.palette;
    function slugOf() { var way = colourwayOf(base, palette) || colourwayOf(base, null); return way ? way.slug : base.slug; }
    function show() { if (pane) pane.update(base, palette, active); if (onPick) onPick(slugOf()); }

    var pair = mountMasterDetail({
        branch:      branch,
        host:        wrap,
        data:        themeTreeData(themes, base.slug),
        expandDepth: 2,
        // Neither. A row is its name and nothing else — the description and the
        // in-use marker live in the content pane, which is what keeps the tree
        // narrow and every name starting at the same x.
        showBadge:   false,
        showNote:    false,
        showRoot:    false,
        onSelect:    function (bodyEl, sel) {
            var t = themeBySlug(themes, slugOfSelection(sel));
            if (!t) return;                  // a group row selects nothing
            if (t !== base) palette = null;
            base = t;
            show();
        },
        onActivate:  function (sel) {
            if (slugOfSelection(sel)) onSwitch(slugOf());
        }
    });

    pane = _previewPane(branch, pair.bodyEl, seq, reg, function (paletteSlug) {
        // The base's own colours are "no palette" — the pair is then the base itself.
        palette = paletteSlug;
        show();
    });
    pane.update(base, palette, active);

    return { treeHost: pair.navEl, renderer: pair.renderer,
             selected: slugOf,
             // The menu, while it is down, owns the keys; otherwise the tree.
             handleKeydown: function (ev) {
                 return pane.colours.isOpen() ? pane.colours.handleKeydown(ev) : pair.renderer.handleKeydown(ev);
             },
             // The theme the page wears has moved — from this dialog, or from
             // elsewhere — so the "in use" chip moves with it (RFC 0064).
             markActive: function (slug) {
                 active = slug;
                 pane.update(base, palette, active);
             } };
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

    return fetchRegistry().then(function (reg) {
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

        var panes = _buildPanes(branch, body, reg, active, mySeq,
            function (slug) { switchToTheme(slug); });
        // The chip follows the page — a switch from here or from anywhere.
        css.onThemeApplied(function (change) { panes.markActive(change.to); });

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
 * Switching a theme is LIVE (RFC 0064, Wish 0018): the CSS manager swaps every
 * sheet on the page in dependency waves and the dialog is the same element
 * afterwards. Apply switches and keeps the dialog open, so you can try the next
 * one; OK switches and closes; Cancel does nothing. The theme already in use
 * is nothing to switch to, so Apply switches off and OK becomes Close then. A
 * switch that fails leaves the page whole, the dialog open and the chip where
 * it was. The header label and the chip follow the manager, so a switch made
 * elsewhere — the workbench, another tab — shows here too.
 */
function mountThemePickerButton(host, opts) {
    if (!host) throw new Error("mountThemePickerButton: host element required");
    var branch = domOpsParty.createBranch("themePickerBtn" + (++_seq));
    branch.activate(_btnOwner);
    var mySeq = _seq;

    return fetchRegistry().then(function (reg) {
        var themes = reg.themes;
        if (!themes || (themes.length < 2 && reg.palettes.length < 2)) return null;   // nothing to switch between
        var active = activeThemeSlug();
        if (!decompose(themes, active)) active = themes[0].slug;
        // The label reads the pair the slug names: "Neo-Brutalism", or "Neo-Brutalism in Forest".
        function labelOf(slug) { var at = decompose(themes, slug); return at ? wornLabel(at.theme, at.palette, reg.palettes) : slug; }

        var btn = branch.createElement("btn" + mySeq, "button");
        btn.type = "button";
        css.addClass(btn, tp_btn);
        btn.setAttribute("aria-haspopup", "dialog");

        var cap = branch.createElement("cap" + mySeq, "span");
        css.addClass(cap, tp_btn_label);
        cap.textContent = (opts && opts.label) || "Theme:";
        btn.appendChild(cap);

        var now = branch.createElement("now" + mySeq, "span");
        now.textContent = labelOf(active);
        btn.appendChild(now);

        var caret = branch.createElement("bcrt" + mySeq, "span");
        caret.textContent = "▾";
        caret.setAttribute("aria-hidden", "true");
        btn.appendChild(caret);

        var dialog = null;   // the SystemDialog handle, while open
        var panes  = null;   // the dialog's tree + pane, while open

        function close() { if (dialog) dialog.close(); }

        // `live` is "the selection would actually change something".
        function refresh(live) {
            if (!dialog) return;
            dialog.setAction("apply", { enabled: live });
            dialog.setAction("ok",    { label: live ? "OK" : "Close" });
        }

        // The page wears another theme now — from Apply, or from anywhere.
        function wear(slug) {
            active = slug;
            now.textContent = labelOf(slug);
            if (panes) { panes.markActive(slug); refresh(panes.selected() !== active); }
        }
        css.onThemeApplied(function (change) { wear(change.to); });

        function open() {
            if (dialog) { close(); return; }

            function applyPicked(keepOpen) {
                var slug = panes && panes.selected();
                if (!slug || slug === active) { if (!keepOpen) close(); return; }
                switchToTheme(slug).then(function () {
                    if (!keepOpen) close();
                }, function (err) {
                    console.error("[theme] switch failed; the page is unchanged", err);
                });
            }

            dialog = openSystemDialog({
                branch:         branch,
                title:          "Theme",
                modal:          true,
                // A showcase wants room: the tree is a narrow column of names
                // and the rest is a page. The dialog clamps this to the viewport.
                size:           { w: 1100, h: 780 },
                restoreFocusTo: btn,
                content: function (pb, bodyEl) {
                    panes = _buildPanes(pb, bodyEl, reg, active, mySeq,
                        // Enter on a row is the primary action, so it means OK.
                        function () { applyPicked(false); },
                        function (slug) { refresh(slug !== active); });
                    return { onKeydown: function (ev) { return panes.handleKeydown(ev); },
                             focusEl:   panes.treeHost };
                },
                actions: [
                    { id: "cancel", label: "Cancel",                onClick: function () { close(); } },
                    { id: "apply",  label: "Apply",                 onClick: function () { applyPicked(true);  } },
                    { id: "ok",     label: "OK",     primary: true, onClick: function () { applyPicked(false); } }
                ],
                // Every close path lands here exactly once — Escape, ✕, scrim,
                // Cancel, OK after its switch.
                onClose: function () {
                    dialog = null;
                    panes = null;
                }
            });
            refresh(panes.selected() !== active);
        }

        btn.addEventListener("click", open);
        host.appendChild(btn);

        return { branch: branch, open: open, close: close,
                 dissolve: function () { close(); branch.dissolve(); } };
    });
}
