// =============================================================================
// SplitGrid — a tree of rows and columns of cells sharing their space by
// ratio, the tracks, with a draggable divider between neighbours. A container
// in the relation grid's sense: the owner mints what goes in a cell, the grid
// arranges the cells and reports the arrangement, and never knows what a cell
// holds. A branch component: the caller makes a sub-branch for it and hands
// it in; dispose() dissolves it.
//
//   new SplitGrid(branch, { host, layout, minCellPx?, onEvent? })
//     branch: the grid's own, handed unactivated
//     host:   a flex box; the grid is its item and fills it.
//     layout: SplitGridTree's — { kind: "cell", id } | { kind: "split", orientation, children: [{ node, ratio? }] }
//
//   grid.el
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
//   grid.remove(id)            → the cell's element, detached, for the owner to
//                                move its content on; its room goes to its
//                                neighbour, a split left with one gives way to
//                                it; the last cell cannot go. Reports Removed
//   grid.dispose()
//
// The cells are the grid's constant: a cell's element is minted once and kept
// through every re-arrangement, so what the owner put in it stays put; the
// splits, the children and the dividers are re-minted around them. Each track
// is a custom property on the child, --sg-ratio, read by its class; the least
// a cell may be is --sg-min on the root. Nothing is positioned by hand.
//
// A drag moves the divider between two neighbours and re-shares those two,
// each kept at the minimum; the change is reported once, on release. Hovered
// or held, the handle is lit: it wears the primary surface by extent — part
// of the way while hovered, at full while held — and nothing at rest.
// `css` is injected with the styles import.
// =============================================================================

const _gridOwner = Object.freeze({ toString: () => "splitGrid" });
var _HOVER = 0.4, _HELD = 1;   // the lit handle's extent of the primary surface

class SplitGrid {
    constructor(branch, opts) {
        if (!branch) throw new Error("[SplitGrid] a branch of its own is required");
        if (!opts || !opts.host) throw new Error("[SplitGrid] opts.host is required");
        branch.activate(_gridOwner);
        this._branch = branch;
        this._minPx = opts.minCellPx == null ? 40 : Math.max(0, opts.minCellPx | 0);
        this._sink = typeof opts.onEvent === "function" ? opts.onEvent : null;
        this._cells = new Map();         // id → the cell element, kept for the grid's life
        this._splits = new Map();        // path → { el, orientation, children: [{ el, node }] }
        this._tree = SplitGridTree.validate(opts.layout);
        this._n = 0;
        this._arrangement = null;        // the sub-branch the current splits, children and dividers are minted on

        var root = branch.createElement("root", "div");
        css.addClass(root, sg_root);
        root.style.setProperty("--sg-min", this._minPx + "px");
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
            var start = horizontal ? down.clientX : down.clientY;
            var size = horizontal ? entry.el.getBoundingClientRect().width : entry.el.getBoundingClientRect().height;
            var a0 = a.node.ratio, b0 = b.node.ratio;
            if (!(size > 0)) return;
            var minShare = Math.min(self._minPx / size, (a0 + b0) / 2);
            var moved = false;
            held = true;
            paint();
            try { divider.setPointerCapture(down.pointerId); } catch (err) {}
            function onMove(e) {
                var delta = ((horizontal ? e.clientX : e.clientY) - start) / size;
                var na = Math.min(Math.max(a0 + delta, minShare), a0 + b0 - minShare);
                var nb = a0 + b0 - na;
                if (na === a.node.ratio) return;
                moved = true;
                a.node.ratio = na; b.node.ratio = nb;
                a.el.style.setProperty("--sg-ratio", String(na));
                b.el.style.setProperty("--sg-ratio", String(nb));
            }
            function onEnd(e) {
                divider.removeEventListener("pointermove", onMove);
                divider.removeEventListener("pointerup", onEnd);
                divider.removeEventListener("pointercancel", onEnd);
                held = false;
                paint();
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

    remove(id) {
        this._tree = SplitGridTree.remove(this._tree, id);
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
