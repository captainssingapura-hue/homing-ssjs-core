// =============================================================================
// CodeSegmentRenderer — verbatim code listing segment (RFC 0024 P1c).
//
// renderCodeSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, title?, language?, body }
//
// Elements owned by the supplied branch: section, h2?, article, pre, code.
// The code's textContent is set raw (safe — code is data, not markup; the
// browser preserves whitespace and entities verbatim).
// =============================================================================

function renderCodeSegment(branch, parent, seg, ctx) {
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

    var pre = branch.createElement('pre', 'pre');
    var code = branch.createElement('code', 'code');
    if (seg.language) {
        // language-X is the highlight-library convention. The framework
        // ships no highlighter; the class is still useful for any consumer
        // that wants to attach one downstream.
        code.className = 'language-' + seg.language;
    }
    code.textContent = seg.body;
    pre.appendChild(code);
    article.appendChild(pre);

    section.appendChild(article);
    parent.appendChild(section);
}
