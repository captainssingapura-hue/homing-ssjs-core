// =============================================================================
// CatalogueHostRenderer — shared renderer for CatalogueAppHost (RFC 0005).
//
// renderCatalogueHost({ catalogueId, brandFallback }) → Node
//
// Fetches /catalogue?id=<catalogueId> for the fully-resolved JSON payload
// (name + summary + brand + breadcrumbs + entries with pre-resolved URLs),
// then emits the page DOM. Per-entry dispatch on JSON `kind` discriminator
// ("doc" | "catalogue" | "app").
//
// Renderer does no URL construction — the server pre-resolves every URL via
// the registry. Renderer does no DOM walking — entries are flat structural
// data, rendered top-to-bottom.
// =============================================================================

var href = HrefManagerInstance;

function renderCatalogueHost(props) {
    var catalogueId   = props.catalogueId;
    var context       = props.context || "";
    var brandFallback = props.brandFallback || { label: "studio", homeUrl: "/" };

    var root = document.createElement("div");
    css.addClass(root, st_root);

    // Loading placeholder while the fetch is in flight.
    var loading = document.createElement("div");
    css.addClass(loading, st_loading);
    loading.textContent = "Loading…";
    loading.style.cssText = "padding:24px;";
    root.appendChild(loading);

    if (!catalogueId) {
        var errMsg = document.createElement("div");
        css.addClass(errMsg, st_error);
        errMsg.appendChild(document.createTextNode("No catalogue specified. Use "));
        var errCode = document.createElement("code"); errCode.textContent = "?id=<class-fqn>";
        errMsg.appendChild(errCode);
        errMsg.appendChild(document.createTextNode("."));
        root.replaceChildren(errMsg);
        return root;
    }

    // RFC 0016: allow callers (e.g. TreeAppHost) to supply their own endpoint URL.
    // When apiUrl is set, the catalogue endpoint defaults are bypassed entirely
    // and the renderer fetches whatever URL the caller built. The shape of the
    // JSON response is expected to match the CatalogueGetAction contract
    // (name, summary, brand, breadcrumbs, entries[]).
    var url = props.apiUrl
        ? props.apiUrl
        : ("/catalogue?id=" + encodeURIComponent(catalogueId)
           + (context ? "&context=" + encodeURIComponent(context) : ""));
    fetch(url)
        .then(function(r) {
            if (!r.ok) throw new Error("HTTP " + r.status);
            return r.json();
        })
        .then(function(data) { _renderCataloguePage(root, data, brandFallback, props.crumbs); })
        .catch(function(err) {
            var errEl = document.createElement("div");
            css.addClass(errEl, st_error);
            errEl.appendChild(document.createTextNode("Failed to load catalogue "));
            var c = document.createElement("code"); c.textContent = catalogueId;
            errEl.appendChild(c);
            errEl.appendChild(document.createTextNode(": " + err.message));
            root.replaceChildren(errEl);
        });

    return root;
}

function _renderCataloguePage(root, data, brandFallback, stampedCrumbs) {
    var brand = data.brand || brandFallback;

    // Browser tab title — `<catalogue> · <brand>`. Same pattern as DocReader
    // and PlanHost. Replaces the static default served by AppHtmlGetAction.
    document.title = data.name + (brand && brand.label ? " · " + brand.label : "");

    // RFC 0051 — the server's stamp, taken whole. This used to rebuild the
    // trail from data.breadcrumbs, which made this page the last one whose
    // breadcrumb was resolved after paint, and made the app the author of a
    // statement about where it sits — a statement only the catalogue can make.
    // The two agree byte for byte today, so this is the same trail arriving
    // earlier and from the one place entitled to say it.
    //
    // No stamp means no CatalogueRegistry on this studio, and then the payload
    // has no chain either: both come from the same registry. So the fallback
    // is not a second trail, it is the absence of one — brand only.
    var crumbs = (stampedCrumbs && stampedCrumbs.length) ? stampedCrumbs.slice() : [];

    var children = [];

    children.push(Header({
        brand:  { href: brand.homeUrl, label: brand.label, logo: brand.logo },
        crumbs: crumbs
    }));

    var main = document.createElement("div");
    css.addClass(main, st_main);

    var title = document.createElement("h1");
    css.addClass(title, st_title);
    title.textContent = data.name;
    main.appendChild(title);

    if (data.summary) {
        var subtitle = document.createElement("p");
        css.addClass(subtitle, st_subtitle);
        subtitle.textContent = data.summary;
        main.appendChild(subtitle);
    }

    // Entries — flat list, per-kind dispatch.
    var tiles = (data.entries || []).map(_renderEntry);
    main.appendChild(Section({ title: "", children: tiles }));

    children.push(main);

    root.replaceChildren.apply(root, children);
}

function _renderEntry(entry) {
    if (entry.kind === "doc") {
        return Card({
            href:    entry.url,
            title:   entry.title,
            summary: entry.summary,
            badge:   entry.category || null,
            link:    "Open →"
        });
    }
    if (entry.kind === "catalogue") {
        // Renders as a Card (uniform with Doc/Plan entries) — server emits
        // a fixed "CATALOGUE" badge so a mixed-kind listing reads at a glance.
        return Card({
            href:    entry.url,
            title:   entry.name,
            summary: entry.summary,
            badge:   entry.category || null,
            link:    "Open →"
        });
    }
    if (entry.kind === "app") {
        // Renders as a Card (uniform with Doc/Plan/Catalogue entries).
        return Card({
            href:    entry.url,
            title:   entry.name,
            summary: entry.summary,
            badge:   entry.category || null,
            link:    "Open →"
        });
    }
    if (entry.kind === "plan") {
        // RFC 0005-ext1: Plan entry. Renders as a Card (same shape as Doc
        // entries) so a catalogue listing reads uniformly. `category` is
        // server-resolved from plan.kicker() (e.g. "RFC 0001").
        return Card({
            href:    entry.url,
            title:   entry.name,
            summary: entry.summary,
            badge:   entry.category || null,
            link:    "Open →"
        });
    }
    if (entry.kind === "svg") {
        // RFC 0015 Phase 5: SVG is a typed Doc kind. Routed through the
        // standard Card with no custom rendering; the framework's chrome
        // (border, hover, audio cues per RFC 0007) all bind via the st-card
        // class. The Doc's URL points at the registered SvgViewer; clicking
        // opens the full-page SVG view via the polymorphic doc viewer.
        return Card({
            href:    entry.url,
            title:   entry.name,
            summary: entry.summary || "",
            badge:   entry.category || null,
            link:    "View →"
        });
    }
    if (entry.kind === "table") {
        // RFC 0020: TableDoc — typed-table Doc. Routed through the standard
        // Card; URL points at the registered TableViewer.
        return Card({
            href:    entry.url,
            title:   entry.name,
            summary: entry.summary || "",
            badge:   entry.category || null,
            link:    "View →"
        });
    }
    if (entry.kind === "image") {
        // RFC 0020: ImageDoc — Raw-tier raster asset. Routed through the
        // standard Card; URL points at the registered ImageViewer.
        return Card({
            href:    entry.url,
            title:   entry.name,
            summary: entry.summary || "",
            badge:   entry.category || null,
            link:    "View →"
        });
    }
    if (entry.kind === "composed") {
        // RFC 0019: ComposedDoc — typed-segment doc (markdown + SVG + ...).
        // Routed through the standard Card; the URL points at the registered
        // ComposedViewer, which fetches the JSON payload and dispatches per
        // segment kind on the client side.
        return Card({
            href:    entry.url,
            title:   entry.name,
            summary: entry.summary || "",
            badge:   entry.category || null,
            link:    "Open →"
        });
    }
    if (entry.kind === "illustration") {
        // Specialized in-place decoration — markdown rendered inline,
        // visually distinct from the tile grid. Not clickable.
        var ill = document.createElement("section");
        ill.style.cssText =
            "padding:20px 24px;margin:8px 0 24px;border-left:4px solid var(--st-accent,#cfa64a);"
          + "background:rgba(207,166,74,0.07);border-radius:6px;line-height:1.55;font-size:15px;";
        try {
            if (typeof marked !== "undefined" && marked && marked.parse) {
                var range = document.createRange();
                range.selectNodeContents(ill);
                ill.appendChild(range.createContextualFragment(marked.parse(entry.body || "")));
            } else {
                ill.textContent = entry.body || "";
            }
        } catch (e) {
            ill.textContent = entry.body || "";
        }
        return ill;
    }
    if (entry.kind === "studio") {
        // RFC 0011: a typed re-attachment of a source L0 catalogue. The
        // server already emitted the proxy's icon prefixed into entry.name,
        // and the URL points at the wrapped source L0's page. Renders as
        // a Card (uniform with the other kinds) — badge defaults to "STUDIO".
        return Card({
            href:    entry.url,
            title:   entry.name,
            summary: entry.summary,
            badge:   entry.category || null,
            link:    "Enter →"
        });
    }
    // Unknown kind — render as a plain text fallback so the page doesn't break.
    var fallback = document.createElement("div");
    css.addClass(fallback, st_error);
    fallback.textContent = "Unknown entry kind: " + entry.kind;
    return fallback;
}
