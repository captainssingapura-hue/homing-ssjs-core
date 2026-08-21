// =============================================================================
// WorkspaceFocusCoordinatorModule — RFC 0049 workspace focus coordinator.
//
// THE owner of the deep/shallow selection logic, generalising RFC 0048's
// PaneFocusNav. SplitPane and MultiTabPane are focus-agnostic primitives; this
// coordinator (shell layer) owns everything positional / mutable:
//
//   · the ONE selection { slotId, tabId, deep } (exclusivity),
//   · the per-tab FocusManagers (created on MTP's tab-content elements,
//     disposed with their tabs),
//   · click routing (MTP reports cover clicks via onChromeInteract; the
//     coordinator decides: single = select shallow, double = select deep),
//   · the shallow keyboard (arrows = cursor via mtp.neighbourOf, Tab = cycle
//     tabs within the pane, Enter = upgrade to deep),
//   · the unified release: the FM does the immutable work; the coordinator's
//     follow-up is its intent (give-up → shallow-select same; select-other →
//     continue; reposition/removal → shallow-select at the new place).
//
// MTP is driven purely through its renderer facet (paintSelection /
// setAddEnabled) and access facet (contentElOf / neighbourOf / getState). The
// chrome fans MTP's structural events into the on*() handlers here.
//
//   new WorkspaceFocusCoordinator({ mtp, host, onDeepChanged? }).attach();
// =============================================================================

class WorkspaceFocusCoordinator {

    constructor(opts) {
        opts = opts || {};
        if (!opts.mtp) throw new Error("[WorkspaceFocusCoordinator] opts.mtp is required");
        this._mtp   = opts.mtp;
        this._host  = opts.host || null;             // workspace content element, for key scoping
        this._FM    = opts.FocusManagerCtor || (typeof FocusManager !== "undefined" ? FocusManager : null);
        // Fired when the DEEP tab changes (prevTabId, nextTabId — either null).
        // The chrome wires this to replay/persistence (WorkspaceActiveChanged).
        this._cbDeepChanged = opts.onDeepChanged || null;

        this._fms          = new Map();   // tabId → FocusManager
        this._selectedSlot = null;        // the cursor pane (may be an empty pane)
        this._selectedTab  = null;        // the cursor pane's active tab id, or null
        this._deep         = false;       // is the selection entered?
        this._transport    = null;        // { tabId, modalEl } — a detached tab in the transport modal
        this._attached     = false;

        var self = this;
        this._onKeyDown = function (e) { self._handleKey(e); };
    }

    attach() {
        if (this._attached) return this;
        document.addEventListener("keydown", this._onKeyDown, true);
        this._attached = true;
        // Adopt any tabs that already exist, then boot shallow on the first pane.
        this._adoptExistingTabs();
        if (this._selectedSlot == null) {
            var first = this._firstSlot();
            if (first != null) this.selectShallow(first);
        }
        return this;
    }

    dispose() {
        if (!this._attached) return this;
        document.removeEventListener("keydown", this._onKeyDown, true);
        this._fms.forEach(function (fm) { try { fm.dispose(); } catch (e) {} });
        this._fms.clear();
        this._attached = false;
        return this;
    }

    // ── selection queries (the shell contract PaneFocusNav-era code read off MTP) ──

    mode()            { return this._deep ? "deep" : "shallow"; }
    selectedSlotId()  { return this._selectedSlot; }
    /** The deep-selected tab id, or null when shallow. */
    deepTabId()       { return this._deep ? this._selectedTab : null; }

    // ── the selection ops ────────────────────────────────────────────────────

    /** Select a pane shallow (the cursor). Invalidates any current selection. */
    selectShallow(slotId) {
        this._invalidate();
        this._selectedSlot = slotId;
        this._selectedTab  = this._activeTabOf(slotId);
        this._deep         = false;
        this._mtp.paintSelection(slotId, "shallow");
        return this;
    }

    /**
     * Select a pane deep (enter its active tab). Invalidates any current
     * selection first; an empty pane degrades to a shallow select. The optional
     * activationFn is the per-request focus function (from intendedFocusIn).
     */
    enterDeep(slotId, activationFn) {
        var tabId = this._activeTabOf(slotId);
        if (tabId == null) return this.selectShallow(slotId);   // empty pane — cursor only
        var prevDeep = this.deepTabId();
        this._invalidate();
        this._selectedSlot = slotId;
        this._selectedTab  = tabId;
        this._deep         = true;
        this._mtp.paintSelection(slotId, "deep");
        var fm = this._fms.get(tabId);
        if (fm) fm.enter(activationFn);
        this._fireDeepChanged(prevDeep, tabId);
        return this;
    }

    /** Enter deep on the pane that owns the given tab (intendedFocusIn path). */
    enterDeepForTab(tabId, activationFn) {
        var slot = this._slotOf(tabId);
        if (slot != null) this.enterDeep(slot, activationFn);
        return this;
    }

    /** Downgrade the current selection to shallow (give-up / programmatic). */
    releaseToShallow() {
        if (!this._deep) return this;
        var prevDeep = this._selectedTab;
        var fm = this._fms.get(this._selectedTab);
        if (fm) fm.release();
        this._deep = false;
        // Escape while in transport: the modal goes dormant (inert content,
        // accent off) and the cursor returns to a pane — the first one, since
        // the tab is in no pane to shallow-select.
        if (this._transport && this._transport.tabId === prevDeep) {
            this._clearTransport();
            this._fireDeepChanged(prevDeep, null);
            var f = this._firstSlot();
            if (f != null) return this.selectShallow(f);
            this._selectedSlot = null; this._selectedTab = null;
            this._mtp.paintSelection(null, null);
            return this;
        }
        // Hat: shallow-select the same tab, at its CURRENT pane.
        var slot = this._slotOf(prevDeep);
        this._selectedSlot = (slot != null) ? slot : this._selectedSlot;
        this._mtp.paintSelection(this._selectedSlot, "shallow");
        this._fireDeepChanged(prevDeep, null);
        return this;
    }

    // ── structural-event handlers (fanned out by the chrome) ─────────────────

    /** A tab was added — adopt it (create + attach its FocusManager). */
    onTabAdded(slotId, tab) { this._ensureFm(tab); }

    /** A dragged tab re-docked — its FM travelled with the content; just ensure. */
    onTabAttached(slotId, tab) { this._ensureFm(tab); }

    /** A tab was removed — dispose its FM; reselect if it was the selection. */
    onTabRemoved(slotId, tab) {
        if (!tab) return;
        var fm = this._fms.get(tab.id);
        if (fm) { try { fm.dispose(); } catch (e) {} this._fms.delete(tab.id); }
        if (this._selectedTab === tab.id) {
            var prevDeep = this.deepTabId();
            this._deep = false;
            if (prevDeep) this._fireDeepChanged(prevDeep, null);
            // The pane survives the close — keep the cursor there, shallow.
            var slot = this._slotExists(slotId) ? slotId : this._firstSlot();
            if (slot != null) this.selectShallow(slot); else { this._selectedSlot = null; this._selectedTab = null; }
        }
    }

    /** A tab moved panes — a positional change downgrades to shallow (click routing). */
    onTabMoved(srcSlot, destSlot, tab) {
        if (!tab || this._selectedTab !== tab.id) return;
        if (this._deep) this.releaseToShallow();
        this.selectShallow(destSlot);
    }

    /** The shown tab of a pane changed (chip click / programmatic switch). */
    onTabActivated(slotId, tabId) {
        // A chip click is an outside-active interaction → at least downgrade;
        // the switched pane becomes the shallow cursor (rule: click routing).
        this.selectShallow(slotId);
    }

    onSplit(srcSlot, orientation, newSlot) { this._repaint(); }

    onMerge(keptSlot, removedSlot) {
        if (this._selectedSlot === removedSlot) this.selectShallow(keptSlot);
        else this._repaint();
    }

    /** MTP reported a chrome interaction — the coordinator decides. */
    onChromeInteract(ev) {
        if (!ev) return;
        if (ev.kind === "cover-click")         this.selectShallow(ev.slotId);
        else if (ev.kind === "cover-dblclick") this.enterDeep(ev.slotId);
        else if (ev.kind === "tab-detached")   this._onTabDetached(ev);
        else if (ev.kind === "tab-docked")     this._onTabDocked(ev);
    }

    /**
     * A tab detached into the transport modal. Decided policy: grabbing a tab
     * out is a deliberate act on THAT tab, so the selection FOLLOWS it — the
     * tab becomes the deep selection in transit (releasing any other), the
     * emptied pane loses its ring (paint cleared), and the modal wears the
     * entered accent. Entering also un-inerts the content, which matters for a
     * previously-covered tab: without it the modal body would arrive inert —
     * unclickable and hidden from AT (a dead modal). The tab's FM travelled
     * with the content, so deep keyboard keeps working inside the modal.
     */
    _onTabDetached(ev) {
        var prevDeep = this.deepTabId();
        this._invalidate();
        this._selectedTab  = ev.tabId;
        this._selectedSlot = null;           // in transport — no pane holds it
        this._deep         = true;
        this._transport    = { tabId: ev.tabId, modalEl: ev.modalEl || null };
        this._mtp.paintSelection(null, null);   // no pane wears a ring
        if (ev.modalEl) { try { ev.modalEl.classList.add("hmtp-modal-entered"); } catch (e) {} }
        var fm = this._fms.get(ev.tabId);
        if (fm) fm.enter();
        this._fireDeepChanged(prevDeep, ev.tabId);
    }

    /**
     * The transport modal docked into a strip. Transport ends; per click
     * routing a completed positional change lands SHALLOW at the destination
     * (same rule as a strip-to-strip move).
     */
    _onTabDocked(ev) {
        this._clearTransport();
        if (this._selectedTab !== ev.tabId) return;
        if (this._deep) {
            var prevDeep = this._selectedTab;
            var fm = this._fms.get(prevDeep);
            if (fm) fm.release();
            this._deep = false;
            this._fireDeepChanged(prevDeep, null);
        }
        this.selectShallow(ev.slotId);
    }

    _clearTransport() {
        if (!this._transport) return;
        if (this._transport.modalEl) {
            try { this._transport.modalEl.classList.remove("hmtp-modal-entered"); } catch (e) {}
        }
        this._transport = null;
    }

    // ── internals ────────────────────────────────────────────────────────────

    /** Release the current selection, whatever it is (select-other intent). */
    _invalidate() {
        if (this._deep && this._selectedTab != null) {
            var fm = this._fms.get(this._selectedTab);
            if (fm) fm.release();
            // Selecting away from a tab in transport leaves its modal dormant
            // (inert content) — drop the entered accent with it.
            if (this._transport && this._transport.tabId === this._selectedTab) this._clearTransport();
            this._fireDeepChanged(this._selectedTab, null);
        }
        this._deep = false;
    }

    _ensureFm(tab) {
        if (!tab || tab.id == null || this._fms.has(tab.id) || !this._FM) return;
        var content = this._mtp.contentElOf ? this._mtp.contentElOf(tab.id) : (tab._contentEl || null);
        if (!content) return;
        var self = this;
        var fm = new this._FM(content, {
            tab: tab,
            onGiveUp:        function ()   { if (self.deepTabId() === tab.id) self.releaseToShallow(); },
            onIntendedFocus: function (fn) { self.enterDeepForTab(tab.id, fn); }
        }).attach();
        this._fms.set(tab.id, fm);
    }

    _adoptExistingTabs() {
        // getState returns flattened copies; the FM needs the LIVE tab object
        // (setActive / defaultActivation), so reach through _tabsBySlot — the
        // same precedent PickerTabFlow.findTabObj uses.
        var self = this;
        if (!this._mtp._tabsBySlot) return;
        this._mtp._tabsBySlot.forEach(function (s) {
            (s.tabs || []).forEach(function (tab) { self._ensureFm(tab); });
        });
    }

    _repaint() {
        if (this._selectedSlot == null) return;
        if (!this._slotExists(this._selectedSlot)) { var f = this._firstSlot(); if (f != null) this.selectShallow(f); return; }
        this._mtp.paintSelection(this._selectedSlot, this._deep ? "deep" : "shallow");
    }

    _fireDeepChanged(prevTabId, nextTabId) {
        if (prevTabId === nextTabId || !this._cbDeepChanged) return;
        try { this._cbDeepChanged(prevTabId || null, nextTabId || null); }
        catch (e) { console.error("[WorkspaceFocusCoordinator] onDeepChanged threw:", e); }
    }

    _state()          { return this._mtp.getState ? this._mtp.getState() : { tabs: {} }; }
    _slotExists(slot) { return !!this._state().tabs[slot] || this._layoutHasSlot(slot); }
    _layoutHasSlot(slot) {
        // Empty panes exist in the layout but may have no tabs entry.
        var found = false;
        (function walk(n) {
            if (!n || found) return;
            if (n.kind === "leaf") { if (n.slotId === slot) found = true; return; }
            walk(n.first); walk(n.second);
        })(this._state().layout);
        return found;
    }
    _firstSlot() {
        var keys = Object.keys(this._state().tabs);
        if (keys.length > 0) return keys[0];
        var first = null;
        (function walk(n) {
            if (!n || first != null) return;
            if (n.kind === "leaf") { first = n.slotId; return; }
            walk(n.first); walk(n.second);
        })(this._state().layout);
        return first;
    }
    _activeTabOf(slot) {
        var s = this._state().tabs[slot];
        return (s && s.activeTabId) ? s.activeTabId : null;
    }
    _slotOf(tabId) {
        var tabs = this._state().tabs, found = null;
        Object.keys(tabs).forEach(function (slot) {
            if (found) return;
            (tabs[slot].tabs || []).forEach(function (t) { if (t.id === tabId) found = slot; });
        });
        return found;
    }

    // ── the shallow keyboard (from RFC 0048's PaneFocusNav) ──────────────────

    _isEditable(t) {
        if (!t || !t.tagName) return false;
        var tag = t.tagName;
        return tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT" || t.isContentEditable === true;
    }

    /** True when the keystroke belongs to this workspace (focus here or nowhere). */
    _inScope(e) {
        if (!this._host) return true;
        var a = document.activeElement;
        if (a == null || a === document.body) return true;
        return this._host.contains(a) || this._host.contains(e.target);
    }

    _handleKey(e) {
        // Deep → the widget owns the keyboard (the FM handles give-up).
        if (this._deep) return;
        if (e.ctrlKey || e.metaKey || e.altKey) return;
        if (this._isEditable(e.target)) return;
        if (!this._inScope(e)) return;

        var slot = this._selectedSlot != null ? this._selectedSlot : this._firstSlot();
        if (slot == null) return;
        switch (e.key) {
            case "ArrowLeft":  this._moveCursor(slot, "left");  break;
            case "ArrowRight": this._moveCursor(slot, "right"); break;
            case "ArrowUp":    this._moveCursor(slot, "up");    break;
            case "ArrowDown":  this._moveCursor(slot, "down");  break;
            case "Tab":        this._cycleTab(slot, e.shiftKey ? -1 : 1); break;
            case "Enter":      this.enterDeep(slot); break;   // empty pane degrades to shallow
            default:           return;                        // not ours — leave it for the browser
        }
        e.preventDefault();
        e.stopPropagation();
    }

    _moveCursor(fromSlot, direction) {
        var next = this._mtp.neighbourOf ? this._mtp.neighbourOf(fromSlot, direction) : null;
        if (next != null) this.selectShallow(next);
    }

    /** Cycle the shown tab WITHIN the cursor pane (never leaves the pane). */
    _cycleTab(slot, delta) {
        var s = this._state().tabs[slot];
        if (!s || !s.tabs || s.tabs.length < 2) return;
        var i = 0;
        for (var k = 0; k < s.tabs.length; k++) {
            if (s.tabs[k].id === s.activeTabId) { i = k; break; }
        }
        var n = s.tabs.length;
        var j = ((i + delta) % n + n) % n;
        this._mtp.switchTab(slot, s.tabs[j].id);   // fires onTabActivated → reselect shallow here
    }
}
