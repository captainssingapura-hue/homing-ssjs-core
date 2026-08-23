// =============================================================================
// GridUpdateBatchModule — the DIRECT update path's queue, split out of the
// facade when the line ratchet forced it (third time; it keeps finding real
// seams). One concern: coalesce domain-pushed cell updates per animation
// frame, LAST WRITE PER CELL WINS, so a hot feed (a popularity tick, a
// Minesweeper flood fill) collapses to one cell.update() per cell per frame.
//
//   · No requestAnimationFrame (headless, or a hidden page at boot) → the
//     flush runs synchronously, so tests and SSR-ish contexts behave.
//   · drain() is the READ-PATH guard: a hidden page gets no frames, so
//     pending writes can sit indefinitely — harmless for painting, fatal for
//     anything that reads cell state (copy, export). Read paths drain first.
//
// No DOM, no maps, no layout: it holds a Map and calls cells.update.
// =============================================================================

class GridUpdateBatch {

    constructor(deps) {
        deps = deps || {};
        if (!deps.cells) throw new Error("[GridUpdateBatch] cells is required");
        this._cells = deps.cells;
        this._pending = new Map();      // key → { pk, col, value }
        this._scheduled = false;
    }

    /** Queue a value for (pk, col); unknown cells are ignored (not an error —
     *  a filtered-out row still has cells, a deleted one does not). */
    push(pk, col, value) {
        if (!this._cells.get(pk, col)) return false;
        this._pending.set(pk + " " + col, { pk: pk, col: col, value: value });
        this._schedule();
        return true;
    }

    _schedule() {
        if (this._scheduled) return;
        this._scheduled = true;
        var self = this;
        var raf = (typeof requestAnimationFrame === "function") ? requestAnimationFrame
                : function (fn) { fn(); };
        raf(function () { self._scheduled = false; self.drain(); });
    }

    /** Apply everything queued, now. Safe to call at any time. */
    drain() {
        var self = this, pending = this._pending;
        this._pending = new Map();
        pending.forEach(function (u) { self._cells.update(u.pk, u.col, u.value); });
        return this;
    }

    pendingCount() { return this._pending.size; }
}
