// =============================================================================
// SplitGridMirror — the arrangement of a grid, drawn at a scale, with a cursor
// the keyboard moves from cell to cell. A branch component: the caller makes
// a sub-branch for it and hands it in; dispose() dissolves it. It never
// touches the grid: the owner tells it the layout and the box, and it draws
// them from the geometry — the same rectangles the grid has, scaled.
//
//   new SplitGridMirror(branch, { host, scale?, dividerPx?, minPx?, onEvent? })
//     host   where the mirror's box goes; scale 0.25 by default
//
//   mirror.el                    the box; focusable
//   mirror.reflect(layout, box)  the grid's layout() and its box { w, h }: redrawn
//   mirror.scale(s?)             read, or set and redraw
//   mirror.cursor(id?)           read, or set: the cell the cursor is at, marked
//                                Current here; reports CursorMoved(id, "call")
//   mirror.rects()               the geometry as drawn, unscaled
//   mirror.dispose()
//
// The keyboard, while the mirror has focus: the arrows move the cursor to the
// cell beside it by the workspace's rule — SplitGridGeometry.neighbour — and
// report CursorMoved(id, direction); an outer edge moves nothing and says
// nothing. A press on a cell puts the cursor there: CursorMoved(id, "pointer").
// When the cell the cursor was at is gone after a reflect, the cursor goes to
// the first cell, reported as "call". The owner decides what the cursor means
// — the active pane, the one to switch to — and marks the real cell itself.
// `css` is injected with the styles import.
// =============================================================================

const _mirrorOwner = Object.freeze({ toString: () => "splitGridMirror" });
var _KEYS = Object.freeze({ ArrowLeft: "left", ArrowRight: "right", ArrowUp: "up", ArrowDown: "down" });

class SplitGridMirror {
    constructor(branch, opts) {
        if (!branch) throw new Error("[SplitGridMirror] a branch of its own is required");
        if (!opts || !opts.host) throw new Error("[SplitGridMirror] opts.host is required");
        var self = this;
        branch.activate(_mirrorOwner);
        this._branch = branch;
        this._sink = typeof opts.onEvent === "function" ? opts.onEvent : null;
        this._scale = opts.scale == null ? 0.25 : Math.max(0.05, Math.min(1, Number(opts.scale) || 0.25));
        this._opts = { dividerPx: opts.dividerPx, minPx: opts.minPx };
        this._layout = null;
        this._box = { w: 0, h: 0 };
        this._rects = { cells: {}, dividers: [] };
        this._cursor = null;
        this._cells = new Map();          // id → the drawn box
        this._n = 0;
        this._drawing = null;             // the sub-branch the boxes are minted on; dissolved on every redraw

        var el = branch.createElement("mirror", "div");
        css.addClass(el, sgm_root);
        el.tabIndex = 0;
        el.setAttribute("role", "group");
        el.setAttribute("aria-label", "Layout");
        el.addEventListener("keydown", function (e) {
            var direction = _KEYS[e.key];
            if (!direction || self._cursor === null) return;
            var to = SplitGridGeometry.neighbour(self._rects, self._cursor, direction);
            e.preventDefault();
            e.stopPropagation();
            if (to) self._moveCursor(to, direction);
        });
        opts.host.appendChild(el);
        this.el = el;
        this._size();
    }

    reflect(layout, box) {
        this._layout = SplitGridTree.validate(layout);
        this._box = { w: Math.max(0, Number(box && box.w) || 0), h: Math.max(0, Number(box && box.h) || 0) };
        this._draw();
        var ids = SplitGridTree.cells(this._layout);
        if (this._cursor !== null && ids.indexOf(this._cursor) < 0) this._moveCursor(ids[0] || null, "call");
        else if (this._cursor === null && ids.length) this._moveCursor(ids[0], "call");
        return this;
    }

    scale(s) {
        if (s === undefined) return this._scale;
        this._scale = Math.max(0.05, Math.min(1, Number(s) || this._scale));
        this._draw();
        return this;
    }

    cursor(id) {
        if (id === undefined) return this._cursor;
        if (id !== null && !this._cells.has(id)) throw new Error("[SplitGridMirror] no cell '" + id + "'");
        this._moveCursor(id, "call");
        return this;
    }

    rects() { return this._rects; }

    dispose() {
        if (this.el.parentNode) this.el.parentNode.removeChild(this.el);
        this._cells.clear();
        this._branch.dissolve();
    }

    // ── the picture ─────────────────────────────────────────────────────────
    _size() {
        this.el.style.setProperty("--sgm-w", Math.round(this._box.w * this._scale) + "px");
        this.el.style.setProperty("--sgm-h", Math.round(this._box.h * this._scale) + "px");
    }
    _draw() {
        var self = this;
        var old = this._drawing;
        this._drawing = this._branch.createBranch("drawing-" + (++this._n));
        this._drawing.activate(_mirrorOwner);
        this._cells.clear();
        while (this.el.firstChild) this.el.removeChild(this.el.firstChild);
        this._size();
        if (!this._layout) { if (old) { try { old.dissolve(); } catch (e) {} } return; }
        this._rects = SplitGridGeometry.rects(this._layout, this._box, this._opts);
        var s = this._scale;
        Object.keys(this._rects.cells).forEach(function (id) {
            var r = self._rects.cells[id];
            var box = self._drawing.createElement("cell-" + id.replace(/[^A-Za-z0-9_-]/g, "_"), "div");
            css.addClass(box, sgm_cell);
            css.toggleClass(box, sgm_cell_current, id === self._cursor);
            box.setAttribute("data-cell", id);
            box.setAttribute("aria-current", id === self._cursor ? "true" : "false");
            box.style.setProperty("--sgm-x", Math.round(r.x * s) + "px");
            box.style.setProperty("--sgm-y", Math.round(r.y * s) + "px");
            box.style.setProperty("--sgm-w", Math.max(1, Math.round(r.w * s)) + "px");
            box.style.setProperty("--sgm-h", Math.max(1, Math.round(r.h * s)) + "px");
            box.addEventListener("pointerdown", function (e) { if (e.button === 0) { self.el.focus(); self._moveCursor(id, "pointer"); } });
            self.el.appendChild(box);
            self._cells.set(id, box);
        });
        if (old) { try { old.dissolve(); } catch (e) {} }
    }

    _moveCursor(id, by) {
        if (id === this._cursor) return;
        var self = this;
        this._cursor = id;
        this._cells.forEach(function (box, cellId) {
            css.toggleClass(box, sgm_cell_current, cellId === id);
            box.setAttribute("aria-current", cellId === id ? "true" : "false");
        });
        if (id !== null) this._fire(SplitGridEvents.CursorMoved(id, by));
    }

    _fire(ev) {
        if (!this._sink) return;
        try { this._sink(ev); }
        catch (e) { console.error("[SplitGridMirror] onEvent threw on " + ev.kind + ":", e); }
    }
}
