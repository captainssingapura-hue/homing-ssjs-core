// =============================================================================
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

class PaneKeys {
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

    /** The law: a tab's widget is a member of a focus branch of its own and answers activate(), so the keys can be given to it and it can take them. */
    static law(widget) {
        var f = widget ? widget.focus : null;
        return !!f && typeof f === "object" && typeof f.leave === "function" && !!f.in && typeof widget.activate === "function";
    }
}
