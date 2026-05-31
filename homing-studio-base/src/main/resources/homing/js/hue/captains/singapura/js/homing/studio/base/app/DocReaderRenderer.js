// =============================================================================
// DocReaderRenderer — shared renderer for DocReader.
//
// renderDocReader({ docId, brand, crumbsAbove }) → Node
//
// Fetches /doc?id=<docId> (UUID — typed Doc reference per RFC 0004), parses
// with marked.js, installs via Range.createContextualFragment (no innerHTML
// write), walks headings to build a TOC sidebar with IntersectionObserver
// scroll-spy.
//
// RFC 0004-ext1: also fetches /doc-refs?id=<docId> (typed Reference list) and
// emits a "References" section beneath the body with stable id="ref:<name>"
// per entry. Markdown citations like [label](#ref:<name>) navigate natively
// via the browser's fragment handling — no DOM walking, no href substitution.
// =============================================================================

var href = HrefManagerInstance;

function _slugify(text) {
    return String(text)
        .toLowerCase()
        .replace(/[^\w\s-]/g, "")
        .replace(/\s+/g, "-")
        .replace(/-+/g, "-")
        .replace(/^-|-$/g, "");
}

// Build the inline onclick string for a TOC anchor. The HTML attribute
// (onclick="...") is what survives Chrome's MHTML export — JS-bound
// listeners (addEventListener / .onclick = fn) get dropped at save time
// because they're runtime state, not serialised DOM. The slug input is
// always _slugify() output, so [a-z0-9-]+ only — no string-escape needed.
function _tocScrollHandler(slug) {
    return "document.getElementById('" + slug
         + "').scrollIntoView({behavior:'smooth'});return false;";
}

function _collectHeadings(rootEl) {
    var out = [];
    function visit(el) {
        if (!el || el.nodeType !== 1) return;
        var tag = el.tagName;
        if (tag === "H1" || tag === "H2" || tag === "H3") out.push(el);
        var children = el.children;
        for (var i = 0; i < children.length; i++) visit(children[i]);
    }
    for (var i = 0; i < rootEl.children.length; i++) visit(rootEl.children[i]);
    return out;
}

function renderDocReader(props) {
    var docId       = props.docId;
    var brand       = props.brand;
    var crumbsAbove = props.crumbsAbove || [];

    var root = document.createElement("div");
    css.addClass(root, st_root);

    // Header is rendered with placeholder text for the breadcrumb; the title
    // is filled in once /doc-refs returns it (server-resolved from the typed Doc).
    var crumbs = [];
    for (var i = 0; i < crumbsAbove.length; i++) crumbs.push(crumbsAbove[i]);
    var leafCrumb = { text: docId ? "Loading…" : "(no document)" };
    crumbs.push(leafCrumb);
    var headerEl = Header({ brand: brand, crumbs: crumbs });
    // data-export-chrome: stripped by exportPageAsHtml when the user picks
    // "content only" — the brand bar + breadcrumb chain is page chrome, not
    // document content.
    headerEl.setAttribute("data-export-chrome", "");
    root.appendChild(headerEl);

    // Export button — floating pill, position:fixed bottom-right. Same shape as
    // the one ComposedWidget installs; mirrored here so plain-markdown DocReader
    // pages (RFC, doctrine, case-study .md sources, etc.) get the same offline-
    // friendly export the ComposedDoc pages already have.
    //
    // data-export-exclude: stripped by exportPageAsHtml before download so the
    // button does not appear in the saved file.
    //
    // The filename slug starts as "doc" and is replaced with a title-derived
    // slug once /doc-refs returns info.title (see the .then handler below).
    var _exportSlug = "doc";

    // Floating export pill — wraps a chrome-include checkbox and the button
    // together so the toggle travels with the action. data-export-exclude on
    // the wrapper strips the whole control from the saved file.
    var exportBar = document.createElement("div");
    exportBar.setAttribute("data-export-exclude", "");
    exportBar.style.cssText = "position:fixed;bottom:24px;right:24px;z-index:200;"
        + "display:flex;align-items:center;gap:8px;font:13px system-ui,sans-serif;"
        + "padding:6px 12px;border:1px solid var(--color-border,rgba(0,0,0,.2));"
        + "border-radius:20px;background:var(--color-surface,#fff);color:inherit;"
        + "box-shadow:0 2px 8px rgba(0,0,0,.15);opacity:0.9;";

    var chromeLabel  = document.createElement("label");
    chromeLabel.style.cssText = "display:flex;align-items:center;gap:4px;cursor:pointer;user-select:none;";
    var chromeChk = document.createElement("input");
    chromeChk.type = "checkbox";
    chromeChk.checked = true; // default: include chrome (current behaviour)
    chromeChk.style.cssText = "margin:0;";
    var chromeTxt = document.createElement("span");
    chromeTxt.textContent = "Include page chrome";
    chromeLabel.appendChild(chromeChk);
    chromeLabel.appendChild(chromeTxt);

    var exportBtn = document.createElement("button");
    exportBtn.textContent = "Export HTML";
    exportBtn.style.cssText = "cursor:pointer;font:inherit;padding:4px 12px;"
        + "border:1px solid var(--color-border,rgba(0,0,0,.2));border-radius:14px;"
        + "background:transparent;color:inherit;";
    exportBtn.addEventListener("click", function () {
        var includeChrome = chromeChk.checked;
        exportBtn.disabled = true;
        exportBtn.textContent = "Exporting…";
        var suffix = includeChrome ? "" : "-content";
        exportPageAsHtml(_exportSlug + suffix + ".html", { includeChrome: includeChrome })
            .then(function () {
                exportBtn.textContent = "Export HTML";
                exportBtn.disabled = false;
            })
            .catch(function (err) {
                exportBtn.textContent = "Export failed";
                exportBtn.disabled = false;
                console.error("exportPageAsHtml failed:", err);
            });
    });

    exportBar.appendChild(chromeLabel);
    exportBar.appendChild(exportBtn);
    document.body.appendChild(exportBar);

    var main = document.createElement("div");
    css.addClass(main, st_main);

    var meta = document.createElement("div");
    css.addClass(meta, st_doc_meta);
    // data-export-content: kept (and lifted out of layout wrappers) by
    // exportPageAsHtml when the user picks "content only".
    meta.setAttribute("data-export-content", "");
    var titleEl = document.createElement("span");
    titleEl.textContent = docId ? "" : "—";
    meta.appendChild(titleEl);
    main.appendChild(meta);

    var layout = document.createElement("div");
    css.addClass(layout, st_layout);

    var sidebar = document.createElement("aside");
    css.addClass(sidebar, st_sidebar);
    // TOC sidebar is page chrome (navigation aid), not document content —
    // exclude from "content only" exports.
    sidebar.setAttribute("data-export-chrome", "");
    var sidebarTitle = document.createElement("div");
    css.addClass(sidebarTitle, st_sidebar_title);
    sidebarTitle.textContent = "In this document";
    var tocEl = document.createElement("nav");
    css.addClass(tocEl, st_toc);
    sidebar.appendChild(sidebarTitle);
    sidebar.appendChild(tocEl);

    var bodyEl = document.createElement("article");
    css.addClass(bodyEl, st_doc);
    bodyEl.setAttribute("data-export-content", "");
    var loading = document.createElement("div");
    css.addClass(loading, st_loading);
    loading.textContent = "Loading…";
    bodyEl.appendChild(loading);

    layout.appendChild(sidebar);
    layout.appendChild(bodyEl);
    main.appendChild(layout);

    // RFC 0004-ext1: References section appended after the layout, populated
    // from /doc-refs?id=<uuid>. Hidden (omitted) when the Doc declares zero
    // references. Per-Reference render dispatches on the JSON `kind` field.
    var refsEl = document.createElement("section");
    css.addClass(refsEl, st_section);
    refsEl.style.cssText = "display:none;";
    refsEl.setAttribute("data-export-content", "");
    main.appendChild(refsEl);

    root.appendChild(main);

    if (!docId) {
        var errMsg = document.createElement("div");
        css.addClass(errMsg, st_error);
        errMsg.appendChild(document.createTextNode("No document specified. Use "));
        var errCode = document.createElement("code"); errCode.textContent = "?doc=<uuid>";
        errMsg.appendChild(errCode);
        errMsg.appendChild(document.createTextNode("."));
        bodyEl.replaceChildren(errMsg);
        return root;
    }

    fetch("/doc?id=" + encodeURIComponent(docId))
        .then(function(r) {
            if (!r.ok) throw new Error("HTTP " + r.status);
            return r.text();
        })
        .then(function(md) { _renderDoc(md, bodyEl, tocEl); })
        .catch(function(err) {
            var errEl = document.createElement("div");
            css.addClass(errEl, st_error);
            errEl.appendChild(document.createTextNode("Failed to load "));
            var c = document.createElement("code"); c.textContent = docId;
            errEl.appendChild(c);
            errEl.appendChild(document.createTextNode(": " + err.message));
            bodyEl.replaceChildren(errEl);
        });

    fetch("/doc-refs?id=" + encodeURIComponent(docId))
        .then(function(r) {
            if (!r.ok) throw new Error("HTTP " + r.status);
            return r.json();
        })
        .then(function(info) {
            // Server-resolved Doc metadata: title (friendly name) + summary + category
            // + breadcrumbs[] + references[]. Update the breadcrumb + meta line with
            // the title; rebuild the breadcrumb chain (RFC 0005-ext2) from
            // info.breadcrumbs when the server provides it; render the References
            // section from info.references.
            if (info && info.title) {
                leafCrumb.text = info.title;
                // Update the export filename to a title-derived slug now that
                // the title is known. Same _slugify rules ComposedWidget uses.
                _exportSlug = _slugify(info.title) || "doc";
                // RFC 0005-ext2: when the server returned a typed breadcrumb chain
                // (catalogue root → ... → containing catalogue), use it instead of
                // whatever crumbsAbove the caller supplied. The leaf crumb (this
                // doc's title) is always appended last as a non-link.
                if (info.breadcrumbs && info.breadcrumbs.length > 0) {
                    crumbs = info.breadcrumbs.slice();
                    crumbs.push(leafCrumb);
                }
                // Re-render header with the updated chain + leaf crumb text.
                var newHeader = Header({ brand: brand, crumbs: crumbs });
                root.replaceChild(newHeader, headerEl);
                headerEl = newHeader;
                titleEl.textContent = info.title;
                // Browser tab title — `<doc> · <brand>`. Replaces the static
                // default served by AppHtmlGetAction (which doesn't know the
                // downstream brand or the doc subject at HTML-render time).
                document.title = info.title
                    + (brand && brand.label ? " · " + brand.label : "");
                if (info.category) {
                    var catSpan = document.createElement("span");
                    catSpan.style.cssText = "margin-left:12px; font-size:11px; color: var(--st-gray-mid); text-transform:uppercase; letter-spacing:0.05em;";
                    catSpan.textContent = info.category;
                    meta.appendChild(catSpan);
                }
            }
            _renderReferences(info && info.references ? info.references : [], refsEl);
        })
        .catch(function() { /* Metadata + references are optional; silently omit on failure. */ });

    return root;
}

// =============================================================================
// _renderReferences — emits the References section for RFC 0004-ext1.
// Per-subtype dispatch on the JSON `kind` field; each entry becomes a card
// with id="ref:<name>" so markdown anchor citations land on the right element.
// =============================================================================
function _renderReferences(refs, container) {
    if (!refs || refs.length === 0) return;
    container.style.cssText = "";

    var heading = document.createElement("h2");
    css.addClass(heading, st_section_title);
    heading.textContent = "References";
    container.appendChild(heading);

    for (var i = 0; i < refs.length; i++) {
        var r = refs[i];
        var card = document.createElement("div");
        css.addClass(card, st_card);
        card.id = "ref:" + r.name;

        var title = document.createElement("h3");
        css.addClass(title, st_card_title);

        var summary = document.createElement("p");
        css.addClass(summary, st_card_summary);

        if (r.kind === "doc") {
            var titleLink = document.createElement("a");
            css.addClass(titleLink, st_card_link);
            href.set(titleLink, "/app?app=doc-reader&doc=" + encodeURIComponent(r.uuid));
            titleLink.textContent = r.title;
            title.appendChild(titleLink);
            summary.textContent = r.summary || "";
        } else if (r.kind === "external") {
            var extLink = document.createElement("a");
            css.addClass(extLink, st_card_link);
            href.set(extLink, r.url);
            extLink.setAttribute("target", "_blank");
            extLink.setAttribute("rel", "noopener");
            extLink.textContent = r.label || r.url;
            title.appendChild(extLink);
            summary.textContent = r.description || "";
        } else if (r.kind === "image") {
            // Image rendering deferred per RFC 0004-ext1 §4.6 (needs /asset endpoint).
            // For v1, surface alt + caption + classpath path as text-only placeholder.
            title.textContent = r.alt || r.name;
            summary.textContent = (r.caption ? r.caption + " — " : "")
                                + "(image at " + r.resourcePath + ")";
        } else {
            // Unknown kind — render the raw JSON for visibility.
            title.textContent = "Unknown reference kind: " + r.kind;
            summary.textContent = JSON.stringify(r);
        }

        card.appendChild(title);
        card.appendChild(summary);
        container.appendChild(card);
    }
}

function _renderDoc(md, bodyEl, tocEl) {
    if (marked && marked.use) marked.use({ gfm: true, breaks: false });

    var range = document.createRange();
    range.selectNodeContents(bodyEl);
    var fragment = range.createContextualFragment(marked.parse(md));
    bodyEl.replaceChildren(fragment);

    var headings = _collectHeadings(bodyEl);
    var slugs = {};
    var tocItems = [];

    for (var hi = 0; hi < headings.length; hi++) {
        var h = headings[hi];
        var text = h.textContent || "";
        var base = _slugify(text) || "section";
        var slug = base;
        var n = 2;
        while (slugs[slug]) { slug = base + "-" + n; n++; }
        slugs[slug] = true;
        h.id = slug;
        tocItems.push({ level: h.tagName.toLowerCase(), text: text, slug: slug });
    }

    if (tocItems.length === 0) {
        var empty = document.createElement("div");
        css.addClass(empty, st_loading);
        empty.style.cssText = "padding:8px 12px;";
        empty.textContent = "No headings.";
        tocEl.replaceChildren(empty);
        return;
    }

    var tocLinks = [];
    for (var i = 0; i < tocItems.length; i++) {
        var item = tocItems[i];
        var levelCls = item.level === "h1" ? st_toc_h1
                      : item.level === "h2" ? st_toc_h2
                      : st_toc_h3;
        var a = document.createElement("a");
        css.addClass(a, st_toc_item);
        css.addClass(a, levelCls);
        href.set(a, "#" + item.slug);
        a.setAttribute("data-slug", item.slug);
        // MHTML-survival: an inline onclick attribute is preserved in the
        // exported file; a JS-bound listener wouldn't be. Live mode still
        // benefits — the handler short-circuits Chrome's hash-rewriting
        // navigation and just scrolls.
        a.setAttribute("onclick", _tocScrollHandler(item.slug));
        a.textContent = item.text;
        tocLinks.push(a);
    }
    tocEl.replaceChildren.apply(tocEl, tocLinks);

    if (typeof IntersectionObserver === "function") {
        var bySlug = {};
        for (var li = 0; li < tocLinks.length; li++) {
            bySlug[tocLinks[li].getAttribute("data-slug")] = tocLinks[li];
        }
        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(e) {
                if (e.isIntersecting) {
                    for (var li = 0; li < tocLinks.length; li++) {
                        css.removeClass(tocLinks[li], st_toc_active);
                    }
                    var link = bySlug[e.target.id];
                    if (link) css.addClass(link, st_toc_active);
                }
            });
        }, { rootMargin: "0px 0px -70% 0px", threshold: 0 });
        for (var hi2 = 0; hi2 < headings.length; hi2++) observer.observe(headings[hi2]);
    }
}

