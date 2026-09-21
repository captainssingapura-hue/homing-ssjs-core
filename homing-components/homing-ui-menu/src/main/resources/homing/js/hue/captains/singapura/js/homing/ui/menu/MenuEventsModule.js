// =============================================================================
// MenuEventsModule — MenuEvents, the closed vocabulary of context menus, as
// data. A class of static factories, one per kind; each validates what the
// Java sealed sum MenuEvent validates and returns a frozen plain object
// tagged by `kind`:
//
//   MenuEvents.Opened(menuKind, x, y)      a menu of this kind opened at a point
//   MenuEvents.Picked(menuKind, itemId)    an item of the open menu was picked
//   MenuEvents.Closed(menuKind, reason)    it closed: pick | escape | outside |
//                                          scroll | blur | taken | replaced | owner
//   MenuEvents.KINDS                       the kinds, in this order
//   MenuEvents.REASONS                     the reasons a menu closes
//
// The object a menu was bound to is not in the events — it is the holder's,
// told through the kind's handler. A consumer switches on `kind`.
// =============================================================================

function _id(v, what) {
    if (typeof v !== "string" || !v) throw new Error("[MenuEvents] " + what + " must be a non-empty string");
    return v;
}
function _finite(v, what) {
    if (typeof v !== "number" || !isFinite(v)) throw new Error("[MenuEvents] " + what + " must be a finite number");
    return v;
}

class MenuEvents {
    static KINDS = Object.freeze(["Opened", "Picked", "Closed"]);
    static REASONS = Object.freeze(["pick", "escape", "outside", "scroll", "blur", "taken", "replaced", "owner"]);

    /** A menu of this kind opened, its top-left at this point of the viewport. */
    static Opened(menuKind, x, y) {
        return Object.freeze({ kind: "Opened", menuKind: _id(menuKind, "Opened.menuKind"), x: _finite(x, "Opened.x"), y: _finite(y, "Opened.y") });
    }
    /** An item of the open menu was picked. */
    static Picked(menuKind, itemId) {
        return Object.freeze({ kind: "Picked", menuKind: _id(menuKind, "Picked.menuKind"), itemId: _id(itemId, "Picked.itemId") });
    }
    /** The open menu closed, for one of the reasons. */
    static Closed(menuKind, reason) {
        if (MenuEvents.REASONS.indexOf(reason) < 0) throw new Error("[MenuEvents] Closed.reason must be one of " + MenuEvents.REASONS.join(", ") + ": " + reason);
        return Object.freeze({ kind: "Closed", menuKind: _id(menuKind, "Closed.menuKind"), reason: reason });
    }
}
