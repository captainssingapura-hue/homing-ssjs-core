// =============================================================================
// MultiTabPane — one pane of tabs: a strip of chips over one content area,
// each tab holding a widget constructed by the base's contract. A branch
// component: the caller makes a sub-branch for it and hands it in; dispose()
// dissolves it.
//
//   new MultiTabPane(branch, { host, slotId?, budget?, addable?, onEvent? })
//     branch: the pane's own, handed unactivated
//     host:   a flex column; the pane is its item and fills it.
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
//   pane.contentElOf(id) .widgetOf(id) .getState() .el .slotId
//   pane.dispose()               → every widget disposed in order, the branch dissolved
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
// decides it there. Pinned tabs sit first, cannot be closed and are not
// dragged; a drop never lands before them.
//
// The strip is TabStrip's; `css` is injected with the styles import.
// =============================================================================

var _DEFAULT_BUDGET = 16;
const _paneOwner = Object.freeze({ toString: () => "multiTabPane" });

class MultiTabPane {
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
        var chip = this._strip.chip(tab, { onSelect: function () { self.switchTab(tab.id); }, onClose: function () { self.removeTab(tab.id); } });
        var panel = this._branch.createElement("panel-" + tab.id.replace(/[^A-Za-z0-9_-]/g, "_"), "div");
        css.addClass(panel, mtp_tab_content, mtp_tab_content_hidden);
        panel.setAttribute("role", "tabpanel");
        panel.appendChild(tab.widget.root);
        return { id: tab.id, tab: tab, pinned: !!tab.pinned, widget: tab.widget, chip: chip, panel: panel };
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
        var entry = this._takeOut(i);
        if (typeof entry.widget.dispose === "function") {
            try { entry.widget.dispose(); } catch (e) { console.error("[MultiTabPane] widget.dispose threw:", e); }
        }
        this._fire(PaneEvents.TabRemoved(this.slotId, entry.tab, i));
        this._activateNeighbour(i);
        return entry.tab;
    }
    detachTab(id) {
        var i = this._require(id);
        var entry = this._takeOut(i);
        entry.panel.removeChild(entry.widget.root);
        this._activateNeighbour(i);
        return entry.tab;
    }
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
    contentElOf(id) { var i = this._find(id); return i < 0 ? null : this._tabs[i].panel; }
    widgetOf(id) { var i = this._find(id); return i < 0 ? null : this._tabs[i].widget; }
    getState() {
        var list = [];
        for (var i = 0; i < this._tabs.length; i++) list.push({ id: this._tabs[i].id, title: this._tabs[i].tab.title, pinned: this._tabs[i].pinned });
        return { slotId: this.slotId, activeTabId: this._activeId, tabs: list };
    }
    dispose() {
        if (this._disposed) return;
        this._disposed = true;
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
