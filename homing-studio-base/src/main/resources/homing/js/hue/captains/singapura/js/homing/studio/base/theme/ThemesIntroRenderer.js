// =============================================================================
// ThemesIntroRenderer — single intro page listing every registered theme.
//
// renderThemesIntro() → Node
//
// Fetches /themes, renders a sticky-header page with an intro paragraph + one
// Listing row per theme. Each row has palette swatches and an "Activate" link
// that points to the same URL with ?theme=<slug>; href.set's session-key
// propagation keeps `locale` (and any future propagated key) sticky on click.
// =============================================================================

function renderThemesIntro() {
    var root = document.createElement("div");
    css.addClass(root, st_root);

    // Loading placeholder while the fetch is in flight.
    var loading = document.createElement("div");
    css.addClass(loading, st_loading);
    loading.textContent = "Loading…";
    loading.style.cssText = "padding:24px;";
    root.appendChild(loading);

    Promise.all([
        fetch("/themes").then(function (r) {
            if (!r.ok) throw new Error("/themes HTTP " + r.status);
            return r.json();
        }),
        fetch("/brand").then(function (r) {
            if (!r.ok) throw new Error("/brand HTTP " + r.status);
            return r.json();
        })
    ])
        .then(function (results) { _draw(root, results[0], results[1]); })
        .catch(function (err) {
            var e = document.createElement("div");
            css.addClass(e, st_error);
            e.textContent = "Failed to load themes: " + err.message;
            e.style.cssText = "padding:24px;";
            root.replaceChildren(e);
        });

    return root;
}

function _draw(root, data, brand) {
    var children = [];

    children.push(Header({
        brand:  { href: brand.homeUrl, label: brand.label, logo: brand.logo },
        crumbs: [ { text: brand.label, href: brand.homeUrl }, { text: "Themes" } ]
    }));

    var main = document.createElement("div");
    css.addClass(main, st_main);

    var kicker = document.createElement("div");
    css.addClass(kicker, st_kicker);
    kicker.textContent = "Themes";
    main.appendChild(kicker);

    var title = document.createElement("h1");
    css.addClass(title, st_title);
    title.textContent = "Palette previews";
    main.appendChild(title);

    var subtitle = document.createElement("p");
    css.addClass(subtitle, st_subtitle);
    subtitle.textContent =
        "Every theme registered in this studio. Each shares the same StudioStyles "
        + "layout — only the primitive palette differs. Pick a theme to activate it; "
        + "your choice sticks across navigation.";
    main.appendChild(subtitle);

    var themes = (data && data.themes) || [];
    var currentSlug = _currentThemeSlug();
    var rows = themes.map(function (t) {
        return ListItem({
            href:        _activateUrl(t.slug),
            marker:      _swatchStrip(t.palette),
            label:       t.label + (t.slug === currentSlug ? "  (active)" : ""),
            description: "?theme=" + t.slug,
            met:         t.slug === currentSlug
        });
    });
    main.appendChild(Listing({ title: "Available themes", children: rows }));

    children.push(main);
    root.replaceChildren.apply(root, children);
}

// ---------- helpers ----------

function _currentThemeSlug() {
    try {
        return new URLSearchParams(window.location.search).get("theme");
    } catch (_) {
        return null;
    }
}

/** Same URL as the current page, with ?theme=<slug> set. href.set's session-
 *  key propagation handles `locale` and any other propagated key. */
function _activateUrl(slug) {
    var params;
    try {
        params = new URLSearchParams(window.location.search);
    } catch (_) {
        params = new URLSearchParams();
    }
    params.set("theme", slug);
    return window.location.pathname + "?" + params.toString();
}

/** A horizontal strip of fixed-size colored boxes — one per palette key.
 *  Returns a Node (used as ListItem.marker so ListItem renders it verbatim). */
function _swatchStrip(palette) {
    var box = document.createElement("div");
    box.style.cssText =
        "display:flex; gap:0; border-radius:3px; overflow:hidden; "
        + "border:1px solid var(--color-border); flex-shrink:0;";

    var keys = ["surface", "surface-inverted", "accent", "text-link",
                "text-primary", "text-muted", "border-emphasis"];
    for (var i = 0; i < keys.length; i++) {
        var v = palette && palette[keys[i]];
        if (!v) continue;
        var sw = document.createElement("div");
        sw.style.cssText = "width:18px; height:36px; background:" + v + ";";
        sw.title = keys[i] + " — " + v;
        box.appendChild(sw);
    }
    return box;
}
