// =============================================================================
// ListSegmentRenderer — ordered / unordered lists of homogeneous Listable items.
//
// renderUnorderedListSegment(branch, parent, seg, ctx) → void   (<ul>)
// renderOrderedListSegment(branch, parent, seg, ctx)   → void   (<ol>)
//
// seg shape: { anchor, items: [<segment object>, ...] }
//
// Each item is a segment, dispatched recursively through ctx.renderContent into
// its own <li> on a per-item sub-branch (so item renderers don't collide on
// element names). Items are never lists (enforced server-side), so depth is 1.
// =============================================================================

function _renderList(tag, branch, parent, seg, ctx) {
    var section = branch.createElement('section', 'section');
    css.addClass(section, st_section);
    section.id = seg.anchor;

    var list = branch.createElement('list', tag);
    var items = seg.items || [];
    for (var i = 0; i < items.length; i++) {
        var li = document.createElement('li');
        var itemBranch = branch.createBranch('item' + i);
        // ctx.renderContent activates itemBranch and dispatches the item segment.
        ctx.renderContent(items[i], li, itemBranch, seg.anchor + '-' + i);
        list.appendChild(li);
    }
    section.appendChild(list);

    parent.appendChild(section);
}

function renderUnorderedListSegment(branch, parent, seg, ctx) {
    _renderList('ul', branch, parent, seg, ctx);
}

function renderOrderedListSegment(branch, parent, seg, ctx) {
    _renderList('ol', branch, parent, seg, ctx);
}
