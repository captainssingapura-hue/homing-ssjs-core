// =============================================================================
// RelationSegmentRenderer — typed relation (table) segment (RFC 0019).
//
// renderRelationSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, caption?, headers: string[], rows: string[][] }
//
// Cell and header values may contain inline markdown (bold, italic, code,
// links). Each value is run through marked.parseInline() and inserted as
// HTML via createContextualFragment, so [label](#ref:name) cross-references
// render the same way they do in MarkdownSegment bodies.
// =============================================================================

function renderRelationSegment(branch, parent, seg, ctx) {
    var section = branch.createElement('section', 'section');
    css.addClass(section, st_section);
    section.id = seg.anchor;

    if (seg.caption) {
        var h = branch.createElement('caption-title', 'h2');
        css.addClass(h, st_section_title);
        h.textContent = seg.caption;
        section.appendChild(h);
    }

    var table = branch.createElement('table', 'table');
    css.addClass(table, st_table);

    // Header row
    var headers = seg.headers || [];
    if (headers.length > 0) {
        var thead = document.createElement('thead');
        css.addClass(thead, st_thead);
        var hrow = document.createElement('tr');
        for (var hi = 0; hi < headers.length; hi++) {
            var th = document.createElement('th');
            css.addClass(th, st_th);
            if (marked && marked.parseInline) {
                var hrange = document.createRange();
                hrange.selectNodeContents(th);
                th.appendChild(hrange.createContextualFragment(marked.parseInline(headers[hi] || '')));
            } else {
                th.textContent = headers[hi] || '';
            }
            hrow.appendChild(th);
        }
        thead.appendChild(hrow);
        table.appendChild(thead);
    }

    // Data rows
    var tbody = document.createElement('tbody');
    var rows = seg.rows || [];
    for (var ri = 0; ri < rows.length; ri++) {
        var row = rows[ri];
        var tr = document.createElement('tr');
        for (var ci = 0; ci < row.length; ci++) {
            var td = document.createElement('td');
            css.addClass(td, st_td);
            if (marked && marked.parseInline) {
                var drange = document.createRange();
                drange.selectNodeContents(td);
                td.appendChild(drange.createContextualFragment(marked.parseInline(row[ci] || '')));
            } else {
                td.textContent = row[ci] || '';
            }
            tr.appendChild(td);
        }
        tbody.appendChild(tr);
    }
    table.appendChild(tbody);

    section.appendChild(table);
    parent.appendChild(section);
}
