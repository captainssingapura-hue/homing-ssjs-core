// =============================================================================
// ThemeSheets — the theme's owner (RFC 0064, D25): the derived sheets, and the
// switch. A provided group is the same in every theme, and the CSS manager
// loads it once. What varies is DERIVED, and is loaded here and swapped on a
// switch. A derived sheet varies with one thing (RFC 0067, appendix Two
// Classes per Leaf), and each is owned on its own:
//
//   theme     a sheet made from the walk, for every theme   ?theme=
//   style     a style's sheet, the same in every palette    ?style=
//   palette   a palette's sheet, the same under every style ?palette=
//
// Switching the style fetches the style's sheets again and touches no
// palette's, and so the other way round: each is swapped on its own. Every
// served module that needs a derived sheet hands it here, saying what it
// varies with: the derived sheet's own module.
//
//   load(ids, fallback, varies)  the sheets under the one worn — the steward's
//                                answer, else the fallback — applied at once
//   switchTheme(to)              every theme sheet loaded, fetched again under
//   switchStyle(to)              `to` under media="not all", flipped in one
//   switchPalette(to)            pass, then the old one's leave: A -> B -> A
//                                works, since leaving forgets
//   theme() style() palette()    the one worn, once anything has loaded
//   onThemeApplied(fn)           fn({ varies, from, to }) after a switch applied
//   onProgress(fn)               fn({ varies, id, slug, state }) as a sheet moves
//   plan()                       each kind's waves a switch would run
//   snapshot()                   what is worn, every sheet, as data
//
// It follows the store: a pick here or in another tab switches it, the
// address still winning inside the steward's resolve — each kind by its own
// preference, "theme", "style", "palette".
// =============================================================================

const ThemeSheetsInstance = (() => {
    const VARIES = Object.freeze(["theme", "style", "palette"]);

    const watchers = new Set();
    const listeners = new Set();

    /** The owner of one kind of derived sheet: its graph, its loads, what it wears, its switch. */
    function owner(varies) {
        const graph = createCssDependencyGraph();

        function hrefFor(id, slug) {
            return "/css-content?class=" + encodeURIComponent(id) + (slug ? "&" + varies + "=" + encodeURIComponent(slug) : "");
        }

        function tell(id, slug, state) {
            for (const fn of Array.from(watchers)) {
                try { fn({ varies, id, slug, state }); } catch (e) { console.error("[themes] watcher failed", e); }
            }
        }

        const procedure = createCssLoadProcedure(graph, (href) => CssLink.append(href), hrefFor, tell);

        let worn = null;          // the one the page wears, once anything has loaded
        let pageDefault = null;   // the fallback, as the first module carried it
        let changing = null;      // the switch in flight, if one is

        /** The one to load under: the one worn, else the steward's answer, else the fallback. */
        function slugFor(fallback) {
            return worn || PreferenceViewInstance.resolve(varies, fallback || null);
        }

        async function load(ids, fallback) {
            const sub = {};
            for (const id of ids) sub[id] = { deps: [] };
            graph.merge(sub);
            if (changing) await changing.catch(() => {});
            if (!pageDefault && fallback) pageDefault = fallback;
            const slug = slugFor(fallback);
            await procedure.load(ids, slug);
            if (!worn) worn = slug;
        }

        /**
         * Every loaded sheet of this kind under another, then the current one retired.
         * Resolves when the page wears `to`; rejects, with the page whole under the old
         * one, if any sheet fails to arrive. No other kind's sheet is touched.
         */
        function switchTo(to) {
            if (!to || to === worn) return Promise.resolve(worn);
            if (changing) return changing.then(() => switchTo(to));
            const from = worn;
            const ids = procedure.loadedUnder(from);
            changing = procedure.load(ids, to).then(() => {
                procedure.retire(from);
                worn = to;
                for (const fn of Array.from(listeners)) {
                    try { fn({ varies, from, to }); } catch (e) { console.error("[themes] listener failed", e); }
                }
                return to;
            }).finally(() => { changing = null; });
            return changing;
        }

        // A pick FORGOTTEN resolves to the page's default, not to what is worn.
        function follow() {
            const next = PreferenceViewInstance.resolve(varies, pageDefault || worn);
            if (worn && next && next !== worn) switchTo(next).catch(e => console.error("[themes] switch failed", e));
        }

        return Object.freeze({
            load, switchTo, follow,
            worn() { return worn; },
            plan() { return graph.plan(procedure.loadedUnder(worn)); },
            snapshot() { return Object.freeze({ worn, graph: graph.snapshot(), sheets: procedure.snapshot() }); }
        });
    }

    const owners = Object.freeze({ theme: owner("theme"), style: owner("style"), palette: owner("palette") });

    function of(varies) {
        const o = owners[varies];
        if (!o) throw new TypeError("themes: a derived sheet varies with one of " + VARIES.join(", ") + ", not " + varies);
        return o;
    }

    /** These derived sheets, of what they vary with, under the one worn, applied at once. */
    function load(ids, fallback, varies) { return of(varies).load(ids, fallback); }

    /** Call fn({ varies, id, slug, state }) as each sheet moves. Returns the function that stops listening. */
    function onProgress(fn) {
        if (typeof fn !== "function") throw new TypeError("themes.onProgress: fn must be a function");
        watchers.add(fn);
        return () => { watchers.delete(fn); };
    }

    /** Call fn({ varies, from, to }) after a switch has applied. Returns the function that stops listening. */
    function onThemeApplied(fn) {
        if (typeof fn !== "function") throw new TypeError("themes.onThemeApplied: fn must be a function");
        listeners.add(fn);
        return () => { listeners.delete(fn); };
    }

    // Follow the store: a pick here or in another tab, each kind by its own
    // preference. The address's override still wins inside resolve(), so a tab
    // pinned by ?style= stays put.
    PreferenceViewInstance.onChange(() => { for (const v of VARIES) owners[v].follow(); });

    /** The waves a switch of each kind would run — nothing appended. */
    function plan() { return Object.freeze({ theme: owners.theme.plan(), style: owners.style.plan(), palette: owners.palette.plan() }); }

    /** Frozen, plain data: each kind's worn, graph and sheets. */
    function snapshot() {
        return Object.freeze({ theme: owners.theme.snapshot(), style: owners.style.snapshot(), palette: owners.palette.snapshot() });
    }

    return Object.freeze({
        load,
        switchTheme(to) { return owners.theme.switchTo(to); },
        switchStyle(to) { return owners.style.switchTo(to); },
        switchPalette(to) { return owners.palette.switchTo(to); },
        theme() { return owners.theme.worn(); },
        style() { return owners.style.worn(); },
        palette() { return owners.palette.worn(); },
        onThemeApplied, onProgress, plan, snapshot
    });
})();
