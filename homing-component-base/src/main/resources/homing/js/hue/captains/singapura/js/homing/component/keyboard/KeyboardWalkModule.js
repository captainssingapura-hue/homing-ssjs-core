// =============================================================================
// KeyboardWalk — the walk over the focus party tree, the steward's own keys.
// While nothing is natively focused, Tab and Shift+Tab move a CANDIDATE — the
// cursor the steward keeps beside the holder — over the tree in pre-order,
// from the candidate, else from whatever the steward starts from; the holder
// does not change. Enter confirms: the candidate claims, and the walk is over.
// Escape calls it off. A walk that comes home to the holder ends there, since
// the candidate is never the holder.
//
// A container is asked wouldOffer(m) about a member of its own branch, and the
// walk steps over the ones it refuses: a pane shows one tab, and the keys are
// not offered to what the page is not showing.
//
// A member may leave the walk altogether: its component answers inWalk()
// false, and neither it nor anything under it is offered — a container that
// moves between its members a way of its own, as a desk between its panes.
// With nothing the walk may offer, and no walk to end, Tab is the browser's.
//
// A pure module of statics on a steward, importing nothing, so the rules are
// read and tested without a browser:
//
//   KeyboardWalk.keyDown(steward, ev)   true when the key was the walk's
//   KeyboardWalk.move(steward, dir)     one step on (1) or back (-1): the
//       member offered, or null when the walk ended
//   KeyboardWalk.step(steward, dir)     where that step lands, offered or home
//   KeyboardWalk.offerable(m)           what the holder of its branch says, unless it or a
//                                       member above it has left the walk
// =============================================================================

class KeyboardWalk {

    /** The walk's own keys: Tab and Shift+Tab move the offer, Enter takes it up, Escape calls it off. True when the key was the walk's. */
    static keyDown(s, ev) {
        if (ev.ctrlKey || ev.altKey || ev.metaKey) return false;
        if (ev.key === "Tab") {
            var dir = ev.shiftKey ? -1 : 1, to = s._focus ? KeyboardWalk.step(s, dir) : null;
            if (!to || (to.id === s.holder() && !s.candidate())) return false;   // nothing it may offer, no walk to end: Tab is the browser's
            KeyboardWalk.move(s, dir);                                           // a walk that comes home ends, and the key is still the walk's
            return true;
        }
        if (!s.candidate()) return false;
        if (ev.key === "Enter") return s.confirm();
        if (ev.key === "Escape") return s.withdraw();
        return false;
    }

    /** One step on (dir 1) or back (-1): the next member offered, or the walk ended when it comes home to the holder. The member offered, or null. */
    static move(s, dir) {
        var to = KeyboardWalk.step(s, dir);
        if (!to) return null;
        if (to.id === s.holder()) { s.withdraw(); return null; }   // home again: the walk is over, and the keys never moved
        s.offer(to);
        return to;
    }

    /** The member `dir` steps from the steward's `from()` in pre-order, wrapping, over those the walk is not offered; null with no tree or nothing to offer. */
    static step(s, dir) {
        var walk = s._focus ? s._focus.walk() : [];
        if (!walk.length) return null;
        var from = s.from(), n = walk.length, i = -1;
        for (var k = 0; k < n; k++) if (walk[k].id === from) { i = k; break; }
        if (i < 0) i = dir > 0 ? -1 : 0;                           // nothing to start from: the first (or the last) is one step away
        for (var j = 1; j <= n; j++) {
            var to = walk[(((i + dir * j) % n) + n) % n];
            if (to.id === s.holder() || KeyboardWalk.offerable(to)) return to;   // home again ends a walk, whatever the holder says
        }
        return null;
    }

    /**
     * Asked of the holder of the branch a member is in: a container may say the keys are not offered to a member
     * it is not showing. Offered unless it says otherwise — and never while the member, or a member above it, has
     * left the walk (inWalk() false).
     */
    static offerable(m) {
        for (var at = m; at; at = at.parent()) {
            var own = at.component;
            if (own && typeof own.inWalk === "function" && own.inWalk() === false) return false;
        }
        var owner = m.in ? m.in.owner : null, c = owner ? owner.component : null;
        return !c || typeof c.wouldOffer !== "function" || c.wouldOffer(m) !== false;
    }
}
