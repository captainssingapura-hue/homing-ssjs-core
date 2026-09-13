// =============================================================================
// PartyMonitorRendererModule — RFC 0063, the branch tree seen from inside.
//
//   renderPartyMonitor(branch, host, opts) → { refresh }
//
//     opts.selfName  the monitor's own branch name, highlighted in the tree —
//                    the honest proof the view is live (D9)
//     opts.view      the projection to draw; defaults to viewParty. Injectable
//                    so a test can hand in a fixture and never touch the party
//
// The renderer holds exactly one thing about the tree: the frozen snapshot
// viewParty() returns. No branch handle, no element, no way back (D4). Its OWN
// elements come from the branch every widget receives, like any view.
//
// Each refresh draws into a fresh sub-branch and dissolves the previous one —
// the same move MermaidPlate makes for its fence — so a re-render is one
// dissolveBranch and no element is ever detached by hand.
// =============================================================================

function renderPartyMonitor(branch, host, opts) {
    opts = opts || {};
    var view     = opts.view || viewParty;
    var selfName = opts.selfName || null;

    var root  = branch.createElement('root', 'div');   css.addClass(root,  pm_root);
    var head  = branch.createElement('head', 'div');   css.addClass(head,  pm_head);
    var title = branch.createElement('title', 'span'); css.addClass(title, pm_title);
    var count = branch.createElement('count', 'span'); css.addClass(count, pm_count);
    var btn   = branch.createElement('refresh', 'button'); css.addClass(btn, pm_btn);
    var note  = branch.createElement('note', 'div');   css.addClass(note,  pm_note);
    title.textContent = 'DomOpsParty';
    btn.textContent   = 'Refresh';
    btn.setAttribute('type', 'button');
    head.appendChild(title);
    head.appendChild(count);
    head.appendChild(btn);
    root.appendChild(head);
    root.appendChild(note);
    host.appendChild(root);

    function refresh() {
        if (branch.hasBranch('tree')) branch.dissolveBranch('tree');
        var tb = branch.createBranch('tree');
        // Owned by the monitor's root element — retained for exactly as long
        // as the monitor is mounted, which is this sub-branch's rightful
        // lifetime (D14). Labelled so it reads in its own tree.
        tb.activate(root, 'partyMonitor:tree');

        var snap  = view();
        var stats = { branches: 0, elements: 0, collected: 0 };
        var seq   = { n: 0 };
        var treeEl = tb.createElement('tree', 'div');
        css.addClass(treeEl, pm_tree);
        treeEl.appendChild(_renderNode(tb, snap, stats, seq, selfName));
        root.appendChild(treeEl);

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
    return { refresh: refresh };
}

// One node: header row, element chips, then children. Element names are
// sequenced because a branch's element names must be unique, and the whole
// sub-branch is dissolved on refresh so the sequence restarts each time.
function _renderNode(tb, node, stats, seq, selfName) {
    stats.branches += 1;
    stats.elements += node.elements.length;
    var leaked = node.ownerAlive === false;
    if (leaked) stats.collected += 1;
    var id = ++seq.n;

    var wrap = tb.createElement('n' + id, 'div');
    css.addClass(wrap, pm_node);

    var header = tb.createElement('h' + id, 'div');
    css.addClass(header, pm_node_header);
    if (leaked) css.addClass(header, pm_node_header_leaked);
    if (selfName !== null && node.name === selfName) css.addClass(header, pm_node_header_self);

    var icon = tb.createElement('i' + id, 'span');
    css.addClass(icon, pm_icon);
    icon.textContent = node.branches.length > 0 ? '▾' : '○';   // ▾ / ○

    var name = tb.createElement('nm' + id, 'span');
    css.addClass(name, pm_name);
    name.textContent = node.name;

    var depth = tb.createElement('d' + id, 'span');
    css.addClass(depth, pm_depth_badge);
    depth.textContent = 'L' + node.depth;

    header.appendChild(icon);
    header.appendChild(name);
    header.appendChild(depth);

    // Owner: label, then the dot — only when the branch was ever activated.
    if (node.owner !== null) {
        var owner = tb.createElement('o' + id, 'span');
        css.addClass(owner, pm_owner);
        owner.textContent = node.owner;
        header.appendChild(owner);
    }
    if (node.ownerAlive !== null) {
        var dot = tb.createElement('dot' + id, 'span');
        css.addClass(dot, pm_owner_dot);
        css.addClass(dot, leaked ? pm_owner_dot_leaked : pm_owner_dot_alive);
        dot.setAttribute('title', leaked ? 'owner collected' : 'owner alive');
        header.appendChild(dot);
    }
    if (node.elements.length > 0) {
        var eb = tb.createElement('eb' + id, 'span');
        css.addClass(eb, pm_badge);
        eb.textContent = node.elements.length + ' el';
        header.appendChild(eb);
    }
    if (node.branches.length > 0) {
        var bb = tb.createElement('bb' + id, 'span');
        css.addClass(bb, pm_badge);
        bb.textContent = node.branches.length + ' branch' + (node.branches.length === 1 ? '' : 'es');
        header.appendChild(bb);
    }
    wrap.appendChild(header);

    if (node.elements.length > 0) {
        var list = tb.createElement('el' + id, 'ul');
        css.addClass(list, pm_elements);
        for (var k = 0; k < node.elements.length; k++) {
            var chip = tb.createElement('c' + id + '_' + k, 'li');
            css.addClass(chip, pm_chip);
            chip.textContent = node.elements[k].name + ' <' + node.elements[k].tagName + '>';
            list.appendChild(chip);
        }
        wrap.appendChild(list);
    }

    if (node.branches.length > 0) {
        var kids = tb.createElement('k' + id, 'div');
        css.addClass(kids, pm_branches);
        for (var j = 0; j < node.branches.length; j++) {
            kids.appendChild(_renderNode(tb, node.branches[j], stats, seq, selfName));
        }
        wrap.appendChild(kids);
        // Fold on the header; the icon says which way it is.
        header.addEventListener('click', function () {
            var folded = css.hasClass(kids, pm_folded);
            css.toggleClass(kids, pm_folded);
            icon.textContent = folded ? '▾' : '▸';   // ▾ / ▸
        });
    }
    return wrap;
}
