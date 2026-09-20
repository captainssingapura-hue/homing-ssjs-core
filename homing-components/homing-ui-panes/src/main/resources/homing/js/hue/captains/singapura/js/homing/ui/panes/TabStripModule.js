// =============================================================================
// TabStrip — the row of chips over a pane: chips with a label and a cross,
// the tail with the add button and the count, the drop mark, and the drag
// that reorders. It holds no tab state: the pane tells it which chips, in
// which order, and which is active; it tells the pane what was clicked and
// where a dragged chip landed. A branch component: the pane makes a
// sub-branch for it and hands it in.
//
//   new TabStrip(branch, { onAdd?, onDrop(chip, dest), onDragOut?(chip, pointerEvent) })
//     strip.el
//     strip.chip({ id, title, pinned, closable }, { onSelect, onClose }, branch?) → chipEl
//                                   minted on the branch given — the tab's own,
//                                   dissolved when the tab leaves — else the strip's
//     strip.arrange(chips)          the chips in order, before the tail
//     strip.remove(chip)
//     strip.select(chips, active)   aria-selected on the active one
//     strip.count(n, budget, addOn) the pill, and the add button on or off
//     strip.markAt(clientX)         the mark where a tab from outside would land → index
//     strip.unmark()
//     strip.size(s?)                the chips' size, −1..1, 0 the design's; every chip, now and later
//     strip.aspect(a?)              the chips' aspect, −1..1, 0 the design's proportion — wide,
//                                   a browser's tab — narrower at −1, wider at +1
//
// Every chip is in the tab order — the Tab key walks the strip, Enter or
// Space selects — and the design draws the hover, the press, the selected
// one and the focus ring, since the chip wears Selectable.
//
// A drag along the strip reorders and only reorders: how far up or sideways
// the hand wanders changes nothing. A chip pulled DOWN off the strip — below
// it by more than the strip's own height — leaves it: the drag is dropped
// here, and onDragOut is told with the pointer event, so whoever holds the
// pane can float the tab under the same hand. Pulling up never detaches.
// Pinned chips are not dragged and never leave.
//
// A drop lands at the count of the other chips whose middle is left of the
// pointer, never before the pinned ones; the pane turns that into a move.
// The mark is a bar inserted between chips while the drag is on; nothing is
// positioned by hand. `css` is injected with the styles import.
// =============================================================================

const _stripOwner = Object.freeze({ toString: () => "tabStrip" });
var _DRAG_THRESHOLD = 4;

class TabStrip {
    constructor(branch, opts) {
        if (!branch) throw new Error("[TabStrip] a branch of its own is required");
        var self = this;
        branch.activate(_stripOwner);
        this._branch = branch;
        this._onDrop = opts && typeof opts.onDrop === "function" ? opts.onDrop : null;
        this._onDragOut = opts && typeof opts.onDragOut === "function" ? opts.onDragOut : null;
        this._order = [];                 // the chips as last arranged
        this._pinned = new Set();         // the chips that are pinned
        this._size = null;                // the chips' size and aspect, null the design's
        this._aspect = null;

        var el = branch.createElement("strip", "div");
        css.addClass(el, mtp_strip);
        el.setAttribute("role", "tablist");
        this.el = el;

        this._tail = branch.createElement("tail", "div");
        css.addClass(this._tail, mtp_strip_tail);
        this._addBtn = null;
        if (opts && typeof opts.onAdd === "function") {
            var addBtn = branch.createElement("add", "button");
            addBtn.type = "button";
            css.addClass(addBtn, mtp_add);
            addBtn.textContent = "+";
            addBtn.setAttribute("aria-label", "Add a tab");
            addBtn.addEventListener("click", function () { if (!addBtn.disabled) opts.onAdd(); });
            this._tail.appendChild(addBtn);
            this._addBtn = addBtn;
        }
        this._pill = branch.createElement("pill", "span");
        css.addClass(this._pill, mtp_pill);
        this._tail.appendChild(this._pill);
        el.appendChild(this._tail);

        this._mark = branch.createElement("mark", "div");     // in the strip only while a drag is on
        css.addClass(this._mark, mtp_drop_mark);
        this._mark.setAttribute("aria-hidden", "true");
    }

    chip(tab, handlers, on) {
        var branch = on || this._branch;
        var name = tab.id.replace(/[^A-Za-z0-9_-]/g, "_");
        var c = branch.createElement("chip-" + name, "div");
        css.addClass(c, mtp_chip);
        c.setAttribute("role", "tab");
        c.setAttribute("aria-selected", "false");
        c.setAttribute("tabindex", "0");
        c.title = tab.title == null ? "" : String(tab.title);
        var label = branch.createElement("label-" + name, "span");
        css.addClass(label, mtp_chip_label);
        label.textContent = tab.title == null ? tab.id : String(tab.title);
        c.appendChild(label);
        var closeBtn = null;
        if (tab.closable !== false && !tab.pinned) {
            closeBtn = branch.createElement("close-" + name, "button");
            closeBtn.type = "button";
            css.addClass(closeBtn, mtp_chip_close);
            closeBtn.textContent = "×";
            closeBtn.setAttribute("aria-label", "Close " + label.textContent);
            closeBtn.setAttribute("tabindex", "-1");
            closeBtn.addEventListener("click", function (ev) { ev.stopPropagation(); handlers.onClose(); });
            c.appendChild(closeBtn);
        }
        c.addEventListener("click", function () { handlers.onSelect(); });
        c.addEventListener("keydown", function (ev) {
            if (ev.key === "Enter" || ev.key === " ") { ev.preventDefault(); handlers.onSelect(); }
        });
        if (this._size != null) css.size(c, this._size);
        if (this._aspect != null) css.aspect(c, this._aspect);
        if (tab.pinned) this._pinned.add(c);
        else this._armDrag(c, closeBtn);
        return c;
    }
    size(s) {
        this._size = s == null ? null : Math.max(-1, Math.min(1, Number(s)));
        for (var i = 0; i < this._order.length; i++) css.size(this._order[i], this._size);
    }
    aspect(a) {
        this._aspect = a == null ? null : Math.max(-1, Math.min(1, Number(a)));
        for (var i = 0; i < this._order.length; i++) css.aspect(this._order[i], this._aspect);
    }

    arrange(chips) {
        this._order = chips.slice();
        for (var i = 0; i < this._order.length; i++) this.el.insertBefore(this._order[i], this._tail);
    }
    remove(c) {
        this._pinned.delete(c);
        if (c.parentNode === this.el) this.el.removeChild(c);
    }
    select(chips, active) {
        for (var i = 0; i < chips.length; i++) chips[i].setAttribute("aria-selected", chips[i] === active ? "true" : "false");
    }
    count(n, budget, addOn) {
        this._pill.textContent = n + " / " + budget;
        this._pill.title = "Tabs in this pane: " + n + " of " + budget;
        if (this._addBtn) {
            this._addBtn.disabled = !addOn;
            css.toggleClass(this._addBtn, mtp_add_off, !addOn);
        }
    }

    // ── Drag to reorder ───────────────────────────────────────────────────
    _armDrag(c, closeBtn) {
        var self = this;
        c.addEventListener("pointerdown", function (down) {
            if (down.button !== 0) return;
            if (closeBtn && closeBtn.contains(down.target)) return;
            var startX = down.clientX, dragging = false, dest = -1;
            function letGo() {
                c.removeEventListener("pointermove", onMove);
                c.removeEventListener("pointerup", onEnd);
                c.removeEventListener("pointercancel", onEnd);
                css.removeClass(c, mtp_chip_dragging);
                if (self._mark.parentNode) self._mark.parentNode.removeChild(self._mark);
                try { c.releasePointerCapture(down.pointerId); } catch (err) {}
            }
            function onMove(e) {
                if (!dragging) {
                    if (Math.abs(e.clientX - startX) < _DRAG_THRESHOLD) return;
                    dragging = true;
                    css.addClass(c, mtp_chip_dragging);
                    try { c.setPointerCapture(down.pointerId); } catch (err) {}
                }
                if (self._onDragOut && self._isPulledDown(e.clientY)) { letGo(); self._onDragOut(c, e); return; }
                dest = self._destAt(c, e.clientX);
                self._markAt(c, dest);
            }
            function onEnd(e) {
                if (!dragging) { letGo(); return; }
                letGo();
                if (e.type === "pointerup" && dest >= 0 && self._onDrop) self._onDrop(c, dest);
            }
            c.addEventListener("pointermove", onMove);
            c.addEventListener("pointerup", onEnd);
            c.addEventListener("pointercancel", onEnd);
        });
    }
    _others(c) {
        var out = [];
        for (var i = 0; i < this._order.length; i++) if (this._order[i] !== c) out.push(this._order[i]);
        return out;
    }
    /** Below the strip by more than its own height: pulled down and off. Up or sideways never is. */
    _isPulledDown(y) {
        var r = this.el.getBoundingClientRect();
        return y > r.bottom + r.height;
    }
    /** The mark where a tab from outside would land, and the index it would take. */
    markAt(x) { var dest = this._destAt(null, x); this._markAt(null, dest); return dest; }
    unmark() { if (this._mark.parentNode) this._mark.parentNode.removeChild(this._mark); }
    /** Where the chip would land: the count of the other chips whose middle is left of x, never before the pinned. */
    _destAt(c, x) {
        var others = this._others(c), k = 0;
        for (var i = 0; i < others.length; i++) {
            var r = others[i].getBoundingClientRect();
            if (x > r.left + r.width / 2) k++;
        }
        return Math.max(k, this._pinned.size);
    }
    _markAt(c, dest) {
        var others = this._others(c);
        this.el.insertBefore(this._mark, dest < others.length ? others[dest] : this._tail);
    }
}
