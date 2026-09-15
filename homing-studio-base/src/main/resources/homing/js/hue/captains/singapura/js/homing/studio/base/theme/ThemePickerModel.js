// ThemePickerModel.js
//
// The theme picker's model half — everything that touches no DOM. Fetching the
// registry, shaping it into the tree payload, reading the active slug, and the
// session flag that carries "the picker was open" across a theme switch.
//
// Split out of ThemePicker when that module crossed the 250 effective-line
// limit. The count was the prompt, not the reason: this half needs no document
// to run, which makes it the seam worth cutting on.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java ThemePickerModel declaration — do not add import/export lines.

var _OPEN_KEY = "homing.themePicker.open";

/**
 * The active theme's slug — what the steward resolves for this page: the
 * address's override, else the stored pick, else null (the caller falls back
 * to the registry's first theme, which is what the page wears then).
 */
function activeThemeSlug() {
    return PreferenceViewInstance.resolve("theme", null);
}

function _slugify(s) {
    return String(s).toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-+)|(-+$)/g, "");
}

/** Find a theme record by slug, or null. */
function themeBySlug(themes, slug) {
    for (var i = 0; i < themes.length; i++) if (themes[i].slug === slug) return themes[i];
    return null;
}

/** GET /themes. The endpoint answers an object with a themes array, not a bare array. */
function fetchThemes() {
    return fetch("/themes").then(function (r) {
        if (!r.ok) throw new Error("/themes HTTP " + r.status);
        return r.json().then(function (j) {
            return (j && j.themes) ? j.themes : (Array.isArray(j) ? j : []);
        });
    });
}

/**
 * Shape the registry into the payload TreeRenderer consumes:
 * { level, segment, display: { label, badge, note, kind }, children }.
 *
 * Registry order decides group order and order within a group, so the server
 * stays the single place that decides presentation order.
 */
function themeTreeData(themes, active) {
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
                    // Nothing but the name in a row. The description and the in-use
                    // marker both live in the content pane, which is what lets the
                    // tree be a narrow column of names.
                    badge: "",
                    note:  "",
                    kind:  "theme"
                },
                children: []
            };
        });
        return {
            level:   "L1",
            segment: _slugify(name),
            // No count either — it told the reader nothing they could act on and
            // competed with the names for attention.
            display: { label: name, badge: "", note: "", kind: "group" },
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

/** A tree selection's theme slug — the last segment of its name-path. */
function slugOfSelection(sel) {
    if (!sel || sel.hasChildren) return null;
    var np = sel.namePath || "";
    var i = np.lastIndexOf("/");
    return i < 0 ? np : np.slice(i + 1);
}

/**
 * Switch to another theme: the pick is REMEMBERED, then the page reloads at
 * its bare address. The choice is a preference the browser keeps, not part of
 * the page's identity, so ?theme= is removed on the way — an override left in
 * the address would outrank the pick that was just made. Sharing a themed
 * view stays deliberate: the Themes page's Activate links carry ?theme= on
 * purpose, and the address wins while it names one.
 */
function switchToTheme(slug) {
    if (!slug) return;
    PreferenceStewardInstance.remember("theme", slug);
    HrefManagerInstance.navigate(HrefManagerInstance.withParam("theme", null));
}

// ── Reopen-after-switch ──────────────────────────────────────────────────────
//
// Switching a theme navigates, because live theme swap is not supported yet.
// The dialog therefore cannot survive the reload, so it is REBUILT: this flag
// says "the picker was open when we left" and the next mount honours it.
//
// sessionStorage rather than a URL parameter, deliberately. This is transient UI
// state, not an address: it must not appear in a shared link, must not outlive
// the tab, and must not become part of the page's identity. It also expires on
// its own, so nothing has to clean it up.
//
// Wrapped because storage throws outright in some contexts (private mode with
// site data blocked), and a picker that cannot remember is still a picker.

function rememberPickerOpen(open) {
    try {
        if (open) window.sessionStorage.setItem(_OPEN_KEY, "1");
        else window.sessionStorage.removeItem(_OPEN_KEY);
    } catch (e) { /* storage unavailable — degrade to not remembering */ }
}

/**
 * ONE-SHOT: reading the flag consumes it. The picker should reopen after the
 * switch that set it and not one navigation later — a flag that merely persisted
 * would follow the reader around the studio, opening a dialog nobody asked for.
 */
function pickerReopenWanted() {
    try {
        var wanted = window.sessionStorage.getItem(_OPEN_KEY) === "1";
        if (wanted) window.sessionStorage.removeItem(_OPEN_KEY);
        return wanted;
    } catch (e) { return false; }
}

// ── Preview ──────────────────────────────────────────────────────────────────

/**
 * The preview page under a theme: the address the picker's frame loads on each
 * selection. `theme` is set explicitly — an override for that page, which the
 * frame's own steward honours. Nothing else rides along: the frame shares this
 * page's origin and so its stored preferences, locale included.
 */
function previewUrl(slug) {
    return "/app?app=theme-preview&theme=" + encodeURIComponent(slug);
}
