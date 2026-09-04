// =============================================================================
// CatalogueHostRenderer — shared renderer for CatalogueAppHost (RFC 0005).
//
// renderCatalogueHost({ catalogueId, brandFallback }) → Node
//
// Fetches /catalogue?id=<catalogueId> for the fully-resolved JSON payload
// (name + summary + brand + breadcrumbs + the catalogue subtree),
//
// Renderer does no URL construction — the server pre-resolves every URL via
// the registry. The tree it draws is the catalogue subtree the server already
// resolved, so there is no second opinion about what a catalogue contains.
// =============================================================================

var href = HrefManagerInstance;

// The listing branch's owner, tracked by WeakRef. Module-scoped and frozen so
// it is never collected while the page lives — an MPA page has no `this` to
// hand in, and a function-local owner would be collectible.
const _catalogueListingOwner = Object.freeze({ toString: () => "catalogueListing" });

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
    // (name, summary, brand, breadcrumbs, tree).
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

    // The listing IS the tree (RFC 0053) — the same subtree the boot gate
    // checks, drawn by the substrate own renderer, so a catalogue costs no
    // bespoke listing code. The tile grid it replaced is gone: the detail card
    // does what a card did, and one derivation of a catalogue is enough.
    _mountListingTree(main, data);

    children.push(main);

    root.replaceChildren.apply(root, children);
}

// The tree that replaced the tile grid. Every row is minted through a branch, so
// the listing tears down with the page rather than leaking into the singleton.
//
// It opens showing exactly what the cards showed — the immediate children —
// with the rest of the subtree one click away. That is the whole upgrade: the
// card view could only ever show one level, because a tile is not a tree.
function _mountListingTree(parent, data) {
    var branch = domOpsParty.createBranch("catalogueListing");
    branch.activate(_catalogueListingOwner);

    // A row's namePath is relative to THIS catalogue, so the authentic URL is the
    // server-computed base with it appended. The renderer still constructs no
    // URLs of its own - it joins two strings the server decided.
    var base = data.treeBase || "";
    var urlFor = function (namePath) {
        return namePath ? base + "/" + namePath : base;
    };

    // Nav LEFT, content right - the same MasterDetail the theme picker uses, so
    // the two surfaces cannot drift again. This split used to put the card first
    // and divide by the golden ratio, which was a reasonable answer to not
    // knowing how wide a tree wanted to be. md_nav sizes to its content, so the
    // question no longer arises and the ordinary pattern is available: a table of
    // contents beside what it points at.
    var pair = mountMasterDetail({
        branch:      branch,
        host:        parent,
        data:        data.tree,
        // 0 = exactly what the cards showed: the immediate children, nothing
        // more. The rest of the subtree is one click away, which is the whole
        // difference between a tile and a tree.
        expandDepth: 0,
        // The badge rides in the row; the summary belongs to the card.
        showBadge:   true,
        // The page IS this catalogue, so its own row would repeat the title.
        showRoot:    false,
        hrefForPath: function (path, namePath) { return urlFor(namePath); },
        // The card IS the selected row, drawn larger - the same Card the grid
        // drew, so bringing the grid back stays a question of how many are
        // rendered rather than of what a card is.
        onSelect:    function (bodyEl, sel) {
            bodyEl.replaceChildren(Card({
                href:    urlFor(sel.namePath),
                title:   sel.label || "",
                summary: sel.summary || "",
                badge:   sel.category || "",
                link:    sel.hasChildren ? "Browse →" : "Open →"
            }));
        },
        onActivate:  function (sel) { HrefManagerInstance.navigate(urlFor(sel.namePath)); }
    });

    var hint = branch.createElement("hint", "div");
    css.addClass(hint, st_subtitle);
    hint.textContent = "Select an entry to see it here.";
    pair.bodyEl.appendChild(hint);

    // The page owns WHEN keys flow; the renderer owns what they mean.
    document.addEventListener("keydown", function (ev) {
        if (pair.renderer && pair.renderer.handleKeydown(ev)) ev.preventDefault();
    });
}
