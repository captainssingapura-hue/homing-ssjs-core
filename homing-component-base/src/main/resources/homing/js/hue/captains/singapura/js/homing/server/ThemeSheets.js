// =============================================================================
// ThemeSheets — the theme's owner (RFC 0064, D25): the derived sheets, and the
// switch. A provided group is the same in every theme, and the CSS manager
// loads it once. What varies with the theme is DERIVED — a design's words,
// the house's look — and is loaded here, under the theme worn, and swapped on
// a switch. Every served module that needs a derived sheet hands it here: the
// derived sheet's own module.
//
//   load(ids, fallbackTheme)   the sheets under the theme worn — the steward's
//                              answer, else the server's default — applied at once
//   switchTheme(to)            every sheet loaded, fetched again under `to` under
//                              media="not all", flipped in one pass, then the old
//                              theme's leave: A -> B -> A works, since leaving a
//                              theme forgets it
//   theme()                    the theme worn, once anything has loaded
//   onThemeApplied(fn)         fn({ from, to }) after a switch has applied
//   onProgress(fn)             fn({ id, theme, state }) as each sheet moves
//   plan()                     the waves a switch would run, nothing appended
//   snapshot()                 the theme worn, the graph, every sheet, as data
//
// It follows the store: a pick here or in another tab switches it, the
// address still winning inside the steward's resolve.
// =============================================================================

const ThemeSheetsInstance = (() => {
    const graph = createCssDependencyGraph();

    function hrefFor(id, theme) {
        return "/css-content?class=" + encodeURIComponent(id) + (theme ? "&theme=" + encodeURIComponent(theme) : "");
    }

    const watchers = new Set();
    function tell(id, theme, state) {
        for (const fn of Array.from(watchers)) {
            try { fn({ id, theme, state }); } catch (e) { console.error("[themes] watcher failed", e); }
        }
    }

    const procedure = createCssLoadProcedure(graph, (href) => CssLink.append(href), hrefFor, tell);

    let worn = null;          // the theme the page wears, once anything has loaded
    let pageDefault = null;   // the server's default, as the first module carried it
    let changing = null;      // the switch in flight, if one is
    const listeners = new Set();

    /** The theme to load under: the one worn, else the steward's answer, else the server's default. */
    function themeFor(fallback) {
        return worn || PreferenceViewInstance.resolve("theme", fallback || null);
    }

    /** These derived sheets under the theme worn, applied at once. */
    async function load(ids, fallbackTheme) {
        const sub = {};
        for (const id of ids) sub[id] = { deps: [] };
        graph.merge(sub);
        if (changing) await changing.catch(() => {});
        if (!pageDefault && fallbackTheme) pageDefault = fallbackTheme;
        const theme = themeFor(fallbackTheme);
        await procedure.load(ids, theme);
        if (!worn) worn = theme;
    }

    /**
     * Every loaded sheet under another theme, then the current one retired.
     * Resolves when the page wears `to`; rejects, with the page whole under the
     * old theme, if any sheet fails to arrive.
     */
    function switchTheme(to) {
        if (!to || to === worn) return Promise.resolve(worn);
        if (changing) return changing.then(() => switchTheme(to));
        const from = worn;
        const ids = procedure.loadedUnder(from);
        changing = procedure.load(ids, to).then(() => {
            procedure.retire(from);
            worn = to;
            for (const fn of Array.from(listeners)) {
                try { fn({ from, to }); } catch (e) { console.error("[themes] listener failed", e); }
            }
            return to;
        }).finally(() => { changing = null; });
        return changing;
    }

    /** Call fn({ id, theme, state }) as each sheet moves. Returns the function that stops listening. */
    function onProgress(fn) {
        if (typeof fn !== "function") throw new TypeError("themes.onProgress: fn must be a function");
        watchers.add(fn);
        return () => { watchers.delete(fn); };
    }

    /** Call fn({ from, to }) after a switch has applied. Returns the function that stops listening. */
    function onThemeApplied(fn) {
        if (typeof fn !== "function") throw new TypeError("themes.onThemeApplied: fn must be a function");
        listeners.add(fn);
        return () => { listeners.delete(fn); };
    }

    // Follow the store: a pick here or in another tab. The address's override
    // still wins inside resolve(), so a tab pinned by ?theme= stays put. A pick
    // FORGOTTEN resolves to the page's default, not to what is worn.
    PreferenceViewInstance.onChange(() => {
        const next = PreferenceViewInstance.resolve("theme", pageDefault || worn);
        if (worn && next && next !== worn) switchTheme(next).catch(e => console.error("[themes] switch failed", e));
    });

    /** The waves a switch would run — nothing appended. */
    function plan() { return graph.plan(procedure.loadedUnder(worn)); }

    /** Frozen, plain data: the theme worn, the graph, every sheet. */
    function snapshot() {
        return Object.freeze({ theme: worn, graph: graph.snapshot(), sheets: procedure.snapshot() });
    }

    return Object.freeze({ load, switchTheme, theme() { return worn; }, onThemeApplied, onProgress, plan, snapshot });
})();
