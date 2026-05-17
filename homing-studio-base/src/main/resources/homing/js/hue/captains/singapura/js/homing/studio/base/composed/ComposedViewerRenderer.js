// =============================================================================
// ComposedViewerRenderer — shared renderer for ComposedViewer (RFC 0019).
//
// renderComposed({ docId, main }) → void
//
// Fetches /doc?id=<docId> (composed-doc JSON: {title, summary, category,
// toc[], segments[]}). Renders a 2-column layout (TOC sidebar + body),
// then dispatches per segment kind:
//   - "markdown" : marked.parse(body); H1-H4 anchors aligned to TOC
//                  entries (seg-N-hM matching the server's enumeration)
//   - "svg"      : fetch(svgUrl) then inline; wrap with caption
//
// No client-side TOC derivation: the sidebar is rendered straight from
// the server's toc[] array (server-derived per RFC 0019 D7).
// =============================================================================

var href = HrefManagerInstance;

function _tocScrollHandler(anchor) {
    // Inline onclick — survives Chrome's MHTML export (addEventListener
    // listeners are dropped at save time).
    return "var el=document.getElementById('" + anchor
         + "');if(el){el.scrollIntoView({behavior:'smooth'});}return false;";
}

function _renderTocSidebar(toc, tocEl) {
    if (!toc || toc.length === 0) {
        var empty = document.createElement("div");
        css.addClass(empty, st_loading);
        empty.style.cssText = "padding:8px 12px;";
        empty.textContent = "No sections.";
        tocEl.replaceChildren(empty);
        return;
    }
    var links = [];
    for (var i = 0; i < toc.length; i++) {
        var entry = toc[i];
        var levelCls = entry.level === 1 ? st_toc_h1
                     : entry.level === 2 ? st_toc_h2
                     : st_toc_h3;
        var a = document.createElement("a");
        css.addClass(a, st_toc_item);
        css.addClass(a, levelCls);
        href.set(a, "#" + entry.anchor);
        a.setAttribute("data-anchor", entry.anchor);
        a.setAttribute("onclick", _tocScrollHandler(entry.anchor));
        a.textContent = entry.text;
        links.push(a);
    }
    tocEl.replaceChildren.apply(tocEl, links);
}

// =============================================================================
// RFC 0018 / Phase 4 — TextSegment renderer. Pure data walk over the
// server-parsed AST shipped in `seg.blocks` / inline `inlines` arrays.
// No markdown library — the grammar (paragraphs + lists + quotes +
// inline emphasis/code/refs) is small enough that the renderer is
// ~30 lines.
// =============================================================================

function _renderInlines(inlines, hostEl) {
    if (!inlines) return;
    for (var i = 0; i < inlines.length; i++) {
        var n = inlines[i];
        if (n.kind === "text") {
            hostEl.appendChild(document.createTextNode(n.text));
        } else if (n.kind === "code") {
            var codeEl = document.createElement("code");
            codeEl.textContent = n.text;
            hostEl.appendChild(codeEl);
        } else if (n.kind === "b") {
            var bEl = document.createElement("strong");
            _renderInlines(n.inlines, bEl);
            hostEl.appendChild(bEl);
        } else if (n.kind === "i") {
            var iEl = document.createElement("em");
            _renderInlines(n.inlines, iEl);
            hostEl.appendChild(iEl);
        } else if (n.kind === "ref") {
            // Native anchor — same href shape as a markdown [label](#ref:name)
            // would compile to; the page-level handler scrolls to id="ref:<name>".
            var aEl = document.createElement("a");
            href.set(aEl, "#ref:" + n.anchor);
            aEl.textContent = n.label;
            hostEl.appendChild(aEl);
        } else {
            var unkEl = document.createElement("span");
            css.addClass(unkEl, st_error);
            unkEl.textContent = "Unknown inline kind: " + n.kind;
            hostEl.appendChild(unkEl);
        }
    }
}

function _renderTextSegment(seg, bodyEl) {
    var section = document.createElement("section");
    css.addClass(section, st_section);
    section.id = seg.anchor;

    if (seg.title) {
        var h = document.createElement("h2");
        css.addClass(h, st_section_title);
        h.textContent = seg.title;
        section.appendChild(h);
    }

    var article = document.createElement("article");
    css.addClass(article, st_doc);

    var blocks = seg.blocks || [];
    for (var bi = 0; bi < blocks.length; bi++) {
        var b = blocks[bi];
        if (b.kind === "p") {
            var p = document.createElement("p");
            _renderInlines(b.inlines, p);
            article.appendChild(p);
        } else if (b.kind === "ul") {
            var ul = document.createElement("ul");
            for (var li = 0; li < b.items.length; li++) {
                var liEl = document.createElement("li");
                _renderInlines(b.items[li], liEl);
                ul.appendChild(liEl);
            }
            article.appendChild(ul);
        } else if (b.kind === "ol") {
            var ol = document.createElement("ol");
            for (var oli = 0; oli < b.items.length; oli++) {
                var oliEl = document.createElement("li");
                _renderInlines(b.items[oli], oliEl);
                ol.appendChild(oliEl);
            }
            article.appendChild(ol);
        } else if (b.kind === "quote") {
            var bq = document.createElement("blockquote");
            _renderInlines(b.inlines, bq);
            article.appendChild(bq);
        } else {
            var unkB = document.createElement("div");
            css.addClass(unkB, st_error);
            unkB.textContent = "Unknown block kind: " + b.kind;
            article.appendChild(unkB);
        }
    }

    section.appendChild(article);
    bodyEl.appendChild(section);
}

function _renderMarkdownSegment(seg, bodyEl) {
    if (marked && marked.use) marked.use({ gfm: true, breaks: false });

    var section = document.createElement("section");
    css.addClass(section, st_section);
    section.id = seg.anchor;

    if (seg.title) {
        var h = document.createElement("h2");
        css.addClass(h, st_section_title);
        h.textContent = seg.title;
        section.appendChild(h);
    }

    var article = document.createElement("article");
    css.addClass(article, st_doc);
    var range = document.createRange();
    range.selectNodeContents(article);
    var fragment = range.createContextualFragment(marked.parse(seg.body || ""));
    article.appendChild(fragment);

    // Assign anchors to H1-H4 headings to match server TOC's seg-N-hM pattern.
    var headingIdx = 0;
    var walker = document.createTreeWalker(article, NodeFilter.SHOW_ELEMENT, null);
    var node;
    while ((node = walker.nextNode())) {
        var tag = node.tagName;
        if (tag === "H1" || tag === "H2" || tag === "H3" || tag === "H4") {
            node.id = seg.anchor + "-h" + headingIdx;
            headingIdx++;
        }
    }

    section.appendChild(article);
    bodyEl.appendChild(section);
}

function _renderTableSegment(seg, bodyEl) {
    var section = document.createElement("section");
    css.addClass(section, st_section);
    section.id = seg.anchor;

    var figure = document.createElement("figure");
    figure.style.cssText = "margin:24px 0;";

    var host = document.createElement("div");
    figure.appendChild(host);

    if (seg.caption) {
        var caption = document.createElement("figcaption");
        caption.style.cssText = "margin-top:8px;font-size:13px;color:var(--st-gray-mid,#666);text-align:center;";
        caption.textContent = seg.caption;
        figure.appendChild(caption);
    }

    section.appendChild(figure);
    bodyEl.appendChild(section);

    // Delegate to the shared TableViewer renderer — same code path as the
    // standalone TableViewer; the segment just supplies a different host.
    renderTable({ docId: seg.tableDocId, host: host });
}

function _renderImageSegment(seg, bodyEl) {
    var section = document.createElement("section");
    css.addClass(section, st_section);
    section.id = seg.anchor;

    var host = document.createElement("div");
    host.style.cssText = "display:flex;justify-content:center;";
    section.appendChild(host);

    if (seg.caption) {
        // The shared ImageViewer renderer already includes its own
        // <figcaption>; for segments we let the segment caption override
        // it by setting the ImageDoc's published caption — but a small
        // belt-and-braces overlay caption here makes the segment-level
        // override visible even when the underlying ImageDoc carries
        // none. Skip when the doc's caption is already going to render.
    }

    bodyEl.appendChild(section);

    // Delegate to the shared ImageViewer renderer.
    renderImage({ docId: seg.imageDocId, host: host });
}

function _renderSvgSegment(seg, bodyEl) {
    var section = document.createElement("section");
    css.addClass(section, st_section);
    section.id = seg.anchor;

    // Fill the main column width (st_main = 1280px max minus padding ≈ 1216px),
    // with a soft cap so on very wide viewports the figure doesn't go absurd.
    // Side padding keeps the SVG from kissing the prose-column boundary.
    var figure = document.createElement("figure");
    figure.style.cssText = "margin:24px 0;padding:0 16px;"
                         + "display:flex;flex-direction:column;align-items:center;";

    var host = document.createElement("div");
    host.style.cssText = "width:100%;max-width:1100px;display:flex;justify-content:center;";
    var loading = document.createElement("div");
    css.addClass(loading, st_loading);
    loading.textContent = "Loading SVG…";
    host.appendChild(loading);
    figure.appendChild(host);

    if (seg.caption) {
        var caption = document.createElement("figcaption");
        caption.style.cssText = "margin-top:8px;font-size:13px;color:var(--st-gray-mid,#666);text-align:center;";
        caption.textContent = seg.caption;
        figure.appendChild(caption);
    }

    section.appendChild(figure);
    bodyEl.appendChild(section);

    fetch(seg.svgUrl)
        .then(function(r) {
            if (!r.ok) throw new Error("HTTP " + r.status);
            return r.text();
        })
        .then(function(svg) {
            var range = document.createRange();
            range.selectNodeContents(host);
            host.replaceChildren(range.createContextualFragment(svg));
            var svgEl = host.querySelector("svg");
            if (svgEl) {
                // Fill the host (which carries the max-width); let aspect
                // ratio (viewBox) drive the height.
                svgEl.style.width = "100%";
                svgEl.style.height = "auto";
            }
        })
        .catch(function(err) {
            var errEl = document.createElement("div");
            css.addClass(errEl, st_error);
            errEl.textContent = "Failed to load SVG: " + err.message;
            host.replaceChildren(errEl);
        });
}

function renderComposed(props) {
    var docId = props.docId;
    var main  = props.main;

    if (!docId) {
        var errMsg = document.createElement("div");
        css.addClass(errMsg, st_error);
        errMsg.textContent = "No document id. Use ?id=<uuid>.";
        main.replaceChildren(errMsg);
        return;
    }

    // 2-column layout: sidebar (TOC) + body (segments).
    var layout = document.createElement("div");
    css.addClass(layout, st_layout);

    var sidebar = document.createElement("aside");
    css.addClass(sidebar, st_sidebar);
    var sidebarTitle = document.createElement("div");
    css.addClass(sidebarTitle, st_sidebar_title);
    sidebarTitle.textContent = "In this document";
    var tocEl = document.createElement("nav");
    css.addClass(tocEl, st_toc);
    sidebar.appendChild(sidebarTitle);
    sidebar.appendChild(tocEl);

    var bodyEl = document.createElement("div");
    var loading = document.createElement("div");
    css.addClass(loading, st_loading);
    loading.textContent = "Loading…";
    bodyEl.appendChild(loading);

    layout.appendChild(sidebar);
    layout.appendChild(bodyEl);
    main.replaceChildren(layout);

    fetch("/doc?id=" + encodeURIComponent(docId))
        .then(function(r) {
            if (!r.ok) throw new Error("HTTP " + r.status);
            return r.json();
        })
        .then(function(payload) {
            bodyEl.replaceChildren();

            // Meta line at the top of the body.
            if (payload.title || payload.category) {
                var meta = document.createElement("div");
                css.addClass(meta, st_doc_meta);
                var titleSpan = document.createElement("span");
                titleSpan.textContent = payload.title || "";
                titleSpan.style.cssText = "font-size:24px;font-weight:600;";
                meta.appendChild(titleSpan);
                if (payload.category) {
                    var catSpan = document.createElement("span");
                    catSpan.style.cssText = "margin-left:12px;font-size:11px;color:var(--st-gray-mid,#666);text-transform:uppercase;letter-spacing:0.05em;";
                    catSpan.textContent = payload.category;
                    meta.appendChild(catSpan);
                }
                if (payload.summary) {
                    var sumP = document.createElement("p");
                    sumP.style.cssText = "margin:4px 0 16px 0;color:var(--st-gray-mid,#666);";
                    sumP.textContent = payload.summary;
                    meta.appendChild(sumP);
                }
                bodyEl.appendChild(meta);
            }

            _renderTocSidebar(payload.toc || [], tocEl);

            var segments = payload.segments || [];
            for (var i = 0; i < segments.length; i++) {
                var seg = segments[i];
                if (seg.kind === "text") {
                    _renderTextSegment(seg, bodyEl);
                } else if (seg.kind === "markdown") {
                    _renderMarkdownSegment(seg, bodyEl);
                } else if (seg.kind === "svg") {
                    _renderSvgSegment(seg, bodyEl);
                } else if (seg.kind === "table") {
                    _renderTableSegment(seg, bodyEl);
                } else if (seg.kind === "image") {
                    _renderImageSegment(seg, bodyEl);
                } else {
                    var unk = document.createElement("div");
                    css.addClass(unk, st_error);
                    unk.textContent = "Unknown segment kind: " + seg.kind;
                    bodyEl.appendChild(unk);
                }
            }

            // Scroll-spy: highlight TOC entry as its anchor enters view.
            if (typeof IntersectionObserver === "function") {
                var tocLinks = tocEl.querySelectorAll("a[data-anchor]");
                var byAnchor = {};
                for (var li = 0; li < tocLinks.length; li++) {
                    byAnchor[tocLinks[li].getAttribute("data-anchor")] = tocLinks[li];
                }
                var observer = new IntersectionObserver(function(entries) {
                    entries.forEach(function(e) {
                        if (e.isIntersecting) {
                            for (var li2 = 0; li2 < tocLinks.length; li2++) {
                                css.removeClass(tocLinks[li2], st_toc_active);
                            }
                            var link = byAnchor[e.target.id];
                            if (link) css.addClass(link, st_toc_active);
                        }
                    });
                }, { rootMargin: "0px 0px -70% 0px", threshold: 0 });
                // Observe every anchored element in the body (sections + headings).
                var anchored = bodyEl.querySelectorAll("[id]");
                for (var ai = 0; ai < anchored.length; ai++) observer.observe(anchored[ai]);
            }
        })
        .catch(function(err) {
            var errEl = document.createElement("div");
            css.addClass(errEl, st_error);
            errEl.textContent = "Failed to load composed doc: " + err.message;
            bodyEl.replaceChildren(errEl);
        });
}
