// =============================================================================
// MenuGeometry — where a menu goes and where its cursor goes, with no
// element in sight. Headless, so the steward draws what these say and they
// are tested without a browser.
//
//   MenuGeometry.place(at, size, viewport, margin?)
//       → { x, y }: a frame of `size` with its top-left at the point — flipped
//         to the left of the point when it would run off the right, above it
//         when it would run off the bottom, and then kept within the
//         viewport less the margin (4) on every side, whatever else.
//   MenuGeometry.beside(anchor, size, viewport, inset?, margin?)
//       → { x, y }: a submenu beside its row — to the right of the anchor
//         rectangle, its first row level with the anchor (the frame's inset
//         above it), flipped to the left of the anchor when it would run off
//         the right; kept within the viewport as place does.
//   MenuGeometry.step(enabled, from, dir)
//       → the index of the next enabled row from `from` in `dir` (+1 or −1),
//         wrapping; from −1 the first enabled from the top (+1) or the bottom
//         (−1); −1 when no row is enabled.
//
// at is { x, y }; size { w, h }; viewport { w, h }; anchor { left, top, right,
// bottom }; enabled an array of booleans, one per row.
// =============================================================================

class MenuGeometry {
    static place(at, size, viewport, margin) {
        var m = margin == null ? 4 : margin;
        var x = at.x, y = at.y;
        if (x + size.w > viewport.w - m) x = at.x - size.w;
        if (y + size.h > viewport.h - m) y = at.y - size.h;
        return MenuGeometry._within(x, y, size, viewport, m);
    }
    static beside(anchor, size, viewport, inset, margin) {
        var m = margin == null ? 4 : margin, pad = inset == null ? 0 : inset;
        var x = anchor.right, y = anchor.top - pad;
        if (x + size.w > viewport.w - m) x = anchor.left - size.w;
        return MenuGeometry._within(x, y, size, viewport, m);
    }
    static _within(x, y, size, viewport, m) {
        x = Math.max(m, Math.min(x, viewport.w - m - size.w));
        y = Math.max(m, Math.min(y, viewport.h - m - size.h));
        return { x: x, y: y };
    }
    static step(enabled, from, dir) {
        var n = enabled.length, d = dir < 0 ? -1 : 1;
        if (!n) return -1;
        var i = from;
        for (var k = 0; k < n; k++) {
            i = from < 0 ? (d > 0 ? k : n - 1 - k) : (i + d + n) % n;
            if (enabled[i]) return i;
        }
        return -1;
    }
}
