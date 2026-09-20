// =============================================================================
// DockEventsModule — DockEvents, the closed vocabulary of docking, as data.
//
// A class of static factories, one per kind. Each validates what the Java
// sealed sum DockEvent validates in its compact constructors and returns a
// frozen plain object tagged by `kind`:
//
//   DockEvents.Docked(tabId, slotId, index)     a floating tab dropped on a dock
//   DockEvents.Undocked(tabId, slotId)          a tab pulled off a dock to float
//   DockEvents.KINDS                            the kinds, in this order
//
// The desk's and the pane's own events say the rest — Opened, Released,
// TabAttached — on the same sink; a consumer switches on `kind`, and a test
// holds the two vocabularies together.
// =============================================================================

function _id(v, what) {
    if (typeof v !== "string" || !v) throw new Error("[DockEvents] " + what + " must be a non-empty string");
    return v;
}
function _index(v, what) {
    if (typeof v !== "number" || v !== (v | 0) || v < 0) throw new Error("[DockEvents] " + what + " must be a non-negative integer");
    return v;
}

class DockEvents {
    static KINDS = Object.freeze(["Docked", "Undocked"]);

    /** A floating tab was dropped on a dock and is its tab now, at this index. */
    static Docked(tabId, slotId, index) {
        return Object.freeze({ kind: "Docked", tabId: _id(tabId, "Docked.tabId"), slotId: _id(slotId, "Docked.slotId"), index: _index(index, "Docked.index") });
    }
    /** A tab was pulled off a dock's strip and floats now, under the same hand. */
    static Undocked(tabId, slotId) {
        return Object.freeze({ kind: "Undocked", tabId: _id(tabId, "Undocked.tabId"), slotId: _id(slotId, "Undocked.slotId") });
    }
}
