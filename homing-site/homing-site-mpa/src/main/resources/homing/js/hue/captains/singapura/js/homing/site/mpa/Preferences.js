// =============================================================================
// Preferences — the user's preferences on the bar. The theme first.
//
// mountPreferences(branch, bar) → the button, appended to the bar
//
// The button names the design the page wears and opens a menu: the designs
// the site offers, then the colours the chosen design is offered in, then
// the way back to the site's default. A pick goes to the steward —
// remember("theme", slug) — and nowhere else: the CSS manager follows the
// store and switches every loaded sheet, and onThemeApplied is when the
// button and the marks are re-read. Nothing here touches a stylesheet; the
// one call to the manager is the way back, which an empty store cannot say.
//
// What the menu knows about themes it learns from /themes, once:
//   { default, themes: [ { slug, label, inspiration, swatches,
//                          colours: [ { palette, slug, own?, fits? } ] } ],
//     palettes: [ { slug, label, inspiration, anchor, swatches } ] }
// A theme is a design worn in colours; a colours entry's slug is the theme
// that pair makes, and the design's own colours are the entry marked own.
//
// The rows are options in a listbox, marked with aria-selected, which is the
// attribute the Selectable word draws its state from. The dots are DATA, the
// theme's own colours, set per dot through the runtime var (RFC 0044).
// =============================================================================

const _prefsOwner = Object.freeze({ toString: () => "mpaPreferences" });

var DOTS = ["surface", "accent", "inverted", "text"];

var _registry = null;
var _registryPromise = null;

function _fetchRegistry() {
    if (!_registryPromise) {
        _registryPromise = fetch("/themes").then(function (r) {
            if (!r.ok) throw new Error("/themes HTTP " + r.status);
            return r.json();
        }).then(function (reg) { _registry = reg; return reg; });
    }
    return _registryPromise;
}

/** The theme this page wears: what the manager loaded, else what the steward resolves. */
function _worn() {
    return css.theme() || PreferenceViewInstance.resolve("theme", _registry ? _registry.default : null);
}

/** { base, colour } for a worn slug, or null when the registry does not know it. */
function _decompose(reg, slug) {
    if (!reg || !slug) return null;
    for (var i = 0; i < reg.themes.length; i++) {
        var t = reg.themes[i];
        for (var j = 0; j < t.colours.length; j++) {
            if (t.colours[j].slug === slug) return { base: t, colour: t.colours[j] };
        }
    }
    return null;
}

function _paletteOf(reg, slug) {
    for (var i = 0; i < reg.palettes.length; i++) if (reg.palettes[i].slug === slug) return reg.palettes[i];
    return null;
}

function mountPreferences(branch, bar) {
    var wrap = branch.createElement("prefs", "div");
    css.addClass(wrap, mpa_prefs);

    var btn = branch.createElement("prefsBtn", "button");
    btn.type = "button";
    css.addClass(btn, mpa_prefs_btn);
    btn.setAttribute("aria-haspopup", "dialog");
    btn.setAttribute("aria-expanded", "false");
    var btnLabel = branch.createElement("prefsBtnLabel", "span");
    css.addClass(btnLabel, mpa_prefs_btn_label);
    btnLabel.textContent = "Theme";
    var btnName = branch.createElement("prefsBtnName", "span");
    btn.appendChild(btnLabel);
    btn.appendChild(btnName);
    wrap.appendChild(btn);
    bar.appendChild(wrap);

    var open_ = null;   // { sub, menu, fills, onKey } while the menu is up: one sub-branch per opening

    function nameOf(slug) {
        var d = _decompose(_registry, slug);
        if (!d) return slug || "default";
        return d.colour.own ? d.base.label : d.base.label + " · " + (_paletteOf(_registry, d.colour.palette) || { label: d.colour.palette }).label;
    }

    // The menu is drawn afresh on every change: each fill is a sub-branch of
    // the opening, dissolved before the next, so names never collide and a
    // close releases the whole thing in one call.
    function fill() {
        if (!open_) return;
        if (open_.fills > 0) open_.sub.dissolveBranch("fill-" + (open_.fills - 1));
        var f = open_.sub.createBranch("fill-" + open_.fills);
        f.activate(_prefsOwner);
        open_.fills += 1;
        _fill(f, open_.menu, close);
    }

    function refresh() {
        btnName.textContent = nameOf(_worn());
        fill();
    }

    function close() {
        if (!open_) return;
        document.removeEventListener("keydown", open_.onKey);
        branch.dissolveBranch("opening");
        open_ = null;
        btn.setAttribute("aria-expanded", "false");
    }

    function open() {
        if (open_) { close(); return; }
        var sub = branch.createBranch("opening");
        sub.activate(_prefsOwner);
        var scrim = sub.createElement("scrim", "div");
        css.addClass(scrim, mpa_prefs_scrim);
        scrim.addEventListener("click", close);
        var menu = sub.createElement("menu", "div");
        css.addClass(menu, mpa_prefs_menu);
        menu.setAttribute("role", "dialog");
        menu.setAttribute("aria-label", "Preferences");
        var onKey = function (e) { if (e.key === "Escape") { e.preventDefault(); close(); btn.focus(); } };
        document.addEventListener("keydown", onKey);
        document.body.appendChild(scrim);
        wrap.appendChild(menu);
        open_ = { sub: sub, menu: menu, fills: 0, onKey: onKey };
        btn.setAttribute("aria-expanded", "true");
        fill();
    }

    btn.addEventListener("click", open);
    css.onThemeApplied(refresh);
    PreferenceViewInstance.onChange(refresh);

    refresh();
    _fetchRegistry().then(refresh).catch(function (err) {
        console.error("[preferences] no theme registry:", err.message);
    });
    return wrap;
}

// ── The menu's contents, on a branch of their own ────────────────────────────

function _fill(branch, menu, close) {
    var reg = _registry;
    if (!reg) {
        var pending = branch.createElement("prefsPending", "div");
        css.addClass(pending, mpa_prefs_label);
        pending.textContent = "Loading themes…";
        menu.appendChild(pending);
        return;
    }
    var worn = _worn();
    var at = _decompose(reg, worn) || { base: reg.themes[0], colour: null };

    menu.appendChild(_label(branch, "design", "Design"));
    var designs = _list(branch, "designs", "Designs");
    for (var i = 0; i < reg.themes.length; i++) {
        var t = reg.themes[i];
        designs.appendChild(_row(branch, "d-" + t.slug, t.label, t.inspiration, t.swatches,
                                 t.slug === at.base.slug, _pick(t.slug, close)));
    }
    menu.appendChild(designs);

    var offered = at.base.colours.filter(function (c) { return c.own || c.fits; });
    if (offered.length > 1) {
        menu.appendChild(_label(branch, "colours", "Colours for " + at.base.label));
        var colours = _list(branch, "colours", "Colours");
        for (var j = 0; j < offered.length; j++) {
            var c = offered[j];
            var p = _paletteOf(reg, c.palette) || { label: c.palette, swatches: {} };
            colours.appendChild(_row(branch, "c-" + c.slug, p.label, c.own ? "own" : "", p.swatches,
                                     c.slug === worn, _pick(c.slug, close)));
        }
        menu.appendChild(colours);
    }

    if (PreferenceViewInstance.preferred("theme")) {
        var reset = _list(branch, "reset", "Default");
        reset.appendChild(_row(branch, "reset", "Use the site's default", _baseLabel(reg, reg.default), null, false,
                               function () {
                                   // Forgetting the pick is the steward's; going back to the default
                                   // is not something the manager can infer from an empty store (it
                                   // falls back to what the page wears), so it is asked outright.
                                   PreferenceStewardInstance.forget("theme");
                                   css.switchTheme(reg.default).catch(function (e) { console.error("[preferences] switch failed", e); });
                                   close();
                               }));
        menu.appendChild(reset);
    }
}

function _baseLabel(reg, slug) {
    var d = _decompose(reg, slug);
    return d ? d.base.label : (slug || "");
}

function _pick(slug, close) {
    return function () { PreferenceStewardInstance.remember("theme", slug); close(); };
}

function _label(branch, key, text) {
    var el = branch.createElement("label-" + key, "div");
    css.addClass(el, mpa_prefs_label);
    el.textContent = text;
    return el;
}

function _list(branch, key, ariaLabel) {
    var ul = branch.createElement("list-" + key, "div");
    css.addClass(ul, mpa_prefs_list);
    ul.setAttribute("role", "listbox");
    ul.setAttribute("aria-label", ariaLabel);
    return ul;
}

function _row(branch, key, name, note, swatches, selected, onPick) {
    var row = branch.createElement("row-" + key, "button");
    row.type = "button";
    css.addClass(row, mpa_prefs_item);
    row.setAttribute("role", "option");
    row.setAttribute("aria-selected", selected ? "true" : "false");
    if (swatches) {
        var dots = branch.createElement("dots-" + key, "span");
        css.addClass(dots, mpa_prefs_dots);
        dots.setAttribute("aria-hidden", "true");
        for (var i = 0; i < DOTS.length; i++) {
            var dot = branch.createElement("dot-" + key + "-" + i, "span");
            css.addClass(dot, mpa_prefs_dot);
            var v = swatches[DOTS[i]];
            if (v) dot.style.setProperty("--mpa-dot", v);   // DATA, via the runtime var (RFC 0044)
            dots.appendChild(dot);
        }
        row.appendChild(dots);
    }
    var text = branch.createElement("name-" + key, "span");
    css.addClass(text, mpa_prefs_name);
    text.textContent = name;
    row.appendChild(text);
    if (note) {
        var n = branch.createElement("note-" + key, "span");
        css.addClass(n, mpa_prefs_note);
        n.textContent = note;
        row.appendChild(n);
    }
    row.addEventListener("click", onPick);
    return row;
}
