// =============================================================================
// TabStrip — the row of chips over a pane: chips with a label and a cross,
// the tail with the add button and the count, the drop mark, and the drag
// that reorders. It holds no tab state: the pane tells it which chips, in
// which order, and which is active; it tells the pane what was clicked and
// where a dragged chip landed. A branch component: the pane makes a
// sub-branch for it and hands it in.
//
//   new TabStrip(branch, { onAdd?, onDrop(chip, dest), onGroundMenu? })
//     onGroundMenu(at) → boolean: a right-click on the strip's own ground —
//     the room the chips leave, and the tail — asks whoever holds the pane
//     for a menu there; true means it was taken and the browser's own menu is
//     suppressed. A right-click on a chip is the chip's, never the ground's.
//     strip.el
//     strip.chip({ id, title, pinned, closable }, { onSelect, onClose, onMenu? }, branch?) → chipEl
//         onMenu(at, keyboard) → boolean: a right-click on the chip, or the
//         ContextMenu key / Shift+F10 on it, asks for the tab's menu at a point;
//         true means it was taken and the browser's own menu is suppressed
//                                   minted on the branch given — the tab's own,
//                                   dissolved when the tab leaves — else the strip's
//     strip.arrange(chips)          the chips in order, before the tail
//     strip.remove(chip)
//     strip.select(chips, active)   aria-selected on the active one
//     strip.keys(chip, state)       where the keys are, said on one chip: "held"
//         while the bar has them — the chip lifted, the colour part of the way —
//         "lent" while what the tab holds has them — the chip down again, the
//         colour at full, the within mark on it — or null on none
//     strip.count(n, budget, addOn) the pill, and the add button on or off
//     strip.at(clientX, clientY)    a point on the strip: the index a tab from
//         outside would land at, marked there; −1 when the point is not on it
//     strip.markAt(clientX)         the mark where a tab from outside would land → index
//     strip.unmark()
//     strip.size(s?)                the chips' size, −1..1, 0 the design's; every chip, now and later
//     strip.dispose()
//     strip.aspect(a?)              the chips' aspect, −1..1, 0 the design's proportion — wide,
//                                   a browser's tab — narrower at −1, wider at +1
//
// No chip takes native focus, and the strip takes no keys: the keys over a
// strip are its pane's — ← → walk the tabs while the pane holds them — and
// a chip that were a focusable button would take them away after every
// press. The design draws the hover, the press and the selected one, since
// the chip wears Selectable. The press is the selection: a chip is selected
// the moment it is pressed, before any release, and the chip in the hand —
// pressed, dragged, pulled off — is the selected one throughout; nothing
// else selects while it is held. A press on the strip moves no native focus
// by the browser's default, and takes it away from whatever had it.
//
// The drag is a browser's, along a rail: the chip pressed is lifted and goes
// where the hand goes along the row — the press remembered as an offset
// within the chip, so the chip is placed and never the hand — kept within
// the row, and never off it; the slot it is nearest is where it will land,
// and the chips between step aside, live, as it passes them. Let go, it
// settles onto its slot, and the pane is told: onDrop(chip, dest), a move.
// TabHand is the hand, TabDrag the arithmetic. Leaving the row — a tab
// that detaches and floats — is not here yet; the pane's dock takes a tab
// by call. Pinned chips are not dragged.
//
// A tab offered from outside is marked where it would land: a bar inserted
// between chips, at the count of those whose middle is left of the point,
// never before the pinned ones. Nothing is positioned by hand: the chip in
// the hand carries --mtp-drag-x, a chip stepping aside --mtp-shift-x. `css`
// is injected with the styles import.
// =============================================================================

const _stripOwner = Object.freeze({ toString: () => "tabStrip" });

class TabStrip {
    /** How much of the colour a chip carries while the cursor is on the bar: part of the way, towards what the tab in use carries at full. */
    static ON_THE_BAR = 0.45;

    constructor(branch, opts) {
        if (!branch) throw new Error("[TabStrip] a branch of its own is required");

        branch.activate(_stripOwner);
        this._branch = branch;
        this._onDrop = opts && typeof opts.onDrop === "function" ? opts.onDrop : null;
        this._hand = new TabHand(this);   // the one hand on the strip's chips
        this._order = [];                 // the chips as last arranged
        this._pinned = new Set();         // the chips that are pinned
        this._size = null;                // the chips' size and aspect, null the design's
        this._aspect = null;

        var el = branch.createElement("strip", "div");
        css.addClass(el, mtp_strip);
        el.setAttribute("role", "tablist");
        this.el = el;
        // a press on the strip moves no native focus by the browser's default: a chip is not focusable, and the
        // native focus is taken away by the press itself (below), so the pane's keys work after it
        el.addEventListener("mousedown", function (ev) { ev.preventDefault(); });
        // the strip's own ground: what the chips leave. A chip's own menu is the chip's, so an event that came through one is left alone
        if (opts && typeof opts.onGroundMenu === "function") {
            el.addEventListener("contextmenu", function (ev) {
                for (var x = ev.target; x && x !== el; x = x.parentNode) if (x._chip) return;
                if (opts.onGroundMenu({ x: ev.clientX, y: ev.clientY })) ev.preventDefault();
            });
        }

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
        c._chip = true;   // so the strip knows its own ground from a chip's
        css.addClass(c, mtp_chip, mtp_chip_seated);
        c.setAttribute("role", "tab");
        c.setAttribute("aria-selected", "false");
        c.title = tab.title == null ? "" : String(tab.title);

        var label = branch.createElement("label-" + name, "span");
        css.addClass(label, mtp_chip_label);
        label.textContent = tab.title == null ? tab.id : String(tab.title);
        c.appendChild(label);
        var mark = branch.createElement("mark-" + name, "span");   // the within mark, after the label: shown only while the keys are in this tab
        css.addClass(mark, mtp_chip_mark);
        mark.setAttribute("aria-hidden", "true");
        c._mark = mark;
        c.appendChild(mark);
        var closeBtn = null;
        if (tab.closable !== false && !tab.pinned) {
            closeBtn = branch.createElement("close-" + name, "button");
            closeBtn.type = "button";
            css.addClass(closeBtn, mtp_chip_close);   // the cross is the design's, drawn by the class's word
            closeBtn.setAttribute("aria-label", "Close " + label.textContent);
            closeBtn.setAttribute("tabindex", "-1");
            closeBtn.addEventListener("click", function (ev) { ev.stopPropagation(); handlers.onClose(); });
            c.appendChild(closeBtn);
        }
        c.addEventListener("pointerdown", function (ev) {
            if (ev.button !== 0 || (closeBtn && closeBtn.contains(ev.target))) return;
            TabStrip._letGo();
            handlers.onSelect();
        });
        if (handlers.onMenu) {
            c.addEventListener("contextmenu", function (ev) {
                if (handlers.onMenu({ x: ev.clientX, y: ev.clientY }, false)) ev.preventDefault();
            });
        }
        if (this._size != null) css.size(c, this._size);
        if (this._aspect != null) css.aspect(c, this._aspect);
        if (tab.pinned) this._pinned.add(c);
        else this._hand.arm(c, closeBtn);
        return c;
    }
    /** The native focus taken away from whatever has it: a press on a chip is the pane's, and the keys after it are the pane's. */
    static _letGo() {
        if (typeof document === "undefined") return;
        var a = document.activeElement;
        if (a && a !== document.body && typeof a.blur === "function") a.blur();
    }
    /** The strip taken down: its element removed, its branch dissolved; chips minted on branches of their own are their owners'. */
    dispose() {
        if (this.el.parentNode) this.el.parentNode.removeChild(this.el);
        this._branch.dissolve();
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
    /**
     * Where the keys are, said on one chip and on no other. The whole
     * operation is lift and shift: the chip the CURSOR is on — the bar has
     * the keys, and the arrows walk the tabs — is LIFTED off the row, and its
     * mark carries the colour part of the way; the chip whose tab the keys
     * are INSIDE sits back down at its regular elevation, and the same mark
     * carries the same colour at FULL. One colour, two degrees, and the
     * elevation says which of the two it is. Everything else is a chip like
     * any other.
     */
    keys(chip, state) {
        var all = this._order;
        for (var i = 0; i < all.length; i++) {
            var c = all[i], on = c === chip && (state === "held" || state === "lent");
            css.toggleClass(c, mtp_chip_lifted, on && state === "held");   // on the bar: picked up. In the tab, or nowhere: down
            if (!c._mark) continue;
            css.toggleClass(c._mark, mtp_chip_mark_on, on);
            css.extent(c._mark, on ? (state === "held" ? TabStrip.ON_THE_BAR : 1) : null);
        }
        return this;
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

    // ── The chip in the hand: the hand's, and what it asks of the strip ──
    /** Taken: the design's word for a thing dragged, in place of the seated one; the strip clips nothing meanwhile. */
    _grip(c) {
        css.removeClass(c, mtp_chip_seated);
        css.addClass(c, mtp_chip_dragging);
        css.addClass(this.el, mtp_strip_loose);
    }
    _release(c) {
        css.removeClass(c, mtp_chip_dragging);
        css.addClass(c, mtp_chip_seated);
        css.removeClass(this.el, mtp_strip_loose);
    }
    /**
     * The chips between the slot left and the slot aimed at step one pitch
     * aside, eased by the design; the rest stand. Over — the hand let go —
     * every step is taken back at once, not eased: the row is about to be
     * arranged, and a chip must not be seen sliding to where it already is.
     */
    _stepAside(from, to, pitch) {
        var over = to === from;
        for (var j = 0; j < this._order.length; j++) {
            if (j === from) continue;
            var s = TabDrag.shift(j, from, to), chip = this._order[j];
            if (s === 0) { css.removeClass(chip, mtp_chip_shifted); chip.style.removeProperty("--mtp-shift-x"); if (over) TabStrip._snap(chip); }
            else { css.addClass(chip, mtp_chip_shifted); chip.style.setProperty("--mtp-shift-x", (s * pitch) + "px"); }
        }
    }
    /** Whatever the chip was easing towards, it is there now. */
    static _snap(chip) {
        if (typeof chip.getAnimations !== "function") return;
        try { chip.getAnimations().forEach(function (a) { a.cancel(); }); } catch (err) {}
    }
    /** The chips in the row, in order. */
    _seated() { return this._order.slice(); }
    _others(c) {
        var out = [];
        for (var i = 0; i < this._order.length; i++) if (this._order[i] !== c) out.push(this._order[i]);
        return out;
    }
    /** The mark where a tab from outside would land, and the index it would take. */
    /** A point on the strip, or not: the index it would land at, marked, else −1 and nothing marked. */
    at(x, y) {
        var s = this.el.getBoundingClientRect();
        if (x < s.left || x > s.right || y < s.top || y > s.bottom) { this.unmark(); return -1; }
        return this.markAt(x);
    }
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
