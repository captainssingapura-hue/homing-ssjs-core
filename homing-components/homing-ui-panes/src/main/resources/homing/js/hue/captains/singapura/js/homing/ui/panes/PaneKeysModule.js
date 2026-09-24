// =============================================================================
// A PANE'S KEYS ARE SWAPPABLE, and this module holds the two that ship. A
// scheme is an object with two methods and no state:
//
//   scheme.keyDown(pane, ev) → true when taken   the pane HOLDS the keys
//   scheme.chord(pane, ev)   → true when taken   something below it is natively
//                                                focused and a chord got past
//   scheme.keeps(pane)       → whether the pane is a PLACE THE KEYS CAN REST
//                                                at all, or only ever a road to
//                                                the tab that is showing
//
// Two entry points because there are two ways a key can arrive, and a scheme
// that means to work in both answers both from one table. A pane is given a
// list and tries them in order; the first to take the key has it — and the
// FIRST one also says whether the pane keeps the keys, because that is not a
// per-key question but a question about what the pane IS. A list is therefore
// one scheme's character with another's keys added, not two characters at
// once.
//
// PaneKeys — the pane in the keyboard party, over the pane's own surface and
// nothing about how the pane is built: what a key means for a container of
// tabs (level 1 of the two levels: the container's own operations and no
// more), what the walk is offered, and the law a tab's widget must keep.
//
//   PaneKeys.keyDown(pane, ev) → true when taken
//     ← →              the active tab moves to the previous / next, at once; no wrap
//     Home / End       the first / last tab
//     Shift+← / →      the active tab moves one slot along the rail, staying active
//     Shift+↓          the active tab asked to detach: pane.requestDetach()
//     Enter            the active tab's widget activates itself — a claim of its own
//     Escape           TAKEN AND KEPT: the dock is where Escape stops. Coming
//                      out of a tab's widget lands on the bar, and pressing it
//                      again does nothing - one key cannot walk you out of the
//                      room by accident. Leaving a dock is its own gesture,
//                      the page's switcher, and never a key you were already
//                      pressing. pane.yieldKeys() remains, for a holder that
//                      wants to give them up by call
//     Shift+F10, ContextMenu   the active tab's menu, at its chip: pane.menuByKey()
//
//   PaneKeys.chord(pane, ev) → false, always. This scheme's keys are the
//     container's own and are meant for a hand that is on the bar; reaching
//     into a tab's widget to take an arrow off it is the very thing the
//     native world is promised it may keep.
//
//   BrowserKeys.keyDown / .chord — the other scheme, below: what a browser
//     does, in both places, because that is its whole character.
//   A chord with Ctrl, Alt or Meta is left; so is anything not above.
//
//   PaneKeys.keysState(pane) → which of the four the keys are in: "held" on
//     the bar, "lent" while they are within a tab, "candidate" while the walk
//     rests on the pane, or null
//   PaneKeys.wouldOffer(pane, m) → whether the walk is offered a member of
//     the dock's branch: only the tab on show, the rest being behind it
//   PaneKeys.law(widget) → whether a tab's widget is logically focusable:
//     a member of a branch, with activate(); level 2 rests on it
//
// Pure: it imports nothing and touches no DOM. A pure module must not import
// a DOM module - its imports resolve without the page's theme and would load
// a second copy of it, and of any one-per-document manager it imports. So
// the yield goes through the pane, which imports Keys as a DOM module does.
// =============================================================================

/**
 * PaneSchemes — one, several, or none, and the order they are asked in. A
 * pane holds a list and this is the whole of what holding a list means: check
 * on the way in that each answers both doors, and on the way through, take the
 * first answer. Here rather than in the pane because it is about SCHEMES, and
 * because a pane has enough to do.
 */
class PaneSchemes {
    /** A scheme, a list of them, or nothing for the container's own; always a list, always answering both doors. */
    static of(given) {
        var list = given == null ? [PaneKeys] : (Array.isArray(given) ? given.slice() : [given]);
        for (var i = 0; i < list.length; i++)
            if (!list[i] || typeof list[i].keyDown !== "function" || typeof list[i].chord !== "function" || typeof list[i].keeps !== "function")
                throw new Error("[PaneSchemes] a scheme answers keyDown(pane, ev), chord(pane, ev) and keeps(pane)");
        return list;
    }

    /** Whether the pane is somewhere the keys can rest: the FIRST scheme's answer, since it is the pane's character and not a key. */
    static keeps(list, pane) { return list.length > 0 && list[0].keeps(pane) === true; }
    static keyDown(list, pane, ev) { for (var i = 0; i < list.length; i++) if (list[i].keyDown(pane, ev) === true) return true; return false; }
    static chord(list, pane, ev) { for (var i = 0; i < list.length; i++) if (list[i].chord(pane, ev) === true) return true; return false; }
}

class PaneKeys {
    /** Not this scheme's: an arrow belongs to whatever is focused, and this one is all arrows. */
    static chord() { return false; }

    /** The bar is somewhere to be. Its arrows walk the tabs, and the keys rest here until Enter takes them into one. */
    static keeps() { return true; }

    static keyDown(pane, ev) {
        if (ev.key === "Escape") return true;   // the dock is the floor: Escape comes back to the bar and stops there, so nothing overshoots
        var ids = pane.tabs(), n = ids.length, active = pane.activeTab(), i = active === null ? -1 : ids.indexOf(active);
        if (ev.shiftKey) {
            if (ev.key === "ArrowLeft" || ev.key === "ArrowRight") { if (i >= 0) pane.moveTab(active, i + (ev.key === "ArrowLeft" ? -1 : 1)); return true; }
            if (ev.key === "ArrowDown") { if (i >= 0) pane.requestDetach(); return true; }
            if (ev.key === "F10") return pane.menuByKey();
            return false;
        }
        if (ev.ctrlKey || ev.altKey || ev.metaKey) return false;
        if (ev.key === "ArrowLeft" || ev.key === "ArrowRight") {
            if (n) pane.switchTab(ids[i < 0 ? 0 : Math.min(n - 1, Math.max(0, i + (ev.key === "ArrowLeft" ? -1 : 1)))]);
            return true;
        }
        if (ev.key === "Home" || ev.key === "End") { if (n) pane.switchTab(ids[ev.key === "Home" ? 0 : n - 1]); return true; }
        if (ev.key === "Enter") { if (i >= 0) pane.widgetOf(active).activate(); return true; }
        if (ev.key === "ContextMenu") return pane.menuByKey();
        return false;
    }

    /** Asked by the walk about a member of the dock's branch: only the tab on show is offered the keys — the others are behind it, and the pane's own arrows are the way to them. */
    static wouldOffer(pane, m) {
        var ids = pane.tabs(), active = pane.activeTab();
        for (var i = 0; i < ids.length; i++) if (pane.widgetOf(ids[i]) === m.component) return ids[i] === active;
        return true;   // not a tab's widget: not the pane's business
    }

    /**
     * Which of the four the keys are in, for the pane to say on its frame and
     * its active chip. The order is the truth of it: the pane HOLDS them — the
     * bar is where the work is, and the arrows walk the tabs; they are WITHIN
     * it, in the tab's own widget, which the chip says as lent and marks; the
     * walk has OFFERED them; or none of those, and nothing is said.
     */
    static keysState(pane) {
        return pane._holds ? "held" : pane._inside ? "lent" : pane._offered ? "candidate" : null;
    }

    /**
     * THE WHOLE LAW AT THE DOOR: what a tab must be for a pane to take it.
     * Here rather than in the pane because the law's other half — what makes a
     * widget one the keys can be given to — is already here, and a rule split
     * over two files is a rule read in one of them. Throws, with the name of
     * the thing that is wrong; a caller that wanted an answer rather than a
     * refusal asks the pane what it can hold before offering.
     */
    static admit(pane, tab) {
        if (!tab || typeof tab.id !== "string" || !tab.id) throw new Error("[MultiTabPane] tab.id must be a non-empty string");
        if (pane.has(tab.id)) throw new Error("[MultiTabPane] tab '" + tab.id + "' is already in slot '" + pane.slotId + "'");
        if (!tab.widget || typeof tab.widget !== "object" || !tab.widget.root) throw new Error("[MultiTabPane] tab '" + tab.id + "' has no widget with a root");
        if (!PaneKeys.law(tab.widget))   // a member of a dock's branch, with activate()
            throw new Error("[MultiTabPane] tab '" + tab.id + "': its widget is not logically focusable - it must join the dock's focus branch (widget.focus) and answer activate()");
        if (pane.count() >= pane.budget()) throw new Error("[MultiTabPane] the budget of " + pane.budget() + " is spent in slot '" + pane.slotId + "'");
    }

    /** The law: a tab's widget is a member of a focus branch of its own and answers activate(), so the keys can be given to it and it can take them. */
    static law(widget) {
        var f = widget ? widget.focus : null;
        return !!f && typeof f === "object" && typeof f.leave === "function" && !!f.in && typeof widget.activate === "function";
    }
}

/**
 * BrowserKeys — a pane that behaves like the thing everyone already has open.
 * Ctrl+Tab forward, Ctrl+Shift+Tab back, Ctrl+PageDown and Ctrl+PageUp the
 * same, and THE ROW WRAPS: past the last tab is the first, which is what a
 * browser does and what the container scheme deliberately does not.
 *
 * It answers in both places. On the bar, because the keys are there; and
 * through the chord, because a browser moves you off a page you are typing on
 * and a scheme that stopped at the edge of a text field would not be the thing
 * it is imitating.
 *
 * ONE CAVEAT, AND IT IS NOT SMALL: a browser keeps Ctrl+Tab, Ctrl+PageUp and
 * Ctrl+PageDown for its own tabs and does not hand them to a page. Inside a
 * browser tab this scheme is therefore silent, and the keys arrive only in a
 * desktop shell, or under the keyboard lock a fullscreen page may ask for. So
 * it answers Ctrl+Shift+← and Ctrl+Shift+→ as well: the same movement by a
 * chord no browser reserves, for a page that wants the behaviour where the
 * faithful keys cannot land.
 */
/**
 * BrowserKeys asks the pane to LAND, and the pane does it, because letting go
 * of a native focus is a DOM act and this module touches no DOM. A caret left
 * blinking in a field that is no longer on the screen is not a tab switch; it
 * is a bug you find later.
 */
class BrowserKeys {
    static keyDown(pane, ev) { return BrowserKeys._walk(pane, ev); }
    static chord(pane, ev) { return BrowserKeys._walk(pane, ev); }

    /**
     * NO. A browser's tab strip is not a place you can be — you are always in
     * a page, and the strip is the road between pages. So a pane wearing this
     * scheme never rests on the bar: handed the keys, it hands them straight
     * on to the tab that is showing, and a widget that yields is not caught
     * here but passed above, because there is nothing here to come back to.
     */
    static keeps() { return false; }

    static _walk(pane, ev) {
        if (!ev.ctrlKey || ev.altKey || ev.metaKey) return false;
        var by = BrowserKeys._by(ev);
        if (by === 0) return false;
        var ids = pane.tabs(), n = ids.length;
        if (!n) return true;   // taken all the same: the scheme is on, and there is simply nowhere to go
        var i = ids.indexOf(pane.activeTab()), to = ids[((i < 0 ? 0 : i) + by + n) % n];   // and it wraps, which is the difference
        pane.switchTab(to);
        pane.land(to);   // A BROWSER LANDS YOU IN THE NEW TAB: the old native focus let go, the new widget holding the keys
        return true;
    }

    /** Forward, back, or not ours. Shift means back on Tab and on the arrows; the pages say it themselves. */
    static _by(ev) {
        var k = ev.key;
        if (k === "Tab") return ev.shiftKey ? -1 : 1;
        if (k === "PageDown") return ev.shiftKey ? 0 : 1;
        if (k === "PageUp") return ev.shiftKey ? 0 : -1;
        if (ev.shiftKey && k === "ArrowRight") return 1;
        if (ev.shiftKey && k === "ArrowLeft") return -1;
        return 0;
    }
}
