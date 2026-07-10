// =============================================================================
// ParagraphSegmentRenderer — a plain paragraph (a list of lines flowed into <p>).
//
// renderParagraphSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, lines: [string, ...] }
//
// The lines join with a single space into one regular, unformatted paragraph;
// the browser wraps it. Owned-by-branch: section, para.
// =============================================================================

function renderParagraphSegment(branch, parent, seg, ctx) {
    var section = branch.createElement('section', 'section');
    css.addClass(section, st_section);
    section.id = seg.anchor;

    var p = branch.createElement('para', 'p');
    var lines = seg.lines || [];
    p.textContent = lines.join(' ');
    section.appendChild(p);

    parent.appendChild(section);
}
