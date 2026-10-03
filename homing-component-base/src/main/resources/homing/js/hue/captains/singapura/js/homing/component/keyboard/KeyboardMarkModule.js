// =============================================================================
// KeyboardMark — where the focus is: the steward's marker, one per page
// (RFC 0066 E3, keyboard §17.5). A sum, and the holder is always its member:
//
//   held   Logical(m)    nothing is natively focused; a key goes to m's keyDown
//   lent   Native(m, e)  the browser's focus is on e, a native control inside m,
//                        m the first member above it; e has the keys
//   away                 the browser's focus is outside every member: the marker
//                        stays as it was, and the steward routes nothing
//
// The steward reads the browser's focus after every focus event and before
// every key, and holds the rules:
//
//   arrives inside a member      that member is marked, lent — granted by "native"
//                                if it did not hold
//   arrives where nobody asked   a member's own root, or something focusable only
//                                because it scrolls: REFUSED — blurred, and the
//                                member is marked held
//   arrives under no member      away; the marker kept
//   leaves for nothing           held, on the same member: from away, the resume
//   an Escape nothing took       from the control lent the keys: it lets go — a YIELD
//                                from the control, one key. Its member is asked first,
//                                wouldHold(control): one with keys of its own once the
//                                control lets go holds them, with nothing focused; one
//                                with nothing designed for that is passed by, and the
//                                keys go on up the tree to the first ancestor that would
//                                hold them, else to the root's default (the home), else
//                                to no one; from the home's own control, the home anew -
//                                it is the anchor. A member outside the tree holds, as ever
//   a claim                      the browser's focus that is not in the claimer's
//                                own area is blurred — a claim is an intention to
//                                have the focus, and the steward makes it true
//   a release                    the browser's focus inside the one that let go, blurred
//
// THE MARKS ARE THE STEWARD'S ALONE: data-keys on every root it enrolled —
// "held" or "lent" on the holder's, "candidate" on the walk's candidate's,
// nothing on any other. The members above the marker are TOLD, on every move,
// within(on, at) — at being the marker — and each does what it does with it.
//
// Statics on a steward, as KeyboardWalk's are:
//
//   KeyboardMark.sync(s)           read the browser's focus, and hold the rules
//   KeyboardMark.claimed(s, id, by)   after a grant: the focus made true
//   KeyboardMark.released(s, id)      after a release: nothing left focused in it
//   KeyboardMark.paint(s)          the marks, and — when the marker moved — the notices
//   KeyboardMark.marker(s)         { id, state, root, el }, or null with no holder
//   KeyboardMark.check(s)          the invariants broken now, as sentences: [] when all hold
//   KeyboardMark.asked(el)         whether an element's focus is one somebody asked for
//   KeyboardMark.focused()         what has the browser's focus, or null for nothing
//   KeyboardMark.fromAFocusedElement(ev)   whether a key came from a focused element
//   KeyboardMark.focusOut(s, ev) .enrolled(s, root, id) .escape(s, kind, ev)   the steward's, for its listeners
// =============================================================================

var _ATTR = "data-keys";
// focusable by what they are: a link, an area and a player take the focus only when they are one — an address,
// controls — so one that has it asked for it
var _ASKED = /^(INPUT|SELECT|TEXTAREA|BUTTON|IFRAME|SUMMARY|EMBED|OBJECT|DIALOG|A|AREA|AUDIO|VIDEO)$/;

class KeyboardMark {

    /** What has the browser's focus, or null when nothing has: the document's active element unless it is the body. */
    static focused() {
        if (typeof document === "undefined") return null;
        var a = document.activeElement;
        return a && a !== document.body && a !== document.documentElement ? a : null;
    }

    /** Whether an element's focus is one somebody asked for: a tabindex, a control, a link, something editable — not a scroller the browser made focusable. */
    static asked(el) {
        if (!el || !el.tagName) return false;
        if (typeof el.hasAttribute === "function" && el.hasAttribute("tabindex")) return true;
        return el.isContentEditable === true || _ASKED.test(String(el.tagName).toUpperCase());
    }

    /** Whether a key came from a focused element — anything but the body, the root element or the document itself. */
    static fromAFocusedElement(ev) {
        var t = ev.target;
        if (!t || typeof document === "undefined") return false;
        return t !== document && t !== document.body && t !== document.documentElement;
    }

    /** The browser's focus gone: to another element, whose focusin says so; to nothing, read now and once more when the browser has settled. */
    static focusOut(s, ev) {
        if (ev && ev.relatedTarget) return;
        KeyboardMark.sync(s);
        if (typeof Promise !== "undefined") Promise.resolve().then(function () { KeyboardMark.sync(s); });
    }

    /** A root enrolled for a member: it wears the member's mark at once. */
    static enrolled(s, root, id) {
        s._rootsOf.set(id, KeyboardMark.rootsOf(s, id).concat([root]));
        KeyboardMark.paint(s);
    }

    /**
     * An Escape from the control lent the keys, which nothing took: the control lets go, and that is a yield from it -
     * where the keys go is the steward's (letGo): its member, if it would hold them, else up the tree. True when it
     * did. Nothing took it: an Escape something on the way up kept - a stage, a dialog, closing - is that thing's.
     */
    static escape(s, kind, ev) {
        if (kind !== "KeyDown" || ev.key !== "Escape" || ev.defaultPrevented || !s._lent || s.memberAt(ev.target) !== s._holder) return false;
        var control = s._lent;
        if (typeof control.blur === "function") control.blur();
        KeyboardMark.sync(s);   // read, not waited for: a page without the window's focus moves the focus and fires nothing
        ev.preventDefault();
        ev.stopPropagation();
        s._letGo(control);
        return true;
    }

    /** The browser's focus read, and the rules held. Nothing when it has not moved since the last read — a window coming back is not a move. */
    static sync(s) {
        var a = KeyboardMark.focused();
        if (a === s._at) return;
        s._at = a;
        var id = a ? s.memberAt(a) : null;
        if (!a || !id || !s._members[id]) {                  // nothing focused: held — or, focused under no member, away
            s._lent = null;
            s._away = !!a;
            return KeyboardMark.paint(s);
        }
        s._away = false;
        if (s._roots.get(a) === id || !KeyboardMark.asked(a)) {   // nobody asked for this focus: refused, and the member is focused logically
            s._lent = null;
            s._at = null;
            if (typeof a.blur === "function") a.blur();
        } else s._lent = a;
        if (s._holder !== id) s._handOn({ id: id }, null, "native");
        else KeyboardMark.paint(s);
    }

    /** After a grant: a claim is an intention to have the focus, so the browser's focus that is not in the claimer's own area goes. */
    static claimed(s, id, by) {
        var a = KeyboardMark.focused();
        if (by !== "native" && a && s.memberAt(a) !== id) { s._at = null; if (typeof a.blur === "function") a.blur(); a = null; }
        if (by !== "native") { s._at = a; s._lent = a; s._away = false; }
        KeyboardMark.paint(s);
    }

    /** After a release: the browser's focus is only ever in the holder's own area, and there is no holder. */
    static released(s, id) {
        var a = KeyboardMark.focused();
        if (a && s.memberAt(a) === id) { s._at = null; if (typeof a.blur === "function") a.blur(); }
        s._lent = null;
        KeyboardMark.paint(s);
    }

    /** The marker: its member, held, lent or away, the member's first root, and the element lent to; null with no holder. */
    static marker(s) {
        var id = s._holder;
        if (id === null) return null;
        var roots = KeyboardMark.rootsOf(s, id);
        return Object.freeze({ id: id, state: s._away ? "away" : s._lent ? "lent" : "held", root: roots.length ? roots[0] : null, el: s._lent });
    }

    /** The roots enrolled for a member. */
    static rootsOf(s, id) { return id == null ? [] : (s._rootsOf.get(id) || []); }

    /** What each enrolled root should wear now: the holder's held or lent, the candidate's candidate. */
    static _wanted(s) {
        var want = new Map(), at = KeyboardMark.marker(s);
        KeyboardMark.rootsOf(s, at ? at.id : null).forEach(function (r) { want.set(r, at.state === "lent" ? "lent" : "held"); });
        KeyboardMark.rootsOf(s, s._candidate).forEach(function (r) { if (!want.has(r)) want.set(r, "candidate"); });
        return want;
    }

    /** The marks written — by the steward alone — and, when the marker moved, everyone above it told. */
    static paint(s) {
        var want = KeyboardMark._wanted(s);
        s._painted.forEach(function (r) { if (!want.has(r)) r.removeAttribute(_ATTR); });
        want.forEach(function (v, r) { if (r.getAttribute(_ATTR) !== v) r.setAttribute(_ATTR, v); });
        s._painted = new Set(want.keys());
        KeyboardMark.tell(s, false);
    }

    /** A root forgotten: its mark goes with it. */
    static forget(s, root, id) {
        var list = s._rootsOf.get(id), i = list ? list.indexOf(root) : -1;
        if (i >= 0) list.splice(i, 1);
        if (list && !list.length) s._rootsOf.delete(id);
        if (s._painted.has(root)) { s._painted.delete(root); root.removeAttribute(_ATTR); }
    }

    /**
     * Everyone above the marker, up to the root: within(true, at) on every move of it — a new holder, held and lent
     * and away — nearest first; within(false, at) to the ones it left. `again` tells them even though the marker did
     * not move: the tree did. Marked on the sink when the marker itself moved.
     */
    static tell(s, again) {
        var at = KeyboardMark.marker(s), was = s._toldAt;
        var moved = !(was === at || (was && at && was.id === at.id && was.state === at.state && was.el === at.el));
        if (!moved && !again) return;
        s._toldAt = at;
        var now = {}, order = [], m = at && s._focus ? s._focus.find(at.id) : null, id;
        for (var p = m ? m.parent() : null; p; p = p.parent()) { now[p.id] = true; order.push(p.id); }
        var before = s._inside || {};
        s._inside = now;
        for (id in before) if (!now[id]) KeyboardMark._within(s, id, false, at);
        for (var i = 0; i < order.length; i++) KeyboardMark._within(s, order[i], true, at);
        if (moved && at) s._fire(KeyboardEvents.Marked(at.id, at.state));
    }
    static _within(s, id, on, at) {
        var h = s._members[id];
        if (h && typeof h.within === "function") { try { h.within(on, at); } catch (e) { console.error("[KeyboardSteward] within threw:", e); } }
    }

    /** The invariants broken now, one sentence each; none when all hold. */
    static check(s) {
        var broken = [], a = KeyboardMark.focused(), id = a ? s.memberAt(a) : null;
        if (a && id && s._members[id]) {
            if (s._roots.get(a) === id) broken.push("the browser's focus is on the root of " + id);
            if (id !== s._holder) broken.push("the browser's focus is in " + id + ", and " + s._holder + " holds");
            if (s._lent !== a) broken.push("the marker is not lent to the element that has the browser's focus");
        } else if (a && !s._away) broken.push("the browser's focus is under no member, and the steward is not away");
        if (!a && s._lent) broken.push("the marker is lent, and nothing has the browser's focus");
        var want = KeyboardMark._wanted(s);
        s._rootsOf.forEach(function (roots, rid) {
            roots.forEach(function (r) {
                var has = r.getAttribute(_ATTR), should = want.has(r) ? want.get(r) : null;
                if (has !== should) broken.push("the root of " + rid + " says " + has + ", not " + should);
            });
        });
        return broken;
    }
}
