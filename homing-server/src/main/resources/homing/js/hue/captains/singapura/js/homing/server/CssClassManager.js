// =============================================================================
// CssClassManager — RFC 0002-ext1
// Per-class JS handles use ES6 classes for prototype sharing and clean variant
// exposition. Each pseudo-state has a dedicated subclass mirroring the Java
// side (HoverVariantOf / FocusVariantOf / ActiveVariantOf): one uniform value
// type for plain classes, one subclass per variant state.
// =============================================================================

class CssClass {
    constructor(name) { this.name = name; }
    toString() { return this.name; }
}

// Dedicated variant classes — mirror Java's HoverVariantOf / FocusVariantOf /
// ActiveVariantOf. Each carries the kebab-name of the state-restricted CSS
// rule. Distinct types so consumers can introspect via `instanceof`.
class HoverVariant  extends CssClass { static pseudoState = ":hover";  }
class FocusVariant  extends CssClass { static pseudoState = ":focus";  }
class ActiveVariant extends CssClass { static pseudoState = ":active"; }

// Lookup: variant state name (as emitted by CssGroupContentProvider) →
// dedicated subclass. Unknown states fall back to plain CssClass — keeps the
// JS forward-compatible if a new VariantOf subtype is added on the Java side
// before the JS is updated.
const VARIANT_CLASSES = {
    hover:  HoverVariant,
    focus:  FocusVariant,
    active: ActiveVariant,
};

class CssUtility extends CssClass {
    /**
     * @param {string} name           kebab-case base class name
     * @param {Object<string,string>} variants  state → variant kebab-name (e.g. { hover: "hover-bg-accent" })
     *
     * Each variant is precomputed once as the appropriate subclass instance
     * (HoverVariant / FocusVariant / etc.) and exposed as a PROPERTY (no
     * parens, no string return). This keeps every class handle in the system
     * the same uniform value type — `cls instanceof CssClass` for all of them
     * — while preserving distinguishability via the dedicated subclasses.
     *
     * Use site: `cn(bg_accent, bg_accent.hover)` — property access, no parens.
     */
    constructor(name, variants) {
        super(name);
        const states = Object.keys(variants || {});
        for (const state of states) {
            const VariantClass = VARIANT_CLASSES[state] || CssClass;
            this[state] = new VariantClass(variants[state]);
        }
    }
}

const CssClassManagerInstance = (() => {
    const loaded = new Set();

    /**
     * RFC 0002-ext1 Phase 09 — auto-load the theme bundle (vars + globals)
     * once per page per theme. The bundle endpoints always return 200 with an
     * empty body when the theme has nothing registered, so this is safe to
     * call before deployments have populated their ThemeRegistry. Once
     * deployments migrate (Phase 10/11), the bundle becomes the canonical
     * source of `:root` and global rules; per-group `/css-content` files are
     * just class rules.
     */
    async function ensureThemeBundleLoaded(theme) {
        // SEQUENTIAL: theme-vars must land in the cascade BEFORE theme-globals.
        // Globals contains `@media (prefers-color-scheme: dark) { :root { … } }`
        // overrides for primitives; if vars loaded last, the unconditional :root
        // would shadow the @media override (last-wins for same specificity).
        //
        // When `theme` is null (no ?theme= URL param), we still call the routes
        // — the server uses its registered default theme. Without this, class
        // bodies that reference `var(--color-*)` resolve to nothing because the
        // cascade is never set up.
        const themeKey = theme || "__default";
        const themeQuery = theme ? "?theme=" + encodeURIComponent(theme) : "";
        const varsKey = themeKey + ":__theme-vars";
        if (!loaded.has(varsKey)) {
            loaded.add(varsKey);
            await appendLink("/theme-vars" + themeQuery).catch(() => {});
        }
        const globalsKey = themeKey + ":__theme-globals";
        if (!loaded.has(globalsKey)) {
            loaded.add(globalsKey);
            await appendLink("/theme-globals" + themeQuery).catch(() => {});
        }
    }

    /**
     * RFC 0064 — which theme this page loads under. The served group module
     * carries the theme the server propagated, and the server now propagates
     * only its default; the steward resolves the address's override, then the
     * stored pick, and falls back to that default. One answer for every group
     * on the page, so a widget mounted later arrives in the same theme.
     */
    function themeFor(fallback) {
        return PreferenceStewardInstance.resolve("theme", fallback);
    }

    async function loadCss(cssBeing, theme) {
        theme = themeFor(theme);
        // Theme-scoped bundle (vars + globals) loads first, idempotently.
        await ensureThemeBundleLoaded(theme);

        const key = cssBeing + (theme ? ":" + theme : "");
        if (loaded.has(key)) return;
        loaded.add(key);

        // Each module emits one loadCss(group) call per group it imports,
        // so transitive dependencies are already resolved at module-link
        // time — no need for a separate /css JSON resolver round-trip.
        let href = "/css-content?class=" + encodeURIComponent(cssBeing);
        if (theme) href += "&theme=" + encodeURIComponent(theme);
        await appendLink(href);
    }

    function appendLink(href) {
        return new Promise((resolve, reject) => {
            const link = document.createElement("link");
            link.rel = "stylesheet";
            link.href = href;
            link.onload = resolve;
            link.onerror = () => reject(new Error("Failed to load CSS: " + href));
            document.head.appendChild(link);
        });
    }

    /**
     * Resolve a class-handle to its kebab-name string. Every class handle in
     * the system — base utilities, plain CssClass records, and variant
     * properties — is a {@link CssClass} instance. No string branch.
     */
    function resolve(cls) {
        if (cls instanceof CssClass) return cls.name;
        throw new Error("Invalid CSS class handle: " + JSON.stringify(cls));
    }

    /**
     * Factory used by emitted CssGroup modules.
     *   _css.cls("st-root")                                   → CssClass
     *   _css.cls("bg-accent", { hover: "hover-bg-accent" })   → CssUtility
     */
    function cls(name, variants) {
        return variants ? new CssUtility(name, variants) : new CssClass(name);
    }

    return {
        loadCss,
        cls,
        addClass(el, ...classes)            { for (const c of classes) el.classList.add(resolve(c)); },
        removeClass(el, ...classes)         { for (const c of classes) el.classList.remove(resolve(c)); },
        toggleClass(el, c, force) {
            arguments.length === 3
                ? el.classList.toggle(resolve(c), force)
                : el.classList.toggle(resolve(c));
        },
        setClass(el, ...classes)            { el.className = classes.map(resolve).join(" "); },
        hasClass(el, c)                     { return el.classList.contains(resolve(c)); },
        className(c)                        { return resolve(c); }
    };
})();
