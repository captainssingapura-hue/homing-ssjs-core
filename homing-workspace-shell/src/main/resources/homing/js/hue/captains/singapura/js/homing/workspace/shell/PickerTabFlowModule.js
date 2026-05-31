// =============================================================================
// PickerTabFlow — Phase 13 of the workspace-shell chrome.
//
// The '+' affordance on every pane strip lands here. Flow:
//
//   1. openInSlot(slotId)
//        ├─ create empty 'picker:N' tab; mtp.addTab + switchTab +
//        │  setWorkspaceActiveTab
//        ├─ mount WidgetPicker into the tab's contentEl with
//        │     entries     = spec.entries minus pinned
//        │     disabledIds = current singletons (one per kind)
//        │     onPick(entry, params)  → mutate tab in place OR focus existing
//        │     onCancel()             → mtp.removeTab
//        └─ done (sync)
//
//   2. picker.onPick(entry, params)
//        ├─ if params === null  → SINGLETON focus-existing path
//        └─ else                → _mutateIntoWidget: same tabId, swap content
//
//   3. _mutateIntoWidget(slotId, tabId, entry, params, holder)
//        ├─ sync: retitle tab; create branch; show loading
//        └─ mounter.resolve(entry).then(mod => { sync mount + attach })
//
// Explicit Substrate doctrine:
//   - Stateful per-workspace instance (NOT a singleton — one per chrome).
//   - All methods are instance methods (no static helpers).
//   - No async sugar — Promises returned/composed explicitly.
//   - I/O boundary is exactly one .then() in _mutateIntoWidget.
// =============================================================================

class PickerTabFlow {

    /**
     * Constructor takes per-instance data (mtp, widgetsBranch, spec,
     * workspaceCtx) AND collaborator overrides (mounter, WidgetPickerCtor).
     * Per-call methods receive only call-time data; no per-call collaborator
     * overrides — the dep-graph walker can therefore enumerate everything
     * this flow depends on from instance fields alone.
     */
    constructor(opts) {
        if (!opts || !opts.mtp)           throw new Error('[PickerTabFlow] opts.mtp required');
        if (!opts.widgetsBranch)          throw new Error('[PickerTabFlow] opts.widgetsBranch required');
        if (!opts.spec)                   throw new Error('[PickerTabFlow] opts.spec required');
        this._mtp              = opts.mtp;
        this._widgetsBranch    = opts.widgetsBranch;
        this._spec             = opts.spec;
        this._workspaceCtx     = opts.workspaceCtx || {};
        this._mounter          = opts.mounter          || WidgetMounter.INSTANCE;
        this._WidgetPickerCtor = opts.WidgetPickerCtor || WidgetPicker;
        // Phase 12 + Phase 6 — optional, but the orchestrator always
        // supplies them in production. Tests can pass null for both
        // to exercise the spawn flow without registry/recorder side
        // effects.
        this._tabRegistry      = opts.tabRegistry || null;
        this._recorder         = opts.recorder    || null;
        // Optional model — when supplied, every spawn from this picker
        // also applies a WidgetSpawnedFromPicker event to it so the
        // in-memory virtual-replay state stays in sync with the live
        // MTP. Without this the model is correct at boot but drifts as
        // soon as the user opens a new tab via the picker.
        this._model            = opts.model       || null;
        this._singletonsByKind = {};
        this._counter          = 0;
    }

    /** Inspect-able state for dev tools (Diligent Secretaries pillar 2). */
    inspect() {
        return {
            singletonsByKind: Object.assign({}, this._singletonsByKind),
            tabsIssued:       this._counter
        };
    }

    /**
     * Open a picker in a brand-new empty tab inside {@code slotId}.
     * Returns the new tab's id so callers can correlate.
     */
    openInSlot(slotId) {
        const self  = this;
        const tabId = 'picker:' + (++this._counter);

        const pickerHost = document.createElement('div');
        const holder = { contentEl: null };
        const tab = {
            id:      tabId,
            title:   'New tab',
            render:  function (contentEl) {
                holder.contentEl = contentEl;
                contentEl.appendChild(pickerHost);
            },
            setActive: function () {},
            onClose:   function () { /* empty picker tab — nothing to dispose */ }
        };

        this._mtp.addTab(slotId, tab);
        if (this._mtp.switchTab)              this._mtp.switchTab(slotId, tabId);
        if (this._mtp.setWorkspaceActiveTab)  this._mtp.setWorkspaceActiveTab(tabId);

        const pickerEntries = this.filterPickable(
                this._spec.entries || [],
                this._spec.pinnedSpawns || []);
        const disabledIds = Object.assign({}, this._singletonsByKind);

        const picker = new this._WidgetPickerCtor({
            entries:     pickerEntries,
            disabledIds: disabledIds,
            onPick:      function (entry, params) {
                if (params === null) {
                    self._focusExistingSingleton(entry, slotId, tabId);
                } else {
                    self._mutateIntoWidget(slotId, tabId, entry, params, holder);
                }
            },
            onCancel:    function () { self._mtp.removeTab(slotId, tabId); }
        });
        picker.mountInto(pickerHost);
        return tabId;
    }

    /**
     * Pure transform — returns entries that should appear in the picker.
     * Pinned entries are auto-spawned at boot; offering them again would
     * let the user spawn duplicate introductions. Instance method per
     * Explicit Substrate (every helper is on the instance, not static).
     */
    filterPickable(entries, pinnedSpawns) {
        const pinnedSet = {};
        for (const k of pinnedSpawns) pinnedSet[k] = true;
        return entries.filter(e => !pinnedSet[e.simpleName]);
    }

    /** SINGLETON focus-existing path: dispose picker tab, focus the live one. */
    _focusExistingSingleton(entry, slotId, tabId) {
        const existingId = this._singletonsByKind[entry.simpleName];
        if (existingId && this._mtp.setWorkspaceActiveTab) {
            const liveSlot = this.findSlotForTab(existingId);
            if (liveSlot && this._mtp.switchTab) {
                this._mtp.switchTab(liveSlot, existingId);
            }
            this._mtp.setWorkspaceActiveTab(existingId);
        }
        this._mtp.removeTab(slotId, tabId);
    }

    /**
     * Picker-tab → widget-tab in-place mutation. Same tab id, so
     * workspace-active stays put — control never left the tab.
     * Sync setup + one explicit .then() on the mounter's resolve.
     */
    _mutateIntoWidget(slotId, tabId, entry, params, holder) {
        const self  = this;
        const tab   = this.findTabObj(tabId);
        if (!tab) return;

        tab.widgetKind         = entry.simpleName;
        tab.widgetInstanceUuid = entry.simpleName + ':' + (++this._counter);

        const contentEl = holder.contentEl;
        if (!contentEl) return;

        // Sync: loading placeholder + retitle.
        const loading = document.createElement('div');
        loading.style.cssText = 'padding:20px;color:#666;font-family:sans-serif;';
        loading.textContent   = 'Loading ' + entry.label + '…';
        while (contentEl.firstChild) contentEl.removeChild(contentEl.firstChild);
        contentEl.appendChild(loading);
        tab.title = entry.label;
        if (this._mtp.switchTab) this._mtp.switchTab(slotId, tabId);

        // Sync: per-widget branch. Sanitise the uuid for branch-name
        // safety — branch names allow only [A-Za-z0-9_-].
        const branchName = 'w-' + tab.widgetInstanceUuid.replace(/[^A-Za-z0-9_-]/g, '_');
        const wBranch = this._widgetsBranch.createBranch(branchName);
        const owner   = Object.freeze({ toString: () => 'widget:' + tab.widgetInstanceUuid });
        wBranch.activate(owner);

        // The ONE async boundary in this whole flow — resolve, then
        // synchronous mount + attach inside the .then().
        this._mounter.resolve(entry).then(function (mod) {
            const controller = self._mounter.mount(mod, wBranch, entry,
                                                   params, self._workspaceCtx);
            self._mounter.attach(controller, tab, holder);
            // Phase 12 — register so replay handlers (Phase 9) can find
            // the live tab for TabClosed / TabMoved / ActiveChanged.
            if (self._tabRegistry) {
                try {
                    self._tabRegistry.register({
                        widgetInstanceUuid: tab.widgetInstanceUuid,
                        tab:                tab,
                        slotId:             slotId,
                        widgetKind:         entry.simpleName,
                        controller:         controller
                    });
                } catch (e) {
                    console.error('[PickerTabFlow] tabRegistry.register threw:', e);
                }
            }
            // Phase 6 — emit so Phase 9 replay can re-spawn this widget.
            // ALSO apply to model (when supplied) so virtual-replay state
            // stays in sync with the live MTP. Apply before emit so model
            // is consistent before any onAfterEmit cadence fires.
            const spawnPayload = {
                widgetInstanceId: tab.widgetInstanceUuid,
                widgetKind:       entry.simpleName,
                title:            entry.label,
                params:           params,
                to: { paneId: '_', tabIndex: 0 }
            };
            if (self._model && typeof self._model.apply === 'function') {
                try { self._model.apply({ name: 'WidgetSpawnedFromPicker',
                                          payload: spawnPayload }); }
                catch (e) {
                    console.error('[PickerTabFlow] model.apply threw:', e);
                }
            }
            if (self._recorder && typeof self._recorder.emit === 'function') {
                self._recorder.emit('WidgetSpawnedFromPicker', spawnPayload);
            }
            tab.onClose = function () {
                try { wBranch.dissolve(); } catch (e) {}
                if (self._tabRegistry) {
                    self._tabRegistry.unregister(tab.widgetInstanceUuid);
                }
                if (entry.lifecycleHint === 'SINGLETON') {
                    delete self._singletonsByKind[entry.simpleName];
                }
            };
            if (entry.lifecycleHint === 'SINGLETON') {
                self._singletonsByKind[entry.simpleName] = tabId;
            }
            if (self._mtp.getWorkspaceActiveTab
             && self._mtp.getWorkspaceActiveTab() === tabId) {
                try { controller.setActive(true); } catch (e) {}
            }
        }).catch(function (err) {
            console.error('[PickerTabFlow] mount failed for', entry.simpleName, ':', err);
            loading.textContent = 'Failed to load ' + entry.label
                                + ': ' + (err && err.message ? err.message : err);
        });
    }

    /** Finds the slot id that hosts the given tab. */
    findSlotForTab(tabId) {
        if (!this._mtp.getState) return null;
        const state = this._mtp.getState();
        for (const slotId in state.tabs) {
            if (!state.tabs.hasOwnProperty(slotId)) continue;
            for (const t of state.tabs[slotId].tabs) {
                if (t.id === tabId) return slotId;
            }
        }
        return null;
    }

    /** Finds the live tab descriptor by id. Reaches through MTP's
     *  _tabsBySlot internal map (public getState returns a flattened
     *  copy missing render/setActive). */
    findTabObj(tabId) {
        if (!this._mtp._tabsBySlot) return null;
        let found = null;
        this._mtp._tabsBySlot.forEach(function (s) {
            if (found) return;
            for (const t of s.tabs) {
                if (t.id === tabId) { found = t; return; }
            }
        });
        return found;
    }
}
