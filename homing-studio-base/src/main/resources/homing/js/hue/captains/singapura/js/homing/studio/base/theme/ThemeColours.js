// ThemeColours.js
//
// The colour control of the theme picker: one row of swatches, one per
// palette the registry offers, the picked base's own colours marked as its
// default. Colour is an orthogonal plane of a design, so the control is
// orthogonal to the tree: the tree picks the base, this picks the colours, and
// the slug the page will wear is the base's entry for the palette — read off
// the registry, never composed here.
//
// A swatch is a radio: the row is a radiogroup and the picked swatch is
// aria-checked. Its look at that state is the design's word for the checked
// interactive edge — no second class applied beside it, no order to win.
//
// The dots are DATA: each palette's own surface, accent and inverted colours,
// set per dot as a custom property (RFC 0044) that the dot's class reads.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java ThemeColours declaration — do not add import/export lines here.

var DOTS = ["surface", "accent", "inverted", "text"];

/**
 * Build the control into `host` and return an UPDATE function.
 *
 * `onPick(paletteSlug)` fires with the palette chosen, or null for the base's
 * own colours. `update(theme, paletteSlug)` marks the chosen swatch and names
 * the base's own colours as the default — the base may have changed since.
 */
function mountColourStrip(branch, host, seq, palettes, onPick) {
    var row = branch.createElement("clr" + seq, "div");
    css.addClass(row, tp_colours);
    row.setAttribute("role", "radiogroup");
    row.setAttribute("aria-label", "Colours");
    host.appendChild(row);

    var label = branch.createElement("clrl" + seq, "span");
    css.addClass(label, tp_colours_label);
    label.textContent = "Colours";
    row.appendChild(label);

    var buttons = {};   // palette slug → { btn, own }
    palettes.forEach(function (p, i) {
        var btn = branch.createElement("sw" + seq + "_" + i, "button");
        btn.type = "button";
        css.addClass(btn, tp_swatch);
        btn.setAttribute("role", "radio");
        btn.setAttribute("aria-checked", "false");
        btn.title = p.inspiration || p.label || p.slug;

        var dots = branch.createElement("swd" + seq + "_" + i, "span");
        css.addClass(dots, tp_swatch_dots);
        dots.setAttribute("aria-hidden", "true");
        DOTS.forEach(function (k, j) {
            var v = p.swatches && p.swatches[k];
            if (!v) return;
            var dot = branch.createElement("swx" + seq + "_" + i + "_" + j, "span");
            css.addClass(dot, tp_swatch_dot);
            dot.style.setProperty("--tp-dot", v);       // DATA, via setProperty (RFC 0044)
            dots.appendChild(dot);
        });
        btn.appendChild(dots);

        var name = branch.createElement("swn" + seq + "_" + i, "span");
        name.textContent = p.label || p.slug;
        btn.appendChild(name);

        var own = branch.createElement("swo" + seq + "_" + i, "span");
        css.addClass(own, tp_swatch_own);
        own.textContent = "default";
        own.hidden = true;
        btn.appendChild(own);

        btn.addEventListener("click", function () { onPick(p.slug); });
        row.appendChild(btn);
        buttons[p.slug] = { btn: btn, own: own };
    });

    return function (theme, paletteSlug) {
        var mine = colourwayOf(theme, null);            // the base's own colours
        var ownSlug = mine ? mine.palette : null;
        var picked = paletteSlug || ownSlug;
        Object.keys(buttons).forEach(function (slug) {
            buttons[slug].btn.setAttribute("aria-checked", slug === picked ? "true" : "false");
            buttons[slug].own.hidden = (slug !== ownSlug);
        });
    };
}
