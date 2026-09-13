// =============================================================================
// PartyMonitorAdapterModule — RFC 0063, a party snapshot as a TreeNode tree.
//
//   partySnapshotToTree(snap)      → TreeNode JSON for TreeRenderer
//   partySnapshotStats(snap)       → { branches, elements, collected }
//   pathToBranch(snap, name)       → child-index path into the tree, or null
//
// Pure. No DOM, no party, no state: the snapshot is frozen data and so is
// everything here. TreeRenderer's headline promise is that any conforming
// tree renders with no bespoke code — this is what makes the party's tree
// conform. A bespoke renderer was built first and retired; see the RFC.
//
// Shape: a branch is a row whose children are its ELEMENTS (as leaf rows,
// "name <tag>") followed by its sub-branches, so the party and what it owns
// read as one tree. The badge carries depth and the collected mark; the note
// carries the owner label. TreeRenderer selects by child-index path, and
// pathToBranch computes one over exactly the ordering used here, so the
// monitor can select its own node.
// =============================================================================

function _branchNode(b) {
    var leaked = b.ownerAlive === false;
    var children = [];
    for (var i = 0; i < b.elements.length; i++) {
        children.push({
            level: b.depth + 1,
            segment: 'el-' + b.elements[i].name,
            display: { kind: 'element', label: b.elements[i].name + ' <' + b.elements[i].tagName + '>',
                       note: null, badge: null },
            children: []
        });
    }
    for (var j = 0; j < b.branches.length; j++) children.push(_branchNode(b.branches[j]));
    var badge = 'L' + b.depth
        + (b.ownerAlive === null ? '' : (leaked ? ' ⚠ collected' : ' ● alive'))
        + (b.elements.length ? ' · ' + b.elements.length + ' el' : '');
    return {
        level: b.depth,
        segment: b.name,
        display: { kind: leaked ? 'leaked' : 'branch', label: b.name, note: b.owner, badge: badge },
        children: children
    };
}

function partySnapshotToTree(snap) { return _branchNode(snap); }

function partySnapshotStats(snap) {
    var s = { branches: 0, elements: 0, collected: 0 };
    (function walk(b) {
        s.branches += 1;
        s.elements += b.elements.length;
        if (b.ownerAlive === false) s.collected += 1;
        for (var i = 0; i < b.branches.length; i++) walk(b.branches[i]);
    })(snap);
    return s;
}

// The child-index path TreeRenderer wants ([] is the root). Elements come
// before sub-branches in _branchNode, so a sub-branch's index is offset by the
// parent's element count — the one fact this function and _branchNode share.
function pathToBranch(snap, name) {
    var found = null;
    (function walk(b, path) {
        if (found) return;
        if (b.name === name) { found = path; return; }
        for (var i = 0; i < b.branches.length; i++) {
            walk(b.branches[i], path.concat([b.elements.length + i]));
        }
    })(snap, []);
    return found;
}
