// =============================================================================
// SplitGridEventsModule — SplitGridEvents, the grid's changes of arrangement, as data.
//
// A class of static factories, one per kind. Each validates what the Java
// sealed sum SplitGridEvent validates in its compact constructors and returns
// a frozen plain object tagged by `kind`:
//
//   SplitGridEvents.TracksChanged(path, ratios)      a split re-shared; the ratios sum to one
//   SplitGridEvents.Subdivided(cellId, newCellId, side)   a new, empty cell beside one
//   SplitGridEvents.Removed(cellId)                  a cell gone, its room to its neighbour
//   SplitGridEvents.CursorMoved(cellId, by)          a mirror's cursor at a cell: by an arrow
//                                                    (left, right, up, down), a pointer, or a call
//   SplitGridEvents.KINDS                            the kinds, in this order
//
// A path names a split by child indexes from the root, joined by "/"; the
// root split's path is "". Data, not classes: an arrangement goes into a
// log, a checkpoint, a replay, as it is.
// =============================================================================

var _PATH = /^(\d+(\/\d+)*)?$/;
var _SIDES = Object.freeze(["left", "right", "top", "bottom"]);
var _BY = Object.freeze(["left", "right", "up", "down", "pointer", "call"]);

function _path(v, what) {
    if (typeof v !== "string" || !_PATH.test(v)) throw new Error("[SplitGridEvents] " + what + " must be child indexes joined by '/', or empty");
    return v;
}
function _ratios(v, what) {
    if (!Array.isArray(v) || v.length < 2) throw new Error("[SplitGridEvents] " + what + " must be two or more ratios");
    var sum = 0, out = [];
    for (var i = 0; i < v.length; i++) {
        if (typeof v[i] !== "number" || !(v[i] > 0)) throw new Error("[SplitGridEvents] " + what + " must be positive numbers");
        sum += v[i]; out.push(v[i]);
    }
    if (Math.abs(sum - 1) > 1e-6) throw new Error("[SplitGridEvents] " + what + " must sum to one");
    return Object.freeze(out);
}
function _id(v, what) {
    if (typeof v !== "string" || !v) throw new Error("[SplitGridEvents] " + what + " must be a non-empty string");
    return v;
}
function _side(v, what) {
    if (_SIDES.indexOf(v) < 0) throw new Error("[SplitGridEvents] " + what + " must be left, right, top or bottom");
    return v;
}
function _by(v, what) {
    if (_BY.indexOf(v) < 0) throw new Error("[SplitGridEvents] " + what + " must be left, right, up, down, pointer or call");
    return v;
}

class SplitGridEvents {
    static KINDS = Object.freeze(["TracksChanged", "Subdivided", "Removed", "CursorMoved"]);
    static SIDES = _SIDES;

    /** A split's children were re-shared, by a divider drag or by setRatios; the ratios sum to one. */
    static TracksChanged(path, ratios) {
        return Object.freeze({ kind: "TracksChanged", path: _path(path, "TracksChanged.path"), ratios: _ratios(ratios, "TracksChanged.ratios") });
    }
    /** A cell was subdivided: a new, empty cell beside it on the side named, each taking half the room. */
    static Subdivided(cellId, newCellId, side) {
        return Object.freeze({ kind: "Subdivided", cellId: _id(cellId, "Subdivided.cellId"), newCellId: _id(newCellId, "Subdivided.newCellId"), side: _side(side, "Subdivided.side") });
    }
    /** A cell was removed: its room went to its neighbour, and a split left with one child gave way to it. */
    static Removed(cellId) {
        return Object.freeze({ kind: "Removed", cellId: _id(cellId, "Removed.cellId") });
    }
    /** A mirror's cursor is at a cell: moved there by an arrow, a pointer, or a call. */
    static CursorMoved(cellId, by) {
        return Object.freeze({ kind: "CursorMoved", cellId: _id(cellId, "CursorMoved.cellId"), by: _by(by, "CursorMoved.by") });
    }
}
