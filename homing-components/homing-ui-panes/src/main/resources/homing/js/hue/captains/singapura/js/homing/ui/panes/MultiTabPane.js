// =============================================================================
// MultiTabPane — one pane of tabs: a strip of chips over one content area,
// each tab holding a widget constructed by the base's contract.
//
//   mountMultiTabPane({ branch, host, slotId?, budget?, addable?,
//                       onAddTab?, onTabAdded?, onTabRemoved?, onTabMoved?,
//                       onTabActivated?, onTabAttached? })                → pane
//
//   pane.addTab({ id, title, widget, pinned?, closable? })  → index; the widget
//       is what construct(branch, params) returned: { root, setActive?, dispose? }.
//       Its root is appended to the tab's panel once and never detached; a
//       switch shows one panel and hides the rest. Reports onTabAdded, then
//       onTabActivated if the tab became the active one.
//   pane.attachTab(tab, index)   → index; the same from outside (a re-dock, a
//       programmatic re-parent), reported as onTabAttached instead.
//   pane.removeTab(id)           → the tab; the widget is disposed, then
//       onTabRemoved(slotId, tab, fromIndex); a neighbour is activated after.
//   pane.detachTab(id)           → the tab, NOT disposed and not reported: it
//       travels on, widget and all, to be attached elsewhere.
//   pane.switchTab(id)           → onTabActivated(slotId, id)
//   pane.moveTab(id, destIndex)  → onTabMoved(slotId, tab, srcIndex, slotId, destIndex),
//       destIndex being where the tab ends up. A drag on the strip is this.
//   pane.tabs() .activeTab() .has(id) .tabIndexOf(id) .count()
//   pane.budget() .canAdd() .setAddEnabled(b)
//   pane.contentElOf(id) .widgetOf(id) .getState() .el .slotId
//   pane.dispose()               → every widget disposed in order, the branch dissolved
//
// The callback names and argument shapes are the studio pane's, so what
// records its mutations records these; slotId is the pane's own, given or
// "main", and appears where the studio's slot did — for a move within one
// pane, as both source and destination.
//
// The pane never calls setActive: which tab's widget is active is the
// holder's to decide, from onTabActivated, as the studio's focus machinery
// decides it there. Pinned tabs sit first, cannot be closed and are not
// dragged; a drop never lands before them.
//
// Everything is on a child of the caller's branch, named after the slot;
// the strip is TabStrip's; `css` is injected with the styles import.
// =============================================================================

var _DEFAULT_BUDGET = 16;
var _seq = 0;
const _paneOwner = Object.freeze({ toString: () => "multiTabPane" });

function mountMultiTabPane(opts) {
    if (!opts || !opts.branch) throw new Error("[MultiTabPane] opts.branch is required");
    if (!opts.host) throw new Error("[MultiTabPane] opts.host is required");
    var slotId = opts.slotId == null ? "main" : String(opts.slotId);
    var budget = opts.budget == null ? _DEFAULT_BUDGET : Math.max(1, opts.budget | 0);
    var addEnabled = opts.addable !== false && typeof opts.onAddTab === "function";
    var branchName = "mtp_" + slotId.replace(/[^A-Za-z0-9_-]/g, "_") + "_" + (++_seq);
    var branch = opts.branch.createBranch(branchName);
    branch.activate(_paneOwner);

    var tabs = [];          // entries in strip order: { id, tab, pinned, widget, chip, panel }
    var activeId = null;
    var disposed = false;

    // ── The frame ─────────────────────────────────────────────────────────
    var root = branch.createElement("pane", "div");
    css.addClass(root, mtp_pane);

    var strip = createTabStrip(branch, {
        onAdd: typeof opts.onAddTab === "function" ? function () { if (canAdd()) _fire(opts.onAddTab, "onAddTab", [slotId]); } : null,
        onDrop: function (chip, dest) { var i = _findChip(chip); if (i >= 0) moveTab(tabs[i].id, dest); }
    });
    root.appendChild(strip.el);

    var content = branch.createElement("content", "div");
    css.addClass(content, mtp_content);
    root.appendChild(content);

    var empty = branch.createElement("empty", "div");
    css.addClass(empty, mtp_empty);
    empty.textContent = "No tabs";
    content.appendChild(empty);

    opts.host.appendChild(root);
    _refresh();

    // ── Reporting ─────────────────────────────────────────────────────────
    function _fire(cb, name, args) {
        if (typeof cb !== "function") return;
        try { cb.apply(null, args); }
        catch (e) { console.error("[MultiTabPane] " + name + " threw:", e); }
    }

    // ── State ─────────────────────────────────────────────────────────────
    function _find(id) {
        for (var i = 0; i < tabs.length; i++) if (tabs[i].id === id) return i;
        return -1;
    }
    function _findChip(chip) {
        for (var i = 0; i < tabs.length; i++) if (tabs[i].chip === chip) return i;
        return -1;
    }
    function _require(id) {
        var i = _find(id);
        if (i < 0) throw new Error("[MultiTabPane] no tab '" + id + "' in slot '" + slotId + "'");
        return i;
    }
    function _pinnedCount() {
        var n = 0;
        while (n < tabs.length && tabs[n].pinned) n++;
        return n;
    }
    function _validate(tab) {
        if (!tab || typeof tab.id !== "string" || !tab.id) throw new Error("[MultiTabPane] tab.id must be a non-empty string");
        if (_find(tab.id) >= 0) throw new Error("[MultiTabPane] tab '" + tab.id + "' is already in slot '" + slotId + "'");
        if (!tab.widget || typeof tab.widget !== "object" || !tab.widget.root)
            throw new Error("[MultiTabPane] tab '" + tab.id + "' has no widget with a root");
        if (tabs.length >= budget) throw new Error("[MultiTabPane] the budget of " + budget + " is spent in slot '" + slotId + "'");
    }

    // ── The chips and the panels ──────────────────────────────────────────
    function _build(tab) {
        var chip = strip.chip(tab, { onSelect: function () { switchTab(tab.id); }, onClose: function () { removeTab(tab.id); } });
        var panel = branch.createElement("panel-" + tab.id.replace(/[^A-Za-z0-9_-]/g, "_"), "div");
        css.addClass(panel, mtp_tab_content, mtp_tab_content_hidden);
        panel.setAttribute("role", "tabpanel");
        panel.appendChild(tab.widget.root);
        return { id: tab.id, tab: tab, pinned: !!tab.pinned, widget: tab.widget, chip: chip, panel: panel };
    }
    /** Into the state at index, clamped to the pinned block or after it; then the strip follows. */
    function _place(entry, index) {
        var lo = entry.pinned ? 0 : _pinnedCount();
        var hi = entry.pinned ? _pinnedCount() : tabs.length;
        if (index == null || index > hi) index = hi;
        if (index < lo) index = lo;
        tabs.splice(index, 0, entry);
        content.appendChild(entry.panel);
        _refresh();
        return index;
    }
    function _chips() {
        var out = [];
        for (var i = 0; i < tabs.length; i++) out.push(tabs[i].chip);
        return out;
    }
    function _refresh() {
        strip.arrange(_chips());
        strip.count(tabs.length, budget, canAdd());
        css.toggleClass(empty, mtp_tab_content_hidden, tabs.length > 0);
    }
    function _show(id) {
        activeId = id;
        var active = null;
        for (var i = 0; i < tabs.length; i++) {
            var on = tabs[i].id === id;
            if (on) active = tabs[i].chip;
            css.toggleClass(tabs[i].panel, mtp_tab_content_hidden, !on);
        }
        strip.select(_chips(), active);
    }
    function _takeOut(i) {
        var entry = tabs[i];
        tabs.splice(i, 1);
        strip.remove(entry.chip);
        content.removeChild(entry.panel);
        _refresh();
        if (activeId === entry.id) activeId = null;
        return entry;
    }
    /** After a tab left index i: the one now there, else the one before, becomes active. */
    function _activateNeighbour(i) {
        if (activeId !== null) return;
        var next = tabs[i] || tabs[i - 1];
        if (next) switchTab(next.id);
    }

    // ── The surface ───────────────────────────────────────────────────────
    function addTab(tab) {
        _validate(tab);
        var index = _place(_build(tab), null);
        _fire(opts.onTabAdded, "onTabAdded", [slotId, tab, index]);
        if (activeId === null) switchTab(tab.id);
        return index;
    }
    function attachTab(tab, index) {
        _validate(tab);
        var at = _place(_build(tab), index == null ? null : index | 0);
        _fire(opts.onTabAttached, "onTabAttached", [slotId, tab, at]);
        if (activeId === null) switchTab(tab.id);
        return at;
    }
    function removeTab(id) {
        var i = _require(id);
        var entry = _takeOut(i);
        if (typeof entry.widget.dispose === "function") {
            try { entry.widget.dispose(); } catch (e) { console.error("[MultiTabPane] widget.dispose threw:", e); }
        }
        _fire(opts.onTabRemoved, "onTabRemoved", [slotId, entry.tab, i]);
        _activateNeighbour(i);
        return entry.tab;
    }
    function detachTab(id) {
        var i = _require(id);
        var entry = _takeOut(i);
        entry.panel.removeChild(entry.widget.root);
        _activateNeighbour(i);
        return entry.tab;
    }
    function switchTab(id) {
        _require(id);
        if (activeId === id) return;
        _show(id);
        _fire(opts.onTabActivated, "onTabActivated", [slotId, id]);
    }
    function moveTab(id, destIndex) {
        var src = _require(id);
        var entry = tabs[src];
        var lo = entry.pinned ? 0 : _pinnedCount();
        var hi = entry.pinned ? _pinnedCount() - 1 : tabs.length - 1;
        var dest = Math.min(Math.max(destIndex | 0, lo), hi);
        if (dest === src) return false;
        tabs.splice(src, 1);
        tabs.splice(dest, 0, entry);
        _refresh();
        _fire(opts.onTabMoved, "onTabMoved", [slotId, entry.tab, src, slotId, dest]);
        return true;
    }
    function canAdd() { return addEnabled && tabs.length < budget; }
    function setAddEnabled(on) { addEnabled = !!on; _refresh(); }
    function getState() {
        var list = [];
        for (var i = 0; i < tabs.length; i++) list.push({ id: tabs[i].id, title: tabs[i].tab.title, pinned: tabs[i].pinned });
        return { slotId: slotId, activeTabId: activeId, tabs: list };
    }
    function dispose() {
        if (disposed) return;
        disposed = true;
        for (var i = 0; i < tabs.length; i++) {
            var w = tabs[i].widget;
            if (typeof w.dispose === "function") {
                try { w.dispose(); } catch (e) { console.error("[MultiTabPane] widget.dispose threw:", e); }
            }
        }
        tabs = [];
        activeId = null;
        if (root.parentNode) root.parentNode.removeChild(root);
        opts.branch.dissolveBranch(branchName);
    }

    return Object.freeze({
        el: root,
        slotId: slotId,
        addTab: addTab,
        attachTab: attachTab,
        removeTab: removeTab,
        detachTab: detachTab,
        switchTab: switchTab,
        moveTab: moveTab,
        tabs: function () { var ids = []; for (var i = 0; i < tabs.length; i++) ids.push(tabs[i].id); return ids; },
        activeTab: function () { return activeId; },
        has: function (id) { return _find(id) >= 0; },
        tabIndexOf: _find,
        count: function () { return tabs.length; },
        budget: function () { return budget; },
        canAdd: canAdd,
        setAddEnabled: setAddEnabled,
        contentElOf: function (id) { var i = _find(id); return i < 0 ? null : tabs[i].panel; },
        widgetOf: function (id) { var i = _find(id); return i < 0 ? null : tabs[i].widget; },
        getState: getState,
        dispose: dispose
    });
}
