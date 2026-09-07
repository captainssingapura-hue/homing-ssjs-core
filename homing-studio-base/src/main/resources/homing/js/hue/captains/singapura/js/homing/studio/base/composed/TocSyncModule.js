// =============================================================================
// TocSyncModule — the Actors around TocSyncSecretary, for a FLAT anchor TOC.
//
//   attachTocSync({ navEl, links, headings }) -> { destroy }
//
// The rigid-tree reader has had two-way TOC↔body sync since RFC 0043: arrow
// through the TOC and the body follows; scroll the body and the TOC follows.
// The markdown reader's TOC could only be clicked. This is the SAME law wired to
// the other TOC shape — the coordinator is TocSyncSecretary, shared and
// unchanged, and what lives here is only the three Actors it coordinates: the
// keyboard, the scroll-spy, and the scroll itself.
//
// KEY IS THE SLUG. Each link carries data-slug and each heading carries the same
// string as its id; that is the entire binding, so no map is passed in.
//
// ONE TAB STOP. The links are anchors, so left alone a forty-heading TOC is
// forty tab stops between the reader and the document. A roving tabindex makes
// it one — the cursor link is tabbable, the rest are not, and the arrows move
// the cursor. The listbox pattern, for the listbox reason.
//
// THE CURSOR FOLLOWS THE SPY. Every SyncTo moves the cursor, not only the ones
// the keyboard caused. Scroll to section five, press ArrowDown, and you go to
// six — because a cursor that stayed at one while the highlight was at five
// would be a second opinion about where you are, which is the thing this whole
// mechanism exists to prevent.
//
// The inline onclick each anchor already carries is left alone: it survives
// static export, where an added listener would not. So a click scrolls twice, to
// the same place, invisibly — and the click handler here is what ARMS the guard,
// which stops the spy flickering through every section on the way.
// =============================================================================

function attachTocSync(opts) {
    var navEl    = opts && opts.navEl;
    var links    = (opts && opts.links) || [];
    var headings = (opts && opts.headings) || [];
    if (!navEl || links.length === 0) return { destroy: function () {} };

    var state  = TocSyncSecretary.initial;
    var cursor = 0;
    var bySlug = {}, headBySlug = {}, i;
    for (i = 0; i < links.length; i++)    bySlug[links[i].getAttribute("data-slug")] = links[i];
    for (i = 0; i < headings.length; i++) headBySlug[headings[i].id] = headings[i];

    function paintCursor() {
        for (var j = 0; j < links.length; j++) {
            links[j].setAttribute("tabindex", j === cursor ? "0" : "-1");
        }
    }

    // The host half of the law: highlight, move the cursor, and scroll ONLY when
    // the Secretary says navigation drove this — never on the spy's report.
    function applyAction(a) {
        if (a.kind !== "SyncTo") return;
        for (var j = 0; j < links.length; j++) {
            css.removeClass(links[j], st_toc_active);
            if (links[j] === bySlug[a.key]) cursor = j;
        }
        var link = bySlug[a.key];
        if (link) css.addClass(link, st_toc_active);
        paintCursor();
        if (a.scroll) {
            var h = headBySlug[a.key];
            if (h && h.scrollIntoView) h.scrollIntoView({ behavior: "smooth", block: "start" });
            armGuard();
        }
    }

    function dispatch(msg) {
        var step = TocSyncSecretary.behavior(state, msg);
        state = step.newState;
        for (var j = 0; j < step.actions.length; j++) applyAction(step.actions[j]);
    }

    // The guard's Actor. scrollend is the real signal; the timer is the fallback
    // for when a scroll moves nothing (the target was already in view) and
    // scrollend therefore never fires — without it the guard would stay up and
    // the spy would be mute for the rest of the session.
    var guardTimer = null;
    function armGuard() {
        if (guardTimer) clearTimeout(guardTimer);
        guardTimer = setTimeout(function () {
            guardTimer = null;
            dispatch({ kind: "ScrollSettled" });
        }, 400);
    }
    function onScrollEnd() { dispatch({ kind: "ScrollSettled" }); }

    function navTo(idx) {
        if (idx < 0 || idx >= links.length) return;
        cursor = idx;
        paintCursor();
        try { links[idx].focus({ preventScroll: true }); } catch (e) { links[idx].focus(); }
        dispatch({ kind: "NavRequested", key: links[idx].getAttribute("data-slug"), path: null });
    }

    function onKey(ev) {
        // A modifier makes the chord somebody else's — Alt+Left is Back. Nothing
        // here reads one, so decline before anything else is considered.
        if (ev.altKey || ev.ctrlKey || ev.metaKey) return;
        var n = links.length;
        if      (ev.key === "ArrowDown") navTo(Math.min(cursor + 1, n - 1));
        else if (ev.key === "ArrowUp")   navTo(Math.max(cursor - 1, 0));
        else if (ev.key === "Home")      navTo(0);
        else if (ev.key === "End")       navTo(n - 1);
        else if (ev.key === "Enter")     navTo(cursor);
        else return;
        // Enter would otherwise follow the anchor's href and put a fragment in
        // the URL — a history entry per heading. The arrows would scroll the page
        // under the TOC we are walking. Both are ours now.
        ev.preventDefault();
    }

    // Walks UP from the event's own target — never a lookup, so Owned References
    // holds: the only elements touched are the ones handed in.
    function onClick(ev) {
        var el = ev.target;
        while (el && el !== navEl && !el.getAttribute("data-slug")) el = el.parentElement;
        if (!el || el === navEl) return;
        dispatch({ kind: "NavRequested", key: el.getAttribute("data-slug"), path: null });
    }

    var observer = null;
    if (typeof IntersectionObserver === "function" && headings.length) {
        // Same rootMargin the reader used before the Secretary arrived, so the
        // heading it calls "current" is the one it always called current.
        observer = new IntersectionObserver(function (entries) {
            for (var j = 0; j < entries.length; j++) {
                if (entries[j].isIntersecting) {
                    dispatch({ kind: "ScrolledTo", key: entries[j].target.id, path: null });
                }
            }
        }, { rootMargin: "0px 0px -70% 0px", threshold: 0 });
        for (i = 0; i < headings.length; i++) observer.observe(headings[i]);
    }

    navEl.addEventListener("keydown", onKey);
    navEl.addEventListener("click", onClick);
    window.addEventListener("scrollend", onScrollEnd);
    paintCursor();

    return {
        destroy: function () {
            navEl.removeEventListener("keydown", onKey);
            navEl.removeEventListener("click", onClick);
            window.removeEventListener("scrollend", onScrollEnd);
            if (guardTimer) clearTimeout(guardTimer);
            if (observer) observer.disconnect();
        }
    };
}
