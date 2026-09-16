// =============================================================================
// CssLoadProcedure — the CSS manager's affiliate: how sheets arrive (RFC 0064).
//
// One procedure, two callers. Loading a group's tree for the first time and
// refreshing every loaded group under a new theme are the same steps:
//
//   1. PLAN     the graph answers waves — the theme globals, the priors, then
//               Kahn's waves (CssDependencyGraph.plan);
//   2. ENSURE   wave by wave, every node's sheet for the theme is appended
//               under media="not all" — fetched, `load` fires, NOT applied —
//               and awaited; the next wave starts only when the whole wave
//               has landed. A node already in flight or landed is awaited,
//               not appended twice, so concurrent callers share one link and
//               a dependent always sits AFTER its dependency in the document;
//   3. FLIP     once everything has landed, every link this call is
//               responsible for goes media="all" in one synchronous pass —
//               one style recalc, one paint, no half-styled frame, for a
//               first load and a theme switch alike;
//   4. RETIRE   (switch only) the outgoing theme's links leave the document
//               and their entries the registry, so a theme can be left and
//               re-entered — A -> B -> A loads A's sheets again, after B's.
//
// A failed fetch aborts: every link this call appended and not yet applied is
// removed, its entries forgotten, and the error rethrown. Nothing was ever
// applied, so the page is whole under whatever it wore before.
//
// The DOM is reached only through the link factory the manager injects —
// appendLink(href) → { link, loaded } — so the one raw createElement in the
// system stays where the baseline has it, and this module tests without a
// document.
// =============================================================================

function createCssLoadProcedure(graph, appendLink, hrefFor, notify) {

    // "<node>:<theme>" → { link, loaded: Promise<entry>, landed, applied }
    const entries = new Map();
    // Progress, for whoever watches: appended → landed → applied, or failed;
    // retired when a theme leaves. The manager fans this out (RFC 0064: the
    // workbench shows a switch as it happens, not after).
    const tell = typeof notify === "function" ? notify : function () {};

    function key(id, theme) { return id + ":" + (theme || "__default"); }

    /** The entry for a node under a theme, appended now if it does not exist. */
    function ensure(id, theme) {
        const k = key(id, theme);
        let e = entries.get(k);
        if (e) return e;
        const made = appendLink(hrefFor(id, theme));
        made.link.media = "not all";
        e = { link: made.link, landed: false, applied: false, loaded: null };
        e.loaded = made.loaded.then(function () { e.landed = true; tell(id, theme, "landed"); return e; });
        entries.set(k, e);
        tell(id, theme, "appended");
        return e;
    }

    function forget(e, theme) {
        for (const [k, have] of entries) {
            if (have === e) { entries.delete(k); tell(k.slice(0, k.lastIndexOf(":")), theme, "failed"); break; }
        }
        try { e.link.remove(); } catch (_) { /* never attached, or already gone */ }
    }

    /**
     * Load these node ids under the theme, dependencies first, and apply them
     * all at once. Resolves with the entries applied by THIS call.
     */
    async function load(ids, theme) {
        const waves = graph.plan(ids);
        const mine = [];
        try {
            for (const wave of waves) {
                const batch = wave.map(function (id) {
                    const e = ensure(id, theme);
                    if (!e.applied) mine.push(e);
                    return e.loaded;
                });
                await Promise.all(batch);
            }
        } catch (err) {
            for (const e of mine) if (!e.applied) forget(e, theme);
            throw err;
        }
        for (const e of mine) {          // one synchronous pass: one recalc
            if (!e.applied) { e.link.media = "all"; e.applied = true; }
        }
        for (const [k, e] of entries) if (mine.indexOf(e) >= 0) tell(k.slice(0, k.lastIndexOf(":")), theme, "applied");
        return mine;
    }

    /** Remove every applied sheet of a theme and forget it. */
    function retire(theme) {
        const suffix = ":" + (theme || "__default");
        for (const [k, e] of Array.from(entries)) {
            if (k.endsWith(suffix)) {
                entries.delete(k);
                try { e.link.remove(); } catch (_) {}
                tell(k.slice(0, k.length - suffix.length), theme, "retired");
            }
        }
    }

    /** Node ids with an applied sheet under the theme. */
    function loadedUnder(theme) {
        const suffix = ":" + (theme || "__default");
        const out = [];
        for (const [k, e] of entries) {
            if (k.endsWith(suffix) && e.applied) out.push(k.slice(0, k.length - suffix.length));
        }
        return out;
    }

    /** Frozen, plain data: every entry as { id, theme, applied }. */
    function snapshot() {
        const out = [];
        for (const [k, e] of entries) {
            const i = k.lastIndexOf(":");
            out.push(Object.freeze({ id: k.slice(0, i), theme: k.slice(i + 1), landed: e.landed, applied: e.applied }));
        }
        return Object.freeze(out);
    }

    return Object.freeze({ load, retire, loadedUnder, snapshot });
}
