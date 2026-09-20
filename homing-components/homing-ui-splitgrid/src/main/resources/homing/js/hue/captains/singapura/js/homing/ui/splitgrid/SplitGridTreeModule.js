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
//   SplitGridTree.remove(tree, id)
//       → a new tree without the cell: its room goes to its neighbour, and a
//         split left with one child gives way to it; the last cell cannot go
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

    static remove(tree, id) {
        var out = SplitGridTree.copy(tree);
        var hit = SplitGridTree.find(out, id);
        if (!hit) throw new Error("[SplitGridTree] no cell '" + id + "'");
        if (!hit.parent) throw new Error("[SplitGridTree] the last cell cannot be removed");
        var siblings = hit.parent.children, gone = siblings[hit.index];
        siblings.splice(hit.index, 1);
        var heir = siblings[hit.index > 0 ? hit.index - 1 : 0];
        heir.ratio += gone.ratio;
        if (siblings.length > 1) return out;
        var only = siblings[0].node;                                   // a split of one gives way to its child
        var above = SplitGridTree._parentOf(out, hit.parent);
        if (!above) return only;
        above.parent.children[above.index].node = only;
        return out;
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
