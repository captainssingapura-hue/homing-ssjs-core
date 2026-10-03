// =============================================================================
// KeyboardHome — the page's home: the root's default allocation, and its
// anchor (RFC 0066 E3, keyboard). Statics on a steward, as KeyboardMark's and
// KeyboardWalk's are; the steward keeps the one field, _home.
//
//   A yield no ancestor would hold, or a leaver's no one would - the page
//   reached - goes to the home the page named, else to no one. What the home
//   gives up itself comes straight back to it: released to the page and
//   granted anew, by "home", so it puts the focus where its keys are again - an
//   Escape in it, by mistake or not, leaves the keys where they were. Named
//   while no one holds, the home holds at once: entering the page is the
//   user's own act.
//
//   A native control letting go of the keys - an Escape it had no use for - is
//   a yield from the control: the member holding it is asked first,
//   wouldHold(control), and keeps them, held with nothing focused, when it says
//   so - the home given them anew, as the anchor; otherwise they go on up the
//   tree as a member's yield does.
//
//   KeyboardHome.name(s, id)                  the home named - taken off with null - holding at once while no one does
//   KeyboardHome.reached(s, from, by) → true  the page reached by what from gave up: handed to the home, or back to it
//   KeyboardHome.letGo(s, control)            a native control's let-go: a yield from it
// =============================================================================

class KeyboardHome {

    static name(s, id) {
        if (id != null && !s.has(id)) throw new Error("[KeyboardSteward] no member '" + id + "' to be the home");
        s._home = id;
        if (id != null && s.holder() === null) s._handOn({ id: id }, null, "home");
    }

    static reached(s, from, by) {
        var home = s._home;
        if (home === null || !s.has(home)) return false;
        if (home === from) s.release(from);   // the home's own, back to it: released to the page, then granted anew
        s._handOn({ id: home }, from, home === from ? "home" : by);
        return true;
    }

    static letGo(s, control) {
        var id = s.holder(), m = id && s._focus ? s._focus.find(id) : null;
        if (!m || m.kind === "proxy") return;   // a member outside the tree holds, as a member always did
        var c = s._catcher(m, control);
        if (c !== m) s._handOn(c, id, "yield");
        else if (id === s._home) KeyboardHome.reached(s, id, "home");   // the anchor: not left holding with nothing focused
    }
}
