// =============================================================================
// TabStrip — the row of chips over a pane: chips with a label and a cross,
// the tail with the add button and the count, the drop mark, and the drag
// that reorders. It holds no tab state: the pane tells it which chips, in
// which order, and which is active; it tells the pane what was clicked and
// where a dragged chip landed. A branch component: the pane makes a
// sub-branch for it and hands it in.
//
//   new TabStrip(branch, { onAdd?, onDrop(chip, dest), onDragOut?(chip, pointerEvent, grab),
//                          floating?, onFloat?(chip, pointerEvent, grab), onLand?(chip, at) })
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
//     strip.dispose()
//     strip.aspect(a?)              the chips' aspect, −1..1, 0 the design's proportion — wide,
//                                   a browser's tab — narrower at −1, wider at +1
//     strip.floating(chip)          → whether the chip is afloat
//     strip.seat(chip)              a floating chip back in the row, at its place in the order
//
// Every chip is in the tab order — the Tab key walks the strip, Enter or
// Space selects — and the design draws the hover, the press, the selected
// one and the focus ring, since the chip wears Selectable. The press is the
// selection: a chip is selected the moment it is pressed, before any
// release, and the chip in the hand — pressed, dragged, pulled off — is the
// selected one throughout; nothing else selects while it is held.
//
// The drag is a browser's: the chip pressed is lifted and goes where the
// hand goes — the press remembered as an offset within the chip, so the
// chip is placed and never the hand — kept within the row and never above
// its slot; the slot it is nearest is where it will land, and the chips
// between step aside, live, as it passes them. TabHand is the hand, TabDrag
// the arithmetic.
// Let go, the chip lands: onDrop(chip, dest), and the pane turns that into
// a move. Pulled down across the strip's edge until more of the chip is
// out than in — two thirds — it leaves the row, one of two ways:
//
//   floating: false   the drag is dropped here and onDragOut is told, with the
//                     pointer event and the grab — the press's offset within
//                     the chip — for a holder that takes the tab away and
//                     floats it under the same hand at the same place in it.
//   floating: true    the chip stays the strip's own, the same element, now
//                     afloat: out of the row, which closes behind it, free
//                     under the hand — onFloat(chip, e, grab) — and left where
//                     the hand lets go, onLand(chip, { x, y }) in the strip's
//                     frame. Pressed again it is in the hand again; brought
//                     back until more of it is on the strip than off, it is
//                     seated: in the row, stepping the others aside, landing
//                     as any chip does. The strip clips nothing while a chip
//                     is loose.
//
// Pinned chips are not dragged and never leave. A tab offered from outside
// is marked where it would land: a bar inserted between the seated chips,
// at the count of those whose middle is left of the point, never before the
// pinned ones. Nothing is positioned by hand: the chip in the hand carries
// --mtp-drag-x/y, a chip stepping aside --mtp-shift-x, a chip afloat
// --mtp-float-x/y. `css` is injected with the styles import.
// =============================================================================

const _stripOwner = Object.freeze({ toString: () => "tabStrip" });

class TabStrip {
    constructor(branch, opts) {
        if (!branch) throw new Error("[TabStrip] a branch of its own is required");
        var self = this;
        branch.activate(_stripOwner);
        this._branch = branch;
        this._onDrop = opts && typeof opts.onDrop === "function" ? opts.onDrop : null;
        this._onDragOut = opts && typeof opts.onDragOut === "function" ? opts.onDragOut : null;
        this._floats = !!(opts && opts.floating);
        this._onFloat = opts && typeof opts.onFloat === "function" ? opts.onFloat : null;
        this._onLand = opts && typeof opts.onLand === "function" ? opts.onLand : null;
        this._afloat = new Set();         // the chips afloat
        this._hand = new TabHand(this);   // the one hand on the strip's chips
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
        css.addClass(c, mtp_chip, mtp_chip_seated);
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
        c.addEventListener("pointerdown", function (ev) {
            if (ev.button !== 0 || (closeBtn && closeBtn.contains(ev.target))) return;
            handlers.onSelect();
        });
        c.addEventListener("keydown", function (ev) {
            if (ev.key === "Enter" || ev.key === " ") { ev.preventDefault(); handlers.onSelect(); }
        });
        if (this._size != null) css.size(c, this._size);
        if (this._aspect != null) css.aspect(c, this._aspect);
        if (tab.pinned) this._pinned.add(c);
        else this._hand.arm(c, closeBtn);
        return c;
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
        this._afloat.delete(c);
        this._loose();
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

    // ── The chip in the hand: the hand's, and what it asks of the strip ──
    _grip(c) {
        css.removeClass(c, mtp_chip_seated, mtp_chip_afloat);
        css.addClass(c, mtp_chip_dragging);
        this._loose();
    }
    _release(c, afloat) {
        css.removeClass(c, mtp_chip_dragging);
        css.toggleClass(c, mtp_chip_afloat, afloat);
        css.toggleClass(c, mtp_chip_seated, !afloat);
        this._loose();
    }
    /** The chips between the slot left and the slot aimed at step one pitch aside; the rest, and all of them once it is over, stand where they are. */
    _stepAside(from, to, pitch) {
        var seated = this._seated();
        for (var j = 0; j < seated.length; j++) {
            if (j === from) continue;
            var s = TabDrag.shift(j, from, to), chip = seated[j];
            if (s === 0) { css.removeClass(chip, mtp_chip_shifted); chip.style.removeProperty("--mtp-shift-x"); }
            else { css.addClass(chip, mtp_chip_shifted); chip.style.setProperty("--mtp-shift-x", (s * pitch) + "px"); }
        }
    }
    /** A chip afloat at a point in the strip's frame. */
    _float(c, x, y) {
        this._afloat.add(c);
        css.addClass(c, mtp_chip_floating);
        c.style.setProperty("--mtp-float-x", x + "px");
        c.style.setProperty("--mtp-float-y", y + "px");
        this._loose();
    }
    _seat(c) {
        this._afloat.delete(c);
        css.removeClass(c, mtp_chip_floating, mtp_chip_afloat);
        c.style.removeProperty("--mtp-float-x");
        c.style.removeProperty("--mtp-float-y");
        css.addClass(c, mtp_chip_seated);
        this._loose();
    }
    /** Where a chip afloat is, in the strip's frame. */
    _at(c) {
        return { x: parseFloat(c.style.getPropertyValue("--mtp-float-x") || "0"), y: parseFloat(c.style.getPropertyValue("--mtp-float-y") || "0") };
    }
    /** The strip clips nothing while a chip is loose: in the hand, or afloat. */
    _loose() { css.toggleClass(this.el, mtp_strip_loose, !!this._hand.held() || this._afloat.size > 0); }
    floating(c) { return this._afloat.has(c); }
    seat(c) { if (this._afloat.has(c) && this._hand.held() !== c) this._seat(c); }
    /** The chips in the row, in order: every chip arranged that is not afloat. */
    _seated() {
        var out = [];
        for (var i = 0; i < this._order.length; i++) if (!this._afloat.has(this._order[i])) out.push(this._order[i]);
        return out;
    }
    _others(c) {
        var out = [], seated = this._seated();
        for (var i = 0; i < seated.length; i++) if (seated[i] !== c) out.push(seated[i]);
        return out;
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
