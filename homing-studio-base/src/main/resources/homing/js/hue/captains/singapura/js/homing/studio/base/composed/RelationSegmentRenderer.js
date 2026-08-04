// =============================================================================
// RelationSegmentRenderer — typed relation (table) segment (RFC 0019).
//
// renderRelationSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, caption?, headers: string[], rows: string[][],
//              articulations?: [ { row, col, badge?, align?, emphasis? } ] }
//
// Cell and header values may contain inline markdown (bold, italic, code,
// links). Each value is run through marked.parseInline() and inserted as
// HTML via createContextualFragment, so [label](#ref:name) cross-references
// render the same way they do in MarkdownSegment bodies.
//
// The optional `articulations` side-car (sparse) enhances individual cells —
// row -1 addresses a header cell, row >= 0 a body cell — with a status badge
// (inline pill), horizontal alignment, and/or emphasis (strong / muted). Plain
// relations omit the key entirely and render exactly as before.
// =============================================================================

function _relAlignClass(align) {
    if (align === 'left')   return st_td_align_left;
    if (align === 'center') return st_td_align_center;
    if (align === 'right')  return st_td_align_right;
    return null;
}

function _relBadgeClass(badge) {
    if (badge === 'success') return st_td_badge_success;
    if (badge === 'warning') return st_td_badge_warning;
    if (badge === 'error')   return st_td_badge_error;
    return null;
}

function _relEmphasisClass(emphasis) {
    if (emphasis === 'strong') return st_td_strong;
    if (emphasis === 'muted')  return st_td_muted;
    return null;
}

// Fill one cell (<th>/<td>): render the value's inline markdown, then apply any
// articulation marks. A badge wraps the rendered content in an inline pill;
// align + emphasis are classes on the cell itself.
function _relFillCell(el, value, art) {
    var frag;
    if (marked && marked.parseInline) {
        var range = document.createRange();
        range.selectNodeContents(el);
        frag = range.createContextualFragment(marked.parseInline(value || ''));
    } else {
        frag = document.createTextNode(value || '');
    }

    var badgeCls = art ? _relBadgeClass(art.badge) : null;
    if (badgeCls) {
        var pill = document.createElement('span');
        css.addClass(pill, badgeCls);
        pill.appendChild(frag);
        el.appendChild(pill);
    } else {
        el.appendChild(frag);
    }

    if (art) {
        var alignCls = _relAlignClass(art.align);
        if (alignCls) css.addClass(el, alignCls);
        var emphCls = _relEmphasisClass(art.emphasis);
        if (emphCls) css.addClass(el, emphCls);
    }
}

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

    // Index the sparse articulation side-car by "row:col" (row -1 = header).
    var artByKey = {};
    var arts = seg.articulations || [];
    for (var ai = 0; ai < arts.length; ai++) {
        var a = arts[ai];
        if (a && typeof a.row === 'number' && typeof a.col === 'number') {
            artByKey[a.row + ':' + a.col] = a;
        }
    }

    var table = branch.createElement('table', 'table');
    css.addClass(table, st_table);

    // Header row (row index -1 in the side-car).
    var headers = seg.headers || [];
    if (headers.length > 0) {
        var thead = document.createElement('thead');
        css.addClass(thead, st_thead);
        var hrow = document.createElement('tr');
        for (var hi = 0; hi < headers.length; hi++) {
            var th = document.createElement('th');
            css.addClass(th, st_th);
            _relFillCell(th, headers[hi], artByKey['-1:' + hi]);
            hrow.appendChild(th);
        }
        thead.appendChild(hrow);
        table.appendChild(thead);
    }

    // Data rows.
    var tbody = document.createElement('tbody');
    var rows = seg.rows || [];
    for (var ri = 0; ri < rows.length; ri++) {
        var row = rows[ri];
        var tr = document.createElement('tr');
        for (var ci = 0; ci < row.length; ci++) {
            var td = document.createElement('td');
            css.addClass(td, st_td);
            _relFillCell(td, row[ci], artByKey[ri + ':' + ci]);
            tr.appendChild(td);
        }
        tbody.appendChild(tr);
    }
    table.appendChild(tbody);

    section.appendChild(table);
    parent.appendChild(section);
}
