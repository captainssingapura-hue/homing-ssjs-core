// =============================================================================
// SplitGridGeometry — the arrangement resolved into rectangles, and the rules
// that read them. No element in sight: what the browser's flex computes for
// the grid, computed here to the pixel, so a mirror draws the same picture,
// a drag's delta becomes ratios without the DOM, and "the cell beside this
// one" is a rule that can be tested.
//
//   SplitGridGeometry.rects(tree, box, opts?)
//       tree   SplitGridTree's, ratios normalised;  box { w, h } in px
//       opts   { dividerPx: 1, minPx: 40 }  the line's thickness — the splitter IS the
//              line — and the least a cell may be
//       → { cells: { id: { x, y, w, h } }, dividers: [ { path, index, orientation, x, y, w, h } ] }
//     A split shares its axis as flex does: each child ratio × the room left
//     after the dividers, and a child under the minimum is held there while
//     the rest re-share — frozen one by one until none is under.
//   SplitGridGeometry.shares(ratios, available, minPx)  → the sizes, in px
//   SplitGridGeometry.reshare(a0, b0, deltaPx, sizePx, minPx)  → [a, b]
//       two neighbours re-shared by a divider moved deltaPx along a split sizePx
//       long, each kept at the minimum — the drag's arithmetic
//   SplitGridGeometry.neighbour(rects, fromId, direction, opts?)  → id or null
//       the cell beside one — "left", "right", "up", "down" — by the workspace's
//       rule: a candidate's edge lies beyond the cell's on that side, with
//       positive overlap across; the nearest gap wins, ties by the larger
//       overlap; an outer edge gives null. opts { gapPx: 24, epsPx: 2 }
//   SplitGridGeometry.hit(rects, x, y)  → { kind: "cell", id } | { kind: "divider", path, index } | null
// =============================================================================

var _DIVIDER = 1, _MIN = 40, _GAP = 24, _EPS = 2;

class SplitGridGeometry {
    static rects(tree, box, opts) {
        var o = { dividerPx: opts && opts.dividerPx != null ? opts.dividerPx : _DIVIDER, minPx: opts && opts.minPx != null ? opts.minPx : _MIN };
        var out = { cells: {}, dividers: [] };
        SplitGridGeometry._place(tree, 0, 0, box.w, box.h, "", out, o);
        return out;
    }

    static _place(node, x, y, w, h, path, out, o) {
        if (node.kind === "cell") { out.cells[node.id] = { x: x, y: y, w: w, h: h }; return; }
        var horizontal = node.orientation === "horizontal", n = node.children.length;
        var along = horizontal ? w : h;
        var ratios = [];
        for (var i = 0; i < n; i++) ratios.push(node.children[i].ratio);
        var sizes = SplitGridGeometry.shares(ratios, along - (n - 1) * o.dividerPx, o.minPx);
        var at = 0;
        for (var j = 0; j < n; j++) {
            if (j > 0) {
                out.dividers.push(horizontal ? { path: path, index: j - 1, orientation: "horizontal", x: x + at, y: y, w: o.dividerPx, h: h }
                                             : { path: path, index: j - 1, orientation: "vertical", x: x, y: y + at, w: w, h: o.dividerPx });
                at += o.dividerPx;
            }
            var childPath = path === "" ? String(j) : path + "/" + j;
            if (horizontal) SplitGridGeometry._place(node.children[j].node, x + at, y, sizes[j], h, childPath, out, o);
            else SplitGridGeometry._place(node.children[j].node, x, y + at, w, sizes[j], childPath, out, o);
            at += sizes[j];
        }
    }

    /** Flex with a minimum: ratio × available each, then the ones under the minimum held there while the rest re-share, until none is under. */
    static shares(ratios, available, minPx) {
        var n = ratios.length, sizes = new Array(n), frozen = new Array(n), room = Math.max(0, available), sumFree;
        for (var i = 0; i < n; i++) frozen[i] = false;
        for (var pass = 0; pass <= n; pass++) {
            sumFree = 0;
            for (var a = 0; a < n; a++) if (!frozen[a]) sumFree += ratios[a];
            var under = -1;
            for (var b = 0; b < n; b++) {
                if (frozen[b]) continue;
                sizes[b] = sumFree > 0 ? ratios[b] / sumFree * room : 0;
                if (sizes[b] < minPx && under < 0) under = b;
            }
            if (under < 0) break;
            frozen[under] = true;
            sizes[under] = minPx;
            room = Math.max(0, room - minPx);
        }
        return sizes;
    }

    /** The drag's arithmetic: a0 and b0 share what they shared, moved by delta over the split's size, each kept at the minimum. */
    static reshare(a0, b0, deltaPx, sizePx, minPx) {
        if (!(sizePx > 0)) return [a0, b0];
        var minShare = Math.min(minPx / sizePx, (a0 + b0) / 2);
        var a = Math.min(Math.max(a0 + deltaPx / sizePx, minShare), a0 + b0 - minShare);
        return [a, a0 + b0 - a];
    }

    static _overlap(a0, a1, b0, b1) { return Math.min(a1, b1) - Math.max(a0, b0); }

    /** The workspace's rule: beyond the edge on that side, overlapping across; the nearest gap, then the larger overlap. */
    static neighbour(rects, fromId, direction, opts) {
        var P = rects.cells[fromId];
        if (!P) return null;
        var gapMax = opts && opts.gapPx != null ? opts.gapPx : _GAP, eps = opts && opts.epsPx != null ? opts.epsPx : _EPS;
        var best = null, bestGap = Infinity, bestOv = -1;
        var pr = P.x + P.w, pb = P.y + P.h;
        Object.keys(rects.cells).forEach(function (id) {
            if (id === fromId) return;
            var Q = rects.cells[id], qr = Q.x + Q.w, qb = Q.y + Q.h, onSide, gap, ov;
            if (direction === "right")     { onSide = Q.x >= pr - eps;  gap = Q.x - pr;  ov = SplitGridGeometry._overlap(P.y, pb, Q.y, qb); }
            else if (direction === "left") { onSide = qr <= P.x + eps;  gap = P.x - qr;  ov = SplitGridGeometry._overlap(P.y, pb, Q.y, qb); }
            else if (direction === "down") { onSide = Q.y >= pb - eps;  gap = Q.y - pb;  ov = SplitGridGeometry._overlap(P.x, pr, Q.x, qr); }
            else if (direction === "up")   { onSide = qb <= P.y + eps;  gap = P.y - qb;  ov = SplitGridGeometry._overlap(P.x, pr, Q.x, qr); }
            else throw new Error("[SplitGridGeometry] direction must be left, right, up or down");
            if (!onSide || ov <= 0 || gap > gapMax) return;
            if (gap < bestGap - eps || (Math.abs(gap - bestGap) <= eps && ov > bestOv)) { best = id; bestGap = gap; bestOv = ov; }
        });
        return best;
    }

    static hit(rects, x, y) {
        for (var i = 0; i < rects.dividers.length; i++) {
            var d = rects.dividers[i];
            if (x >= d.x && x < d.x + d.w && y >= d.y && y < d.y + d.h) return { kind: "divider", path: d.path, index: d.index };
        }
        var ids = Object.keys(rects.cells);
        for (var j = 0; j < ids.length; j++) {
            var c = rects.cells[ids[j]];
            if (x >= c.x && x < c.x + c.w && y >= c.y && y < c.y + c.h) return { kind: "cell", id: ids[j] };
        }
        return null;
    }
}
