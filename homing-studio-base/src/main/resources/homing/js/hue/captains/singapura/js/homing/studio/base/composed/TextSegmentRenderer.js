// =============================================================================
// TextSegmentRenderer — Block/Inline AST walker (RFC 0018, RFC 0024 P1c).
//
// renderTextSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, title?, blocks: [...] }
// blocks: { kind: "p" | "ul" | "ol" | "quote", inlines | items }
// inlines: { kind: "text" | "code" | "b" | "i" | "ref", ... }
//
// Owned-by-branch elements: section, h2?, article. Block-level + inline
// elements created inline (descendants of article) are NOT individually
// branch-owned — they cascade out when article is released on dissolve.
// =============================================================================

var href = HrefManagerInstance;

function _renderInlines(inlines, hostEl) {
    if (!inlines) return;
    for (var i = 0; i < inlines.length; i++) {
        var n = inlines[i];
        if (n.kind === 'text') {
            hostEl.appendChild(document.createTextNode(n.text));
        } else if (n.kind === 'code') {
            var codeEl = document.createElement('code');
            codeEl.textContent = n.text;
            hostEl.appendChild(codeEl);
        } else if (n.kind === 'b') {
            var bEl = document.createElement('strong');
            _renderInlines(n.inlines, bEl);
            hostEl.appendChild(bEl);
        } else if (n.kind === 'i') {
            var iEl = document.createElement('em');
            _renderInlines(n.inlines, iEl);
            hostEl.appendChild(iEl);
        } else if (n.kind === 'ref') {
            var aEl = document.createElement('a');
            href.set(aEl, '#ref:' + n.anchor);
            // MHTML-survival: Chrome rewrites page-local fragment hrefs on export
            // but preserves inline onclick attributes. Scroll to the ref card
            // directly; return false prevents the rewritten href from firing.
            aEl.setAttribute('onclick',
                "var el=document.getElementById('ref:" + n.anchor + "');if(el){el.scrollIntoView({behavior:'smooth'});}return false;");
            aEl.textContent = n.label;
            hostEl.appendChild(aEl);
        } else {
            var unkEl = document.createElement('span');
            css.addClass(unkEl, st_error);
            unkEl.textContent = 'Unknown inline kind: ' + n.kind;
            hostEl.appendChild(unkEl);
        }
    }
}

function renderTextSegment(branch, parent, seg, ctx) {
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

    var blocks = seg.blocks || [];
    for (var bi = 0; bi < blocks.length; bi++) {
        var b = blocks[bi];
        if (b.kind === 'p') {
            var p = document.createElement('p');
            _renderInlines(b.inlines, p);
            article.appendChild(p);
        } else if (b.kind === 'ul') {
            var ul = document.createElement('ul');
            for (var li = 0; li < b.items.length; li++) {
                var liEl = document.createElement('li');
                _renderInlines(b.items[li], liEl);
                ul.appendChild(liEl);
            }
            article.appendChild(ul);
        } else if (b.kind === 'ol') {
            var ol = document.createElement('ol');
            for (var oli = 0; oli < b.items.length; oli++) {
                var oliEl = document.createElement('li');
                _renderInlines(b.items[oli], oliEl);
                ol.appendChild(oliEl);
            }
            article.appendChild(ol);
        } else if (b.kind === 'quote') {
            var bq = document.createElement('blockquote');
            _renderInlines(b.inlines, bq);
            article.appendChild(bq);
        } else {
            var unkB = document.createElement('div');
            css.addClass(unkB, st_error);
            unkB.textContent = 'Unknown block kind: ' + b.kind;
            article.appendChild(unkB);
        }
    }

    section.appendChild(article);
    parent.appendChild(section);
}
