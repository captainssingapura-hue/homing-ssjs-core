// =============================================================================
// ImageSegmentRenderer — inline image via shared ImageViewerRenderer
// (RFC 0024 P1c).
//
// renderImageSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, caption?, imageUrl }
//
// The imageUrl is a server-emitted leveled URL — typically
// /doc?id=<rootUuid>&l1=...&l<N>=<segIndex>. Embedded image docs don't need
// separate UUID registration; their addressability is the parent path.
// =============================================================================

function renderImageSegment(branch, parent, seg, ctx) {
    var section = branch.createElement('section', 'section');
    css.addClass(section, st_section);
    section.id = seg.anchor;

    var host = branch.createElement('host', 'div');
    host.style.cssText = 'display:flex;justify-content:center;';
    section.appendChild(host);

    parent.appendChild(section);

    // Delegate to the shared ImageViewer renderer. Prefer the leveled URL
    // (the new shape); fall back to the legacy docId field.
    renderImage({ docUrl: seg.imageUrl, docId: seg.imageDocId, host: host });
}
