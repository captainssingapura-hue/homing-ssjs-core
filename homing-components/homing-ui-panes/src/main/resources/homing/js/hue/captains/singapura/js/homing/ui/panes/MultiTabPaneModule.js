// =============================================================================
// MultiTabPane — one pane of tabs: a strip of chips over one content area,
// each tab holding a widget constructed by the base's contract. A branch
// component: the caller makes a sub-branch for it and hands it in; dispose()
// dissolves it.
//
//   new MultiTabPane(branch, { host, slotId?, budget?, addable?, onEvent?, menus?, keyboard?, keyboardId? })
//     branch: the pane's own, handed unactivated
//     host:   a flex column; the pane is its item and fills it.
//     menus:  the page's ContextMenuSteward, when the page offers menus: a
//             right-click on a chip, or the ContextMenu key / Shift+F10 on it,
//             asks the steward for MultiTabPane.MENU — the kind "tab", which
//             the pane's declaration names as its need and any site serving
//             the pane holds — bound to { pane, tab, anchor }: this pane, the
//             tab record, the chip. The steward says whether it took the
//             request; only then is the browser's menu suppressed. What a
//             pick does — detach, close — is the page's handler for "tab".
//     keyboard: the page's KeyboardSteward: the pane joins the keyboard party as
//             keyboardId (its branch's name, unless said) and claims by the
//             convention — a press or the focus arriving anywhere in it. It holds
//             the keys for what is inside: a key goes to the strip (Enter, Space,
//             the menu key on a focused chip), then to the active tab's widget by
//             its key(ev), when it has one; the strip and the widgets are built
//             without a steward of their own. No keydown listener of its own.
//
//   pane.addTab({ id, title, widget, pinned?, closable? })  → index; the widget
//       is an instance by the base's contract: root, setActive?, dispose?.
//       Its root is appended to the tab's panel once and never detached; a
//       switch shows one panel and hides the rest. Reports TabAdded, then
//       TabActivated if the tab became the active one.
//   pane.attachTab(tab, index)   → index; the same from outside (a re-dock, a
//       programmatic re-parent), reported as TabAttached instead.
//   pane.removeTab(id)           → the tab; the widget is disposed, then
//       TabRemoved(slotId, tab, fromIndex); a neighbour is activated after.
//   pane.detachTab(id)           → the tab, NOT disposed and not reported: it
//       travels on, widget and all, to be attached elsewhere.
//   pane.switchTab(id)           → TabActivated(slotId, id)
//   pane.moveTab(id, destIndex)  → TabMoved(slotId, tab, srcIndex, slotId, destIndex),
//       destIndex being where the tab ends up. A drag on the strip is this.
//   pane.tabs() .activeTab() .has(id) .tabIndexOf(id) .count()
//   pane.budget() .canAdd() .setAddEnabled(b)
//   pane.size(s?) .aspect(a?)    the chips' size and aspect, −1..1, null the design's
//   pane.contentElOf(id) .widgetOf(id) .getState() .el .slotId
//   pane.dispose()               → every widget disposed in order, the branch dissolved
//
// The pane is a dock. A tab leaves it by call — detachTab, for a holder that
// floats it — the strip's own drag staying on its rail for now. A tab from
// outside is offered by dropAt(clientX, clientY):
// over the strip — the dock's landing, not its content, since docks may tile
// a box and a float let go over content stays afloat — it marks where the tab
// would land and answers the index, elsewhere −1; the pane wears the drop-
// target word while an offer stands, and dropClear() ends it. attachTab is the
// drop.
//
// Every mutation is one event on one sink, onEvent(ev): a frozen object from
// PaneEvents tagged by kind — TabAdded, TabRemoved, TabMoved, TabActivated,
// TabAttached, AddRequested — whose fields are the Java PaneEvent records'
// components. The vocabulary is the studio pane's; the shape is data, so an
// event goes into a log or a checkpoint as it is. slotId is the pane's own,
// given or "main"; a move within one pane names it as both ends.
//
// The pane never calls setActive: which tab's widget is active is the
// holder's to decide, from TabActivated, as the studio's focus machinery
// decides it there. A chip is activated when it is pressed — before any
// release — so the tab in the hand is the active one through a reorder or
// a pull off the strip. Pinned tabs sit first, cannot be closed and are not
// dragged; a drop never lands before them.
//
// A tab's chip and panel are minted on a sub-branch of the pane's, tab-<id>,
// dissolved when the tab is removed or detached — so a tab that leaves and
// comes back, the same id to the same pane, is minted afresh; the widget's
// root is its holder's and only passes through.
//
// The strip is TabStrip's; `css` is injected with the styles import.
// =============================================================================

var _DEFAULT_BUDGET = 16;
const _paneOwner = Object.freeze({ toString: () => "multiTabPane" });

class MultiTabPane {
    /** The kind a chip's menu is asked for: the Java TabMenu's. */
    static MENU = "tab";

    constructor(branch, opts) {
        if (!branch) throw new Error("[MultiTabPane] a branch of its own is required");
        if (!opts || !opts.host) throw new Error("[MultiTabPane] opts.host is required");
        var self = this;
        branch.activate(_paneOwner);
        this._branch = branch;
        this.slotId = opts.slotId == null ? "main" : String(opts.slotId);
        this._budget = opts.budget == null ? _DEFAULT_BUDGET : Math.max(1, opts.budget | 0);
        this._addEnabled = opts.addable !== false;
        this._sink = typeof opts.onEvent === "function" ? opts.onEvent : null;
        this._menus = opts.menus && typeof opts.menus.open === "function" ? opts.menus : null;
        this._tabs = [];          // entries in strip order: { id, tab, pinned, widget, chip, panel }
        this._activeId = null;
        this._disposed = false;

        // ── The frame ─────────────────────────────────────────────────────
        var root = branch.createElement("pane", "div");
        css.addClass(root, mtp_pane);
        this.el = root;

        this._strip = new TabStrip(branch.createBranch("strip"), {
            onAdd: opts.addable === false ? null : function () { if (self.canAdd()) self._fire(PaneEvents.AddRequested(self.slotId)); },
            onDrop: function (chip, dest) { var i = self._findChip(chip); if (i >= 0) self.moveTab(self._tabs[i].id, dest); }
        });
        root.appendChild(this._strip.el);
        this._kb = null; this._kbId = null; this._offKeys = null;
        if (opts.keyboard) {
            this._kb = opts.keyboard;
            this._kbId = this._kb.join(opts.keyboardId != null ? String(opts.keyboardId) : branch.name, { keyDown: function (ev) { return self.key(ev); } });
            this._offKeys = Keys.claimOn(root, this._kb, this._kbId);
        }

        this._content = branch.createElement("content", "div");
        css.addClass(this._content, mtp_content);
        root.appendChild(this._content);

        this._empty = branch.createElement("empty", "div");
        css.addClass(this._empty, mtp_empty);
        this._empty.textContent = "No tabs";
        this._content.appendChild(this._empty);

        opts.host.appendChild(root);
        this._refresh();
    }

    // ── Reporting ─────────────────────────────────────────────────────────
    _fire(ev) {
        if (!this._sink) return;
        try { this._sink(ev); }
        catch (e) { console.error("[MultiTabPane] onEvent threw on " + ev.kind + ":", e); }
    }

    // ── State ─────────────────────────────────────────────────────────────
    _find(id) {
        for (var i = 0; i < this._tabs.length; i++) if (this._tabs[i].id === id) return i;
        return -1;
    }
    _findChip(chip) {
        for (var i = 0; i < this._tabs.length; i++) if (this._tabs[i].chip === chip) return i;
        return -1;
    }
    _require(id) {
        var i = this._find(id);
        if (i < 0) throw new Error("[MultiTabPane] no tab '" + id + "' in slot '" + this.slotId + "'");
        return i;
    }
    _pinnedCount() {
        var n = 0;
        while (n < this._tabs.length && this._tabs[n].pinned) n++;
        return n;
    }
    _validate(tab) {
        if (!tab || typeof tab.id !== "string" || !tab.id) throw new Error("[MultiTabPane] tab.id must be a non-empty string");
        if (this._find(tab.id) >= 0) throw new Error("[MultiTabPane] tab '" + tab.id + "' is already in slot '" + this.slotId + "'");
        if (!tab.widget || typeof tab.widget !== "object" || !tab.widget.root)
            throw new Error("[MultiTabPane] tab '" + tab.id + "' has no widget with a root");
        if (this._tabs.length >= this._budget) throw new Error("[MultiTabPane] the budget of " + this._budget + " is spent in slot '" + this.slotId + "'");
    }

    // ── The chips and the panels ──────────────────────────────────────────
    _build(tab) {
        var self = this;
        var own = this._branch.createBranch("tab-" + tab.id.replace(/[^A-Za-z0-9_-]/g, "_"));
        own.activate(_paneOwner);
        var handlers = { onSelect: function () { self.switchTab(tab.id); }, onClose: function () { self.removeTab(tab.id); } };
        if (this._menus) handlers.onMenu = function (at, keyboard) {
            var i = self._find(tab.id);
            if (i < 0) return false;
            self.switchTab(tab.id);
            return self._menus.open(MultiTabPane.MENU, { pane: self, tab: self._tabs[i].tab, anchor: self._tabs[i].chip }, at, { anchor: self._tabs[i].chip, keyboard: keyboard });
        };
        var chip = this._strip.chip(tab, handlers, own);
        var panel = own.createElement("panel", "div");
        css.addClass(panel, mtp_tab_content, mtp_tab_content_hidden);
        panel.setAttribute("role", "tabpanel");
        panel.appendChild(tab.widget.root);
        return { id: tab.id, tab: tab, pinned: !!tab.pinned, widget: tab.widget, chip: chip, panel: panel, branch: own };
    }
    /** Into the state at index, clamped to the pinned block or after it; then the strip follows. */
    _place(entry, index) {
        var lo = entry.pinned ? 0 : this._pinnedCount();
        var hi = entry.pinned ? this._pinnedCount() : this._tabs.length;
        if (index == null || index > hi) index = hi;
        if (index < lo) index = lo;
        this._tabs.splice(index, 0, entry);
        this._content.appendChild(entry.panel);
        this._refresh();
        return index;
    }
    _chips() {
        var out = [];
        for (var i = 0; i < this._tabs.length; i++) out.push(this._tabs[i].chip);
        return out;
    }
    _refresh() {
        this._strip.arrange(this._chips());
        this._strip.count(this._tabs.length, this._budget, this.canAdd());
        css.toggleClass(this._empty, mtp_tab_content_hidden, this._tabs.length > 0);
    }
    _show(id) {
        this._activeId = id;
        var active = null;
        for (var i = 0; i < this._tabs.length; i++) {
            var on = this._tabs[i].id === id;
            if (on) active = this._tabs[i].chip;
            css.toggleClass(this._tabs[i].panel, mtp_tab_content_hidden, !on);
        }
        this._strip.select(this._chips(), active);
    }
    _takeOut(i) {
        var entry = this._tabs[i];
        this._tabs.splice(i, 1);
        this._strip.remove(entry.chip);
        this._content.removeChild(entry.panel);
        this._refresh();
        if (this._activeId === entry.id) this._activeId = null;
        return entry;
    }
    /** After a tab left index i: the one now there, else the one before, becomes active. */
    _activateNeighbour(i) {
        if (this._activeId !== null) return;
        var next = this._tabs[i] || this._tabs[i - 1];
        if (next) this.switchTab(next.id);
    }

    // ── The surface ───────────────────────────────────────────────────────
    addTab(tab) {
        this._validate(tab);
        var index = this._place(this._build(tab), null);
        this._fire(PaneEvents.TabAdded(this.slotId, tab, index));
        if (this._activeId === null) this.switchTab(tab.id);
        return index;
    }
    attachTab(tab, index) {
        this._validate(tab);
        var at = this._place(this._build(tab), index == null ? null : index | 0);
        this._fire(PaneEvents.TabAttached(this.slotId, tab, at));
        if (this._activeId === null) this.switchTab(tab.id);
        return at;
    }
    removeTab(id) {
        var i = this._require(id);
        // closed from its own chip — the keyboard, or a menu whose close gave the focus back — the focus stays on the strip
        var hadFocus = typeof document !== "undefined" && document.activeElement === this._tabs[i].chip;
        var entry = this._takeOut(i);
        if (typeof entry.widget.dispose === "function") {
            try { entry.widget.dispose(); } catch (e) { console.error("[MultiTabPane] widget.dispose threw:", e); }
        }
        entry.branch.dissolve();
        this._fire(PaneEvents.TabRemoved(this.slotId, entry.tab, i));
        this._activateNeighbour(i);
        if (hadFocus) {
            var j = this._find(this._activeId);
            if (j >= 0) { try { this._tabs[j].chip.focus({ preventScroll: true }); } catch (e) {} }
        }
        return entry.tab;
    }
    detachTab(id) {
        var i = this._require(id);
        var entry = this._takeOut(i);
        entry.panel.removeChild(entry.widget.root);
        entry.branch.dissolve();
        this._activateNeighbour(i);
        return entry.tab;
    }
    /** A tab from outside, offered at a point: the index it would take on the strip, or −1 when the point is not on the strip. */
    dropAt(x, y) {
        var s = this._strip.el.getBoundingClientRect();
        if (x < s.left || x > s.right || y < s.top || y > s.bottom) { this.dropClear(); return -1; }
        var index = this._strip.markAt(x);
        css.addClass(this.el, mtp_dock_target);
        return index;
    }
    dropClear() { this._strip.unmark(); css.removeClass(this.el, mtp_dock_target); }
    switchTab(id) {
        this._require(id);
        if (this._activeId === id) return;
        this._show(id);
        this._fire(PaneEvents.TabActivated(this.slotId, id));
    }
    moveTab(id, destIndex) {
        var src = this._require(id);
        var entry = this._tabs[src];
        var lo = entry.pinned ? 0 : this._pinnedCount();
        var hi = entry.pinned ? this._pinnedCount() - 1 : this._tabs.length - 1;
        var dest = Math.min(Math.max(destIndex | 0, lo), hi);
        if (dest === src) return false;
        this._tabs.splice(src, 1);
        this._tabs.splice(dest, 0, entry);
        this._refresh();
        this._fire(PaneEvents.TabMoved(this.slotId, entry.tab, src, this.slotId, dest));
        return true;
    }
    tabs() { var ids = []; for (var i = 0; i < this._tabs.length; i++) ids.push(this._tabs[i].id); return ids; }
    activeTab() { return this._activeId; }
    has(id) { return this._find(id) >= 0; }
    tabIndexOf(id) { return this._find(id); }
    count() { return this._tabs.length; }
    budget() { return this._budget; }
    canAdd() { return this._addEnabled && this._tabs.length < this._budget; }
    setAddEnabled(on) { this._addEnabled = !!on; this._refresh(); }
    size(s) { this._strip.size(s); }
    aspect(a) { this._strip.aspect(a); }
    contentElOf(id) { var i = this._find(id); return i < 0 ? null : this._tabs[i].panel; }
    widgetOf(id) { var i = this._find(id); return i < 0 ? null : this._tabs[i].widget; }
    getState() {
        var list = [];
        for (var i = 0; i < this._tabs.length; i++) list.push({ id: this._tabs[i].id, title: this._tabs[i].tab.title, pinned: this._tabs[i].pinned });
        return { slotId: this.slotId, activeTabId: this._activeId, tabs: list };
    }
    /** A keydown while the pane holds the keys: the strip first, then the active tab's widget; true when taken. */
    key(ev) {
        if (this._strip.key(ev)) return true;
        var i = this._activeId === null ? -1 : this._find(this._activeId), w = i < 0 ? null : this._tabs[i].widget;
        return !!(w && typeof w.key === "function" && w.key(ev));
    }

    dispose() {
        if (this._disposed) return;
        this._disposed = true;
        if (this._offKeys) { this._offKeys(); this._offKeys = null; }
        if (this._kb) { this._kb.leave(this._kbId); this._kb = null; }
        for (var i = 0; i < this._tabs.length; i++) {
            var w = this._tabs[i].widget;
            if (typeof w.dispose === "function") {
                try { w.dispose(); } catch (e) { console.error("[MultiTabPane] widget.dispose threw:", e); }
            }
        }
        this._tabs = [];
        this._activeId = null;
        if (this.el.parentNode) this.el.parentNode.removeChild(this.el);
        this._branch.dissolve();
    }
}
