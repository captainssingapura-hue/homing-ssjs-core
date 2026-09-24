// =============================================================================
// TabFit — how many tabs a bar can show, how wide they must be, and which of
// them the window is over. Arithmetic only: no DOM, no state. The strip
// measures, asks, and applies.
//
//   TabFit.row(n, room, natural, floor)   → { width, per }
//   TabFit.window(at, want, per, n)       → the window's first index
//
// THE ROW IS MADE TO FIT BEFORE IT IS MADE TO SCROLL. A bar with a few tabs
// draws them at the size the design asked for. As they multiply they are
// squeezed, all alike, until they reach a floor below which a tab is not a
// tab any more — a word and a cross need room. Only then does the bar give
// up and become a window over a row longer than itself.
//
// And the squeeze does one more thing, which is why it is worth doing well:
// it chooses a width at which a WHOLE NUMBER of tabs fills the bar exactly.
// `per` tabs at `width` each are the room, to the pixel. So a window is never
// half over a tab, nothing is ever clipped down the middle, and the arithmetic
// that moves the window is multiplication rather than a search. The
// alternative — the natural width, and a window wherever the scroll happens
// to stop — shows a sliver of a tab at one end and a gap at the other, and
// asks a reader to judge which of two half-tabs is the one they are on.
//
// The floor is a PROPORTION of the design's own tab, not a number of pixels,
// so a design with chunky tabs keeps chunky tabs at their narrowest and one
// with fine tabs keeps fine ones. A caller with a reason may put a pixel
// floor under it as well; the larger of the two wins.
// =============================================================================

class TabFit {
    /**
     * The width every tab takes and how many of them the bar then shows.
     *
     *   n        how many tabs there are
     *   room     the bar's room for them, in pixels
     *   natural  the width one tab wants, from the design
     *   floor    { px, ratio } — the narrowest a tab may be: ratio of natural,
     *            or px, whichever is larger
     *
     * → { width, per }. width is null when nothing is squeezed — the row fits
     *   as it is — and per is n, which is the strip's own word for "no window".
     */
    static row(n, room, natural, floor) {
        if (!(n > 0) || !(room > 0) || !(natural > 0)) return { width: null, per: Math.max(1, n | 0) };
        if (natural * n <= room + 0.5) return { width: null, per: n };   // it fits: leave the design's width alone
        var f = floor || {};
        var least = Math.max(f.px > 0 ? f.px : 0, (f.ratio > 0 ? f.ratio : 0.45) * natural);
        if (least > room) least = room;                                  // a bar narrower than one tab shows one tab
        var per = Math.max(1, Math.floor(room / least));
        if (per >= n) return { width: room / n, per: n };                // squeezed, and all of them still fit
        return { width: room / per, per: per };                          // squeezed to the floor, and a window over the rest
    }

    /**
     * Where the window goes to put tab `want` in it, moving as little as it
     * can: nothing while the tab is already inside, to the tab itself when it
     * is off the near end, and to the window that ends on it when it is off
     * the far end. `at` is where the window is now, in tabs.
     */
    static window(at, want, per, n) {
        var last = Math.max(0, n - per);
        var i = Math.max(0, Math.min(last, at | 0));
        if (per >= n) return 0;
        if (want < i) i = want;
        else if (want >= i + per) i = want - per + 1;
        return Math.max(0, Math.min(last, i));
    }

    /** The window moved by whole tabs, and never off either end: what a wheel or a step asks for. */
    static step(at, by, per, n) {
        if (per >= n) return 0;
        return Math.max(0, Math.min(n - per, (at | 0) + (by | 0)));
    }
}
