// =============================================================================
// SplitEventsModule — SplitEvents, the closed vocabulary of a splitter's
// mutations, as data: a class of static factories, one per kind.
//
//   SplitEvents.RatioChanged(path, ratios)   a split's children re-shared
//   SplitEvents.KINDS
//
// `path` names the split in the layout tree: "" for the root, else the
// child indexes from the root joined by "/". `ratios` are the split's
// children's shares after the change, in order, summing to one. Frozen
// plain objects tagged by kind, the Java SplitEvent records' components as
// fields; a test holds the two vocabularies together.
// =============================================================================

function _path(v, what) {
    if (typeof v !== "string" || !/^(\d+(\/\d+)*)?$/.test(v)) throw new Error("[SplitEvents] " + what + " must be a path of child indexes");
    return v;
}
function _ratios(v, what) {
    if (!Array.isArray(v) || v.length < 2) throw new Error("[SplitEvents] " + what + " must hold two or more ratios");
    var sum = 0;
    for (var i = 0; i < v.length; i++) {
        if (typeof v[i] !== "number" || !(v[i] > 0)) throw new Error("[SplitEvents] " + what + "[" + i + "] must be a positive number");
        sum += v[i];
    }
    if (Math.abs(sum - 1) > 1e-6) throw new Error("[SplitEvents] " + what + " must sum to one");
    return Object.freeze(v.slice());
}

class SplitEvents {
    static KINDS = Object.freeze(["RatioChanged"]);

    /** A split's children were re-shared: by a divider drag, or by setRatios. */
    static RatioChanged(path, ratios) {
        return Object.freeze({ kind: "RatioChanged", path: _path(path, "RatioChanged.path"), ratios: _ratios(ratios, "RatioChanged.ratios") });
    }
}
