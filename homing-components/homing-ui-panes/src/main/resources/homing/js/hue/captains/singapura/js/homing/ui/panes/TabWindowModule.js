// =============================================================================
// TabWindow — the part of a tab row a bar can show, and where over the row it
// is. It owns the RAIL: the element the chips sit in, the only thing that
// moves. The strip makes the rail and hands it over; the arithmetic is
// TabFit's; this is where the two meet the page.
//
//   new TabWindow(rail, { minTabPx?, minTabAspect?, onFit? })
//     minTabAspect  how squat a tab may get against its own HEIGHT — which
//                   does not move, so the squeeze is a width and only ever a
//                   width. 2½ to 1 unless said; a design's tab is six to one
//                   at rest, and somewhere around two and a half it stops
//                   being a tab and becomes a button with a cross on it.
//     minTabPx      a hard floor under that, for a caller with a reason
//
//   win.row(chips)      the chips it is a window over, in order
//   win.fit(rest)       squeeze the row to the room, then bring `rest` into
//                       the window: called when the row changes, when the
//                       axes change, and by the rail's own observer when the
//                       room changes with nobody touching the tabs
//   win.reveal(chip)    that chip in the window, moving it as little as it can
//   win.step(by)        the window moved by whole tabs; true when it moved
//   win.dispose()
//
// THE WINDOW RESTS ON WHOLE TABS AND ONLY ON WHOLE TABS. That is what the
// squeeze buys: a width at which `per` tabs are the room exactly, so moving
// the window is multiplication and nothing is ever clipped down the middle.
// The rail's own scrollLeft, never scrollIntoView — which scrolls whatever it
// must to obey, and a workspace may not move under a widget because a tab was
// selected somewhere in it.
// =============================================================================

class TabWindow {
    constructor(rail, opts) {
        if (!rail) throw new Error("[TabWindow] the rail is what it is a window over");
        var o = opts || {}, self = this;
        this.rail = rail;
        this._chips = [];
        this._rest = null;      // the chip the window is kept on when the room changes
        this._per = 0;          // how many chips it shows; 0 until measured
        this._w = 0;            // what each of them is, in pixels
        this._minPx = o.minTabPx > 0 ? Number(o.minTabPx) : 0;
        this._minAspect = o.minTabAspect > 0 ? Number(o.minTabAspect) : 2.5;
        this._sees = typeof ResizeObserver === "function" ? new ResizeObserver(function () { self.fit(self._rest); }) : null;
        if (this._sees) this._sees.observe(rail);
    }

    /** The chips it is a window over, in order. */
    row(chips) { this._chips = chips ? chips.slice() : []; return this; }

    /**
     * The row squeezed to the room, and then the window put right. Measured at
     * the design's own width first — the rail is only as wide as its content
     * until the content is wider than the room, so one reading says both what
     * a tab wants and what the bar can give.
     *
     * The window is then brought back INSIDE the row, always. A bar measured
     * before the page had laid itself out squeezes against a room it does not
     * have yet and moves the window to suit; the measurement that follows puts
     * the width right, and would leave the window where the wrong one left
     * it — the first tabs scrolled off a row that now fits, with nothing to
     * say why. So the resting place is worked out from what is true now.
     */
    fit(rest) {
        var rail = this.rail, n = this._chips.length;
        if (rest) this._rest = rest;
        rail.style.setProperty("--chip-fit", "none");
        if (n === 0) { this._per = 0; this._w = 0; return this; }
        var box = this._chips[0].getBoundingClientRect(), natural = box.width, room = rail.clientWidth;
        if (!(natural > 0) || !(room > 0)) return this;   // not laid out yet: the observer will come back to it
        var got = TabFit.row(n, room, natural, Math.max(this._minPx, this._minAspect * box.height));
        rail.style.setProperty("--chip-fit", got.width == null ? "none" : got.width + "px");
        this._per = got.per;
        this._w = got.width == null ? natural : got.width;
        var last = Math.max(0, n - this._per) * this._w;
        if (rail.scrollLeft > last) rail.scrollLeft = last;
        return this.reveal(this._rest);
    }

    /**
     * That chip in the window, moving it as little as it can. Every way a tab
     * becomes the shown one comes through the strip's select, so this is the
     * one place it has to be said: the arrows walk onto a chip past the edge,
     * a new tab lands past it, a merge brings eight at once.
     */
    reveal(chip) {
        var n = this._chips.length;
        if (chip) this._rest = chip;
        if (!(this._w > 0)) return this;
        if (this._per >= n) { this.rail.scrollLeft = 0; return this; }   // no window: the row begins where it begins
        var j = chip ? this._chips.indexOf(chip) : -1;
        if (j < 0) return this;
        var at = Math.round(this.rail.scrollLeft / this._w);
        this.rail.scrollLeft = TabFit.window(at, j, this._per, n) * this._w;
        return this;
    }

    /** The window moved by whole tabs; true when it moved, so a wheel knows whether it was taken. */
    step(by) {
        var n = this._chips.length;
        if (!(this._w > 0) || this._per >= n) return false;
        var at = Math.round(this.rail.scrollLeft / this._w), to = TabFit.step(at, by, this._per, n);
        if (to === at) return false;
        this.rail.scrollLeft = to * this._w;
        return true;
    }

    /** The chip the window is resting on, if it still has one. */
    forget(chip) { if (this._rest === chip) this._rest = null; return this; }

    dispose() {
        if (this._sees) { this._sees.disconnect(); this._sees = null; }
        this._chips = [];
        this._rest = null;
    }
}
