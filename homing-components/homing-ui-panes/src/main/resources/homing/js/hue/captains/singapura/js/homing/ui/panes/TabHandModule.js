// =============================================================================
// TabHand — the hand on a strip's chip: the press, the drag in the row, the
// drag afloat, the crossing between them, and the letting go. The strip
// arms each chip with it and keeps the state — which chips are seated,
// which afloat, which is held — and the classes; the hand reads the
// rectangles, does the arithmetic through TabDrag, writes the chip's
// position as custom properties, and tells the strip what happened. One
// hand per strip; it holds at most one chip.
//
//   new TabHand(strip)
//     hand.arm(chip, closeBtn?)   the chip answers the hand from now on
//     hand.held()                 → the chip in the hand, or null
//
// In the row the chip goes where the hand goes at the press's offset
// within it, kept within the slots and never above its own; the slot it is
// nearest is its destination, and the strip is told who steps aside. Off
// the strip by more than two thirds it leaves the row: handed off through
// the strip's onDragOut, or, on a strip that floats, set afloat where it is
// and free under the hand from then on. A chip afloat is seated again when
// more than half of it is back over the strip; let go afloat, it stays.
// Nothing is positioned by hand: --mtp-drag-x/y in the row, --mtp-float-x/y
// afloat, both the strip's classes read.
// =============================================================================

var _DRAG_THRESHOLD = 4;
var _DETACH = 2 / 3;         // the part of the chip that must be off the strip before it leaves the row
var _REENTER = 1 / 2;        // and how little must be off before a chip afloat is seated again: more than half on

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
            var startX = down.clientX, startY = down.clientY, dragging = false, afloat = false;
            var slots = null, origin = null, bar = null, grab = null, lo = 0, from = -1, dest = -1;
            function begin() {
                dragging = true;
                self._held = c;
                afloat = strip.floating(c);
                bar = TabHand._rect(strip.el);
                origin = TabHand._rect(c);
                grab = { x: startX - origin.left, y: startY - origin.top };
                lo = strip._pinned.size;
                strip._grip(c);
                if (!afloat) row();
                try { c.setPointerCapture(down.pointerId); } catch (err) {}
            }
            /** The row measured with the chip in it: the slots, its own among them. */
            function row() {
                var seated = strip._seated();
                slots = seated.map(TabHand._rect);
                from = seated.indexOf(c);
                origin = slots[from];
                dest = from;
            }
            /** In the row: under the hand at the remembered offset, within the row and never above its slot; the others stepping aside. */
            function placeInRow(x, y) {
                var left = TabDrag.clamp(x - grab.x, slots, lo);
                var top = Math.max(origin.top, y - grab.y);
                c.style.setProperty("--mtp-drag-x", (left - origin.left) + "px");
                c.style.setProperty("--mtp-drag-y", (top - origin.top) + "px");
                var d = TabDrag.dest(left, slots, lo);
                if (d !== dest) { dest = d; strip._stepAside(from, dest, TabDrag.pitch(slots)); }
                return TabDrag.outside(top, origin.height, bar.top, bar.bottom);
            }
            /** Afloat: under the hand, free, in the strip's frame. */
            function placeAfloat(x, y) {
                var left = x - grab.x, top = y - grab.y;
                strip._float(c, left - bar.left, top - bar.top);
                return TabDrag.outside(top, origin.height, bar.top, bar.bottom);
            }
            /** Out of the row, where it is: the row closes behind it. */
            function leave() {
                afloat = true;
                var left = origin.left + parseFloat(c.style.getPropertyValue("--mtp-drag-x") || "0");
                var top = origin.top + parseFloat(c.style.getPropertyValue("--mtp-drag-y") || "0");
                clearRow();
                strip._float(c, left - bar.left, top - bar.top);
            }
            /** Back in the row: seated at its place in the order, the row re-measured with it, then under the hand as before. */
            function reenter() {
                afloat = false;
                strip._seat(c);
                strip._grip(c);
                row();
            }
            function clearRow() {
                c.style.removeProperty("--mtp-drag-x");
                c.style.removeProperty("--mtp-drag-y");
                strip._stepAside(from, from, 0);
            }
            function letGo() {
                c.removeEventListener("pointermove", onMove);
                c.removeEventListener("pointerup", onEnd);
                c.removeEventListener("pointercancel", onEnd);
                if (!dragging) return;
                self._held = null;
                if (!afloat) clearRow();
                strip._release(c, afloat);
                try { c.releasePointerCapture(down.pointerId); } catch (err) {}
            }
            function onMove(e) {
                if (!dragging) {
                    if (Math.abs(e.clientX - startX) < _DRAG_THRESHOLD && Math.abs(e.clientY - startY) < _DRAG_THRESHOLD) return;
                    begin();
                }
                if (afloat) {
                    if (placeAfloat(e.clientX, e.clientY) < _REENTER) { reenter(); placeInRow(e.clientX, e.clientY); }
                    return;
                }
                if (placeInRow(e.clientX, e.clientY) <= _DETACH) return;
                if (strip._floats) { leave(); if (strip._onFloat) strip._onFloat(c, e, grab); }
                else if (strip._onDragOut) { letGo(); strip._onDragOut(c, e, grab); }
            }
            function onEnd(e) {
                var wasAfloat = afloat, landed = dragging && !afloat && e.type === "pointerup" && dest !== from;
                letGo();
                if (!dragging) return;
                if (wasAfloat) { if (strip._onLand) strip._onLand(c, strip._at(c)); }
                else if (landed && strip._onDrop) strip._onDrop(c, dest);
            }
            c.addEventListener("pointermove", onMove);
            c.addEventListener("pointerup", onEnd);
            c.addEventListener("pointercancel", onEnd);
        });
    }

    static _rect(el) {
        var r = el.getBoundingClientRect();
        return { left: r.left, top: r.top, width: r.width, height: r.height, right: r.left + r.width, bottom: r.top + r.height };
    }
}
