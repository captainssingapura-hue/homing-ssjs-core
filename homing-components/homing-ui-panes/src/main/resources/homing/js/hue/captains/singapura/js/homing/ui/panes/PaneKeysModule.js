// =============================================================================
// PaneKeys — the pane's keys while it holds them, as one function over the
// pane's own surface: what a key means for a container of tabs, and nothing
// about how the pane is built. Level 1 of the two levels: the container's
// own operations and no more.
//
//   PaneKeys.keyDown(pane, ev) → true when taken
//     ← →              the active tab moves to the previous / next, at once; no wrap
//     Home / End       the first / last tab
//     Shift+← / →      the active tab moves one slot along the rail, staying active
//     Shift+↓          the active tab asked to detach: pane.requestDetach()
//     Enter            the active tab's widget activates itself — a claim of its own
//     Escape           the pane yields, up the tree
//     Shift+F10, ContextMenu   the active tab's menu, at its chip: pane.menuByKey()
//   A chord with Ctrl, Alt or Meta is left; so is anything not above.
// =============================================================================

class PaneKeys {
    static keyDown(pane, ev) {
        if (ev.key === "Escape") { Keys.yield(pane.focus.owner); return true; }
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
}
