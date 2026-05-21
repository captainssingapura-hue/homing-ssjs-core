// =============================================================================
// ImageSegmentRenderer — inline image via shared ImageViewerRenderer
// (RFC 0024 P1c).
//
// renderImageSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, caption?, imageDocId }
// =============================================================================

function renderImageSegment(branch, parent, seg, ctx) {
    var section = branch.createElement('section', 'section');
    css.addClass(section, st_section);
    section.id = seg.anchor;

    var host = branch.createElement('host', 'div');
    host.style.cssText = 'display:flex;justify-content:center;';
    section.appendChild(host);

    parent.appendChild(section);

    // Delegate to the shared ImageViewer renderer.
    renderImage({ docId: seg.imageDocId, host: host });
}
