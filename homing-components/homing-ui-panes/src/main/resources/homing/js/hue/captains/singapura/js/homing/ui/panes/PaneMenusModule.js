// =============================================================================
// PaneMenus — the menus a pane offers, and where they are asked for. Three
// openings, one rule: the page's steward is asked, and true means it took the
// request, which is the only thing that suppresses the browser's own menu.
//
//   PaneMenus.forChip(pane, tabId, kind) → the opener a chip is given, or null when
//       the page offers no menus: a right-click on the chip, or the menu key
//       on it, selects that tab and asks for the TAB's kind, bound to
//       { pane, tab, anchor } — the chip being the anchor a scroll closes on
//   PaneMenus.byKey(pane)            → the active tab's menu, at its chip: what
//       the ContextMenu key and Shift+F10 do while the pane holds the keys
//   PaneMenus.onGround(pane, at)     → the kind the PAGE named for the strip's
//       own ground, bound to { pane } alone. What that menu offers is about
//       the room the pane sits in — parting it, closing it — which the pane
//       knows nothing of, so the kind is the page's and not the pane's
//
// Pure: it imports nothing and opens nothing itself. It reads the pane's
// surface and its steward, as PaneKeys reads the pane's.
// =============================================================================

class PaneMenus {

    /** The opener a chip is given, or null: the page offers no menus, and a right-click on a chip is the browser's. */
    static forChip(pane, tabId, kind) {
        if (!pane._menus) return null;
        return function (at, keyboard) {
            var i = pane.tabIndexOf(tabId);
            if (i < 0) return false;
            pane.switchTab(tabId);
            var entry = pane._tabs[i];
            return pane._menus.open(kind, { pane: pane, tab: entry.tab, anchor: entry.chip }, at, { anchor: entry.chip, keyboard: keyboard });
        };
    }

    /** The active tab's menu, at its chip; true when the steward took it. */
    static byKey(pane) {
        var i = pane.activeTab() === null ? -1 : pane.tabIndexOf(pane.activeTab());
        if (i < 0 || !pane._tabs[i].menu) return false;
        var r = pane.chipOf(i).getBoundingClientRect();
        return !!pane._tabs[i].menu({ x: r.left + 12, y: r.bottom - 2 }, true);
    }

    /** The strip's ground, right-clicked: the page's own kind, with the pane bound; false when the page named none. */
    static onGround(pane, at) {
        if (!pane._stripMenu) return false;
        return !!pane._menus.open(pane._stripMenu, { pane: pane }, at, { anchor: pane.el });
    }
}
