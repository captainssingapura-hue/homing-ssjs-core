// =============================================================================
// SplitGrid — a tree of rows and columns of cells sharing their space by
// ratio, the tracks, with a draggable divider between neighbours. A container
// in the relation grid's sense: the owner mints what goes in a cell, the grid
// arranges the cells and reports the arrangement, and never knows what a cell
// holds. A branch component: the caller makes a sub-branch for it and hands
// it in; dispose() dissolves it.
//
//   new SplitGrid(branch, { host, layout, minCellPx?, seam?, thickness?, onEvent? })
//     branch: the grid's own, handed unactivated
//     host:   a flex box; the grid is its item and fills it.
//     layout: SplitGridTree's — { kind: "cell", id } | { kind: "split", orientation, children: [{ node, ratio? }] }
//     thickness: how thick the lines are, in pixels; 1 unless said. The
//             splitter IS the line — one line shared by two rooms, flush on
//             both sides — and the hand reaches 3px past it either way, so a
//             line of one pixel is still seven to grab. 0 draws nothing and
//             still drags.
//     seam:   the lattice is drawn — every splitter, AND the grid's own edge,
//             in one line of one width and one colour, so that no room can
//             tell which of its sides has another room beyond it and which has
//             the end of the workspace. Off by default: a joint has no
//             presence of its own, and a grid whose cells carry their own
//             frames wants none.
//
//   grid.el
//   grid.box()                 → { w, h }: the grid's box in px, for a mirror to reflect
//   grid.cell(id)              → the cell's element, the box the owner fills (a
//                                flex column; what is put in it is its item)
//   grid.cells()               → the ids, in tree order
//   grid.layout()              → the tree with the tracks as they are now
//   grid.setRatios(path, ratios)        re-share one split; reports TracksChanged
//   grid.subdivide(id, side, newId?)    → the new cell's id: an empty cell beside
//                                the one named, on its left, right, top or
//                                bottom, each taking half the room; a sibling in
//                                the same row or column when the orientation
//                                matches, else the cell becomes a split of the
//                                two. Reports Subdivided
//   grid.seam(on)              the lattice drawn, live: every splitter and the
//                                grid's own edge, in one line, or none of them
//   grid.thickness(px?)        the lines' thickness, live; read with no argument
//   grid.splitters(id)         → the cells across a splitter of this one's own:
//                                [ { side, axis, id } ]. At most two — the panes
//                                its room can go to whole, one per side.
//   grid.heirs(id, toward?)    → the cells that gain the room if that one goes:
//                                the pane across the splitter when the cell
//                                named is it, else the group beside it.
//   grid.remove(id, toward?)   → the cell's element, detached, for the owner to
//                                move its content on; a split left with one
//                                gives way to it; the last cell cannot go.
//                                Its room goes toward the cell named, if one
//                                is: the whole of it to that cell when they
//                                share a splitter, else to the neighbour
//                                holding it; unnamed, to the one beside it.
//                                Reports Removed
//   grid.dispose()
//
// The cells are the grid's constant: a cell's element is minted once and kept
// through every re-arrangement, so what the owner put in it stays put; the
// splits, the children and the dividers are re-minted around them. Each track
// is a custom property on the child, --sg-ratio, read by its class; the least
// a cell may be is --sg-min on the root. Nothing is positioned by hand.
//
// A drag moves the divider between two neighbours and re-shares those two,
// each kept at the minimum; the change is reported once, on release. THE
// SPLITTER HAS NO PRESENCE UNTIL IT IS WANTED: nothing at rest, the rooms'
// own edges being seam enough — unless the owner asks for the lattice with
// `seam`, for a grid whose cells bring no edges; found, the handle
// is lit — the primary surface, part of the way; held, at full, and the two
// rooms it is trading are ringed for as long as it is held, which is its
// coverage. The hand reaches a little past the gutter on either side, so it
// is found before it is seen.
// `css` is injected with the styles import.
// =============================================================================

const _gridOwner = Object.freeze({ toString: () => "splitGrid" });
var _HOVER = 0.4, _HELD = 1, _TRADED = 0.55;   // the lit handle's extent of the primary surface, and the ring on the rooms it trades

class SplitGrid {
    constructor(branch, opts) {
        if (!branch) throw new Error("[SplitGrid] a branch of its own is required");
        if (!opts || !opts.host) throw new Error("[SplitGrid] opts.host is required");
        branch.activate(_gridOwner);
        this._branch = branch;
        this._minPx = opts.minCellPx == null ? 40 : Math.max(0, opts.minCellPx | 0);
        this._seam = !!opts.seam;
        this._line = opts.thickness == null ? 1 : Math.max(0, opts.thickness | 0);
        this._sink = typeof opts.onEvent === "function" ? opts.onEvent : null;
        this._cells = new Map();         // id → the cell element, kept for the grid's life
        this._splits = new Map();        // path → { el, orientation, children: [{ el, node }] }
        this._tree = SplitGridTree.validate(opts.layout);
        this._n = 0;
        this._arrangement = null;        // the sub-branch the current splits, children and dividers are minted on

        var root = branch.createElement("root", "div");
        css.addClass(root, sg_root);
        root.style.setProperty("--sg-min", this._minPx + "px");
        root.style.setProperty("--sg-line", this._line + "px");
        if (this._seam) css.addClass(root, sg_root_seam);   // the grid's own edge is a line of the lattice, not the holder's frame
        this.el = root;
        this._arrange();
        opts.host.appendChild(root);
    }

    // ── Reporting ─────────────────────────────────────────────────────────
    _fire(ev) {
        if (!this._sink) return;
        try { this._sink(ev); }
        catch (e) { console.error("[SplitGrid] onEvent threw on " + ev.kind + ":", e); }
    }

    // ── The arrangement: the tree rendered around the cells ───────────────
    _cellEl(id) {
        var el = this._cells.get(id);
        if (!el) {
            el = this._branch.createElement("cell-" + id.replace(/[^A-Za-z0-9_-]/g, "_"), "div");
            css.addClass(el, sg_cell);
            el.setAttribute("data-cell", id);
            this._cells.set(id, el);
        }
        return el;
    }
    _arrange() {
        var old = this._arrangement;
        this._dividers = [];   // kept from the minting, so the seam turns on and off without a lookup
        this._arrangement = this._branch.createBranch("arrangement-" + (++this._n));
        this._arrangement.activate(_gridOwner);
        this._splits.clear();
        var fresh = this._render(this._tree, "");
        while (this.el.firstChild) this.el.removeChild(this.el.firstChild);
        this.el.appendChild(fresh);
        if (old) { try { old.dissolve(); } catch (e) {} }
    }
    _render(node, path) {
        var branch = this._arrangement;
        var name = path === "" ? "root" : path.replace(/\//g, "_");
        if (node.kind === "cell") return this._cellEl(node.id);
        var horizontal = node.orientation === "horizontal";
        var el = branch.createElement("split-" + name, "div");
        css.addClass(el, sg_split, horizontal ? sg_split_h : sg_split_v);
        var entry = { el: el, orientation: node.orientation, children: [] };
        for (var i = 0; i < node.children.length; i++) {
            if (i > 0) {
                var divider = branch.createElement("divider-" + name + "-" + i, "div");
                css.addClass(divider, sg_divider, horizontal ? sg_divider_h : sg_divider_v);
                if (this._seam) css.addClass(divider, sg_divider_seam);
                this._dividers.push({ el: divider, horizontal: horizontal });
                divider.setAttribute("role", "separator");
                divider.setAttribute("aria-orientation", horizontal ? "vertical" : "horizontal");
                this._armDrag(divider, path, i - 1);
                el.appendChild(divider);
            }
            var child = branch.createElement("child-" + name + "-" + i, "div");
            css.addClass(child, sg_child, horizontal ? sg_child_h : sg_child_v);
            child.style.setProperty("--sg-ratio", String(node.children[i].ratio));
            child.appendChild(this._render(node.children[i].node, path === "" ? String(i) : path + "/" + i));
            entry.children.push({ el: child, node: node.children[i] });
            el.appendChild(child);
        }
        this._splits.set(path, entry);
        return el;
    }

    _apply(path, ratios) {
        var entry = this._splits.get(path);
        for (var i = 0; i < ratios.length; i++) {
            entry.children[i].node.ratio = ratios[i];
            entry.children[i].el.style.setProperty("--sg-ratio", String(ratios[i]));
        }
    }
    _ratiosOf(path) {
        var entry = this._splits.get(path), out = [];
        for (var i = 0; i < entry.children.length; i++) out.push(entry.children[i].node.ratio);
        return out;
    }

    // ── Hover and drag a divider ──────────────────────────────────────────
    _armDrag(divider, path, before) {
        var self = this;
        var hovering = false, held = false;
        function paint() {
            if (held || hovering) { css.addClass(divider, sg_divider_lit); css.extent(divider, held ? _HELD : _HOVER); }
            else { css.removeClass(divider, sg_divider_lit); css.extent(divider, null); }
        }
        divider.addEventListener("pointerenter", function () { hovering = true; paint(); });
        divider.addEventListener("pointerleave", function () { hovering = false; paint(); });
        divider.addEventListener("pointerdown", function (down) {
            if (down.button !== 0 || held) return;
            var entry = self._splits.get(path), horizontal = entry.orientation === "horizontal";
            var a = entry.children[before], b = entry.children[before + 1];
            // what the drag is trading: the two rooms either side, ringed for as long as it is held
            [a, b].forEach(function (side) { css.addClass(side.el, sg_child_lit); css.extent(side.el, _TRADED); });
            var start = horizontal ? down.clientX : down.clientY;
            var size = horizontal ? entry.el.getBoundingClientRect().width : entry.el.getBoundingClientRect().height;
            var a0 = a.node.ratio, b0 = b.node.ratio;
            if (!(size > 0)) return;
            var moved = false;
            held = true;
            paint();
            try { divider.setPointerCapture(down.pointerId); } catch (err) {}
            function onMove(e) {
                var pair = SplitGridGeometry.reshare(a0, b0, (horizontal ? e.clientX : e.clientY) - start, size, self._minPx);
                if (pair[0] === a.node.ratio) return;
                moved = true;
                a.node.ratio = pair[0]; b.node.ratio = pair[1];
                a.el.style.setProperty("--sg-ratio", String(pair[0]));
                b.el.style.setProperty("--sg-ratio", String(pair[1]));
            }
            function onEnd(e) {
                divider.removeEventListener("pointermove", onMove);
                divider.removeEventListener("pointerup", onEnd);
                divider.removeEventListener("pointercancel", onEnd);
                held = false;
                paint();
                [a, b].forEach(function (side) { css.removeClass(side.el, sg_child_lit); css.extent(side.el, null); });
                try { divider.releasePointerCapture(down.pointerId); } catch (err) {}
                if (e.type !== "pointerup") { self._apply(path, SplitGrid._restore(entry, before, a0, b0)); return; }
                if (moved) self._fire(SplitGridEvents.TracksChanged(path, self._ratiosOf(path)));
            }
            divider.addEventListener("pointermove", onMove);
            divider.addEventListener("pointerup", onEnd);
            divider.addEventListener("pointercancel", onEnd);
        });
    }
    static _restore(entry, before, a0, b0) {
        var out = [];
        for (var i = 0; i < entry.children.length; i++) out.push(i === before ? a0 : i === before + 1 ? b0 : entry.children[i].node.ratio);
        return out;
    }

    // ── The surface ───────────────────────────────────────────────────────
    box() { var r = this.el.getBoundingClientRect(); return { w: r.width, h: r.height }; }
    cell(id) { return this._cells.get(id) || null; }
    cells() { return SplitGridTree.cells(this._tree); }
    layout() { return SplitGridTree.copy(this._tree); }

    setRatios(path, ratios) {
        var entry = this._splits.get(path);
        if (!entry) throw new Error("[SplitGrid] no split at '" + path + "'");
        if (!Array.isArray(ratios) || ratios.length !== entry.children.length) throw new Error("[SplitGrid] setRatios: " + entry.children.length + " ratios expected at '" + path + "'");
        var sum = 0;
        for (var i = 0; i < ratios.length; i++) {
            if (typeof ratios[i] !== "number" || !(ratios[i] > 0)) throw new Error("[SplitGrid] setRatios: ratios must be positive numbers");
            sum += ratios[i];
        }
        var shares = [];
        for (var j = 0; j < ratios.length; j++) shares.push(ratios[j] / sum);
        this._apply(path, shares);
        this._fire(SplitGridEvents.TracksChanged(path, shares));
    }

    subdivide(id, side, newId) {
        var fresh = newId == null ? this._freshId() : String(newId);
        this._tree = SplitGridTree.subdivide(this._tree, id, side, fresh);
        this._arrange();
        this._fire(SplitGridEvents.Subdivided(id, fresh, side));
        return fresh;
    }

    /** The lines' thickness in pixels, live; read with no argument. The hand's reach past them does not change with it. */
    thickness(px) {
        if (arguments.length === 0) return this._line;
        this._line = Math.max(0, px | 0);
        this.el.style.setProperty("--sg-line", this._line + "px");
        return this;
    }

    /** The lattice drawn, live: every splitter AND the grid's own edge, or none of them. */
    seam(on) {
        this._seam = !!on;
        var want = this._seam;
        css.toggleClass(this.el, sg_root_seam, want);
        this._dividers.forEach(function (d) { css.toggleClass(d.el, sg_divider_seam, want); });
        return this;
    }

    splitters(id) { return SplitGridTree.splitters(this._tree, id); }

    heirs(id, toward) { return SplitGridTree.heirs(this._tree, id, toward); }

    remove(id, toward) {
        this._tree = SplitGridTree.remove(this._tree, id, toward);
        var el = this._cells.get(id);
        this._cells.delete(id);
        this._arrange();
        if (el && el.parentNode) el.parentNode.removeChild(el);
        this._fire(SplitGridEvents.Removed(id));
        return el || null;
    }

    _freshId() {
        var taken = this.cells(), k = taken.length;
        while (taken.indexOf("cell-" + (++k)) >= 0) {}
        return "cell-" + k;
    }

    dispose() {
        if (this.el.parentNode) this.el.parentNode.removeChild(this.el);
        this._cells.clear();
        this._splits.clear();
        this._branch.dissolve();
    }
}
