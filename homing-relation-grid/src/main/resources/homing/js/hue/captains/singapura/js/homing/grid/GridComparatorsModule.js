// =============================================================================
// GridComparatorsModule — RFC 0050-ext6's stock comparators: the orderings a
// Relation declares for its columns through columnMeta(column).compare.
//
// A comparator is MANDATORY to sort. GridViewState refuses sortBy() on a
// column whose meta supplies none and drops such a sort on restore; there is
// no fallback to raw `<` — that is the bug this module retires (enums ignored
// their declared order, text sorted by code point, nulls landed anywhere).
//
// The grid guarantees a comparator NEVER receives null or undefined: absent
// values are set aside before the sort and appended after it, in base order,
// whichever the direction. Direction is the grid's too — a comparator states
// the ascending order and nothing else.
//
//   compareNumbers(a, b)      numeric; NaN after every number
//   compareText(a, b)         NATURAL order: case- and accent-folded, digit
//                             runs compared numerically ('item 2' before
//                             'item 10'). Deliberately NOT a locale collator —
//                             engine-independent, so it orders identically in
//                             the headless GraalVM harness (no Intl-402) and
//                             in every browser whatever the user's locale. A
//                             host wanting locale collation supplies
//                             { compare: (a, b) => collator.compare(a, b) }.
//   compareByOrder(options)   an enum's DECLARED order; values outside it sort
//                             after those inside, among themselves by base order
// =============================================================================

function compareNumbers(a, b) {
    var x = Number(a), y = Number(b);
    var xn = x !== x, yn = y !== y;                  // NaN, without isNaN's coercion
    if (xn || yn) return xn === yn ? 0 : (xn ? 1 : -1);
    return x < y ? -1 : (x > y ? 1 : 0);
}

/** Case and accents folded; combining marks stripped after NFD. Engines
 *  without normalize() (none current) fall back to case folding alone. */
function _hgrFoldText(v) {
    var s = String(v);
    if (typeof s.normalize === "function") s = s.normalize("NFD").replace(/[̀-ͯ]/g, "");
    return s.toLowerCase();
}
var _HGR_RUNS = /\d+|\D+/g;

function compareText(a, b) {
    var sa = _hgrFoldText(a), sb = _hgrFoldText(b);
    if (sa === sb) return 0;
    var ra = sa.match(_HGR_RUNS) || [], rb = sb.match(_HGR_RUNS) || [];
    var n = Math.min(ra.length, rb.length);
    for (var i = 0; i < n; i++) {
        var x = ra[i], y = rb[i];
        if (x === y) continue;
        if (/^\d/.test(x) && /^\d/.test(y)) {
            var c = compareNumbers(x, y);            // '2' vs '10' — by value
            if (c) return c;
            continue;                                // '02' vs '2' — same value, read on
        }
        return x < y ? -1 : 1;                       // code-point order on the folded run
    }
    return ra.length - rb.length;                    // the shorter prefix first
}

function compareByOrder(options) {
    var rank = new Map();
    (options || []).forEach(function (o, i) { if (!rank.has(o)) rank.set(o, i); });
    return function (a, b) {
        var ra = rank.has(a) ? rank.get(a) : Infinity;
        var rb = rank.has(b) ? rank.get(b) : Infinity;
        return ra < rb ? -1 : (ra > rb ? 1 : 0);
    };
}
