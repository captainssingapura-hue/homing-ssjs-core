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
    var sectionsByKey = {};   // INDEX path key -> the node's own <section> (TOC scroll nav)
    var kidsWrapByKey = {};   // INDEX path key -> the node's children container (fold)
    var contentKeyByIdx = {}; // INDEX path key -> CONTENT key (name-path in V2, index in V1)
    var orderedSections = []; // { key, path, el } in document (pre-order) order — the scroll-spy's ordered index (RFC 0043)
    function keyOf(path) { return path.join('/'); }              // canonical child-index key
    function idOf(key)   { return 'doc-node-' + (key === '' ? 'root' : key.replace(/\//g, '_')); }
    // The stable, URL-safe node id every node carries in a name-path doc
    // (RigidDocV2). Present -> content is addressed by the '/'-joined chain of
    // these ids (stable across sibling reordering); absent (V1) -> by child-index.
    function nameOf(node) {
        var dims = (node && node.dimensions) || [];
        for (var i = 0; i < dims.length; i++) {
            if (dims[i] && dims[i].key === 'nodeName') return (dims[i].text || null);
        }
        return null;
    }

    function walk(node, parentEl, idxPath, namePath, depth) {
        var idxKey = keyOf(idxPath);
        // Content key: the name-path chain when the substrate carries node ids
        // (V2, stable across sibling reordering), else the child-index path (V1).
        var key = (namePath !== null) ? namePath.join('/') : idxKey;
        contentKeyByIdx[idxKey] = key;
        // DomOpsParty branch/element names allow only [A-Za-z0-9_-]; the key
        // ('animals/turtle', '1/0', '') is not safe, so sanitize a name suffix.
        var nk = (key === '') ? 'root' : key.replace(/[^0-9A-Za-z_-]/g, '_');
        var section = branch.createElement('docSection_' + nk, 'section');
        section.id = idOf(key);
        // padding-left holds a gutter for the active-node accent bar (an inset
        // box-shadow), so the highlight never overlaps text and adds no layout
        // shift when it toggles.
        section.style.cssText = 'scroll-margin-top:12px;padding-left:12px;border-radius:4px;'
            + 'transition:background-color .25s ease, box-shadow .25s ease;';
        // Keyed by the INDEX path — the TOC's TreeRenderer addresses nodes
        // positionally, so nav callbacks resolve through this map regardless of
        // whether content is keyed by name (V2) or index (V1).
        sectionsByKey[idxKey] = section;
        // Stash the node's own index-key on the element so the IntersectionObserver
        // can map an intersecting <section> back to a path, and record it in
        // document order for the top-biased "which section is current" pick.
        section.__docKey = idxKey;
        orderedSections.push({ key: idxKey, path: idxPath.slice(), el: section });

        // heading from the node's universal displayLabel dimension
        var label = labelOf(node);
        if (label) {
            var hLevel = depth < 1 ? 1 : (depth > 5 ? 6 : depth + 1);
            var heading = branch.createElement('docHeading_' + nk, 'h' + hLevel);
            heading.textContent = label;
            section.appendChild(heading);
        }

        // content for THIS node: a ComposedLeaf bundle (RFC 0041) — an array of
        // segments rendered into the node's body, in order. A node with both
        // content and children shows this as a lead-in above its (foldable) kids.
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
            kidsWrapByKey[idxKey] = kidsWrap;
            for (var i = 0; i < kids.length; i++) {
                // Extend the name-path with the child's node id; a missing id
                // (V1, or a partial tree) drops the whole branch to index keys.
                var childName = nameOf(kids[i]);
                var childNamePath = (namePath !== null && childName !== null)
                    ? namePath.concat([childName]) : null;
                walk(kids[i], kidsWrap, idxPath.concat([i]), childNamePath, depth + 1);
            }
        }
    }
    // Seed the name-path with [] when the root carries a node id (V2); null makes
    // the whole doc fall back to child-index keys (V1) — feature detection, no flag.
    if (structure) walk(structure, bodyHost, [], (nameOf(structure) !== null ? [] : null), 0);

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
    // ── The local Secretary (RFC 0043) ──────────────────────────────────────
    // Two-way TOC↔body sync has exactly ONE bug risk: a feedback loop — a TOC
    // click scrolls the body → the scroll fires → a scroll-spy re-selects →
    // re-scrolls. We dissolve it structurally: one authority (`currentKey`), two
    // writers (a TOC Actor via NavRequested, a scroll-spy Actor via ScrolledTo),
    // and an asymmetry — navigation SCROLLS, the spy only HIGHLIGHTS + SELECTS,
    // never scrolls. The only residual (flicker through intermediate sections
    // DURING a programmatic scroll) is absorbed by one `programmaticScroll` field.
    // The coordinator is a pure (state, message) -> { state, actions } reducer,
    // exactly RFC 0028's Secretary shape, hosted locally with no hierarchy.
    var secretary = {
        state: { currentKey: null, programmaticScroll: false },
        reduce: function (state, msg) {
            switch (msg.kind) {
                case 'NavRequested':                        // TOC click / keyboard reports up
                    return { state: { currentKey: msg.key, programmaticScroll: true },
                             actions: [{ kind: 'SyncTo', key: msg.key, path: msg.path, scroll: true }] };
                case 'ScrolledTo':
                    if (state.programmaticScroll) return { state: state, actions: [] };   // ignore our own scroll
                    if (msg.key === state.currentKey) return { state: state, actions: [] };
                    return { state: { currentKey: msg.key, programmaticScroll: false },
                             actions: [{ kind: 'SyncTo', key: msg.key, path: msg.path, scroll: false }] };
                case 'ScrollSettled':                       // scrollend clears the guard
                    return { state: { currentKey: state.currentKey, programmaticScroll: false }, actions: [] };
                default:
                    return { state: state, actions: [] };
            }
        }
    };
    // Scroll a body section to the top of the DETECTED scroll container. We scroll
    // the container EXPLICITLY (scrollTo on `scrollParent`) rather than calling
    // sec.scrollIntoView(): for a nested overflow container — the workspace pane's
    // st_doc_pane — scrollIntoView is unreliable (it may act on the wrong ancestor,
    // and the TOC's own instant row-reveal can cancel its pending smooth animation
    // on the SAME container). Scrolling the container we detected — the one the spy
    // observes — by a computed offset is deterministic. The viewport case (the
    // standalone page, scrollParent === null) keeps scrollIntoView, which works there.
    function scrollSectionToTop(sec) {
        if (scrollParent) {
            var delta = sec.getBoundingClientRect().top - scrollParent.getBoundingClientRect().top;
            var top = scrollParent.scrollTop + delta - 12;   // 12 = the section's scroll-margin-top
            if (top < 0) top = 0;
            try { scrollParent.scrollTo({ top: top, behavior: 'smooth' }); }
            catch (e) { scrollParent.scrollTop = top; }
        } else if (sec.scrollIntoView) {
            sec.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }
    // Apply one Secretary action. SyncTo{key, path, scroll} is the only broadcast:
    //   body — highlight the section (and scroll to it ONLY when navigation drove it);
    //   toc  — move the selection SILENTLY (no onSelect → no re-scroll → no loop).
    // Order matters: highlight + move the TOC selection FIRST (the reveal may do an
    // INSTANT row scroll), then issue the body's smooth scroll LAST so the reveal
    // can never cancel it.
    function applyAction(a) {
        if (a.kind !== 'SyncTo') return;
        var sec = sectionsByKey[a.key];
        if (sec) setActiveSection(sec);
        if (a.path && toc && toc.selectPath) toc.selectPath(a.path, { silent: true, reveal: true });
        if (sec && a.scroll) {
            scrollSectionToTop(sec);
            armScrollGuard();   // fallback in case 'scrollend' never fires (target already in view)
        }
    }
    function dispatch(msg) {
        var step = secretary.reduce(secretary.state, msg);
        secretary.state = step.state;
        for (var i = 0; i < step.actions.length; i++) applyAction(step.actions[i]);
    }
    // TOC selection (click / arrow key, via onSelect) and the public scrollToPath
    // both report up as NavRequested — the Secretary owns what happens next.
    function navigateTo(path) {
        var p = path || [];
        dispatch({ kind: 'NavRequested', key: keyOf(p), path: p });
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
        // makes the TOC navigable in a static HTML export with no JS. The TOC
        // hands us a child-index path; map it to the node's CONTENT key (the
        // name-path in V2) so the anchor matches the body section's id.
        hrefForPath: function (path) {
            var idxKey = keyOf(path || []);
            var ck = contentKeyByIdx.hasOwnProperty(idxKey) ? contentKeyByIdx[idxKey] : idxKey;
            return '#' + idOf(ck);
        }
    });

    // Keyboard wiring lives just below, after the scroll container is detected,
    // so the key handler can sit on the element that actually scrolls (see there).

    // ── The scroll-spy Actor: an IntersectionObserver reporting ScrolledTo ────
    // Declarative (no scroll-math, no rAF): a collapsed subtree's sections are
    // display:none, so they simply never intersect — folding falls out for free.
    // Its `root` must be the DETECTED scroll container (the workspace pane, or
    // the viewport when standalone), not assumed. A top-biased band (top 25% of
    // the root) defines "current"; among sections crossing it, the topmost in
    // document order wins.
    function detectScrollParent(el) {
        var node = el ? el.parentElement : null;
        while (node && node !== document.body && node !== document.documentElement) {
            var oy = '';
            try { oy = window.getComputedStyle(node).overflowY; } catch (e) { oy = ''; }
            if ((oy === 'auto' || oy === 'scroll') && node.scrollHeight > node.clientHeight + 1) return node;
            node = node.parentElement;
        }
        return null;   // viewport
    }
    var scrollParent = detectScrollParent(layout);
    var scrollTarget = scrollParent || window;

    // ── Keyboard: ALWAYS drives the TOC, NEVER the scrollbar (RFC 0043) ───────
    // Focus-gating on the TOC pane was the bug: a scroll or a stray click could
    // move focus off it and arrow keys fell back to the browser default —
    // scrolling the body. Fix: attach the key handler to the element that would
    // ACTUALLY scroll — the detected scroll container, or the page `document`
    // when a standalone page scrolls the viewport. A consumed key is
    // preventDefault'd, so that container can never scroll from the keyboard; the
    // keyboard only ever moves the TOC (which scrolls the body THROUGH the
    // selection), and the scrollbar stays mouse-only (wheel / drag / click).
    // Sitting on the scroll container, the handler catches the keydown wherever
    // focus lands inside it (TOC row, body section, or the container itself) —
    // there is no focus to lose. Up/Down move+select; Right/Left fold; Enter opens.
    layout.tabIndex = 0;
    layout.style.outline = 'none';
    tocHost.style.outline = 'none';
    var keyTarget = scrollParent ? scrollParent : (opts.autoFocus ? document : layout);
    function onDocKeydown(ev) {
        // Don't steal keys from real controls OUTSIDE the doc view (page buttons,
        // inputs, editors). TOC anchors live inside `layout`, so they still route.
        var t = ev.target;
        if (t && !layout.contains(t)) {
            var tag = t.tagName;
            if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT'
                || tag === 'BUTTON' || t.isContentEditable) return;
        }
        if (toc.handleKeydown(ev)) ev.preventDefault();
    }
    keyTarget.addEventListener('keydown', onDocKeydown);
    // A click anywhere in the view pulls focus in (preventScroll so it never jogs
    // the scrollbar), keeping focus in the container subtree; `opts.autoFocus`
    // (standalone page — the doc IS the page) focuses on mount so no click is needed.
    layout.addEventListener('mousedown', function () {
        if (!layout.contains(document.activeElement)) {
            try { layout.focus({ preventScroll: true }); } catch (e) { layout.focus(); }
        }
    });
    if (opts.autoFocus) {
        try { layout.focus({ preventScroll: true }); } catch (e) { layout.focus(); }
    }

    // The click-vs-scroll guard clears on 'scrollend'; a timeout is the fallback
    // for when a programmatic scroll moves nothing (target already in view) so
    // 'scrollend' never fires — without it the spy would stay muted forever.
    var scrollGuardTimer = null;
    function armScrollGuard() {
        if (scrollGuardTimer) clearTimeout(scrollGuardTimer);
        scrollGuardTimer = setTimeout(function () {
            scrollGuardTimer = null;
            dispatch({ kind: 'ScrollSettled' });
        }, 400);
    }
    function onScrollEnd() {
        if (scrollGuardTimer) { clearTimeout(scrollGuardTimer); scrollGuardTimer = null; }
        dispatch({ kind: 'ScrollSettled' });
    }
    scrollTarget.addEventListener('scrollend', onScrollEnd);

    var spy = null;
    if (typeof IntersectionObserver === 'function' && orderedSections.length) {
        var inBand = {};
        spy = new IntersectionObserver(function (entries) {
            for (var i = 0; i < entries.length; i++) {
                var k = entries[i].target.__docKey;
                if (entries[i].isIntersecting) inBand[k] = true; else delete inBand[k];
            }
            // Topmost section (document order) currently crossing the band.
            var cur = null;
            for (var j = 0; j < orderedSections.length; j++) {
                if (inBand[orderedSections[j].key]) { cur = orderedSections[j]; break; }
            }
            if (cur) dispatch({ kind: 'ScrolledTo', key: cur.key, path: cur.path });
        }, { root: scrollParent, rootMargin: '0px 0px -75% 0px', threshold: 0 });
        for (var oi = 0; oi < orderedSections.length; oi++) spy.observe(orderedSections[oi].el);
    }

    // The teardown seam (RFC 0043). branch.dissolve() reaps DOM only; these two
    // are NOT DOM — the observer, and the scroll/scrollend listener living on an
    // ANCESTOR of the widget's branch (the scroll container), so they genuinely
    // leak if left. The Secretary itself is pure JS, GC'd with this closure.
    function dispose() {
        if (spy) { try { spy.disconnect(); } catch (e) {} spy = null; }
        try { scrollTarget.removeEventListener('scrollend', onScrollEnd); } catch (e) {}
        try { keyTarget.removeEventListener('keydown', onDocKeydown); } catch (e) {}
        if (scrollGuardTimer) { clearTimeout(scrollGuardTimer); scrollGuardTimer = null; }
    }

    return { toc: toc, scrollToPath: navigateTo, dispose: dispose };
}

// Read a node's displayLabel dimension text (the substrate's universal label).
function labelOf(node) {
    var dims = (node && node.dimensions) || [];
    for (var i = 0; i < dims.length; i++) {
        if (dims[i] && dims[i].key === 'displayLabel') return dims[i].text || '';
    }
    return '';
}
