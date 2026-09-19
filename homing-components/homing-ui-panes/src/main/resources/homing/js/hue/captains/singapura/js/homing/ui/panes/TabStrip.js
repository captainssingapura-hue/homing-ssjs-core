// =============================================================================
// TabStrip — the row of chips over a pane: chips with a label and a cross,
// the tail with the add button and the count, the drop mark, and the drag
// that reorders. It holds no tab state: the pane tells it which chips, in
// which order, and which is active; it tells the pane what was clicked and
// where a dragged chip landed.
//
//   createTabStrip(branch, { onAdd?, onDrop(chip, dest) }) → strip
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

var _DRAG_THRESHOLD = 4;

function createTabStrip(branch, opts) {
    var el = branch.createElement("strip", "div");
    css.addClass(el, mtp_strip);
    el.setAttribute("role", "tablist");

    var tail = branch.createElement("tail", "div");
    css.addClass(tail, mtp_strip_tail);
    var addBtn = null;
    if (typeof opts.onAdd === "function") {
        addBtn = branch.createElement("add", "button");
        addBtn.type = "button";
        css.addClass(addBtn, mtp_add);
        addBtn.textContent = "+";
        addBtn.setAttribute("aria-label", "Add a tab");
        addBtn.addEventListener("click", function () { if (!addBtn.disabled) opts.onAdd(); });
        tail.appendChild(addBtn);
    }
    var pill = branch.createElement("pill", "span");
    css.addClass(pill, mtp_pill);
    tail.appendChild(pill);
    el.appendChild(tail);

    var mark = branch.createElement("mark", "div");     // in the strip only while a drag is on
    css.addClass(mark, mtp_drop_mark);
    mark.setAttribute("aria-hidden", "true");

    var order = [];                 // the chips as last arranged
    var pinned = new Set();         // the chips that are pinned

    function chip(tab, handlers) {
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
        if (tab.pinned) pinned.add(c);
        else _armDrag(c, closeBtn);
        return c;
    }

    function arrange(chips) {
        order = chips.slice();
        for (var i = 0; i < order.length; i++) el.insertBefore(order[i], tail);
    }
    function remove(c) {
        pinned.delete(c);
        if (c.parentNode === el) el.removeChild(c);
    }
    function select(chips, active) {
        for (var i = 0; i < chips.length; i++) {
            var on = chips[i] === active;
            chips[i].setAttribute("aria-selected", on ? "true" : "false");
            chips[i].setAttribute("tabindex", on ? "0" : "-1");
        }
    }
    function count(n, budget, addOn) {
        pill.textContent = n + " / " + budget;
        pill.title = "Tabs in this pane: " + n + " of " + budget;
        if (addBtn) {
            addBtn.disabled = !addOn;
            css.toggleClass(addBtn, mtp_add_off, !addOn);
        }
    }

    // ── Drag to reorder ───────────────────────────────────────────────────
    function _armDrag(c, closeBtn) {
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
                dest = _destAt(c, e.clientX);
                _markAt(c, dest);
            }
            function onEnd(e) {
                c.removeEventListener("pointermove", onMove);
                c.removeEventListener("pointerup", onEnd);
                c.removeEventListener("pointercancel", onEnd);
                if (!dragging) return;
                css.removeClass(c, mtp_chip_dragging);
                if (mark.parentNode) mark.parentNode.removeChild(mark);
                try { c.releasePointerCapture(down.pointerId); } catch (err) {}
                if (e.type === "pointerup" && dest >= 0) opts.onDrop(c, dest);
            }
            c.addEventListener("pointermove", onMove);
            c.addEventListener("pointerup", onEnd);
            c.addEventListener("pointercancel", onEnd);
        });
    }
    function _others(c) {
        var out = [];
        for (var i = 0; i < order.length; i++) if (order[i] !== c) out.push(order[i]);
        return out;
    }
    function _destAt(c, x) {
        var others = _others(c), k = 0;
        for (var i = 0; i < others.length; i++) {
            var r = others[i].getBoundingClientRect();
            if (x > r.left + r.width / 2) k++;
        }
        return Math.max(k, pinned.size);
    }
    function _markAt(c, dest) {
        var others = _others(c);
        el.insertBefore(mark, dest < others.length ? others[dest] : tail);
    }

    return Object.freeze({ el: el, chip: chip, arrange: arrange, remove: remove, select: select, count: count });
}
