// =============================================================================
// WorkspaceFocusCoordinatorModule — RFC 0049 workspace focus coordinator,
// SELECTION-RESOLUTION edition: recompute, don't track.
//
// Events update INTENT (the one remembered fact — single-writer, here); a
// TOTAL resolver derives the definitive selection from intent + the live
// world; a reconciler enforces it. Missed events degrade to staleness (cured
// by the next trigger), never to corruption: a two-glow state is
// unrepresentable because every trigger repaints from one resolved answer.
//
//   intent   { kind: 'tab', id }   — deep on that tab (deep ⇒ widget)
//            { kind: 'slot', id }  — shallow cursor on that pane
//            { kind: null }        — no act yet (boot) → resolver floor
//
//   resolve  1. intent tab in a slot        → that tab, deep, there
//            2. intent tab alive, NO slot   → that tab, deep, IN TRANSPORT
//            3. intent tab gone             → floor
//            4. intent slot exists          → its active tab (or none), shallow
//            5. floor                       → first pane, shallow
//
//   apply    diff vs the last-applied memo: equal → drift-repair only;
//            changed → FM sweep (idempotent applyDeep — the FM's own state
//            gives the setActive edges), one paintSelection (MTP diffs
//            internally), modal accents, onDeepChanged from the same diff.
//
// Invariant (amended by RFC 0052 to ONE LOCUS PER INPUT DEVICE): the KEYBOARD
// axis keeps exactly ONE selection target at all times; at
// most one selected widget; mode ∈ {shallow, deep}; deep ⇒ widget. The
// POINTER axis (RFC 0052) holds at most one pointer-live pane, synced by
// _syncPointerLive — so at most TWO widgets are un-firewalled at any moment.
//
// The shallow keyboard lives in its own module (ShallowKeyboard); the per-tab
// FocusManagers own the immutable mechanics. MTP is driven only through its
// renderer/access facet.
// =============================================================================

class WorkspaceFocusCoordinator {

    constructor(opts) {
        opts = opts || {};
        if (!opts.mtp) throw new Error("[WorkspaceFocusCoordinator] opts.mtp is required");
        this._mtp   = opts.mtp;
        this._host  = opts.host || null;
        this._FM    = opts.FocusManagerCtor || (typeof FocusManager !== "undefined" ? FocusManager : null);
        this._cbDeepChanged = opts.onDeepChanged || null;   // (prevTabId, nextTabId) → replay/persistence

        this._intent  = { kind: null, id: null };
        this._applied = null;             // last-applied resolution — diff basis + lifecycle edges
        this._fms     = new Map();        // tabId → FocusManager (world fact: alive tabs)
        this._transportModals = new Map();// tabId → modal el (accent registry; state never depends on it)
        this._pendingActivationFn = null; // per-request activation fn for the next deep apply
        this._hoverSlot = null;           // RFC 0052 — the pointer axis: pane under the pointer
        this._applying = false;           // re-entrancy guard: notifications during apply re-run once
        this._dirty    = false;
        this._attached = false;
        this._Keyboard = opts.ShallowKeyboardCtor
                       || (typeof ShallowKeyboard !== "undefined" ? ShallowKeyboard : null);
        this._keyboard = null;
        this._Scope    = opts.KeyboardScopeCtor
                       || (typeof KeyboardScope !== "undefined" ? KeyboardScope : null);
        this._scope    = null;
    }

    attach() {
        if (this._attached) return this;
        if (this._Keyboard) {
            this._keyboard = new this._Keyboard({ coordinator: this, mtp: this._mtp, host: this._host }).attach();
        }
        // RFC 0052 — the keyboard-scope watcher owns its own fact and reports
        // changes; the coordinator only forwards them to the renderer.
        var self = this;
        if (this._Scope) {
            this._scope = new this._Scope({ host: this._host,
                onChange: function (on) { if (self._mtp.paintKeyboardScope) self._mtp.paintKeyboardScope(on); }
            }).attach();
        }
        this._attached = true;
        this._adoptExistingTabs();
        this._refresh();                  // empty intent hits the resolver floor → boot shallow
        return this;
    }

    dispose() {
        if (!this._attached) return this;
        if (this._scope) { try { this._scope.dispose(); } catch (e) {} this._scope = null; }
        if (this._keyboard) { try { this._keyboard.dispose(); } catch (e) {} this._keyboard = null; }
        this._fms.forEach(function (fm) { try { fm.dispose(); } catch (e) {} });
        this._fms.clear();
        this._transportModals.clear();
        this._attached = false;
        return this;
    }

    // ── selection queries (read the APPLIED resolution) ──────────────────────

    mode()           { return (this._applied && this._applied.mode === "deep") ? "deep" : "shallow"; }
    selectedSlotId() { return this._applied ? this._applied.slotId : null; }
    deepTabId()      { return (this._applied && this._applied.mode === "deep") ? this._applied.tabId : null; }

    // ── intent updates (the ONLY writers) ────────────────────────────────────

    /** Shallow-select a pane (the cursor). */
    selectShallow(slotId) {
        this._intent = { kind: "slot", id: slotId };
        return this._refresh();
    }

    /** Deep-select a pane's active tab; an empty pane degrades to the cursor. */
    enterDeep(slotId, activationFn) {
        var tabId = this._activeTabOf(slotId);
        if (tabId == null) return this.selectShallow(slotId);
        this._intent = { kind: "tab", id: tabId };
        this._pendingActivationFn = (typeof activationFn === "function") ? activationFn : null;
        return this._refresh();
    }

    /** Deep-select a specific tab wherever it lives (intendedFocusIn path). */
    enterDeepForTab(tabId, activationFn) {
        this._intent = { kind: "tab", id: tabId };
        this._pendingActivationFn = (typeof activationFn === "function") ? activationFn : null;
        return this._refresh();
    }

    /** Downgrade to shallow — the cursor lands on the deep tab's pane (or first). */
    releaseToShallow() {
        var deepTab = this.deepTabId();
        if (deepTab == null) return this;
        var slot = this._slotOf(deepTab);
        this._intent = { kind: "slot", id: (slot != null) ? slot : this._firstSlot() };
        return this._refresh();
    }

    // ── structural-event handlers (fanned out by the chrome) — triggers ──────

    onTabAdded(slotId, tab)    { this._ensureFm(tab); this._refresh(); }
    onTabAttached(slotId, tab) { this._ensureFm(tab); this._refresh(); }

    onTabRemoved(slotId, tab) {
        if (!tab) return;
        var fm = this._fms.get(tab.id);
        if (fm) { try { fm.dispose(); } catch (e) {} this._fms.delete(tab.id); }
        this._dropTransportModal(tab.id);
        // A dead tab can't be intent — keep the cursor at its pane.
        if (this._intent.kind === "tab" && this._intent.id === tab.id) {
            this._intent = { kind: "slot", id: slotId };
        }
        this._refresh();
    }

    /** A strip-to-strip chip drag (no transit) downgrades at the destination. */
    onTabMoved(srcSlot, destSlot, tab) {
        if (tab && this._intent.kind === "tab" && this._intent.id === tab.id) {
            this._intent = { kind: "slot", id: destSlot };
        }
        this._refresh();
    }

    /** Tab activation (chip click or keyboard cycle) — a pure STATE event:
     *  shallow cursor there. A chip CLICK additionally reports chip-click,
     *  which arrives after this and upgrades to deep (RFC 0052) — while the
     *  keyboard tab-cycle, sharing this fire site, stays shallow. */
    onTabActivated(slotId, tabId) { this.selectShallow(slotId); }

    onSplit(srcSlot, orientation, newSlot) { this._refresh(); }

    onMerge(keptSlot, removedSlot) {
        if (this._intent.kind === "slot" && this._intent.id === removedSlot) this._intent.id = keptSlot;
        this._refresh();
    }

    /** MTP reported a chrome interaction — the coordinator decides. */
    onChromeInteract(ev) {
        if (!ev) return;
        // RFC 0052 — cover-free interpretation. The pointer is a declaration
        // of intent: pane-hover drives the POINTER axis only (liveness + the
        // hover degree paint — never the keyboard locus, so typing can never
        // follow mouse drift); pane-press (capture-phase, before the browser's
        // focus step) and chip-click are deliberate entries: straight to deep.
        // The keyboard keeps shallow for its cursor; Escape yields back down.
        if (ev.kind === "pane-press" || ev.kind === "chip-click") this.enterDeep(ev.slotId);
        else if (ev.kind === "pane-hover") {
            this._hoverSlot = (ev.slotId != null) ? ev.slotId : null;
            this._syncPointerLive();
            if (this._mtp.paintHover) this._mtp.paintHover(this._hoverSlot);
        }
        else if (ev.kind === "tab-detached") {
            // Transport policy: grabbing a tab out is a deliberate act on THAT
            // tab — the selection follows it. The resolver derives "in
            // transport" (alive FM, no slot); the modal el is only registered
            // for the accent.
            if (ev.modalEl) this._transportModals.set(ev.tabId, ev.modalEl);
            this.enterDeepForTab(ev.tabId);
        } else if (ev.kind === "tab-docked") {
            // Pure trigger: intent is unchanged; the resolver now finds the
            // tab in a slot again, so the glow lands there — still deep.
            this._dropTransportModal(ev.tabId);
            this._refresh();
        }
    }

    /** Un-accent + unregister a transport modal (dock / close). */
    _dropTransportModal(tabId) {
        var el = this._transportModals.get(tabId);
        if (el) { try { el.classList.remove("hmtp-modal-entered"); } catch (e) {} }
        this._transportModals.delete(tabId);
    }

    // ── resolve + apply ──────────────────────────────────────────────────────

    _resolve() {
        var it = this._intent;
        if (it.kind === "tab" && it.id != null) {
            var slot = this._slotOf(it.id);
            if (slot != null)        return { tabId: it.id, slotId: slot, mode: "deep", transport: false };
            if (this._fms.has(it.id)) return { tabId: it.id, slotId: null, mode: "deep", transport: true };
        }
        if (it.kind === "slot" && it.id != null && this._slotExists(it.id)) {
            return { tabId: this._activeTabOf(it.id), slotId: it.id, mode: "shallow", transport: false };
        }
        var f = this._firstSlot();   // the floor — a workspace always has a pane
        return { tabId: (f != null) ? this._activeTabOf(f) : null, slotId: f, mode: "shallow", transport: false };
    }

    _refresh() {
        if (this._applying) { this._dirty = true; return this; }
        this._applying = true;
        try {
            var guard = 0;
            do { this._dirty = false; this._apply(this._resolve()); } while (this._dirty && ++guard < 4);
        } finally { this._applying = false; }
        // RFC 0052 — the pointer axis re-syncs after every keyboard-axis apply:
        // structural changes (tab switch, close, move) can change WHICH tab is
        // the hovered pane's active one without any pointer event firing.
        this._syncPointerLive();
        return this;
    }

    /**
     * RFC 0052 — enforce the pointer axis: the hovered pane's ACTIVE tab is
     * pointer-live; every other FM is not. At most one FM is pointer-live, so
     * with the keyboard axis at most TWO widgets are un-firewalled at any
     * moment (the entered one and the pointed one) — the amended envelope.
     */
    _syncPointerLive() {
        if (this._scope) this._scope.resync();   // cure any missed focus event
        var live = (this._hoverSlot != null) ? this._activeTabOf(this._hoverSlot) : null;
        this._fms.forEach(function (fm, tabId) { if (fm.setPointerLive) fm.setPointerLive(tabId === live); });
    }

    _apply(next) {
        var prev = this._applied || { tabId: null, slotId: null, mode: null, transport: false };
        var same = prev.tabId === next.tabId && prev.slotId === next.slotId && prev.mode === next.mode && prev.transport === next.transport;
        var nextDeep = (next.mode === "deep") ? next.tabId : null;
        // Redundant trigger — drift repair only (focus back into the deep tab).
        if (same) { var f0 = nextDeep != null ? this._fms.get(nextDeep) : null; if (f0) f0.reconcile(); return; }
        var prevDeep = (prev.mode === "deep") ? prev.tabId : null;
        var self = this;
        // FM sweep — idempotent per tab; the FM's own state yields the
        // setActive edges (fires exactly once per change), release before enter.
        this._fms.forEach(function (fm, tabId) { if (tabId !== nextDeep) fm.applyDeep(false); });
        this._mtp.paintSelection(next.transport ? null : next.slotId, next.transport ? null : next.mode);
        this._transportModals.forEach(function (el, tabId) {
            try { el.classList[(next.transport && tabId === nextDeep) ? "add" : "remove"]("hmtp-modal-entered"); } catch (e) {}
        });
        if (nextDeep != null) {
            var fm = this._fms.get(nextDeep);
            // Self-heal: a resolved deep tab without an FM means an adoption was
            // missed (or a tab arrived through an unwired path) — adopt now.
            if (!fm) { this._adoptExistingTabs(); fm = this._fms.get(nextDeep); }
            if (fm) {
                fm.applyDeep(true, this._pendingActivationFn);
                // Same deep tab, new place (dock / structural churn) — restore focus.
                if (prevDeep === nextDeep) fm.reconcile();
            }
        }
        this._pendingActivationFn = null;
        this._applied = next;
        if (prevDeep !== nextDeep && this._cbDeepChanged) {
            try { this._cbDeepChanged(prevDeep, nextDeep); }
            catch (e) { console.error("[WorkspaceFocusCoordinator] onDeepChanged threw:", e); }
        }
    }

    // ── FM lifecycle + world queries ─────────────────────────────────────────

    _ensureFm(tab) {
        if (!tab || tab.id == null || this._fms.has(tab.id) || !this._FM) return;
        var content = this._mtp.contentElOf ? this._mtp.contentElOf(tab.id) : (tab._contentEl || null);
        if (!content) return;
        var self = this;
        var fm = new this._FM(content, {
            tab: tab,
            // Notifications, not commands: guarded by the CURRENT resolution.
            onGiveUp:        function ()   { if (self.deepTabId() === tab.id) self.releaseToShallow(); },
            onIntendedFocus: function (fn) { self.enterDeepForTab(tab.id, fn); }
        }).attach();
        this._fms.set(tab.id, fm);
    }

    _adoptExistingTabs() {
        // The FM needs the LIVE tab object (setActive / defaultActivation) —
        // reach through _tabsBySlot, the PickerTabFlow.findTabObj precedent.
        var self = this;
        if (!this._mtp._tabsBySlot) return;
        this._mtp._tabsBySlot.forEach(function (s) {
            (s.tabs || []).forEach(function (tab) { self._ensureFm(tab); });
        });
    }

    _state() { return this._mtp.getState ? this._mtp.getState() : { tabs: {} }; }

    _slotExists(slot) {
        if (this._state().tabs[slot]) return true;
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
}
