// =============================================================================
// ServingContextModule — the query this module was served with.
//
//   servingContext()      → { theme: string|null, locale: string|null }
//   withServingContext(u) → u, with &theme= and &locale= appended if absent
//
// A module's imports inherit the query its OWN request carried: the chrome is
// served as /module?class=…&theme=T&locale=L, and every import it emits carries
// the same, so the browser keys every module under it by that URL. A request
// that starts BARE starts a second chain — a second instance of every module
// the first chain already loaded. Harmless for a stateless module; for one
// that holds a singleton it is a split brain. RFC 0063 found it by building a
// monitor for the party tree and having it draw an empty second root.
//
// import.meta.url is the one honest source: it is the URL THIS module was
// loaded from, and since this module is imported by the chrome's chain it
// carries the chrome's query. It lives alone here because import.meta does not
// parse in a classic script, which is how the test harness loads raw modules;
// anything that needs the context imports this and stubs it under test.
// =============================================================================

function servingContext() {
    try {
        const p = new URL(import.meta.url).searchParams;
        return { theme: p.get('theme'), locale: p.get('locale') };
    } catch (e) {
        return { theme: null, locale: null };
    }
}

function withServingContext(url) {
    const ctx = servingContext();
    let out = String(url);
    if (ctx.theme  && !/[?&]theme=/.test(out))  out += '&theme='  + encodeURIComponent(ctx.theme);
    if (ctx.locale && !/[?&]locale=/.test(out)) out += '&locale=' + encodeURIComponent(ctx.locale);
    return out;
}
