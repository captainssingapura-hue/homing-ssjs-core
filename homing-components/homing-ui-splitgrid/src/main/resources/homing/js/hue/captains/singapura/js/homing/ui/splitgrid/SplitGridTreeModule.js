// =============================================================================
// SplitGridTree — the grid's arrangement as data, and the algebra on it. No
// element in sight: an arrangement can be validated, copied, subdivided,
// reduced and searched apart from any grid, persisted and replayed.
//
//   a layout:  { kind: "cell", id }
//            | { kind: "split", orientation: "horizontal" | "vertical",
//                children: [ { node: <layout>, ratio }, … ] }        two or more
//     "horizontal" puts the children in a row, "vertical" stacks them. The
//     ratios are the split's tracks: shares of its space, summing to one.
//
//   SplitGridTree.validate(layout)      → a normalised copy, or throws: every id once,
//                                          every split two or more, ratios normalised
//   SplitGridTree.copy(tree)            → a deep copy
//   SplitGridTree.cells(tree)           → the ids, in tree order
//   SplitGridTree.find(tree, id)        → { node, parent, index, path } or null; path
//                                          names the parent split from the root
//   SplitGridTree.subdivide(tree, id, side, newId)
//       → a new tree with an empty cell beside `id` on `side`: a sibling in the
//         same row or column when the parent's orientation matches, else the
//         cell becomes a split of the two; each takes half the room
//   SplitGridTree.splitters(tree, id)
//       → the panes across a splitter of this one's own — a whole divider with
//         this pane alone on one side and that pane alone on the other:
//         [ { side: "before" | "after", axis, id, at } ]. At most two, one per
//         side of its parent's axis. These are exactly the panes it can merge
//         with: any other neighbour shares its divider with somebody.
//   SplitGridTree.heirs(tree, id, toward?)
//       → the cells that gain the room if that cell goes: the one pane across
//         the splitter when the cell named is that pane, else every cell in
//         the group beside it, which grows together
//   SplitGridTree.remove(tree, id, toward?)
//       → a new tree without the cell, and a split left with one child gives
//         way to it; the last cell cannot go. Its room goes toward the cell
//         named, if one is: the whole of it to that pane when they share a
//         splitter of their own, else to the neighbour holding it. Unnamed, or
//         named somewhere else, the room goes to the neighbour beside it.
//
// Every operation returns a new tree and leaves the one given alone.
// =============================================================================

var _SIDE = Object.freeze({ left: "horizontal", right: "horizontal", top: "vertical", bottom: "vertical" });

class SplitGridTree {
    static validate(node) { return SplitGridTree._validate(node, "layout", new Set()); }

    static _validate(node, at, seen) {
        if (!node || typeof node !== "object") throw new Error("[SplitGridTree] " + at + " must be a cell or a split");
        if (node.kind === "cell") {
            if (typeof node.id !== "string" || !node.id) throw new Error("[SplitGridTree] " + at + ".id must be a non-empty string");
            if (seen.has(node.id)) throw new Error("[SplitGridTree] cell '" + node.id + "' appears twice");
            seen.add(node.id);
            return { kind: "cell", id: node.id };
        }
        if (node.kind !== "split") throw new Error("[SplitGridTree] " + at + ".kind must be 'cell' or 'split'");
        if (node.orientation !== "horizontal" && node.orientation !== "vertical") throw new Error("[SplitGridTree] " + at + ".orientation must be 'horizontal' or 'vertical'");
        if (!Array.isArray(node.children) || node.children.length < 2) throw new Error("[SplitGridTree] " + at + " needs two or more children");
        var children = [], sum = 0;
        for (var i = 0; i < node.children.length; i++) {
            var c = node.children[i];
            var r = c && c.ratio != null ? c.ratio : 1;
            if (typeof r !== "number" || !(r > 0)) throw new Error("[SplitGridTree] " + at + ".children[" + i + "].ratio must be a positive number");
            children.push({ node: SplitGridTree._validate(c && c.node, at + ".children[" + i + "].node", seen), ratio: r });
            sum += r;
        }
        for (var j = 0; j < children.length; j++) children[j].ratio = children[j].ratio / sum;
        return { kind: "split", orientation: node.orientation, children: children };
    }

    static copy(node) {
        if (node.kind === "cell") return { kind: "cell", id: node.id };
        var children = [];
        for (var i = 0; i < node.children.length; i++) children.push({ node: SplitGridTree.copy(node.children[i].node), ratio: node.children[i].ratio });
        return { kind: "split", orientation: node.orientation, children: children };
    }

    static cells(node) {
        if (node.kind === "cell") return [node.id];
        var out = [];
        for (var i = 0; i < node.children.length; i++) out = out.concat(SplitGridTree.cells(node.children[i].node));
        return out;
    }

    /** The cell's node, its parent split (null at the root), its index in it, and the parent's path from the root. */
    static find(node, id) { return SplitGridTree._find(node, id, null, -1, "", ""); }

    static _find(node, id, parent, index, parentPath, nodePath) {
        if (node.kind === "cell") return node.id === id ? { node: node, parent: parent, index: index, path: parentPath } : null;
        for (var i = 0; i < node.children.length; i++) {
            var hit = SplitGridTree._find(node.children[i].node, id, node, i, nodePath, nodePath === "" ? String(i) : nodePath + "/" + i);
            if (hit) return hit;
        }
        return null;
    }

    static subdivide(tree, id, side, newId) {
        var orientation = _SIDE[side];
        if (!orientation) throw new Error("[SplitGridTree] side must be left, right, top or bottom");
        if (typeof newId !== "string" || !newId) throw new Error("[SplitGridTree] newId must be a non-empty string");
        if (SplitGridTree.cells(tree).indexOf(newId) >= 0) throw new Error("[SplitGridTree] cell '" + newId + "' already exists");
        var out = SplitGridTree.copy(tree);
        var hit = SplitGridTree.find(out, id);
        if (!hit) throw new Error("[SplitGridTree] no cell '" + id + "'");
        var before = side === "left" || side === "top";
        var fresh = { kind: "cell", id: newId };
        if (hit.parent && hit.parent.orientation === orientation) {   // a sibling in the same row or column: the room is halved between the two
            var share = hit.parent.children[hit.index].ratio / 2;
            hit.parent.children[hit.index].ratio = share;
            hit.parent.children.splice(before ? hit.index : hit.index + 1, 0, { node: fresh, ratio: share });
            return out;
        }
        var pair = before ? [{ node: fresh, ratio: 0.5 }, { node: hit.node, ratio: 0.5 }] : [{ node: hit.node, ratio: 0.5 }, { node: fresh, ratio: 0.5 }];
        var split = { kind: "split", orientation: orientation, children: pair };
        if (!hit.parent) return split;
        hit.parent.children[hit.index].node = split;
        return out;
    }

    /**
     * The panes across a splitter of this one's own: a whole divider with this
     * pane alone on one side and that pane alone on the other. At most two —
     * a pane is one of two or more children along its parent's axis, so it is
     * always shorter than a divider further up and can never own one; and once
     * it is not the edge child on a side, nothing above offers that side either.
     */
    static splitters(tree, id) {
        var chain = SplitGridTree._chain(tree, id, []);
        if (!chain) throw new Error("[SplitGridTree] no cell '" + id + "'");
        var out = [], axis = null, before = true, after = true;
        for (var k = chain.length - 1; k >= 0 && (before || after); k--) {
            var s = chain[k].split, i = chain[k].index, last = s.children.length - 1, far;
            if (axis === null) axis = s.orientation;
            else if (s.orientation !== axis) break;              // a cross split: its dividers lie the other way
            if (before && i > 0) {
                far = SplitGridTree._facing(s.children[i - 1].node, axis, false);
                if (far) out.push({ side: "before", axis: axis, id: far, at: k });
            }
            if (after && i < last) {
                far = SplitGridTree._facing(s.children[i + 1].node, axis, true);
                if (far) out.push({ side: "after", axis: axis, id: far, at: k });
            }
            if (i > 0) before = false;                           // not the edge child: nothing above is its own either
            if (i < last) after = false;
        }
        return out;
    }

    /** The one pane facing a divider from the far side, or null where that side is shared by several. */
    static _facing(node, axis, first) {
        while (node.kind === "split") {
            if (node.orientation !== axis) return null;
            node = node.children[first ? 0 : node.children.length - 1].node;
        }
        return node.id;
    }

    /** The splits from the root down to the cell, each with the child taken. */
    static _chain(node, id, acc) {
        if (node.kind === "cell") return node.id === id ? acc : null;
        for (var i = 0; i < node.children.length; i++) {
            var hit = SplitGridTree._chain(node.children[i].node, id, acc.concat([{ split: node, index: i }]));
            if (hit) return hit;
        }
        return null;
    }

    /** Who gains the room when the cell goes: one pane across a splitter of its own, or the group beside it. */
    static heirs(tree, id, toward) {
        var over = toward ? SplitGridTree.splitters(tree, id).filter(function (s) { return s.id === toward; })[0] : null;
        if (over) return [over.id];
        var hit = SplitGridTree.find(tree, id);
        if (!hit || !hit.parent) return [];
        return SplitGridTree.cells(hit.parent.children[SplitGridTree._lean(hit, toward)].node);
    }

    static remove(tree, id, toward) {
        var out = SplitGridTree.copy(tree);
        var hit = SplitGridTree.find(out, id);
        if (!hit) throw new Error("[SplitGridTree] no cell '" + id + "'");
        if (!hit.parent) throw new Error("[SplitGridTree] the last cell cannot be removed");
        if (toward && toward !== id) {
            var over = SplitGridTree.splitters(out, id).filter(function (s) { return s.id === toward; })[0];
            if (over) return SplitGridTree._across(out, id, over);
        }
        var siblings = hit.parent.children, gone = siblings[hit.index];
        var lean = SplitGridTree._lean(hit, toward);
        siblings.splice(hit.index, 1);
        var heir = siblings[lean > hit.index ? hit.index : lean];
        heir.ratio += gone.ratio;
        if (siblings.length > 1) return out;
        var only = siblings[0].node;                                   // a split of one gives way to its child
        var above = SplitGridTree._parentOf(out, hit.parent);
        if (!above) return only;
        above.parent.children[above.index].node = only;
        return out;
    }

    /** Which neighbour the room leans to: the one holding the cell named, else the one before it, else the one after. */
    static _lean(hit, toward) {
        var kids = hit.parent.children, i = hit.index, j;
        if (toward) for (var d = 0; d < 2; d++) {
            j = d ? i + 1 : i - 1;
            if (j >= 0 && j < kids.length && SplitGridTree.cells(kids[j].node).indexOf(toward) >= 0) return j;
        }
        return i > 0 ? i - 1 : i + 1;
    }

    /**
     * The room crosses the splitter: the pane goes and the one facing it over
     * that divider takes the whole of its room, every other pane keeping the
     * size it had. The divider's two sides are children of one split, so the
     * share is taken off the one and given to the other; within each it runs
     * along the parallel chain to the pane at the end of it.
     */
    static _across(out, id, over) {
        var chain = SplitGridTree._chain(out, id, []);
        var s = chain[over.at].split, i = chain[over.at].index, j = over.side === "before" ? i - 1 : i + 1;
        var mine = s.children[i], theirs = s.children[j], share = mine.ratio;
        for (var k = over.at + 1; k < chain.length; k++) share *= chain[k].split.children[chain[k].index].ratio;
        SplitGridTree._gains(theirs.node, theirs.ratio, share, over.id);
        theirs.ratio += share;
        var left = SplitGridTree._loses(mine.node, mine.ratio, share, id);
        if (left) { mine.node = left; mine.ratio -= share; }
        else s.children.splice(i, 1);
        if (s.children.length > 1) return out;
        var only = s.children[0].node, above = SplitGridTree._parentOf(out, s);
        if (!above) return only;
        above.parent.children[above.index].node = only;
        return out;
    }

    /** The subtree grows by what the pane left, and all of the gain goes to the one pane named. */
    static _gains(node, have, extra, id) {
        if (node.kind === "cell") return;
        var kids = node.children, total = have + extra, i = SplitGridTree._holding(kids, id), at = have * kids[i].ratio;
        for (var j = 0; j < kids.length; j++) kids[j].ratio = (j === i ? at + extra : have * kids[j].ratio) / total;
        SplitGridTree._gains(kids[i].node, at, extra, id);
    }

    /** The subtree loses the pane and shrinks by its share; everything else in it keeps the size it had. */
    static _loses(node, have, share, id) {
        if (node.kind === "cell") return null;
        var kids = node.children, total = have - share, i = SplitGridTree._holding(kids, id), at = have * kids[i].ratio;
        var inner = SplitGridTree._loses(kids[i].node, at, share, id), sizes = [];
        for (var j = 0; j < kids.length; j++) sizes.push(j === i ? at - share : have * kids[j].ratio);
        if (inner) kids[i].node = inner; else { kids.splice(i, 1); sizes.splice(i, 1); }
        for (var m = 0; m < kids.length; m++) kids[m].ratio = sizes[m] / total;
        return kids.length > 1 ? node : kids[0].node;
    }

    /** Which child holds the cell. */
    static _holding(kids, id) {
        for (var i = 0; i < kids.length; i++) if (SplitGridTree.cells(kids[i].node).indexOf(id) >= 0) return i;
        return -1;
    }

    static _parentOf(node, target, parent, index) {
        if (node === target) return parent ? { parent: parent, index: index } : null;
        if (node.kind === "cell") return null;
        for (var i = 0; i < node.children.length; i++) {
            var hit = SplitGridTree._parentOf(node.children[i].node, target, node, i);
            if (hit) return hit;
        }
        return null;
    }
}
