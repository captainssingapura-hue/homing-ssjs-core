// =============================================================================
// GridCellTypesModule — RFC 0050's EffectiveTypes: the strong, fine-grained
// value types that gate BULK EDITING. A type owns identity (name — the
// homogeneity check compares names, so 'price' ≠ 'calories' even though both
// are numbers), the opening characters that start a virtual session, the
// session mode, and VALIDATION. Display formatting stays in the cell; what a
// value may BE lives here.
//
// The type object shape:
//   { name,                      // identity — equality by name
//     opens(ch),                 // does this printable char open a session?
//     instantaneous,             // true: the opening char IS the whole edit
//     parse(buffer) }            // {ok:true, value} | {ok:false, error}
//
// Stock factories:
//   textType(name)                       — any printable opens; free text
//   numberType(name, {integer, min, max})— digits/-/. open; validated number
//   enumType(name, options)              — letters open; prefix-matches an option
//   instantType(name, chars)             — mine-tile style: keystroke = value
// =============================================================================

function textType(name) {
    return {
        name: name || "text",
        opens: function (ch) { return typeof ch === "string" && ch.length === 1; },
        instantaneous: false,
        parse: function (buffer) { return { ok: true, value: buffer }; }
    };
}

function numberType(name, opts) {
    opts = opts || {};
    return {
        name: name || "number",
        opens: function (ch) { return /^[0-9.\-]$/.test(ch); },
        instantaneous: false,
        parse: function (buffer) {
            if (buffer === "") return { ok: false, error: "empty" };
            var n = Number(buffer);
            if (isNaN(n)) return { ok: false, error: "not a number" };
            if (opts.integer && n !== Math.trunc(n)) return { ok: false, error: "integer required" };
            if (typeof opts.min === "number" && n < opts.min) return { ok: false, error: "below " + opts.min };
            if (typeof opts.max === "number" && n > opts.max) return { ok: false, error: "above " + opts.max };
            return { ok: true, value: n };
        }
    };
}

function enumType(name, options) {
    options = options || [];
    return {
        name: name || ("enum:" + options.join("|")),
        opens: function (ch) { return /^[A-Za-z]$/.test(ch); },
        instantaneous: false,
        /** Prefix match, case-insensitive; unique match required to be valid. */
        parse: function (buffer) {
            if (buffer === "") return { ok: false, error: "empty" };
            var low = buffer.toLowerCase();
            var hits = options.filter(function (o) {
                return String(o).toLowerCase().indexOf(low) === 0;
            });
            if (hits.length === 1) return { ok: true, value: hits[0] };
            return { ok: false, error: hits.length ? "ambiguous" : "no match" };
        }
    };
}

/** Mine-tile style: the opening keystroke is the complete value, committed
 *  on the spot — the virtual editor exists only for that instant. */
function instantType(name, chars) {
    var set = {};
    (chars || []).forEach(function (c) { set[c] = true; });
    return {
        name: name,
        opens: function (ch) { return set[ch] === true; },
        instantaneous: true,
        parse: function (buffer) { return { ok: true, value: buffer }; }
    };
}
