// =============================================================================
// TocSidebarRenderer — TOC sidebar for ComposedWidget (RFC 0024 P1c).
//
// renderTocSidebar(branch, tocEl, tocEntries) → void
//
// Populates tocEl (a <nav> element the orchestrator already created) with
// anchor links owned by the supplied branch. Each anchor's level controls
// its CSS class (st_toc_h1 / h2 / h3). Inline onclick handlers survive
// Chrome's MHTML export — the listeners that addEventListener attaches
// are dropped at save time, so we use the attribute form.
// =============================================================================

var href = HrefManagerInstance;

function _tocScrollHandler(anchor) {
    return "var el=document.getElementById('" + anchor
         + "');if(el){el.scrollIntoView({behavior:'smooth'});}return false;";
}

function renderTocSidebar(branch, tocEl, tocEntries) {
    if (!tocEntries || tocEntries.length === 0) {
        var empty = branch.createElement('empty', 'div');
        css.addClass(empty, st_loading);
        empty.style.cssText = 'padding:8px 12px;';
        empty.textContent = 'No sections.';
        tocEl.replaceChildren(empty);
        return;
    }
    var links = [];
    for (var i = 0; i < tocEntries.length; i++) {
        var entry = tocEntries[i];
        var levelCls = entry.level === 1 ? st_toc_h1
                     : entry.level === 2 ? st_toc_h2
                     : st_toc_h3;
        var a = branch.createElement('link-' + i, 'a');
        css.addClass(a, st_toc_item);
        css.addClass(a, levelCls);
        href.set(a, '#' + entry.anchor);
        a.setAttribute('data-anchor', entry.anchor);
        a.setAttribute('onclick', _tocScrollHandler(entry.anchor));
        a.textContent = entry.text;
        links.push(a);
    }
    tocEl.replaceChildren.apply(tocEl, links);
}
