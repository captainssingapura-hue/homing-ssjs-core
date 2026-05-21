// =============================================================================
// MarkdownSegmentRenderer — marked.js-backed markdown body (RFC 0024 P1c).
//
// renderMarkdownSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, title?, body }
//
// Elements owned by the supplied branch: section, h2?, article.
// The article's content is set via a DocumentFragment built from marked.parse();
// the resulting H1-H4 elements get IDs matching the server's TOC pattern
// (seg-N-hM) so anchor links scroll to them.
// =============================================================================

function renderMarkdownSegment(branch, parent, seg, ctx) {
    if (marked && marked.use) marked.use({ gfm: true, breaks: false });

    var section = branch.createElement('section', 'section');
    css.addClass(section, st_section);
    section.id = seg.anchor;

    if (seg.title) {
        var h = branch.createElement('title', 'h2');
        css.addClass(h, st_section_title);
        h.textContent = seg.title;
        section.appendChild(h);
    }

    var article = branch.createElement('article', 'article');
    css.addClass(article, st_doc);
    var range = document.createRange();
    range.selectNodeContents(article);
    var fragment = range.createContextualFragment(marked.parse(seg.body || ''));
    article.appendChild(fragment);

    // Assign anchors to H1-H4 to match server TOC's seg-N-hM pattern.
    var headingIdx = 0;
    var walker = document.createTreeWalker(article, NodeFilter.SHOW_ELEMENT, null);
    var node;
    while ((node = walker.nextNode())) {
        var tag = node.tagName;
        if (tag === 'H1' || tag === 'H2' || tag === 'H3' || tag === 'H4') {
            node.id = seg.anchor + '-h' + headingIdx;
            headingIdx++;
        }
    }

    section.appendChild(article);
    parent.appendChild(section);
}
