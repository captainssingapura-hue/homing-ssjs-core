// =============================================================================
// TabHand — the hand on a strip's chip: the press, the drag along the row,
// and the letting go. The strip arms each chip with it and keeps the state
// — which chip is held — and the classes; the hand reads the rectangles,
// does the arithmetic through TabDrag, writes the chip's position as a
// custom property, and tells the strip what happened. One hand per strip;
// it holds at most one chip.
//
//   new TabHand(strip)
//     hand.arm(chip, closeBtn?)   the chip answers the hand from now on
//     hand.held()                 → the chip in the hand, or null
//
// The row is a rail. The chip goes where the hand goes along it, at the
// press's offset within the chip, kept within the slots — never before a
// pinned one — and never off the rail: the hand's height is nothing. The
// slot the chip is nearest is its destination, and the strip is told who
// steps aside. Let go, the chip settles onto its slot from where the hand
// left it, eased as the design eases a selectable, and the strip is told
// where it landed. Nothing is positioned by hand: --mtp-drag-x on the chip,
// which the strip's class reads.
//
// The pointer is captured at the press, not at the first move: a capture
// asked for mid-gesture is not always granted, and without it the chip
// hears the hand only while the hand is over it — it lags, a quick hand
// escapes it, and a release elsewhere is never heard, so the chip keeps
// following a hand that let go. Should the capture be lost all the same,
// the window hears the release and the drag ends there.
// =============================================================================

var _DRAG_THRESHOLD = 4;

class TabHand {
    constructor(strip) {
        this._strip = strip;
        this._held = null;
    }
    held() { return this._held; }

    arm(c, closeBtn) {
        var self = this, strip = this._strip;
        c.addEventListener("pointerdown", function (down) {
            if (down.button !== 0 || self._held) return;
            if (closeBtn && closeBtn.contains(down.target)) return;
            var startX = down.clientX, dragging = false;
            var slots = null, origin = null, grabX = 0, lo = 0, from = -1, dest = -1, left = 0;
            var win = typeof window !== "undefined" ? window : null;
            try { c.setPointerCapture(down.pointerId); } catch (err) {}
            function begin() {
                dragging = true;
                self._held = c;
                var seated = strip._seated();
                slots = seated.map(TabHand._rect);
                from = seated.indexOf(c);
                origin = slots[from];
                left = origin.left;
                grabX = startX - origin.left;
                lo = strip._pinned.size;
                dest = from;
                strip._grip(c);
            }
            /** Under the hand at the remembered offset, along the rail and within it; the others stepping aside. */
            function place(x) {
                left = TabDrag.clamp(x - grabX, slots, lo);
                c.style.setProperty("--mtp-drag-x", (left - origin.left) + "px");
                var d = TabDrag.dest(left, slots, lo);
                if (d !== dest) { dest = d; strip._stepAside(from, dest, TabDrag.pitch(slots)); }
            }
            function letGo() {
                c.removeEventListener("pointermove", onMove);
                c.removeEventListener("pointerup", onEnd);
                c.removeEventListener("pointercancel", onEnd);
                c.removeEventListener("lostpointercapture", onEnd);
                if (win) { win.removeEventListener("pointerup", onEnd); win.removeEventListener("pointercancel", onEnd); win.removeEventListener("blur", onEnd); }
                if (!dragging) return;
                self._held = null;
                c.style.removeProperty("--mtp-drag-x");
                strip._stepAside(from, from, 0);
                strip._release(c);
                try { c.releasePointerCapture(down.pointerId); } catch (err) {}
            }
            function onMove(e) {
                if (!dragging) {
                    if (Math.abs(e.clientX - startX) < _DRAG_THRESHOLD) return;
                    begin();
                }
                place(e.clientX);
            }
            function onEnd(e) {
                var landed = dragging && e.type === "pointerup" && dest !== from;
                var slotLeft = dragging ? slots[landed ? dest : from].left : 0, fromLeft = left;
                letGo();
                if (!dragging) return;
                if (landed && strip._onDrop) strip._onDrop(c, dest);
                TabHand._settle(c, fromLeft - slotLeft);
            }
            c.addEventListener("pointermove", onMove);
            c.addEventListener("pointerup", onEnd);
            c.addEventListener("pointercancel", onEnd);
            c.addEventListener("lostpointercapture", onEnd);
            if (win) { win.addEventListener("pointerup", onEnd); win.addEventListener("pointercancel", onEnd); win.addEventListener("blur", onEnd); }
        });
    }

    /** From where the hand left it onto its slot, eased as the design eases the chip: the last leg, drawn once the row is arranged. */
    static _settle(c, dx) {
        if (!(Math.abs(dx) > 0.5) || typeof c.animate !== "function") return;
        var ms = TabHand._easeMs(c);
        if (!(ms > 0)) return;
        try { c.animate([{ translate: dx + "px 0" }, { translate: "0 0" }], { duration: ms, easing: "ease" }); } catch (err) {}
    }
    /** The design's ease for the chip, in milliseconds: the first duration of its transition. */
    static _easeMs(c) {
        if (typeof getComputedStyle !== "function") return 0;
        var d = String(getComputedStyle(c).transitionDuration || "").split(",")[0].trim();
        if (!d) return 0;
        return d.endsWith("ms") ? parseFloat(d) : parseFloat(d) * 1000;
    }
    static _rect(el) {
        var r = el.getBoundingClientRect();
        return { left: r.left, top: r.top, width: r.width, height: r.height };
    }
}
