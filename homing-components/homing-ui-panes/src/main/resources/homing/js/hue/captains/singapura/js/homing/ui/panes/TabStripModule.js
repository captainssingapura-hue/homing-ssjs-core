// =============================================================================
// TabStrip — the row of chips over a pane: chips with a label and a cross,
// the tail with the add button and the count, the drop mark, and the drag
// that reorders. It holds no tab state: the pane tells it which chips, in
// which order, and which is active; it tells the pane what was clicked and
// where a dragged chip landed. A branch component: the pane makes a
// sub-branch for it and hands it in.
//
//   new TabStrip(branch, { onAdd?, onDrop(chip, dest) })
//     strip.el
//     strip.chip({ id, title, pinned, closable }, { onSelect, onClose }) → chipEl
//     strip.arrange(chips)          the chips in order, before the tail
//     strip.remove(chip)
//     strip.select(chips, active)   aria-selected and the roving tabindex
//     strip.count(n, budget, addOn) the pill, and the add button on or off
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
        this._order = [];                 // the chips as last arranged
        this._pinned = new Set();         // the chips that are pinned

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

    chip(tab, handlers) {
        var branch = this._branch;
        var name = tab.id.replace(/[^A-Za-z0-9_-]/g, "_");
        var c = branch.createElement("chip-" + name, "div");
        css.addClass(c, mtp_chip);
        c.setAttribute("role", "tab");
        c.setAttribute("aria-selected", "false");
        c.setAttribute("tabindex", "-1");
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
        if (tab.pinned) this._pinned.add(c);
        else this._armDrag(c, closeBtn);
        return c;
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
        for (var i = 0; i < chips.length; i++) {
            var on = chips[i] === active;
            chips[i].setAttribute("aria-selected", on ? "true" : "false");
            chips[i].setAttribute("tabindex", on ? "0" : "-1");
        }
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
            function onMove(e) {
                if (!dragging) {
                    if (Math.abs(e.clientX - startX) < _DRAG_THRESHOLD) return;
                    dragging = true;
                    css.addClass(c, mtp_chip_dragging);
                    try { c.setPointerCapture(down.pointerId); } catch (err) {}
                }
                dest = self._destAt(c, e.clientX);
                self._markAt(c, dest);
            }
            function onEnd(e) {
                c.removeEventListener("pointermove", onMove);
                c.removeEventListener("pointerup", onEnd);
                c.removeEventListener("pointercancel", onEnd);
                if (!dragging) return;
                css.removeClass(c, mtp_chip_dragging);
                if (self._mark.parentNode) self._mark.parentNode.removeChild(self._mark);
                try { c.releasePointerCapture(down.pointerId); } catch (err) {}
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
