// =============================================================================
// PartyMonitorRendererModule — RFC 0063, the branch tree seen from inside.
//
//   renderPartyMonitor(branch, host, opts) → { refresh, handleKeydown }
//
//     opts.selfName  the monitor's own branch name; its row is selected — the
//                    honest proof the view is live (D9)
//     opts.view      the projection to draw; defaults to viewParty. Injectable
//                    so a test hands in a fixture and never touches the party
//     opts.tree      the tree renderer class; defaults to TreeRenderer
//
// The renderer holds exactly one thing about the tree: the frozen snapshot
// viewParty() returns (D4). The tree itself is drawn by TreeRenderer over the
// adapter's TreeNode JSON — no bespoke row rendering here.
//
// A snapshot is immutable, so each refresh builds a NEW TreeRenderer in a
// NEW sub-branch and dissolves the previous one. setData is never called
// twice on one renderer: TreeRenderer is flat and cannot release a previous
// render, and the sub-branch is the unit that can. Same move as the fence in
// MermaidPlate.
// =============================================================================

function renderPartyMonitor(branch, host, opts) {
    opts = opts || {};
    var view     = opts.view || viewParty;
    var TreeCtor = opts.tree || TreeRenderer;
    var selfName = opts.selfName || null;
    var current  = null;   // the TreeRenderer of the latest snapshot — keys go here

    var root  = branch.createElement('root', 'div');   css.addClass(root,  pm_root);
    var head  = branch.createElement('head', 'div');   css.addClass(head,  pm_head);
    var title = branch.createElement('title', 'span'); css.addClass(title, pm_title);
    var count = branch.createElement('count', 'span'); css.addClass(count, pm_count);
    var btn   = branch.createElement('refresh', 'button'); css.addClass(btn, pm_btn);
    var note  = branch.createElement('note', 'div');   css.addClass(note,  pm_note);
    var treeHost = branch.createElement('treeHost', 'div'); css.addClass(treeHost, pm_tree);
    title.textContent = 'DomOpsParty';
    btn.textContent   = 'Refresh';
    btn.setAttribute('type', 'button');
    head.appendChild(title);
    head.appendChild(count);
    head.appendChild(btn);
    root.appendChild(head);
    root.appendChild(note);
    root.appendChild(treeHost);
    host.appendChild(root);

    function refresh() {
        if (branch.hasBranch('tree')) branch.dissolveBranch('tree');
        var tb = branch.createBranch('tree');
        // Owned by the monitor's root element — retained exactly as long as the
        // monitor is mounted, which is this sub-branch's lifetime (D14).
        tb.activate(root, 'partyMonitor:tree');

        var snap  = view();
        var stats = partySnapshotStats(snap);
        var tree  = current = new TreeCtor({
            branch:      tb,
            container:   treeHost,
            data:        partySnapshotToTree(snap),
            showBadge:   true,
            showNote:    true,
            expandDepth: 99
        });
        if (selfName !== null) {
            var p = pathToBranch(snap, selfName);
            if (p) tree.selectPath(p, { reveal: true });
        }

        count.textContent = stats.branches + ' branch' + (stats.branches === 1 ? '' : 'es')
                          + ' · ' + stats.elements + ' element' + (stats.elements === 1 ? '' : 's');
        // D8 — never "no leaks". Detection waits on the engine having collected
        // the owner, so a clean tree says only that nothing has been collected.
        if (stats.collected === 0) {
            note.textContent = 'No owners collected — not the same as no leaks; detection waits on GC.';
            css.removeClass(note, pm_note_leaked);
        } else {
            note.textContent = stats.collected + ' owner' + (stats.collected === 1 ? '' : 's')
                + ' collected — a leak where the owner should have outlived the branch (D14).';
            css.addClass(note, pm_note_leaked);
        }
    }

    btn.addEventListener('click', refresh);
    refresh();
    return {
        refresh: refresh,
        // TreeRenderer owns the key semantics; the host owns WHEN keys flow. The
        // widget forwards keydown only while workspace-active (RFC 0049), and
        // it lands on whichever renderer the latest snapshot built.
        handleKeydown: function (ev) { return current ? current.handleKeydown(ev) : false; }
    };
}
