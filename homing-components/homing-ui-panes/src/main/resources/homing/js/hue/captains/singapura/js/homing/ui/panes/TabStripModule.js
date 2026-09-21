// =============================================================================
// TabStrip — the row of chips over a pane: chips with a label and a cross,
// the tail with the add button and the count, the drop mark, and the drag
// that reorders. It holds no tab state: the pane tells it which chips, in
// which order, and which is active; it tells the pane what was clicked and
// where a dragged chip landed. A branch component: the pane makes a
// sub-branch for it and hands it in.
//
//   new TabStrip(branch, { onAdd?, onDrop(chip, dest), keyboard?, keyboardId? })
//     keyboard   the page's KeyboardSteward, for a strip that stands alone: it joins
//                the keyboard party as keyboardId (its branch's name, unless said) and
//                claims by the convention. A strip inside a pane is built without: the
//                pane holds the keys and hands them to the strip by strip.key(ev)
//     strip.el
//     strip.key(ev)                 a keydown, from whoever holds the keys: Enter or Space on
//                                   a chip selects it, ContextMenu or Shift+F10 asks for its
//                                   menu; true when taken. No keydown listener of its own
//     strip.chip({ id, title, pinned, closable }, { onSelect, onClose, onMenu? }, branch?) → chipEl
//         onMenu(at, keyboard) → boolean: a right-click on the chip, or the
//         ContextMenu key / Shift+F10 on it, asks for the tab's menu at a point;
//         true means it was taken and the browser's own menu is suppressed
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
//
// Every chip is in the tab order — the Tab key walks the strip, Enter or
// Space selects — and the design draws the hover, the press, the selected
// one and the focus ring, since the chip wears Selectable. The press is the
// selection: a chip is selected the moment it is pressed, before any
// release, and the chip in the hand — pressed, dragged, pulled off — is the
// selected one throughout; nothing else selects while it is held.
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
    constructor(branch, opts) {
        if (!branch) throw new Error("[TabStrip] a branch of its own is required");
        var self = this;
        branch.activate(_stripOwner);
        this._branch = branch;
        this._onDrop = opts && typeof opts.onDrop === "function" ? opts.onDrop : null;
        this._hand = new TabHand(this);   // the one hand on the strip's chips
        this._order = [];                 // the chips as last arranged
        this._pinned = new Set();         // the chips that are pinned
        this._size = null;                // the chips' size and aspect, null the design's
        this._aspect = null;
        this._handlers = new Map();       // chip → its handlers, for the keys

        var el = branch.createElement("strip", "div");
        css.addClass(el, mtp_strip);
        el.setAttribute("role", "tablist");
        this.el = el;
        // the keys: through the party, when the strip stands alone and is handed the steward
        this._kb = null; this._kbId = null; this._offKeys = null;
        if (opts && opts.keyboard) {
            this._kb = opts.keyboard;
            this._kbId = this._kb.join(opts.keyboardId != null ? String(opts.keyboardId) : branch.name, { keyDown: function (ev) { return self.key(ev); } });
            this._offKeys = Keys.claimOn(el, this._kb, this._kbId);
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
        this._handlers.set(c, handlers);
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
    /** A keydown from whoever holds the keys, on a chip: Enter or Space selects it, ContextMenu or Shift+F10 asks for its menu; true when taken. */
    key(ev) {
        var c = ev && ev.target, handlers = c ? this._handlers.get(c) : null;
        if (!handlers) return false;
        if (ev.key === "Enter" || ev.key === " ") { handlers.onSelect(); return true; }
        if (handlers.onMenu && (ev.key === "ContextMenu" || (ev.shiftKey && ev.key === "F10"))) {
            var r = c.getBoundingClientRect();
            return !!handlers.onMenu({ x: r.left + 12, y: r.bottom - 2 }, true);
        }
        return false;
    }
    /** The strip taken down: its element removed, its branch dissolved; chips minted on branches of their own are their owners'. */
    dispose() {
        if (this._offKeys) { this._offKeys(); this._offKeys = null; }
        if (this._kb) { this._kb.leave(this._kbId); this._kb = null; }
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
        this._handlers.delete(c);
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
