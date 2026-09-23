// =============================================================================
// PaneMerge — whether one dock can take another's tabs, answered before a
// single tab moves. A merge is many moves and there is no half of one: a
// budget spent or a name already taken part way through would leave the tabs
// scattered over two docks, one of which is about to go. So the whole of it
// is decided first, from the two lists of names and the receiving pane's
// budget, and the page either does all of it or refuses and says why.
//
//   PaneMerge.plan(from, to, budget)
//     from    the going pane's tab ids, in the order they sit   pane.tabs()
//     to      the receiving pane's                              pane.tabs()
//     budget  what the receiving pane may hold                  pane.budget()
//     → { ok: true, ids: [ … ] }              the order they will arrive in
//     | { ok: false, reason, says }           "budget" | "twice", and the words
//                                             for a log line or a menu's hint
//
// Headless, and pure: it imports nothing and touches nothing — the page does
// the moving, one detachTab/attachTab a name, in the order given here.
// =============================================================================

class PaneMerge {

    /** The whole merge, decided: the order the tabs arrive in, or the reason there is no merge. */
    static plan(from, to, budget) {
        var going = from || [], holding = to || [], taken = {}, i, id;
        for (i = 0; i < holding.length; i++) taken[holding[i]] = true;
        for (i = 0; i < going.length; i++) {
            id = going[i];
            if (taken[id]) return { ok: false, reason: "twice", says: "both hold a tab called '" + id + "'" };
            taken[id] = true;
        }
        var room = (budget == null ? going.length + holding.length : budget) - holding.length;
        if (going.length > room) {
            return { ok: false, reason: "budget", says: "it holds " + holding.length + " of " + budget
                                                     + ", so there is room for " + room + ", not " + going.length };
        }
        return { ok: true, ids: going.slice() };
    }
}
