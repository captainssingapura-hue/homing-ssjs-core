// =============================================================================
// ThemesIntroRenderer — the Themes page: every design and every palette the
// registry offers, and the picker to wear one.
//
// renderThemesIntro() → Node
//
// A theme is a design worn in colours — two planes. The page says so and
// lists both: one row per DESIGN (a base, in its own colours by default,
// with the palettes it is offered) and one row per PALETTE (with the design
// it was crafted for and the others it suits). A row is a link that wears
// the pair — ?theme=<slug>, an explicit override, a shareable themed view;
// the address wins for the page it names (RFC 0064). The row the page wears
// is marked, and the marks move when the theme does, from here or anywhere.
//
// Everything the page mints is on one branch; the rows' chrome is the shared
// Listing/ListItem, the dots and the offer lines are this module's own.
// =============================================================================

const _introOwner = Object.freeze({ toString: () => "themesIntro" });

var DOTS = ["surface", "accent", "inverted", "text"];

function renderThemesIntro() {
    var branch = domOpsParty.createBranch("themesIntro");
    branch.activate(_introOwner);

    var root = branch.createElement("root", "div");
    css.addClass(root, st_root);

    // Loading placeholder while the fetch is in flight.
    var loading = branch.createElement("loading", "div");
    css.addClass(loading, st_loading);
    loading.textContent = "Loading…";
    root.appendChild(loading);

    Promise.all([
        fetchRegistry(),
        fetch("/brand").then(function (r) {
            if (!r.ok) throw new Error("/brand HTTP " + r.status);
            return r.json();
        })
    ])
        .then(function (results) { _draw(branch, root, results[0], results[1]); })
        .catch(function (err) {
            var e = branch.createElement("error", "div");
            css.addClass(e, st_error);
            e.textContent = "Failed to load themes: " + err.message;
            root.replaceChildren(e);
        });

    return root;
}

function _draw(branch, root, reg, brand) {
    var children = [];

    children.push(Header({
        brand:  { href: brand.homeUrl, label: brand.label, logo: brand.logo },
        crumbs: [ { text: brand.label, href: brand.homeUrl }, { text: "Themes" } ]
    }));

    var main = branch.createElement("main", "div");
    css.addClass(main, st_main);

    var kicker = branch.createElement("kicker", "div");
    css.addClass(kicker, st_kicker);
    kicker.textContent = "Themes";
    main.appendChild(kicker);

    var title = branch.createElement("title", "h1");
    css.addClass(title, st_title);
    title.textContent = "Designs and colours";
    main.appendChild(title);

    var subtitle = branch.createElement("subtitle", "p");
    css.addClass(subtitle, st_subtitle);
    subtitle.textContent =
        "A theme is a design worn in colours — two planes, cut by what a class targets: "
        + "shape, type, motion and depth on one, every colour on the other. Any design can "
        + "wear any palette, but a palette says which design it was crafted for and which "
        + "others it suits, and only those are offered. Pick a pair to wear it: the switch "
        + "is live, and it sticks across navigation.";
    main.appendChild(subtitle);

    // The shared picker, the same component the chrome uses — so the two
    // cannot drift. The listings below are the inventory; this is the control.
    // It mounts once its own fetch lands, so it gets a host of its own here,
    // in place, rather than landing after whatever was appended meanwhile.
    var pickerHost = branch.createElement("picker", "div");
    main.appendChild(pickerHost);
    mountThemePickerTree(pickerHost, { heading: "Switch theme" });

    var labels = _labels(reg);
    var marks = [];   // { row, chip, wears(at) } — re-marked when the page's theme moves

    var designs = reg.themes.map(function (t, i) {
        var own = colourwayOf(t, null);
        var offer = _names(_offered(t).filter(function (s) { return !own || s !== own.palette; }), labels);
        var line = own ? "Worn in " + labels[own.palette] : "";
        if (offer) line += (line ? " · also " : "Also ") + offer;
        var m = _row(branch, "d" + i, t, line, _activateUrl(t.slug), function (at) { return at.theme.slug === t.slug; });
        marks.push(m);
        return m.row;
    });
    main.appendChild(Listing({ title: "Designs", children: designs }));

    var palettes = reg.palettes.map(function (p, i) {
        var suits = _names(_suited(p, reg), labels);
        var line = p.anchor === p.slug ? labels[p.slug] + "’s own colours" : "Crafted for " + (labels[p.anchor] || p.anchor);
        if (suits) line += " · also suits " + suits;
        var m = _row(branch, "p" + i, p, line, _wearUrl(p, reg), function (at) {
            var way = colourwayOf(at.theme, at.palette);
            return !!way && way.palette === p.slug;
        });
        marks.push(m);
        return m.row;
    });
    main.appendChild(Listing({ title: "Colours", children: palettes }));

    // The page wears one pair; mark its design and its colours. A switch is
    // live (RFC 0064) and may come from the picker above, the chrome or
    // another tab, so the marks follow the manager, not the address.
    function mark(slug) {
        var at = decompose(reg.themes, slug) || { theme: reg.themes[0], palette: null };
        marks.forEach(function (m) {
            var on = m.wears(at);
            if (on) css.addClass(m.row, st_list_item_met); else css.removeClass(m.row, st_list_item_met);
            m.chip.hidden = !on;
        });
    }
    mark(_currentThemeSlug() || (reg.themes[0] && reg.themes[0].slug));
    css.onThemeApplied(function (change) { mark(change.to); });

    children.push(main);
    root.replaceChildren.apply(root, children);
}

// ---------- rows ----------

/**
 * One listing row: the dots of `t`'s colours, its label, its inspiration and
 * the line under it, as a link to `href`. Returns the row and what re-marking
 * needs: the "in use" chip and the predicate saying whether the row wears the
 * decomposed pair.
 */
function _row(branch, id, t, line, href, wears) {
    var desc = branch.createElement(id + "_desc", "div");
    css.addClass(desc, tp_intro_desc);

    var inspiration = branch.createElement(id + "_insp", "div");
    inspiration.textContent = t.inspiration || "";
    desc.appendChild(inspiration);

    var offer = branch.createElement(id + "_offer", "div");
    css.addClass(offer, tp_intro_offer);
    var chip = branch.createElement(id + "_chip", "span");
    css.addClass(chip, tp_current);
    chip.textContent = "in use";
    chip.hidden = true;
    offer.appendChild(chip);
    var text = branch.createElement(id + "_line", "span");
    text.textContent = line;
    offer.appendChild(text);
    desc.appendChild(offer);

    var row = ListItem({
        href:        href,
        marker:      _dots(branch, id + "_dots", t.swatches),
        label:       t.label || t.slug,
        description: desc
    });
    return { row: row, chip: chip, wears: wears };
}

/** A strip of the palette's colours: surface, accent, inverted, text — each dot's colour is DATA (RFC 0044). */
function _dots(branch, id, swatches) {
    var strip = branch.createElement(id, "span");
    css.addClass(strip, tp_swatch_dots);
    strip.setAttribute("aria-hidden", "true");
    DOTS.forEach(function (k, j) {
        var v = swatches && swatches[k];
        if (!v) return;
        var dot = branch.createElement(id + "_" + j, "span");
        css.addClass(dot, tp_intro_dot);
        dot.style.setProperty("--tp-dot", v);       // DATA, via setProperty (RFC 0044)
        dot.title = k + " — " + v;
        strip.appendChild(dot);
    });
    return strip;
}

// ---------- the registry, read ----------

/** slug → label, over designs and palettes alike. */
function _labels(reg) {
    var out = {};
    reg.themes.forEach(function (t) { out[t.slug] = t.label || t.slug; });
    reg.palettes.forEach(function (p) { out[p.slug] = p.label || p.slug; });
    return out;
}

/** The palettes a base is offered, own first — the registry's `fits`. */
function _offered(t) {
    var list = t.colours || [], out = [];
    list.forEach(function (w) { if (w.own) out.push(w.palette); });
    list.forEach(function (w) { if (w.fits && !w.own) out.push(w.palette); });
    return out;
}

/** The designs a palette is offered for beside its anchor. */
function _suited(p, reg) {
    var out = [];
    reg.themes.forEach(function (t) {
        if (t.slug === p.anchor) return;
        var way = colourwayOf(t, p.slug);
        if (way && way.fits) out.push(t.slug);
    });
    return out;
}

function _names(slugs, labels) {
    return slugs.map(function (s) { return labels[s] || s; }).join(", ");
}

// ---------- addresses ----------

/**
 * The theme this page wears — what the steward resolves: the address's
 * override, else the stored pick, else null (the caller falls back to the
 * registry's first theme, which is what the page wears then). RFC 0064: the
 * address alone was wrong the moment a bare URL rendered under a stored pick.
 */
function _currentThemeSlug() {
    return PreferenceViewInstance.resolve("theme", null);
}

/**
 * Same URL as the current page, with ?theme=<slug> set — an explicit override,
 * on purpose: a row is a shareable themed view, and the address wins while it
 * names one. Built through href, the one reader of the address.
 */
function _activateUrl(slug) {
    return HrefManagerInstance.withParam("theme", slug);
}

/** A palette's row wears its anchor in it — the design it was made for, in these colours. */
function _wearUrl(p, reg) {
    var anchor = themeBySlug(reg.themes, p.anchor);
    var way = anchor ? colourwayOf(anchor, p.slug) : null;
    return _activateUrl(way ? way.slug : (anchor ? anchor.slug : p.slug));
}
