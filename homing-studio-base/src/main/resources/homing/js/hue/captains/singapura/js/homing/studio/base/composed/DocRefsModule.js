// =============================================================================
// DocRefsModule — the References section and the category chip (RFC 0004-ext1).
//
// attachDocRefs({ branch, docId, metaHost, refsParent }) → void
//
// One fetch of /doc-refs?id=<uuid> answers the two questions a doc tree cannot:
// which category the catalogue filed this doc under, and which typed References
// it declares. Both are Doc metadata rather than document structure, which is
// why they travel beside the tree instead of inside it.
//
// Lifted out of DocReaderRenderer by RFC 0059 Phase 3, where it was the last
// thing the standalone reader had that the rigid readers did not -- 159 of the
// self-studio's 171 docs declare at least one reference, so this was never a
// feature the convergence could quietly drop. Living here, it lands once for
// every reader that renders a doc tree.
//
// Each card carries id="ref:<name>", the address a markdown citation links to
// (TextSegmentRenderer writes '#ref:' + anchor). A colon is legal in an id and
// illegal in a selector; nothing here selects, so it stands.
//
// Failure is silent by design: metadata is an enrichment, and a doc whose
// references will not load is still a readable doc.
// =============================================================================

function attachDocRefs(opts) {
    var branch = opts.branch;
    var docId  = opts.docId;
    if (!docId) return;

    fetch('/doc-refs?id=' + encodeURIComponent(docId))
        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
        .then(function (info) {
            if (!info) return;
            if (info.category && opts.metaHost) {
                var cat = branch.createElement('docCategory', 'span');
                css.addClass(cat, st_doc_category);
                cat.textContent = info.category;
                opts.metaHost.appendChild(cat);
            }
            _renderReferences(branch, opts.refsParent, info.references || []);
        })
        .catch(function () { /* an enrichment: a doc without it still reads */ });
}

function _renderReferences(branch, parent, refs) {
    if (!parent || !refs.length) return;

    // The section is minted only when there is something to put in it -- an
    // empty one would still draw its rule and its margins.
    var section = branch.createElement('refsSection', 'section');
    css.addClass(section, st_section);
    section.setAttribute('data-export-content', '');

    var heading = branch.createElement('refsHeading', 'h2');
    css.addClass(heading, st_section_title);
    heading.textContent = 'References';
    section.appendChild(heading);

    for (var i = 0; i < refs.length; i++) {
        section.appendChild(_refCard(branch, refs[i], i));
    }
    parent.appendChild(section);
}

function _refCard(branch, r, i) {
    var card = branch.createElement('refCard' + i, 'div');
    css.addClass(card, st_card);
    card.id = 'ref:' + r.name;

    var title = branch.createElement('refTitle' + i, 'h3');
    css.addClass(title, st_card_title);
    var summary = branch.createElement('refSummary' + i, 'p');
    css.addClass(summary, st_card_summary);

    if (r.kind === 'doc') {
        var link = branch.createElement('refLink' + i, 'a');
        css.addClass(link, st_card_link);
        // RFC 0051 -- the server supplies the target's (app, args); /app is the
        // generic redirect that turns it into the authentic path.
        HrefManagerInstance.set(link,
            r.url || ('/app?app=doc-reader&doc=' + encodeURIComponent(r.uuid)));
        link.textContent = r.title;
        title.appendChild(link);
        summary.textContent = r.summary || '';
    } else if (r.kind === 'external') {
        var ext = branch.createElement('refExt' + i, 'a');
        css.addClass(ext, st_card_link);
        HrefManagerInstance.set(ext, r.url);
        ext.setAttribute('target', '_blank');
        ext.setAttribute('rel', 'noopener');
        ext.textContent = r.label || r.url;
        title.appendChild(ext);
        summary.textContent = r.description || '';
    } else if (r.kind === 'image') {
        // Image rendering deferred per RFC 0004-ext1 4.6 (needs /asset endpoint).
        // Alt + caption + classpath path, as text, until there is one.
        title.textContent = r.alt || r.name;
        summary.textContent = (r.caption ? r.caption + ' — ' : '')
            + '(image at ' + r.resourcePath + ')';
    } else {
        // Unknown kind -- render the raw JSON, so it is visible rather than lost.
        title.textContent = 'Unknown reference kind: ' + r.kind;
        summary.textContent = JSON.stringify(r);
    }

    card.appendChild(title);
    card.appendChild(summary);
    return card;
}
