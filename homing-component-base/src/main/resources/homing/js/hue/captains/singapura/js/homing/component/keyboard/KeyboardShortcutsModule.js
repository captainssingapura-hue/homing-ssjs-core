// =============================================================================
// KeyboardShortcuts — the keys a PAGE claims before anyone. The steward routes
// every other key by state: to the holder while nothing is natively focused,
// to no one while something is. A page's own commands cannot wait for that —
// the one that summons a switcher has to work while the hand is in a grid's
// editor — so the steward tries these first, before the native world and
// before the holder.
//
// The rule that keeps it honest: A CHORD, OR A FUNCTION KEY. A page may claim
// Ctrl+K or F6; it may not claim "k", because someone is typing it. Nothing
// else is offered first, and a shortcut that does not take the key leaves it
// to the ordinary routing.
//
//   KeyboardShortcuts.add(list, fn) → off   fn(ev) → true when it took the key
//   KeyboardShortcuts.took(list, ev)        true when one of them took it
//   KeyboardShortcuts.claimable(ev)         whether a page may be offered it
//
// A pure module of statics over the caller's list, so the rule is read and
// tested without a browser.
// =============================================================================

class KeyboardShortcuts {

    /** Another key of the page's; the function returned takes it off the list. */
    static add(list, fn) {
        if (typeof fn !== "function") throw new Error("[KeyboardShortcuts] a shortcut is a function of the event");
        list.push(fn);
        return function () { var i = list.indexOf(fn); if (i >= 0) list.splice(i, 1); };
    }

    /** Whether a page may be offered this key at all: a chord, or a function key — never a key someone could be typing. */
    static claimable(ev) {
        return !!ev && (ev.ctrlKey === true || ev.altKey === true || ev.metaKey === true || /^F\d{1,2}$/.test(String(ev.key)));
    }

    /** The page's keys, in the order they were claimed: true as soon as one takes it. */
    static took(list, ev) {
        if (!list.length || !KeyboardShortcuts.claimable(ev)) return false;
        var some = list.slice();
        for (var i = 0; i < some.length; i++) {
            var taken = false;
            try { taken = some[i](ev) === true; } catch (e) { console.error("[KeyboardShortcuts] a shortcut threw:", e); }
            if (taken) return true;
        }
        return false;
    }
}
