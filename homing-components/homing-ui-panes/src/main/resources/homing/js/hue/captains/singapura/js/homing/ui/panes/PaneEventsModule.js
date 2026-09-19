// =============================================================================
// PaneEventsModule — PaneEvents, the closed vocabulary of a pane's mutations, as data.
//
// Each factory validates what the Java sealed sum PaneEvent validates in its
// compact constructors and returns a frozen plain object tagged by `kind`:
//
//   PaneEvents.TabAdded(slotId, tab, index)
//   PaneEvents.TabRemoved(slotId, tab, fromIndex)
//   PaneEvents.TabMoved(srcSlotId, tab, srcIndex, destSlotId, destIndex)
//   PaneEvents.TabActivated(slotId, tabId)
//   PaneEvents.TabAttached(slotId, tab, atIndex)
//   PaneEvents.AddRequested(slotId)
//   PaneEvents.KINDS                       the kinds, in this order
//
// Data, not classes: an event goes into a log, a checkpoint, a replay, and
// across module instances, where `instanceof` would lie. A consumer switches
// on `kind`; the field names are the record components' names, and a test
// holds the two vocabularies together. `tab` is the descriptor the holder
// gave the pane — id, title, pinned, widget — as it is.
// =============================================================================

function _slot(v, what) {
    if (typeof v !== "string" || !v) throw new Error("[PaneEvents] " + what + " must be a non-empty string");
    return v;
}
function _tab(v, what) {
    if (!v || typeof v !== "object" || typeof v.id !== "string" || !v.id) throw new Error("[PaneEvents] " + what + " must be a tab with an id");
    return v;
}
function _index(v, what) {
    if (typeof v !== "number" || v !== (v | 0) || v < 0) throw new Error("[PaneEvents] " + what + " must be a non-negative integer");
    return v;
}

var PaneEvents = Object.freeze({
    KINDS: Object.freeze(["TabAdded", "TabRemoved", "TabMoved", "TabActivated", "TabAttached", "AddRequested"]),

    /** A tab was added at the end of its block via addTab. */
    TabAdded: function (slotId, tab, index) {
        return Object.freeze({ kind: "TabAdded", slotId: _slot(slotId, "TabAdded.slotId"), tab: _tab(tab, "TabAdded.tab"), index: _index(index, "TabAdded.index") });
    },
    /** A tab was closed via removeTab — the cross or the holder; its widget is already disposed. */
    TabRemoved: function (slotId, tab, fromIndex) {
        return Object.freeze({ kind: "TabRemoved", slotId: _slot(slotId, "TabRemoved.slotId"), tab: _tab(tab, "TabRemoved.tab"), fromIndex: _index(fromIndex, "TabRemoved.fromIndex") });
    },
    /** A tab moved, within a pane or between two; destIndex is where it ended up. */
    TabMoved: function (srcSlotId, tab, srcIndex, destSlotId, destIndex) {
        return Object.freeze({ kind: "TabMoved", srcSlotId: _slot(srcSlotId, "TabMoved.srcSlotId"), tab: _tab(tab, "TabMoved.tab"),
                               srcIndex: _index(srcIndex, "TabMoved.srcIndex"), destSlotId: _slot(destSlotId, "TabMoved.destSlotId"), destIndex: _index(destIndex, "TabMoved.destIndex") });
    },
    /** The active tab changed — a chip, a key, or switchTab. */
    TabActivated: function (slotId, tabId) {
        return Object.freeze({ kind: "TabActivated", slotId: _slot(slotId, "TabActivated.slotId"), tabId: _slot(tabId, "TabActivated.tabId") });
    },
    /** A tab was attached from outside via attachTab — a re-dock, a programmatic re-parent. */
    TabAttached: function (slotId, tab, atIndex) {
        return Object.freeze({ kind: "TabAttached", slotId: _slot(slotId, "TabAttached.slotId"), tab: _tab(tab, "TabAttached.tab"), atIndex: _index(atIndex, "TabAttached.atIndex") });
    },
    /** The add button was pressed while a tab could be added; the holder decides what that means. */
    AddRequested: function (slotId) {
        return Object.freeze({ kind: "AddRequested", slotId: _slot(slotId, "AddRequested.slotId") });
    }
});
