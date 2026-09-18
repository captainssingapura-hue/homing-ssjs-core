// ThemeColours.js
//
// The colour control of the theme picker: a pill floating in the corner of
// the preview, naming the colours the base is shown in, and the menu it
// opens — one option per palette the registry OFFERS for the base. Colour is
// an orthogonal plane of a design, so the control is orthogonal to the tree:
// the tree picks the base, this picks the colours, and the slug the page will
// wear is the base's entry for the palette — read off the registry, never
// composed here.
//
// What is offered is the registry's word, not this module's: a base's entry
// for a palette carries `fits` when the palette says it was crafted for the
// base or suits it. An unoffered pair the page already wears — reached by
// its slug, deliberately — is listed too, so the pill always names an option;
// it is simply never suggested.
//
// The menu is a listbox: the options are Selectable — aria-selected for the
// pick, data-highlighted for the cursor — and the look at either state is the
// design's word, no second class applied beside it. The pill is hidden
// outright when the base has nothing to choose but its own colours.
//
// The frame under the menu is a document of its own, so a click on it never
// reaches this document: an open menu lays a transparent scrim over the wrap
// to catch that click, and a document listener catches the rest.
//
// The dots are DATA: each palette's own surface, accent, inverted and text
// colours, set per dot as a custom property (RFC 0044) that the dot's class
// reads.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java ThemeColours declaration — do not add import/export lines here.

var DOTS = ["surface", "accent", "inverted", "text"];

// Module-scoped, frozen owner for each opening's sub-branch (DomOpsParty tracks
// an owner by WeakRef; a function-local one would be collectible while its
// elements were still on the page).
const _menuOwner = Object.freeze({ toString: () => "themeColoursMenu" });

/**
 * Build the control into `wrap` — the positioned box holding the preview
 * frame — and return its handle:
 *
 *   update(theme, paletteSlug, themes)  re-list for the base and mark the pick
 *   isOpen()                            whether the menu is down
 *   handleKeydown(ev)                   the menu's keys while it is down; true if taken
 *   close()
 *
 * `onPick(paletteSlug)` fires with the palette chosen, or null for the base's
 * own colours.
 */
function mountColourMenu(branch, wrap, seq, palettes, onPick) {
    var bySlug = {};
    palettes.forEach(function (p) { bySlug[p.slug] = p; });

    var pill = branch.createElement("clr" + seq, "button");
    pill.type = "button";
    css.addClass(pill, tp_colours);
    pill.setAttribute("aria-haspopup", "listbox");
    pill.setAttribute("aria-expanded", "false");
    pill.hidden = true;
    wrap.appendChild(pill);

    var label = branch.createElement("clrl" + seq, "span");
    css.addClass(label, tp_colours_label);
    label.textContent = "Colours";
    pill.appendChild(label);

    // The pill's dots are minted once and recoloured per update: a branch
    // keeps every element it minted, so nothing here is rebuilt in place.
    var pillDots = _dots(branch, pill, "clrd" + seq);

    var pillName = branch.createElement("clrn" + seq, "span");
    pill.appendChild(pillName);

    var caret = branch.createElement("clrc" + seq, "span");
    caret.textContent = "▾";
    caret.setAttribute("aria-hidden", "true");
    pill.appendChild(caret);

    var open_ = null;                  // while open: { name: the sub-branch, menu }
    var items = [];                    // { el, slug } in menu order, while open
    var cursor = -1;                   // the highlighted option's index
    var opened = 0;                    // numbers the openings — each is a sub-branch, dissolved on close

    var theme = null, picked = null, ownSlug = null, offered = [];
    var anchorLabels = {};             // base slug -> label, for a borrowed palette's provenance

    /** Four dots in a strip, into `host`; returns the recolouring function. */
    function _dots(b, host, id) {
        var strip = b.createElement(id, "span");
        css.addClass(strip, tp_swatch_dots);
        strip.setAttribute("aria-hidden", "true");
        host.appendChild(strip);
        var dots = DOTS.map(function (k, j) {
            var dot = b.createElement(id + "_" + j, "span");
            css.addClass(dot, tp_swatch_dot);
            strip.appendChild(dot);
            return dot;
        });
        return function (p) {
            DOTS.forEach(function (k, j) {
                var v = p && p.swatches && p.swatches[k];
                dots[j].hidden = !v;
                if (v) dots[j].style.setProperty("--tp-dot", v);       // DATA, via setProperty (RFC 0044)
            });
        };
    }

    // The base's colourways the registry offers, own first; plus the one
    // worn now if it is not among them.
    function _offered(t, pickedSlug) {
        var list = (t && t.colours) || [];
        var out = [];
        list.forEach(function (w) { if (w.own) out.push(w.palette); });
        list.forEach(function (w) { if (w.fits && !w.own) out.push(w.palette); });
        if (pickedSlug && out.indexOf(pickedSlug) < 0) out.push(pickedSlug);
        return out;
    }

    function _highlight(i) {
        if (cursor >= 0 && items[cursor]) items[cursor].el.removeAttribute("data-highlighted");
        cursor = i;
        if (cursor >= 0 && items[cursor]) {
            items[cursor].el.setAttribute("data-highlighted", "");
            open_.menu.setAttribute("aria-activedescendant", items[cursor].el.id);
            if (items[cursor].el.scrollIntoView) items[cursor].el.scrollIntoView({ block: "nearest" });
        }
    }

    function _onDocMousedown(ev) {
        if (open_ && (open_.menu.contains(ev.target) || pill.contains(ev.target))) return;
        close();
    }

    // The opening's sub-branch goes with it: scrim, menu and options together.
    function close() {
        if (!open_) return;
        document.removeEventListener("mousedown", _onDocMousedown, true);
        branch.dissolveBranch(open_.name);
        open_ = null;
        items = [];
        cursor = -1;
        pill.setAttribute("aria-expanded", "false");
        if (document.contains(pill)) pill.focus();
    }

    function pick(slug) {
        close();
        onPick(slug === ownSlug ? null : slug);
    }

    function open() {
        if (open_ || !theme) return;
        var name = "clrmenu" + (++opened);
        var b = branch.createBranch(name);
        b.activate(_menuOwner);

        var scrim = b.createElement("scrim", "div");
        css.addClass(scrim, tp_colours_scrim);
        scrim.addEventListener("mousedown", function (ev) { ev.preventDefault(); close(); });
        wrap.appendChild(scrim);

        var menu = b.createElement("menu", "div");
        css.addClass(menu, tp_colours_menu);
        menu.setAttribute("role", "listbox");
        menu.setAttribute("aria-label", "Colours");
        menu.tabIndex = -1;
        wrap.appendChild(menu);
        open_ = { name: name, menu: menu };

        offered.forEach(function (slug, i) {
            var p = bySlug[slug];
            if (!p) return;
            var el = b.createElement("opt" + i, "div");
            el.id = "tp-clr-" + seq + "-" + i;
            css.addClass(el, tp_colours_item);
            el.setAttribute("role", "option");
            el.setAttribute("aria-selected", slug === picked ? "true" : "false");
            el.title = p.inspiration || "";

            _dots(b, el, "dots" + i)(p);

            var label = b.createElement("name" + i, "span");
            label.textContent = p.label || slug;
            el.appendChild(label);

            if (slug === ownSlug) {
                var own = b.createElement("own" + i, "span");
                css.addClass(own, tp_swatch_own);
                own.textContent = "default";
                el.appendChild(own);
            } else if (p.anchor) {
                var forEl = b.createElement("for" + i, "span");
                css.addClass(forEl, tp_swatch_for);
                forEl.textContent = (anchorLabels[p.anchor] || p.anchor) + "’s";
                el.appendChild(forEl);
            }

            el.addEventListener("mousemove", function () { if (cursor !== i) _highlight(i); });
            el.addEventListener("click", function () { pick(slug); });
            menu.appendChild(el);
            items.push({ el: el, slug: slug });
        });

        pill.setAttribute("aria-expanded", "true");
        document.addEventListener("mousedown", _onDocMousedown, true);
        var at = 0;
        for (var i = 0; i < items.length; i++) if (items[i].slug === picked) at = i;
        _highlight(at);
        menu.focus();
    }

    pill.addEventListener("click", function () { if (open_) close(); else open(); });

    function handleKeydown(ev) {
        if (!open_) return false;
        var k = ev.key;
        if (k === "Escape") { close(); return true; }
        if (k === "ArrowDown") { _highlight(Math.min(items.length - 1, cursor + 1)); return true; }
        if (k === "ArrowUp")   { _highlight(Math.max(0, cursor - 1)); return true; }
        if (k === "Home")      { _highlight(0); return true; }
        if (k === "End")       { _highlight(items.length - 1); return true; }
        if (k === "Enter" || k === " ") { if (cursor >= 0 && items[cursor]) pick(items[cursor].slug); return true; }
        if (k === "Tab") { close(); return false; }
        return false;
    }
    // Inline — no dialog routing the keys — the menu hears its own, from the
    // pill or the listbox, both inside the wrap.
    wrap.addEventListener("keydown", function (ev) { if (handleKeydown(ev)) { ev.preventDefault(); ev.stopPropagation(); } });

    /**
     * Re-list for `theme` and mark `paletteSlug` (null: the base's own) as
     * the pick. `themes` — the registry's bases — is what names an anchor.
     */
    function update(t, paletteSlug, themes) {
        close();
        theme = t;
        if (themes) themes.forEach(function (b) { anchorLabels[b.slug] = b.label || b.slug; });
        var mine = colourwayOf(t, null);
        ownSlug = mine ? mine.palette : null;
        picked = paletteSlug || ownSlug;
        offered = _offered(t, picked);
        var p = bySlug[picked];
        pillName.textContent = p ? (p.label || picked) : (picked || "");
        pillDots(p);
        pill.hidden = offered.length < 2;
    }

    return { update: update, isOpen: function () { return !!open_; }, handleKeydown: handleKeydown, close: close };
}
