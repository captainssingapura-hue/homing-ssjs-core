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
//   KeyboardEvents.Marked(id, state)  the marker moved (RFC 0066 E3, keyboard §17.5): the holder is id, and
//                                     it is "held" (nothing natively focused), "lent" (a native control of
//                                     its own has the browser's focus) or "away" (the browser's focus is
//                                     outside every member, and nothing is routed)
//   KeyboardEvents.KINDS              the kinds, in this order
//
// And how the steward's listeners hear them - its sink for these events, its
// trace for the keys: KeyboardEvents.listen(list, fn, what) adds one and gives
// back the function that takes it off; KeyboardEvents.tell(list, thing, what)
// tells every one on the list, each on its own - one that throws is reported,
// and the rest are still told.
//
// The keys themselves are not events of ours: they are the browser's, passed
// through to the holder. One event per change of holder, on the steward's sink,
// and one per move of the marker.
// =============================================================================

function _id(v, what) {
    if (typeof v !== "string" || !v) throw new Error("[KeyboardEvents] " + what + " must be a non-empty string");
    return v;
}

class KeyboardEvents {
    static KINDS = Object.freeze(["Granted", "Taken", "Released", "Offered", "Withdrawn", "Marked"]);

    /** A listener added to a list of the steward's; the function returned takes it off. */
    static listen(list, fn, what) {
        if (typeof fn !== "function") throw new Error("[KeyboardSteward] " + what + " wants a function");
        list.push(fn);
        return function () { var i = list.indexOf(fn); if (i >= 0) list.splice(i, 1); };
    }

    /** Something told to every listener on a list, each on its own: one that throws is reported, and the rest are still told. */
    static tell(list, thing, what) {
        var sinks = list.slice();
        for (var i = 0; i < sinks.length; i++) {
            try { sinks[i](thing); } catch (e) { console.error("[KeyboardSteward] " + what + " threw on " + thing.kind + ":", e); }
        }
    }
    static STATES = Object.freeze(["held", "lent", "away"]);

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
    /** The marker moved: the holder is id, held, lent or away. */
    static Marked(id, state) {
        if (KeyboardEvents.STATES.indexOf(state) < 0) throw new Error("[KeyboardEvents] Marked.state must be one of " + KeyboardEvents.STATES.join(", ") + ": " + state);
        return Object.freeze({ kind: "Marked", id: _id(id, "Marked.id"), state: state });
    }
}
