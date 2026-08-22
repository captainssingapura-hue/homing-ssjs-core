// =============================================================================
// GridHeaderDragModule — the header's two pointer gestures, split out of
// GridLayout when the 250-line ratchet forced it (the ratchet doing its job):
//
//   · RESIZE (ext2): an 8px handle element on the header's right edge (hover
//     shows col-resize); STAGED — a guide line tracks the pointer, the width
//     commits once on release, Escape abandons. D7 structural: nothing in the
//     cell tree is touched mid-gesture.
//   · REORDER (ext1 row 17): mousedown on the header BODY (the handle stops
//     propagation, so the gestures cannot collide), a 4px threshold separates
//     a drag from a stray press, the dragged header dims, and the LANDING
//     SLOT shows as a band the dragged column's width — both edges drawn,
//     so the destination reads the same from either direction. Commit on
//     release, Escape abandons.
//
// Positional only — emits (j, …); the facade translates to identity.
//
//   new GridHeaderDrag({ table, onColResize(j, px), onColReorder(j, t) })
//     .wire(th, j)   // called by GridLayout for every header it mints
// =============================================================================

function _hgdAddClass(el, c) {
    var parts = el.className ? el.className.split(" ") : [];
    if (parts.indexOf(c) < 0) el.className = parts.concat(c).join(" ");
}
function _hgdRemoveClass(el, c) {
    if (!el.className) return;
    el.className = el.className.split(" ").filter(function (x) { return x !== c; }).join(" ");
}

class GridHeaderDrag {

    constructor(opts) {
        opts = opts || {};
        if (!opts.table) throw new Error("[GridHeaderDrag] opts.table is required");
        this._table = opts.table;
        this._onColResize  = opts.onColResize || null;
        this._onColReorder = opts.onColReorder || null;
    }

    /** Wire both gestures onto a freshly minted header cell. */
    wire(th, j) {
        var self = this;
        if (this._onColResize) {
            var handle = document.createElement("span");
            handle.className = "hgr-resize-handle";
            th.appendChild(handle);
            handle.addEventListener("mousedown", function (e) {
                var rect = th.getBoundingClientRect ? th.getBoundingClientRect() : null;
                if (!rect || e.clientX == null) return;
                if (e.preventDefault) e.preventDefault();
                if (e.stopPropagation) e.stopPropagation();
                self._startColDrag(j, rect.right - rect.left, e.clientX);
            });
        }
        if (this._onColReorder) {
            th.addEventListener("mousedown", function (e) {
                if (e.clientX == null) return;
                self._startHeaderDrag(j, th, e.clientX);
            });
        }
        return this;
    }

    _makeGuide(atX) {
        var rect = this._table.getBoundingClientRect ? this._table.getBoundingClientRect() : null;
        if (!document.body || !rect) return null;
        var guide = document.createElement("div");
        guide.className = "hgr-resize-guide";
        guide.style.setProperty("--hgr-guide-top", rect.top + "px");
        guide.style.setProperty("--hgr-guide-h", rect.height + "px");
        guide.style.setProperty("--hgr-guide-x", atX + "px");
        document.body.appendChild(guide);
        return guide;
    }

    _startColDrag(j, startW, startX) {
        var self = this, lastX = startX;
        var guide = this._makeGuide(startX);
        function onMove(e) {
            lastX = e.clientX;
            if (guide) guide.style.setProperty("--hgr-guide-x", lastX + "px");
        }
        function teardown() {
            document.removeEventListener("mousemove", onMove);
            document.removeEventListener("mouseup", onUp);
            document.removeEventListener("keydown", onKey, true);
            if (guide && guide.parentNode) guide.parentNode.removeChild(guide);
        }
        function onUp() { teardown(); self._onColResize(j, startW + (lastX - startX)); }
        function onKey(e) {   // Escape ABANDONS — nothing was applied yet
            if (e.key === "Escape") { teardown(); if (e.stopPropagation) e.stopPropagation(); }
        }
        document.addEventListener("mousemove", onMove);
        document.addEventListener("mouseup", onUp);
        document.addEventListener("keydown", onKey, true);
    }

    _startHeaderDrag(j, th, startX) {
        var self = this, moved = false, target = j, guide = null;
        var rects = [], row = th.parentNode;
        for (var k = 0; k < row.children.length; k++) {
            var r = row.children[k].getBoundingClientRect ? row.children[k].getBoundingClientRect() : null;
            if (!r) return;                               // no geometry: inert (headless)
            rects.push(r);
        }
        function computeTarget(x) {
            var t = 0;
            for (var k = 0; k < rects.length; k++)
                if (x > (rects[k].left + rects[k].right) / 2) t = k + 1;
            return t;
        }
        function boundaryX(t) {
            return t < rects.length ? rects[t].left : rects[rects.length - 1].right;
        }
        function onMove(e) {
            if (!moved) {
                if (Math.abs(e.clientX - startX) <= 4) return;   // not a drag yet
                moved = true;
                _hgdAddClass(th, "hgr-dragging");
                guide = self._makeGuide(e.clientX);
                if (guide) {                       // the LANDING SLOT, not a line:
                    guide.className = "hgr-drop-band";   // both edges drawn, the
                    guide.style.setProperty("--hgr-band-w",   // dragged column wide
                        (rects[j].right - rects[j].left) + "px");
                }
            }
            target = computeTarget(e.clientX);
            if (guide) guide.style.setProperty("--hgr-guide-x", boundaryX(target) + "px");
        }
        function teardown() {
            document.removeEventListener("mousemove", onMove);
            document.removeEventListener("mouseup", onUp);
            document.removeEventListener("keydown", onKey, true);
            _hgdRemoveClass(th, "hgr-dragging");
            if (guide && guide.parentNode) guide.parentNode.removeChild(guide);
        }
        function onUp() {
            teardown();
            // before-itself and after-itself are both "no move"
            if (moved && target !== j && target !== j + 1) self._onColReorder(j, target);
        }
        function onKey(e) {
            if (e.key === "Escape") { teardown(); if (e.stopPropagation) e.stopPropagation(); }
        }
        document.addEventListener("mousemove", onMove);
        document.addEventListener("mouseup", onUp);
        document.addEventListener("keydown", onKey, true);
    }
}
