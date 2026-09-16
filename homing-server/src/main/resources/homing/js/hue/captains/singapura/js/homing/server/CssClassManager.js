// =============================================================================
// CssClassManager — RFC 0002-ext1, RFC 0064
//
// The one module that puts a stylesheet on the page. Every served CSS group
// module calls loadCss(group, fallbackTheme, subgraph); the manager merges the
// group's dependency subgraph into the page's graph (CssDependencyGraph),
// resolves the theme through the preference steward, and has the load
// procedure (CssLoadProcedure) bring the group's whole tree in — dependencies
// first, missing ones by name, applied all at once.
//
// switchTheme(to) is the same procedure over every node the page has loaded:
// the new theme's sheets arrive in dependency waves under media="not all",
// flip in one pass, and the old theme's leave. A -> B -> A works because
// leaving a theme forgets it. The manager also FOLLOWS the store: when the
// steward reports a change and the resolved theme differs from the one worn,
// it switches — which is how another tab's pick reaches this one.
//
// The handles (CssHandles) are identity, bound late by the cascade — every
// holder keeps its handle across a switch. The graph and the procedure are
// pure; this module is where the DOM is touched, in one place.
// =============================================================================

const CssClassManagerInstance = (() => {
    const graph = createCssDependencyGraph();

    function hrefFor(id, theme) {
        const q = theme ? "theme=" + encodeURIComponent(theme) : "";
        return "/css-content?class=" + encodeURIComponent(id) + (q ? "&" + q : "");
    }

    function appendLink(href) {
        const link = document.createElement("link");
        link.rel = "stylesheet";
        link.href = href;
        const loaded = new Promise((resolve, reject) => {
            link.onload = () => resolve(link);
            link.onerror = () => reject(new Error("Failed to load CSS: " + href));
        });
        document.head.appendChild(link);
        return { link, loaded };
    }

    // Progress fans out to whoever watches — the workbench draws a switch as
    // it happens. A notification is { id, theme, state } with state one of
    // appended, landed, applied, failed, retired.
    const watchers = new Set();
    function tell(id, theme, state) {
        for (const fn of Array.from(watchers)) {
            try { fn({ id, theme, state }); } catch (e) { console.error("[css] watcher failed", e); }
        }
    }
    const procedure = createCssLoadProcedure(graph, appendLink, hrefFor, tell);

    let worn = null;          // the theme the page wears, once anything has loaded
    let changing = null;      // the switch in flight, if one is
    const listeners = new Set();

    /**
     * Which theme this page loads under: the steward's answer — the address as
     * this page's override, else the stored pick — falling back to what the
     * served group module carries, which is the server's default. One answer
     * for every group, so a widget mounted later arrives in the same theme.
     */
    function themeFor(fallback) {
        return worn || PreferenceViewInstance.resolve("theme", fallback || null);
    }

    async function loadCss(cssBeing, fallbackTheme, subgraph) {
        graph.merge(subgraph || { [cssBeing]: { deps: [] } });
        if (changing) await changing.catch(() => {});
        const theme = themeFor(fallbackTheme);
        await procedure.load(graph.closureOf([cssBeing]), theme);
        if (!worn) worn = theme;
    }

    /**
     * Refresh every loaded node under another theme, then retire the current
     * one. Resolves when the page wears `to`; rejects, with the page whole
     * under the old theme, if any sheet fails to arrive.
     */
    function switchTheme(to) {
        if (!to || to === worn) return Promise.resolve(worn);
        if (changing) return changing.then(() => switchTheme(to));
        const from = worn;
        const nodes = procedure.loadedUnder(from);
        changing = procedure.load(nodes, to).then(() => {
            procedure.retire(from);
            worn = to;
            for (const fn of Array.from(listeners)) {
                try { fn({ from, to }); } catch (e) { console.error("[css] listener failed", e); }
            }
            return to;
        }).finally(() => { changing = null; });
        return changing;
    }

    /** Call fn({ id, theme, state }) as each sheet moves. Returns the function that stops listening. */
    function onProgress(fn) {
        if (typeof fn !== "function") throw new TypeError("css.onProgress: fn must be a function");
        watchers.add(fn);
        return () => { watchers.delete(fn); };
    }

    /** Call fn({ from, to }) after a switch has applied. Returns the function that stops listening. */
    function onThemeApplied(fn) {
        if (typeof fn !== "function") throw new TypeError("css.onThemeApplied: fn must be a function");
        listeners.add(fn);
        return () => { listeners.delete(fn); };
    }

    // Follow the store: a pick here or in another tab. The address's override
    // still wins inside resolve(), so a tab pinned by ?theme= stays put.
    PreferenceViewInstance.onChange(() => {
        const next = PreferenceViewInstance.resolve("theme", worn);
        if (worn && next && next !== worn) switchTheme(next).catch(e => console.error("[css] switch failed", e));
    });

    /** The waves a switch to `to` (or a load of `ids`) WOULD run — nothing appended. */
    function plan(ids) {
        const set = ids || procedure.loadedUnder(worn);
        return graph.plan(set);
    }

    /** Frozen, plain data — the workbench's whole view: the theme worn, the graph, every sheet. */
    function snapshot() {
        return Object.freeze({ theme: worn, graph: graph.snapshot(), sheets: procedure.snapshot() });
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
        switchTheme,
        onThemeApplied,
        onProgress,
        plan,
        snapshot,
        theme() { return worn; },
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
