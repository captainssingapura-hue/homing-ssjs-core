// =============================================================================
// KeyboardEventsModule — KeyboardEvents, the closed vocabulary of the keyboard
// party, as data: a class of static factories, one per kind; each validates
// what the Java sealed sum KeyboardEvent validates and returns a frozen plain
// object tagged by `kind`.
//
//   KeyboardEvents.Granted(id, by)    a member holds the keyboard now: by a claim, a yield
//                                     that reached it, or a member that left (claim unless said)
//   KeyboardEvents.Taken(id, by)      it lost the keyboard — to `by`, or to no one
//   KeyboardEvents.Released(id)       it gave the keyboard up, or left
//   KeyboardEvents.Offered(id)        the walk's cursor rests on it: a confirming key would give it the keys
//   KeyboardEvents.Withdrawn(id)      the offer is off: the walk moved on, confirmed, or was called off
//   KeyboardEvents.KINDS              the kinds, in this order
//
// The keys themselves are not events of ours: they are the browser's, passed
// through to the holder. One event per change of holder, on the steward's sink.
// =============================================================================

function _id(v, what) {
    if (typeof v !== "string" || !v) throw new Error("[KeyboardEvents] " + what + " must be a non-empty string");
    return v;
}

class KeyboardEvents {
    static KINDS = Object.freeze(["Granted", "Taken", "Released", "Offered", "Withdrawn"]);

    /** A member holds the keyboard now: by a claim, by a yield that reached it, by a member that left, or by the steward's Tab. */
    static Granted(id, by) {
        if (by != null && (typeof by !== "string" || !by)) throw new Error("[KeyboardEvents] Granted.by must be a non-empty string");
        return Object.freeze({ kind: "Granted", id: _id(id, "Granted.id"), by: by == null ? "claim" : by });
    }
    /** A member lost the keyboard: evicted by `by`, or, when `by` is null, released or gone. */
    static Taken(id, by) {
        if (by != null) _id(by, "Taken.by");
        return Object.freeze({ kind: "Taken", id: _id(id, "Taken.id"), by: by == null ? null : by });
    }
    /** A member gave the keyboard up, or left the party while holding it. */
    static Released(id) {
        return Object.freeze({ kind: "Released", id: _id(id, "Released.id") });
    }
    /** The keys are offered to a member: the walk's cursor rests on it. Never the holder. */
    static Offered(id) {
        return Object.freeze({ kind: "Offered", id: _id(id, "Offered.id") });
    }
    /** The offer is withdrawn: the walk moved on, was confirmed, or was called off. */
    static Withdrawn(id) {
        return Object.freeze({ kind: "Withdrawn", id: _id(id, "Withdrawn.id") });
    }
}
