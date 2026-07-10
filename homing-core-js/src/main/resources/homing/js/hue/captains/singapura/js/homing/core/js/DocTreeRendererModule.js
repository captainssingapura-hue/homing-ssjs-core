// =============================================================================
// DocTreeRendererModule — the JS `renderDoc` view (RFC 0039).
//
// renderDocTree({ branch, container, payload, renderContent, expandDepth })
//   payload       = { structure: <NormalizedNode JSON>,
//                     content:   { "<canonical-path>": <contentObj> } }
//   renderContent = function(contentObj, hostEl, segBranch, pathKey) -> void
//                   (the doc widget wires this to the per-segment renderers)
//
// Lays out a TOC (TreeRenderer over `structure`) beside a body of one <section>
// per structure node (content dispatched by its canonical position). Selecting
// a TOC node fires onSelect({path, ...}); we scroll the body to that node's
// section — the substrate's ONE TreeRenderer driving intra-doc navigation,
// exactly as it drives catalogue navigation. No bespoke TOC sidebar.
//
// Ownership: every element is created through the caller's DomOpsParty branch,
// so dissolving the branch tears the whole view (TOC + body) down cleanly.
// =============================================================================

function renderDocTree(opts) {
    opts = opts || {};
    var branch        = opts.branch;
    var container     = opts.container;
    var payload       = opts.payload || {};
    var structure     = payload.structure || null;
    var content       = payload.content || {};
    var renderContent = opts.renderContent || function () {};
    var renderCaption = opts.renderCaption || function () {};

    // ── two-pane layout: TOC | body ──
    var layout = branch.createElement('docTreeLayout', 'div');
    layout.style.cssText = 'display:flex;gap:28px;align-items:flex-start;';
    var tocHost = branch.createElement('docTreeToc', 'div');
    tocHost.style.cssText = 'flex:0 0 260px;position:sticky;top:12px;'
        + 'max-height:calc(100vh - 24px);overflow:auto;';
    var bodyHost = branch.createElement('docTreeBody', 'div');
    bodyHost.style.cssText = 'flex:1 1 auto;min-width:0;max-width:820px;';
    // HTML-export (DocReader export): the WHOLE two-pane doc — its TOC and body
    // together — IS the document content. A content-only export keeps the entire
    // layout (so the navigable TOC survives) and strips only the app shell; the
    // TOC here is part of the document, not page chrome.
    layout.setAttribute('data-export-content', '');
    // The two-pane needs more than the single-column default: TOC (260) + gap
    // (28) + body (820) + the export <main>'s padding. Without this the sidebar
    // crushes the body in the exported file.
    layout.setAttribute('data-export-width', '1180px');
    layout.appendChild(tocHost);
    layout.appendChild(bodyHost);
    container.appendChild(layout);

    // ── body: one <section> per structure node, NESTED so each node's whole
    // subtree sits in its own container the TOC can fold in sync. ──
    var sectionsByKey = {};   // path key -> the node's own <section> (scroll nav)
    var kidsWrapByKey = {};   // path key -> the node's children container (fold)
    function keyOf(path) { return path.join('/'); }              // canonical path key
    function idOf(key)   { return 'doc-node-' + (key === '' ? 'root' : key.replace(/\//g, '_')); }

    function walk(node, parentEl, path, depth) {
        var key = keyOf(path);
        // DomOpsParty branch/element names allow only [A-Za-z0-9_-]; the path key
        // ('1/0', '') is not safe, so derive a sanitized suffix for names.
        var nk = (key === '') ? 'root' : key.replace(/[^0-9A-Za-z_-]/g, '_');
        var section = branch.createElement('docSection_' + nk, 'section');
        section.id = idOf(key);
        // padding-left holds a gutter for the active-node accent bar (an inset
        // box-shadow), so the highlight never overlaps text and adds no layout
        // shift when it toggles.
        section.style.cssText = 'scroll-margin-top:12px;padding-left:12px;border-radius:4px;'
            + 'transition:background-color .25s ease, box-shadow .25s ease;';
        sectionsByKey[key] = section;

        // heading from the node's universal displayLabel dimension
        var label = labelOf(node);
        if (label) {
            var hLevel = depth < 1 ? 1 : (depth > 5 ? 6 : depth + 1);
            var heading = branch.createElement('docHeading_' + nk, 'h' + hLevel);
            heading.textContent = label;
            section.appendChild(heading);
        }

        // content for THIS node's position: a ComposedLeaf bundle (RFC 0041) —
        // an array of segments rendered into the node's body, in order. A node
        // with both content and children shows this as a lead-in above its
        // (foldable) children.
        var nc = new NodeContent(content[key]);
        if (nc.caption || (nc.segments && nc.segments.length)) {
            var contentHost = branch.createElement('docContent_' + nk, 'div');
            section.appendChild(contentHost);
            if (nc.caption) {
                renderCaption(nc.caption, contentHost);
            }
            for (var ci = 0; ci < nc.segments.length; ci++) {
                var segBranch = branch.createBranch('docSeg_' + nk + '_' + ci);
                renderContent(nc.segments[ci], contentHost, segBranch, key + ':' + ci);
            }
        }

        parentEl.appendChild(section);

        var kids = (node && node.children) || [];
        if (kids.length) {
            // The subtree lives in its own container, so collapsing this node in
            // the TOC hides its whole subtree's body in one move while the node's
            // own heading + content stay visible. Folds nest naturally — a
            // descendant collapsed on its own keeps that state when an ancestor
            // re-expands (its container is simply revealed, still display:none).
            var kidsWrap = branch.createElement('docKids_' + nk, 'div');
            parentEl.appendChild(kidsWrap);
            kidsWrapByKey[key] = kidsWrap;
            for (var i = 0; i < kids.length; i++) {
                walk(kids[i], kidsWrap, path.concat([i]), depth + 1);
            }
        }
    }
    if (structure) walk(structure, bodyHost, [], 0);

    // ── TOC: the substrate TreeRenderer; selection navigates the body (scroll
    // + highlight), and expand/collapse folds the body subtree in sync. ──
    var activeSection = null;
    // Mark the selected node's body section, mirroring the TOC's row highlight
    // (same accent as TreeRenderer's selected row) so the eye keeps both panes
    // in step. A faint wash + a left accent bar in the section's gutter.
    function setActiveSection(sec) {
        if (activeSection && activeSection !== sec) {
            activeSection.style.backgroundColor = '';
            activeSection.style.boxShadow = '';
        }
        activeSection = sec;
        if (sec) {
            sec.style.backgroundColor = 'rgba(59,130,246,0.07)';
            sec.style.boxShadow = 'inset 3px 0 0 rgba(59,130,246,0.6)';
        }
    }
    function navigateTo(path) {
        var sec = sectionsByKey[keyOf(path || [])];
        if (!sec) return;
        setActiveSection(sec);
        if (sec.scrollIntoView) {
            sec.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }
    // Fold/unfold the body subtree under a node, mirroring its TOC caret.
    function setFold(path, expanded) {
        var wrap = kidsWrapByKey[keyOf(path || [])];
        if (wrap) wrap.style.display = expanded ? '' : 'none';
    }
    var toc = new TreeRenderer({
        branch:      branch,
        container:   tocHost,
        data:        structure,
        expandDepth: (opts.expandDepth != null) ? opts.expandDepth : 99,
        onSelect:    function (sel) { navigateTo(sel && sel.path); },
        onToggle:    function (ev)  { setFold(ev && ev.path, ev && ev.expanded); },
        // Anchor hrefs to each node's body section id — live nav still uses the
        // smooth-scroll onSelect (the row handler preventDefaults), but the href
        // makes the TOC navigable in a static HTML export with no JS.
        hrefForPath: function (path) { return '#' + idOf(keyOf(path || [])); }
    });

    // Keyboard navigation. renderDocTree is the host of this TreeRenderer, so it
    // owns WHEN keys flow (the renderer owns the semantics — Up/Down move +
    // select, which scrolls the body via onSelect; Right/Left expand/fold). We
    // gate on FOCUS rather than a workspace-active signal: the TOC pane is
    // focusable, and arrow keys drive the tree only while it holds focus —
    // elsewhere on the page arrows scroll the body as usual. Clicking anywhere
    // in the TOC moves focus here so the keys take over immediately.
    tocHost.tabIndex = 0;
    tocHost.style.outline = 'none';
    tocHost.addEventListener('keydown', function (ev) {
        if (toc.handleKeydown(ev)) ev.preventDefault();
    });
    tocHost.addEventListener('click', function () { tocHost.focus(); });

    return { toc: toc, scrollToPath: navigateTo };
}

// Read a node's displayLabel dimension text (the substrate's universal label).
function labelOf(node) {
    var dims = (node && node.dimensions) || [];
    for (var i = 0; i < dims.length; i++) {
        if (dims[i] && dims[i].key === 'displayLabel') return dims[i].text || '';
    }
    return '';
}
