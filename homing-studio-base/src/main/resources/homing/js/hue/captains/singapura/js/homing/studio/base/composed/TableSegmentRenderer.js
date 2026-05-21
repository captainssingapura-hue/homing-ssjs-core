// =============================================================================
// TableSegmentRenderer — inline table via shared TableViewerRenderer
// (RFC 0024 P1c).
//
// renderTableSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, caption?, tableDocId }
// =============================================================================

function renderTableSegment(branch, parent, seg, ctx) {
    var section = branch.createElement('section', 'section');
    css.addClass(section, st_section);
    section.id = seg.anchor;

    var figure = branch.createElement('figure', 'figure');
    figure.style.cssText = 'margin:24px 0;';

    var host = branch.createElement('host', 'div');
    figure.appendChild(host);

    if (seg.caption) {
        var caption = branch.createElement('caption', 'figcaption');
        caption.style.cssText = 'margin-top:8px;font-size:13px;color:var(--st-gray-mid,#666);text-align:center;';
        caption.textContent = seg.caption;
        figure.appendChild(caption);
    }

    section.appendChild(figure);
    parent.appendChild(section);

    // Delegate to the shared TableViewer renderer.
    renderTable({ docId: seg.tableDocId, host: host });
}
