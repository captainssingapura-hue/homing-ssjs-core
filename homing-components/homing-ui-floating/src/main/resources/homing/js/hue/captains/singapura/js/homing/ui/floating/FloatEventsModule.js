// =============================================================================
// FloatEventsModule — FloatEvents, the closed vocabulary of a desk's mutations, as data.
//
// A class of static factories, one per kind. Each validates what the Java
// sealed sum FloatEvent validates in its compact constructors and returns a
// frozen plain object tagged by `kind`:
//
//   FloatEvents.Opened(id, title, x, y, w, h)
//   FloatEvents.Moved(id, x, y)
//   FloatEvents.Resized(id, w, h)
//   FloatEvents.Raised(id)
//   FloatEvents.Closed(id)
//   FloatEvents.Released(id)
//   FloatEvents.KINDS                       the kinds, in this order
//
// Data, not classes: an event goes into a log, a checkpoint, a replay, and
// across module instances, where `instanceof` would lie. A consumer switches
// on `kind`; the field names are the record components' names, and a test
// holds the two vocabularies together. Positions and measures are whole
// pixels within the desk.
// =============================================================================

function _id(v, what) {
    if (typeof v !== "string" || !v) throw new Error("[FloatEvents] " + what + " must be a non-empty string");
    return v;
}
function _text(v, what) {
    if (typeof v !== "string") throw new Error("[FloatEvents] " + what + " must be a string");
    return v;
}
function _int(v, what) {
    if (typeof v !== "number" || v !== (v | 0)) throw new Error("[FloatEvents] " + what + " must be an integer");
    return v;
}
function _positive(v, what) {
    if (typeof v !== "number" || v !== (v | 0) || v <= 0) throw new Error("[FloatEvents] " + what + " must be a positive integer");
    return v;
}

class FloatEvents {
    static KINDS = Object.freeze(["Opened", "Moved", "Resized", "Raised", "Closed", "Released"]);

    /** A pane was opened on the desk, at this place and measure, and is the active one. */
    static Opened(id, title, x, y, w, h) {
        return Object.freeze({ kind: "Opened", id: _id(id, "Opened.id"), title: _text(title, "Opened.title"),
                               x: _int(x, "Opened.x"), y: _int(y, "Opened.y"), w: _positive(w, "Opened.w"), h: _positive(h, "Opened.h") });
    }
    /** A pane came to rest somewhere else — the hand let go, or moveTo returned. */
    static Moved(id, x, y) {
        return Object.freeze({ kind: "Moved", id: _id(id, "Moved.id"), x: _int(x, "Moved.x"), y: _int(y, "Moved.y") });
    }
    /** A pane has another measure — the grip let go, or resizeTo returned. */
    static Resized(id, w, h) {
        return Object.freeze({ kind: "Resized", id: _id(id, "Resized.id"), w: _positive(w, "Resized.w"), h: _positive(h, "Resized.h") });
    }
    /** A pane became the active one, on top of the stack — a press on it, focus into it, or raise. */
    static Raised(id) {
        return Object.freeze({ kind: "Raised", id: _id(id, "Raised.id") });
    }
    /** A pane was closed — the cross, Escape, or close; its widget is already disposed. */
    static Closed(id) {
        return Object.freeze({ kind: "Closed", id: _id(id, "Closed.id") });
    }
    /** A pane left the desk for a dock — release; its widget travels on, not disposed. */
    static Released(id) {
        return Object.freeze({ kind: "Released", id: _id(id, "Released.id") });
    }
}
