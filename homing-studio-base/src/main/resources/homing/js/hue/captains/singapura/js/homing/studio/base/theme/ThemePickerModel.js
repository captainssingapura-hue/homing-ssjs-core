// ThemePickerModel.js
//
// The theme picker's model half — everything that touches no DOM. Fetching the
// registry, shaping it into the tree payload, reading the active slug, and the
// switch itself — the manager, the address, the store, in that order.
//
// Split out of ThemePicker when that module crossed the 250 effective-line
// limit. The count was the prompt, not the reason: this half needs no document
// to run, which makes it the seam worth cutting on.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java ThemePickerModel declaration — do not add import/export lines.

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

/**
 * GET /themes: the registry on its two axes — { themes: [base…], palettes: [colours…] }.
 * A base carries `colours`, one entry per palette with the slug that names the
 * base worn in it (`own: true` for its own), so the client composes nothing.
 */
function fetchRegistry() {
    return fetch("/themes").then(function (r) {
        if (!r.ok) throw new Error("/themes HTTP " + r.status);
        return r.json().then(function (j) {
            return { themes: (j && j.themes) || [], palettes: (j && j.palettes) || [] };
        });
    });
}

/** GET /themes, unwrapped to the bases alone. */
function fetchThemes() {
    return fetchRegistry().then(function (reg) { return reg.themes; });
}

/**
 * A base worn in some colours: the base's entry for the palette slug, or its own
 * entry when `paletteSlug` is null or names its own colours. Null when the base
 * has no such entry.
 */
function colourwayOf(theme, paletteSlug) {
    var list = (theme && theme.colours) || [];
    var own = null;
    for (var i = 0; i < list.length; i++) {
        if (list[i].own) own = list[i];
        if (paletteSlug && list[i].palette === paletteSlug) return list[i];
    }
    return paletteSlug ? null : own;
}

/**
 * The base and palette a worn slug names: search every base's colourways for it.
 * `{ theme, palette }` with `palette` null for a base in its own colours; null
 * when no base wears it.
 */
function decompose(themes, slug) {
    for (var i = 0; i < themes.length; i++) {
        var list = themes[i].colours || [];
        for (var j = 0; j < list.length; j++) {
            if (list[j].slug === slug) return { theme: themes[i], palette: list[j].own ? null : list[j].palette };
        }
        if (themes[i].slug === slug) return { theme: themes[i], palette: null };
    }
    return null;
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
 * Switch to another theme — live, on this page (RFC 0064). Three steps, in an
 * order that is the whole design:
 *
 *   1. the CSS manager switches: every loaded sheet arrives under the new
 *      theme in dependency waves and flips at once; if any sheet fails, it
 *      rejects with the page whole under the old theme and nothing below runs;
 *   2. the address drops any ?theme= override, in place — no history entry:
 *      an override left there would outrank the pick on the next resolve;
 *   3. the pick is remembered. The steward's change reaches the manager's own
 *      follower, which resolves — address silent, store says the theme the
 *      page already wears — and does nothing. Remember BEFORE the address and
 *      the follower would resolve to the stale override and switch back.
 *
 * Resolves when the page wears the theme. Sharing a themed view stays
 * deliberate: the Themes page's Activate links carry ?theme= on purpose.
 */
function switchToTheme(slug) {
    if (!slug) return Promise.resolve();
    return CssClassManagerInstance.switchTheme(slug).then(function () {
        HrefManagerInstance.replaceParam("theme", null);
        PreferenceStewardInstance.remember("theme", slug);
    });
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
