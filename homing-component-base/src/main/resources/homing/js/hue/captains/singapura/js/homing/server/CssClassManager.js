// =============================================================================
// CssClassManager — RFC 0002-ext1, RFC 0064
//
// The CSS manager handles what does not vary (RFC 0064, D25). Every served
// provided group module calls loadCss(group, subgraph); the manager merges the
// group's dependency subgraph into the page's graph and has the load procedure
// bring the group's whole tree in — dependencies first, missing ones by name,
// applied all at once — ONCE, with no theme: a provided group is the same in
// every theme, so a theme switch reloads nothing here. What varies is derived,
// and is the theme's owner's (ThemeSheets).
//
// The rest is the element's: its classes by handle — a provided class with
// the tokens it wears — and its degrees, size, extent, aspect and elevation.
// The handles (CssHandles) are identity, bound late by the cascade.
// =============================================================================

const CssClassManagerInstance = (() => {
    const graph = createCssDependencyGraph();

    function hrefFor(id) { return "/css-content?class=" + encodeURIComponent(id); }

    // Progress fans out to whoever watches: { id, theme, state }, with state one
    // of appended, landed, applied, failed; the theme is always null here.
    const watchers = new Set();
    function tell(id, theme, state) {
        for (const fn of Array.from(watchers)) {
            try { fn({ id, theme, state }); } catch (e) { console.error("[css] watcher failed", e); }
        }
    }

    const procedure = createCssLoadProcedure(graph, (href) => CssLink.append(href), hrefFor, tell);

    /** A provided group and its tree, loaded once and applied at once. */
    async function loadCss(cssBeing, subgraph) {
        graph.merge(subgraph || { [cssBeing]: { deps: [] } });
        await procedure.load(graph.closureOf([cssBeing]), null);
    }

    /** Call fn({ id, theme, state }) as each sheet moves. Returns the function that stops listening. */
    function onProgress(fn) {
        if (typeof fn !== "function") throw new TypeError("css.onProgress: fn must be a function");
        watchers.add(fn);
        return () => { watchers.delete(fn); };
    }

    /** Frozen, plain data: the graph, and every sheet. */
    function snapshot() {
        return Object.freeze({ graph: graph.snapshot(), sheets: procedure.snapshot() });
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
    function cls(name, variants, wears) {
        return variants ? new CssUtility(name, variants, wears) : new CssClass(name, wears);
    }

    /** The tokens an add or remove touches: the class and everything it wears. */
    function tokensOf(c) { return [resolve(c)].concat(c.wears || []); }

    return {
        loadCss,
        onProgress,
        snapshot,
        cls,
        addClass(el, ...classes)            { for (const c of classes) el.classList.add(...tokensOf(c)); },
        removeClass(el, ...classes)         { for (const c of classes) el.classList.remove(...tokensOf(c)); },
        toggleClass(el, c, force) {
            const on = arguments.length === 3 ? !!force : !el.classList.contains(resolve(c));
            for (const t of tokensOf(c)) el.classList.toggle(t, on);
        },
        setClass(el, ...classes)            { el.className = classes.flatMap(tokensOf).join(" "); },
        hasClass(el, c)                     { return el.classList.contains(resolve(c)); },
        className(c)                        { return resolve(c); },
        /**
         * The element's EXTENT: how much of the meaning its coloured words
         * carry, from -1 (the meaning turned the other way) through 0
         * (neutral) to 1 (the word at full — the default, and what null
         * restores). The one number a component may set on an element:
         * a design's word for every pair the class names in extents()
         * interpolates by it. Clamped; not inherited by the children.
         */
        extent(el, t) {
            if (t == null) { el.style.removeProperty("--extent"); return; }
            const n = Math.max(-1, Math.min(1, Number(t)));
            el.style.setProperty("--extent", String(Number.isFinite(n) ? n : 1));
        },
        /**
         * The element's SIZE: from -1 (the smallest) through 0 (regular —
         * the default, and what null restores) to 1 (the biggest). The other
         * number a component may set on an element: every length of a
         * design's word for the pairs the class names in sizes() grows by
         * the word's ratio to the power of it. Clamped; not inherited.
         */
        size(el, s) {
            if (s == null) { el.style.removeProperty("--size"); return; }
            const n = Math.max(-1, Math.min(1, Number(s)));
            el.style.setProperty("--size", String(Number.isFinite(n) ? n : 0));
        },
        /**
         * The element's ASPECT: how wide a box whose measure is its own, from
         * -1 (the tallest the design allows) through 0 (square - the default,
         * and what null restores) to 1 (the widest). The design's proportion
         * for the pairs the class names in aspects() is its widest to the
         * power of it. Clamped; not inherited.
         */
        aspect(el, a) {
            if (a == null) { el.style.removeProperty("--aspect"); return; }
            const n = Math.max(-1, Math.min(1, Number(a)));
            el.style.setProperty("--aspect", String(Number.isFinite(n) ? n : 0));
        },
        /**
         * The element's ELEVATION: how far off its own plane it sits -
         * "elevated", "sunken", or flat, which is null and writes nothing.
         * One attribute, read by the design as a state on the word that says
         * what the thing is; a design with no idiom for depth answers neither
         * register, and the element stays where it is, which is its right.
         * IT MEANS NOTHING BY ITSELF: what a register stands for is the
         * caller's, said in the caller's own code.
         */
        elevation(el, e) {
            if (e == null || e === "flat") { el.removeAttribute("data-elevation"); return; }
            if (e !== "elevated" && e !== "sunken") throw new Error("[css] elevation: 'elevated', 'sunken' or null, not " + e);
            el.setAttribute("data-elevation", e);
        }
    };
})();
