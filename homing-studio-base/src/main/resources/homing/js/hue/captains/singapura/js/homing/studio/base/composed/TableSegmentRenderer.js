// =============================================================================
// TableSegmentRenderer — inline table via shared TableViewerRenderer
// (RFC 0024 P1c).
//
// renderTableSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, caption?, tableUrl }
//
// The tableUrl is a server-emitted leveled URL — typically
// /doc?id=<rootUuid>&l1=...&l2=...&l<N>=<segIndex>. The embedded table doc
// doesn't need separate UUID registration; its addressability is the path.
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

    // Delegate to the shared TableViewer renderer. Prefer the leveled URL
    // (the new shape); fall back to the legacy docId field during the
    // transition so older ComposedDoc emissions keep rendering.
    renderTable({ docUrl: seg.tableUrl, docId: seg.tableDocId, host: host });
}
