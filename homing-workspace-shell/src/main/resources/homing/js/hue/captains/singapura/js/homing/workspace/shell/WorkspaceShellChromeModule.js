// =============================================================================
// WorkspaceShellChrome — orchestrator for the workspace shell.
//
// VIRTUAL REPLAY ARCHITECTURE (post-RFC-0034 refactor):
//
//   1. Sync setup: host + WorkspaceLayout + widgetsBranch
//   2. Async chains kick off in parallel: codec registrar, identity,
//      event recorder, write lock, checkpoint, parties
//   3. ReplayEngine.boot folds every recorded event into an in-memory
//      WorkspaceStateModel (decoded from checkpoint if present, else
//      fresh). No DOM mutation per event.
//   4. After fold completes: model is the final state. MTP is constructed
//      NOW with initialLayout = model.layout(); every tab in
//      model.tabsBySlot() is silently mounted via the widget mounter +
//      tab registry; workspace-active is set.
//   5. Recorder fence drops. Picker + control modal construct. User
//      interactions begin emitting events normally; MTP callbacks both
//      EMIT to recorder AND apply to model so model stays in sync.
//
// Why virtual replay over physical replay:
//   - Spawn-then-close cycles collapse to zero DOM work
//   - Spawn-then-move-five-times collapses to one final placement
//   - Replay-time errors don't leave half-constructed MTP state
//   - Checkpoint state IS the model serialized — same artifact for
//     reconstruction and capture
//   - Slow-motion observation moves to a separate read-only app
//
// Phase status (renumbered for the new architecture):
//   1. host + WorkspaceLayout      | DONE
//   2. PartyBootstrap              | DONE
//   3. CodecRegistrar              | DONE
//   4. PersistenceLayer attach     | DONE
//   5. WorkspaceDirectory + ident. | DONE
//   6. EventEmitter                | DONE
//   7. WriteLockGuard + Overlay    | DONE
//   8. CheckpointService           | DONE (captureState = model.toSnapshot)
//   9. ReplayEngine (fold-style)   | DONE
//        └ vocabulary fold via WorkspaceStateModel.apply
//  10. LayoutCodec                 | DONE
//  11. MultiTabPane (post-replay)  | DONE
//  12. TabRegistry                 | DONE
//  13. PickerTabFlow (post-MTP)    | DONE
//  14. PinnedTabSpawner            | DONE (seeds model on fresh session)
//  15. WidgetMounter               | DONE
//  16+17+18 WorkspaceControl       | DONE
//
// Known limitations (filed):
//   - Floating-modal state (drag-detach into Modal) not journaled
//   - Splitter-drag resize not journaled (SplitPane callback missing)
// =============================================================================

class WorkspaceShellChrome {

    constructor(spec, deps) {
        if (!spec)             throw new Error('[WorkspaceShellChrome] spec is required');
        if (!spec.kind)        throw new Error('[WorkspaceShellChrome] spec.kind is required');
        if (!spec.title)       throw new Error('[WorkspaceShellChrome] spec.title is required');
        if (!spec.ribbonItems) throw new Error('[WorkspaceShellChrome] spec.ribbonItems is required');
        if (!spec.footerItems) throw new Error('[WorkspaceShellChrome] spec.footerItems is required');
        this._spec = spec;

        deps = deps || {};
        this._layoutCodec         = deps.layoutCodec         || LayoutCodec.INSTANCE;
        this._partyBootstrap      = deps.partyBootstrap      || PartyBootstrap.INSTANCE;
        this._codecRegistrar      = deps.codecRegistrar      || CodecRegistrar.INSTANCE;
        this._persistenceAttacher = deps.persistenceAttacher || PersistenceAttacher.INSTANCE;
        this._workspaceDirectory  = deps.workspaceDirectory  || WorkspaceDirectory.INSTANCE;
        this._eventEmitter        = deps.eventEmitter        || EventEmitter.INSTANCE;
        this._writeLockGuard      = deps.writeLockGuard      || WriteLockGuard.INSTANCE;
        this._checkpointService   = deps.checkpointService   || CheckpointService.INSTANCE;
        this._replayEngine        = deps.replayEngine        || ReplayEngine.INSTANCE;
        this._widgetMounter       = deps.widgetMounter       || WidgetMounter.INSTANCE;
        this._pinnedTabSpawner    = deps.pinnedTabSpawner    || PinnedTabSpawner.INSTANCE;
        this._WorkspaceLayoutCtor = deps.WorkspaceLayoutCtor || WorkspaceLayout;
        this._MultiTabPaneCtor    = deps.MultiTabPaneCtor    || MultiTabPane;
        this._PickerTabFlowCtor   = deps.PickerTabFlowCtor   || PickerTabFlow;
        this._TabRegistryCtor     = deps.TabRegistryCtor     || TabRegistry;
        this._WorkspaceStateModelCtor =
                deps.WorkspaceStateModelCtor || WorkspaceStateModel;
        this._WorkspaceControlModalCtor =
                deps.WorkspaceControlModalCtor || WorkspaceControlModal;

        // Per-workspace registry — replay-time silent spawns + live
        // user spawns + MTP callbacks all read/write through this.
        this._tabRegistry = new this._TabRegistryCtor();

        // The single source of truth for workspace state. Populated by
        // ReplayEngine fold (post-async); kept in sync by MTP callbacks
        // post-projection so checkpoint capture reads from it.
        this._model = new this._WorkspaceStateModelCtor();

        // Late-bound instance fields.
        this._layout                = null;
        this._mtp                   = null;  // built AFTER replay
        this._widgetsBranch         = null;
        this._parties               = {};
        this._workspaceCtx          = {};
        this._pickerFlow            = null;  // built post-MTP
        this._eventRecorder         = null;  // populated by Phase 6
        this._lockController        = null;  // populated by Phase 7
        this._checkpointController  = null;  // populated by Phase 8
        this._workspaceControlModal = null;  // populated post-MTP
        this._identity = {
            id: null, name: null, isDefault: true, isNew: false, ready: false
        };
    }

    mount(branch, parent) {
        if (!branch) throw new Error('[WorkspaceShellChrome] branch is required');
        if (!parent) throw new Error('[WorkspaceShellChrome] parent is required');

        const self = this;

        // ── Sync: host + WorkspaceLayout + widgetsBranch ────────────────
        const host = branch.createElement('host', 'div');
        host.style.cssText = 'display:flex;flex-direction:column;flex:1;'
                           + 'min-height:0;min-width:0;height:100%;';
        parent.appendChild(host);

        this._layout = new this._WorkspaceLayoutCtor({
            container:   host,
            title:       this._spec.title,
            subtitle:    '',
            onTitleClick: function () {
                if (!self._workspaceControlModal) {
                    console.warn('[WorkspaceShellChrome] control modal '
                               + 'not ready yet (boot still racing)');
                    return;
                }
                self._workspaceControlModal.toggle();
            },
            ribbonItems: this._spec.ribbonItems,
            footerItems: this._spec.footerItems,
            onAction:    function (actionId, value) { self._dispatchAction(actionId, value); }
        });

        this._widgetsBranch = branch.createBranch('widgets');
        this._widgetsBranch.activate(Object.freeze({
            toString: function () { return 'workspaceShell:widgets'; }
        }));

        // ── Phase 3: CodecRegistrar (async) ─────────────────────────────
        this._codecsReady = this._codecRegistrar.registerAll({
            entries:      this._spec.entries      || [],
            widgetCodecs: this._spec.widgetCodecs || []
        }).then(function () {
            console.log('[WorkspaceShellChrome] CodecRegistrar done; '
                      + 'defaults for', (self._spec.entries || []).length, 'entries; '
                      + 'custom overlays:', (self._spec.widgetCodecs || []).length);
        }).catch(function (err) {
            console.error('[WorkspaceShellChrome] CodecRegistrar failed:', err);
        });

        // ── Phase 4: PersistenceAttacher (sync) ─────────────────────────
        this._persistenceLayer = this._persistenceAttacher.attach({
            workspaceKind: this._spec.kind
        });

        // ── Phase 5: WorkspaceDirectory (async identity resolution) ─────
        this._identityReady = this._workspaceDirectory
                .resolveIdentity(this._spec.kind)
                .then(function (identity) {
                    self._identity.id        = identity.id;
                    self._identity.name      = identity.name;
                    self._identity.isDefault = !!identity.isDefault;
                    self._identity.isNew     = !!identity.isNew;
                    self._identity.ready     = true;
                    console.log('[WorkspaceShellChrome] identity:',
                                self._identity.name, '(' + self._identity.id + ')',
                                self._identity.isDefault ? '[default]' : '',
                                self._identity.isNew ? '[new]' : '');
                    if (self._layout && self._layout.setSubtitle) {
                        self._layout.setSubtitle(self._identity.name || '');
                    }
                    return self._workspaceDirectory
                            .registerInCatalogue(self._spec.kind, identity)
                            .then(function () { return identity; });
                })
                .catch(function (err) {
                    console.error('[WorkspaceShellChrome] identity resolution failed:', err);
                });

        // ── Phase 6: EventEmitter (gated on identity) ───────────────────
        this._eventRecorderReady = this._identityReady.then(function (identity) {
            if (!identity) return null;
            self._eventRecorder = self._eventEmitter.attach({
                workspaceKind: self._spec.kind,
                workspaceId:   identity.id,
                workspaceName: identity.name
            });
            console.log('[WorkspaceShellChrome] EventEmitter attached');
            return self._eventRecorder;
        });

        // ── Phase 7: WriteLockGuard (gated on recorder) ─────────────────
        this._lockReady = this._eventRecorderReady.then(function (recorder) {
            if (!recorder) return null;
            return self._writeLockGuard.attach({
                lockName:      'homing-workspace:' + self._spec.kind
                             + ':' + self._identity.id,
                host:          host,
                workspaceName: self._identity.name,
                recorder:      recorder,
                onChange:      function (held, reason) {
                    console.log('[WorkspaceShellChrome] write lock '
                              + (held ? 'held' : 'released'),
                              '(' + reason + ')');
                }
            }).then(function (lockCtrl) {
                self._lockController = lockCtrl;
                return lockCtrl;
            });
        }).catch(function (err) {
            console.error('[WorkspaceShellChrome] WriteLockGuard failed:', err);
        });

        // ── Phase 8: CheckpointService (gated on lock) ──────────────────
        //   captureState reads directly from the model — single source of
        //   truth. Snapshot is what fromSnapshot reconstructs.
        this._checkpointReady = this._lockReady.then(function () {
            if (!self._eventRecorder) return null;
            self._checkpointController = self._checkpointService.attach({
                workspaceKind: self._spec.kind,
                workspaceId:   self._identity.id,
                recorder:      self._eventRecorder,
                eventLog:      self._eventRecorder && self._eventRecorder.log(),
                captureState:  function () {
                    if (!self._model) return null;
                    try { return self._model.toSnapshot(); }
                    catch (e) {
                        console.warn('[WorkspaceShellChrome] toSnapshot failed:',
                            e && e.message ? e.message : e);
                        return null;
                    }
                }
            });
            console.log('[WorkspaceShellChrome] CheckpointService attached');
            return self._checkpointController;
        }).catch(function (err) {
            console.error('[WorkspaceShellChrome] CheckpointService failed:', err);
        });

        // ── Phase 2: PartyBootstrap (async, independent) ────────────────
        this._partiesReady = this._partyBootstrap.bootstrap(this._spec.parties || [])
            .then(function (result) {
                Object.assign(self._parties,      result.parties);
                Object.assign(self._workspaceCtx, result.workspaceCtx);
                console.log('[WorkspaceShellChrome] PartyBootstrap done; parties=',
                            Object.keys(self._parties));
                return result;
            })
            .catch(function (err) {
                console.error('[WorkspaceShellChrome] PartyBootstrap failed:', err);
                throw err;
            });

        // ── Phase 9 boot: virtual replay fold ───────────────────────────
        //   ReplayEngine folds every event into self._model via
        //   model.apply(event). After fold:
        //     - If fellThroughToEmpty (fresh session, no checkpoint): seed
        //       model with pinned widgets, schedule post-projection emit
        //       so the next session has them in the log.
        //     - Otherwise: model is the final state from log/checkpoint.
        //   Then construct MTP + project + drop fence + wire picker/modal.
        this._pinnedReady = Promise.all([
                this._eventRecorderReady,
                this._checkpointReady,
                this._partiesReady
            ]).then(function () {
                if (!self._eventRecorder) return null;
                const cpStore = self._checkpointController
                              ? self._checkpointController.store() : null;
                // Track whether onEmpty applied pinned spawns — if yes,
                // emit them after fence drops so next session can replay.
                let seededPinnedDescriptors = null;
                return self._replayEngine.boot({
                    workspaceKind:   self._spec.kind,
                    workspaceId:     self._identity.id,
                    eventLog:        self._eventRecorder.log(),
                    checkpointStore: cpStore,
                    recorder:        self._eventRecorder,
                    initialState:    function () {
                        return new self._WorkspaceStateModelCtor();
                    },
                    decodeCheckpoint: function (cpRow) {
                        // cpRow.state is what captureState returned —
                        // a model.toSnapshot()-produced envelope.
                        if (!cpRow || !cpRow.state) return null;
                        try {
                            return self._WorkspaceStateModelCtor
                                       .fromSnapshot(cpRow.state);
                        } catch (e) {
                            console.warn('[WorkspaceShellChrome] '
                                + 'checkpoint decode failed:',
                                e && e.message ? e.message : e);
                            return null;
                        }
                    },
                    fold: function (state, event) {
                        try { state.apply(event); }
                        catch (e) {
                            console.warn('[WorkspaceShellChrome] '
                                + 'model.apply threw on', event.name, ':',
                                e && e.message ? e.message : e);
                        }
                        return state;
                    },
                    onEmpty: function (state) {
                        // Fresh session — seed model with pinned widgets.
                        seededPinnedDescriptors = self._seedPinnedIntoModel(state);
                        return state;
                    },
                    onProgress: function (s) {
                        // Lightweight per-event log. No banner; user
                        // sees boot complete from the console.
                        if (s.idx === 1 || s.idx === s.total || s.idx % 25 === 0) {
                            console.log('[WorkspaceShellChrome] reconstructing '
                                      + s.idx + '/' + s.total + ': ' + s.name);
                        }
                    }
                }).then(function (summary) {
                    self._model = summary.finalState || self._model;
                    console.log('[WorkspaceShellChrome] virtual replay done:',
                                'replayed=' + summary.replayed
                              + ' cp=' + summary.restoredFromCheckpoint
                              + ' empty=' + summary.fellThroughToEmpty
                              + ' lastSeq=' + summary.lastSeq);
                    // Build MTP from the final model.
                    self._buildMtp();
                    // Project: spawn every tab in the model into MTP.
                    return self._projectModel().then(function () {
                        // Set workspace-active from the model (or first tab).
                        self._restoreWorkspaceActive();
                        // Drop the replay fence — user interactions now emit.
                        try { self._eventRecorder.setReplaying(false); }
                        catch (e) {}
                        // Build picker + control modal now that MTP exists.
                        self._buildPickerFlow();
                        self._buildControlModal();
                        // Fresh session: emit pinned-spawn events so next
                        // boot has them recorded. Fence is now off so the
                        // recorder accepts them.
                        if (seededPinnedDescriptors) {
                            self._emitSeededPinnedSpawns(seededPinnedDescriptors);
                        }
                        return summary;
                    });
                });
            }).catch(function (err) {
                console.error('[WorkspaceShellChrome] boot pipeline failed:', err);
            });
        this._replayDone = this._pinnedReady;

        // ── Dep-graph dump once boot stabilises ─────────────────────────
        this._pinnedReady.then(function () {
            DepGraphWalker.INSTANCE.dump(self, 'WorkspaceShellChrome');
        });

        // ── DevTools surface ────────────────────────────────────────────
        if (typeof window !== 'undefined') {
            window.__homing_v2__ = {
                dumpEvents: function () {
                    const log = self._eventRecorder && self._eventRecorder.log();
                    if (!log || typeof log.query !== 'function') {
                        console.warn('[__homing_v2__] event log not attached');
                        return Promise.resolve([]);
                    }
                    return log.query().then(function (rows) {
                        console.log('[__homing_v2__] event log: ' + rows.length + ' row(s)');
                        console.table(rows.map(function (r) {
                            return { seq: r.seq, name: r.name,
                                     payload: JSON.stringify(r.payload || {}),
                                     emittedAt: r.emittedAt };
                        }));
                        return rows;
                    });
                },
                readCheckpoint: function () {
                    const store = self._checkpointController
                                && self._checkpointController.store();
                    if (!store || typeof store.read !== 'function') {
                        console.warn('[__homing_v2__] no checkpoint store');
                        return Promise.resolve(null);
                    }
                    return store.read(self._spec.kind, self._identity.id)
                        .then(function (cp) {
                            console.log('[__homing_v2__] checkpoint:', cp);
                            return cp;
                        });
                },
                dumpGraph: function () { return DepGraphWalker.INSTANCE.dump(self, 'WorkspaceShellChrome'); },
                tabs:      function () { return self._tabRegistry.inspect(); },
                model:     function () { return self._model && self._model.inspect(); },
                captureNow: function () {
                    if (!self._checkpointController) return Promise.resolve(null);
                    return self._checkpointController.captureNow('manual');
                }
            };
        }

        return {
            kind:                 this._spec.kind,
            layout:               this._layout,
            mtp:                  () => self._mtp,
            widgetsBranch:        this._widgetsBranch,
            layoutCodec:          this._layoutCodec,
            persistenceLayer:     this._persistenceLayer,
            codecsReady:          this._codecsReady,
            partiesReady:         this._partiesReady,
            pinnedReady:          this._pinnedReady,
            identityReady:        this._identityReady,
            identity:             () => self._identity,
            eventRecorderReady:   this._eventRecorderReady,
            eventRecorder:        () => self._eventRecorder,
            lockReady:            this._lockReady,
            lockController:       () => self._lockController,
            checkpointReady:      this._checkpointReady,
            checkpointController: () => self._checkpointController,
            replayDone:           this._replayDone,
            tabRegistry:          () => self._tabRegistry,
            model:                () => self._model,
            dumpGraph:            function () { return DepGraphWalker.INSTANCE.dump(self, 'WorkspaceShellChrome'); },
            parties:              () => self._parties,
            workspaceCtx:         () => self._workspaceCtx,
            pickerFlow:           () => self._pickerFlow,
            inspect: function () {
                return {
                    kind:         self._spec.kind,
                    identity:     Object.assign({}, self._identity),
                    tabRegistry:  self._tabRegistry ? self._tabRegistry.inspect() : null,
                    lock:         self._lockController ? self._lockController.inspect() : null,
                    checkpoint:   self._checkpointController ? self._checkpointController.inspect() : null,
                    recorder:     self._eventRecorder ? self._eventRecorder.inspect() : null,
                    model:        self._model ? self._model.inspect() : null,
                    partiesReady: Object.keys(self._parties).length > 0,
                    partyNames:   Object.keys(self._parties),
                    pickerReady:  !!self._pickerFlow,
                    mtpReady:     !!self._mtp
                };
            }
        };
    }

    // ── Boot helpers (post-replay) ──────────────────────────────────────

    /**
     * Construct MTP with initialLayout taken from the model. Wire all
     * MTP callbacks to BOTH emit events AND apply them to the model
     * synchronously so the model stays in sync with live state.
     */
    _buildMtp() {
        const self = this;
        const initialLayout = this._model.layout();
        this._mtp = new this._MultiTabPaneCtor({
            container:     this._layout.contentEl,
            budget:        16,
            initialLayout: initialLayout,
            onAddTab: function (slotId) {
                if (!self._pickerFlow) {
                    console.warn('[WorkspaceShellChrome] picker not ready '
                               + '(boot still racing) — slot:', slotId);
                    return;
                }
                self._pickerFlow.openInSlot(slotId);
            },
            onTabMoved: function (srcSlot, destSlot, tab, _fromIdx, toIdx) {
                if (!tab || !tab.widgetInstanceUuid) return;
                const uuid = tab.widgetInstanceUuid;
                if (self._tabRegistry) self._tabRegistry.updateSlot(uuid, destSlot);
                const payload = {
                    widgetInstanceId: uuid,
                    widgetKind:       tab.widgetKind,
                    from: { paneId: self._mtp.paneIdOf(srcSlot)  || '_' },
                    to:   { paneId: self._mtp.paneIdOf(destSlot) || '_',
                            tabIndex: toIdx }
                };
                self._applyToModel('TabMoved', payload);
                if (self._eventRecorder) self._eventRecorder.emit('TabMoved', payload);
            },
            onTabRemoved: function (slotId, tab, _fromIdx) {
                if (!tab || !tab.widgetInstanceUuid) return;
                const uuid = tab.widgetInstanceUuid;
                if (self._tabRegistry) self._tabRegistry.unregister(uuid);
                const payload = {
                    widgetInstanceId: uuid,
                    widgetKind:       tab.widgetKind,
                    from: { paneId: self._mtp.paneIdOf(slotId) || '_' }
                };
                self._applyToModel('TabClosed', payload);
                if (self._eventRecorder) self._eventRecorder.emit('TabClosed', payload);
            },
            onTabAttached: function (destSlot, tab, toIdx) {
                if (!tab || !tab.widgetInstanceUuid) return;
                const uuid     = tab.widgetInstanceUuid;
                const prior    = self._tabRegistry && self._tabRegistry.lookup(uuid);
                const fromSlot = prior ? prior.slotId : null;
                if (self._tabRegistry) self._tabRegistry.updateSlot(uuid, destSlot);
                if (fromSlot === destSlot) return;
                const payload = {
                    widgetInstanceId: uuid,
                    widgetKind:       tab.widgetKind,
                    from: { paneId: fromSlot
                                  ? (self._mtp.paneIdOf(fromSlot) || '_')
                                  : '_' },
                    to:   { paneId: self._mtp.paneIdOf(destSlot) || '_',
                            tabIndex: toIdx }
                };
                self._applyToModel('TabMoved', payload);
                if (self._eventRecorder) self._eventRecorder.emit('TabMoved', payload);
            },
            onSplit: function (srcSlot, orientation, _newSlot) {
                const childPath  = self._mtp.paneIdOf(srcSlot) || '_';
                const parentPath = childPath.replace(/_[12]$/, '') || '_';
                const payload = { paneId: parentPath, orientation: orientation };
                self._applyToModel('SplitCreated', payload);
                if (self._eventRecorder) self._eventRecorder.emit('SplitCreated', payload);
            },
            onMerge: function (keptSlot, _removedSlot) {
                const parentPath = self._mtp.paneIdOf(keptSlot) || '_';
                const payload = { paneId: parentPath };
                self._applyToModel('SplitMerged', payload);
                if (self._eventRecorder) self._eventRecorder.emit('SplitMerged', payload);
            },
            onWorkspaceActiveChanged: function (prevTabId, nextTabId) {
                const prevHit = prevTabId && self._tabRegistry.findByTabId(prevTabId);
                const nextHit = nextTabId && self._tabRegistry.findByTabId(nextTabId);
                const payload = {
                    from: prevHit ? { widgetInstanceId: prevHit.widgetInstanceUuid } : null,
                    to:   nextHit ? { widgetInstanceId: nextHit.widgetInstanceUuid } : null
                };
                self._applyToModel('WorkspaceActiveChanged', payload);
                if (self._eventRecorder) self._eventRecorder.emit('WorkspaceActiveChanged', payload);
            },
            // Divider-drag stop — diff new layout vs model and emit one
            // SplitRatioChanged per changed split. Most drags only touch
            // a single split; the diff handles nested splits too if a
            // single gesture somehow cascades.
            onRatioChanged: function (newLayout) {
                if (!newLayout) return;
                const oldLayout = self._model && self._model.layout();
                if (!oldLayout) return;
                const diffs = self._diffSplitRatios(oldLayout, newLayout, '_');
                for (const d of diffs) {
                    self._applyToModel('SplitRatioChanged', d);
                    if (self._eventRecorder) self._eventRecorder.emit('SplitRatioChanged', d);
                }
            }
        });
        console.log('[WorkspaceShellChrome] MTP constructed with',
                    Object.keys(this._model.tabsBySlot ? {} : {}).length,
                    'replayed slot(s)');
    }

    /**
     * Walk old + new layouts in parallel, finding split nodes whose
     * first-child ratio changed by > 0.5%. Returns
     * [{paneId, ratio}, …] suitable for SplitRatioChanged payloads.
     * Stops descending where the topology diverges (split↔leaf) —
     * structural changes are journaled separately as SplitCreated/Merged.
     */
    _diffSplitRatios(oldNode, newNode, path) {
        if (!oldNode || !newNode) return [];
        if (oldNode.kind !== newNode.kind) return [];
        if (oldNode.kind !== 'split') return [];
        const out = [];
        const oldRatio = oldNode.children[0].ratio;
        const newRatio = newNode.children[0].ratio;
        if (Math.abs(oldRatio - newRatio) > 0.005) {
            out.push({ paneId: path, ratio: newRatio });
        }
        for (let i = 0; i < 2; i++) {
            const childPath = (path === '_')
                            ? ('_' + (i + 1))
                            : (path + '_' + (i + 1));
            const sub = this._diffSplitRatios(
                    oldNode.children[i].pane,
                    newNode.children[i].pane,
                    childPath);
            for (const d of sub) out.push(d);
        }
        return out;
    }

    /** Wrapper: apply a (name, payload) event to the model. */
    _applyToModel(name, payload) {
        if (!this._model) return;
        try { this._model.apply({ name: name, payload: payload }); }
        catch (e) {
            console.warn('[WorkspaceShellChrome] _applyToModel threw:', e);
        }
    }

    /**
     * Project the model onto MTP: for each (slot, tabs) pair, mount each
     * widget silently (no emit — fence still on). Returns a Promise that
     * resolves once every widget's dynamic import + mount completes.
     */
    _projectModel() {
        const self    = this;
        const tabsBySlot = this._model.tabsBySlot();
        const tasks   = [];
        tabsBySlot.forEach(function (descriptors, slot) {
            for (const d of descriptors) {
                tasks.push(self._silentSpawn(slot, d));
            }
        });
        if (tasks.length > 0) {
            console.log('[WorkspaceShellChrome] projecting model — '
                      + tasks.length + ' widget(s) to mount');
        }
        return Promise.all(tasks);
    }

    /**
     * Mount one widget into MTP without emitting any event. Used during
     * projection (post-fold, fence on). Mirrors PinnedTabSpawner /
     * PickerTabFlow's mount path: branch, addTab, mounter.resolve→mount→
     * attach, register.
     */
    _silentSpawn(slotId, descriptor) {
        const self  = this;
        const uuid  = descriptor.widgetInstanceUuid;
        const kind  = descriptor.widgetKind;
        const entry = (this._spec.entries || []).find(
                e => e.simpleName === kind);
        if (!entry) {
            console.warn('[WorkspaceShellChrome] projection: unknown kind', kind);
            return Promise.resolve();
        }
        // Tab object — matches the shape PinnedTabSpawner/PickerTabFlow create.
        const branchName = 'w-' + uuid.replace(/[^A-Za-z0-9_-]/g, '_');
        const wBranch    = this._widgetsBranch.createBranch(branchName);
        wBranch.activate(Object.freeze({ toString: () => 'projection:' + uuid }));
        const holder  = { contentEl: null };
        const loading = document.createElement('div');
        loading.textContent = 'Loading ' + (descriptor.title || entry.label) + '…';
        const tab = {
            id:                 uuid,
            title:              descriptor.title || entry.label,
            pinned:             !!descriptor.pinned,
            widgetKind:         kind,
            widgetInstanceUuid: uuid,
            render:    function (el) { holder.contentEl = el; el.appendChild(loading); },
            setActive: function () {},
            onClose:   function () {
                try { wBranch.dissolve(); } catch (e) {}
                if (self._tabRegistry) self._tabRegistry.unregister(uuid);
            }
        };
        try { this._mtp.addTab(slotId, tab); }
        catch (e) {
            console.error('[WorkspaceShellChrome] projection addTab failed:',
                          slotId, uuid, e);
            return Promise.resolve();
        }
        // Register synchronously so subsequent project steps + post-mount
        // callbacks can find the entry.
        this._tabRegistry.register({
            widgetInstanceUuid: uuid,
            tab:                tab,
            slotId:             slotId,
            widgetKind:         kind,
            controller:         null
        });
        const params = descriptor.params || entry.defaults || {};
        return this._widgetMounter.resolve(entry).then(function (mod) {
            if (!self._tabRegistry.lookup(uuid)) return;   // closed mid-flight
            const ctrl = self._widgetMounter.mount(mod, wBranch, entry,
                                                   params, self._workspaceCtx);
            self._widgetMounter.attach(ctrl, tab, holder);
            const existing = self._tabRegistry.lookup(uuid);
            if (existing) existing.controller = ctrl;
        }).catch(function (err) {
            console.error('[WorkspaceShellChrome] projection mount failed for',
                          kind, ':', err);
            if (holder.contentEl) {
                loading.textContent = 'Failed to restore ' + (descriptor.title || entry.label);
            }
        });
    }

    /**
     * After projection: set workspace-active from the model. If the model
     * has no active uuid (e.g., first fresh boot), pick the first
     * projected tab so MTP's click-to-activate cover doesn't sit on
     * the strip-active tab.
     */
    _restoreWorkspaceActive() {
        if (!this._mtp || !this._mtp.setWorkspaceActiveTab) return;
        const activeUuid = this._model.activeUuid();
        if (activeUuid) {
            const live = this._tabRegistry.lookup(activeUuid);
            if (live && live.tab) {
                try { this._mtp.setWorkspaceActiveTab(live.tab.id); }
                catch (e) {}
                return;
            }
        }
        // Fallback: first registered uuid.
        const uuids = this._tabRegistry.uuids();
        if (uuids.length > 0) {
            const live = this._tabRegistry.lookup(uuids[0]);
            if (live && live.tab) {
                try { this._mtp.setWorkspaceActiveTab(live.tab.id); }
                catch (e) {}
            }
        }
    }

    /**
     * Seed the model with pinned-widget descriptors for a fresh session.
     * Returns the descriptors so the orchestrator can emit corresponding
     * WidgetSpawnedPinned events after fence drops.
     */
    _seedPinnedIntoModel(model) {
        const seeded = [];
        const byName = {};
        for (const e of (this._spec.entries || [])) byName[e.simpleName] = e;
        const pinned = Array.isArray(this._spec.pinnedSpawns) ? this._spec.pinnedSpawns : [];
        for (const kind of pinned) {
            const entry = byName[kind];
            if (!entry) continue;
            const uuid = entry.simpleName + ':pinned';
            const descriptor = {
                widgetInstanceId: uuid,
                widgetKind:       entry.simpleName,
                title:            entry.label,
                params:           entry.defaults || {},
                to: { paneId: '_', tabIndex: 0 }
            };
            model.apply({ name: 'WidgetSpawnedPinned', payload: descriptor });
            seeded.push(descriptor);
        }
        if (seeded.length > 0) {
            console.log('[WorkspaceShellChrome] seeded model with',
                        seeded.length, 'pinned widget(s)');
        }
        return seeded;
    }

    /**
     * Emit WidgetSpawnedPinned events for each seeded pinned widget so
     * the next session's replay re-spawns them. Run AFTER fence drops.
     */
    _emitSeededPinnedSpawns(descriptors) {
        if (!this._eventRecorder) return;
        for (const d of descriptors) {
            this._eventRecorder.emit('WidgetSpawnedPinned', d);
        }
    }

    _buildPickerFlow() {
        this._pickerFlow = new this._PickerTabFlowCtor({
            mtp:           this._mtp,
            widgetsBranch: this._widgetsBranch,
            spec:          this._spec,
            workspaceCtx:  this._workspaceCtx,
            mounter:       this._widgetMounter,
            tabRegistry:   this._tabRegistry,
            recorder:      this._eventRecorder,
            model:         this._model
        });
        console.log('[WorkspaceShellChrome] PickerTabFlow ready');
    }

    _buildControlModal() {
        const cpStore = this._checkpointController
                      ? this._checkpointController.store() : null;
        this._workspaceControlModal = new this._WorkspaceControlModalCtor({
            workspaceKind:   this._spec.kind,
            workspaceTitle:  this._spec.title,
            identity:        this._identity,
            catalogueStore:  this._workspaceDirectory._catalogueStore,
            eventLog:        this._eventRecorder
                            ? this._eventRecorder.log() : null,
            checkpointStore: cpStore
        });
        console.log('[WorkspaceShellChrome] WorkspaceControl modal ready');
    }

    // ── Action dispatch (unchanged from V1) ────────────────────────────

    _dispatchAction(actionId, value) {
        const entry = this._spec.actionDispatch && this._spec.actionDispatch[actionId];
        if (!entry) {
            console.log('[WorkspaceShellChrome] unhandled action:', actionId, value);
            return;
        }
        if (entry.kind === 'tellParty') {
            const party = this._parties[entry.party];
            if (!party) {
                console.warn('[WorkspaceShellChrome] action "' + actionId
                    + '" → tellParty references party "' + entry.party
                    + '" which is not yet constructed — boot still racing.');
                return;
            }
            const msg = { kind: entry.messageKind };
            msg[entry.valueKey] = value;
            party.tellFrom(entry.actor, msg);
            return;
        }
        if (entry.kind === 'logOnly') {
            console.log('[WorkspaceShellChrome] action:', actionId, value);
            return;
        }
        console.warn('[WorkspaceShellChrome] unknown ActionDispatch kind:', entry.kind);
    }
}

/** Convenience entry-point — typical call shape. */
function mountWorkspaceShell(branch, parent, spec) {
    return new WorkspaceShellChrome(spec).mount(branch, parent);
}

// =============================================================================
// DepGraphWalker — traverses underscore-prefixed instance fields and
// dumps the dependency tree to console. Unchanged from physical-replay
// architecture; works for any object graph regardless of replay style.
// =============================================================================

class DepGraphWalker {

    constructor(opts) {
        opts = opts || {};
        this._maxDepth = (typeof opts.maxDepth === 'number') ? opts.maxDepth : 8;
        this._logger   = opts.logger || console;
    }

    dump(root, label) {
        const seen = new Map();
        const out  = [];
        out.push((label || 'root') + ' → ' + this._kind(root));
        this._walk(root, 1, seen, out);
        this._logger.log(out.join('\n'));
        return seen;
    }

    _walk(node, depth, seen, out) {
        if (depth > this._maxDepth) {
            out.push('  '.repeat(depth) + '… (maxDepth reached)');
            return;
        }
        const myLabel = this._kind(node) + '#' + (seen.size + 1);
        seen.set(node, myLabel);
        const collaborators = this._collaboratorsOf(node);
        for (const { field, value } of collaborators) {
            const kind  = this._kind(value);
            const prior = seen.get(value);
            if (prior) {
                out.push('  '.repeat(depth) + field + ' → ' + kind + ' (shared: ' + prior + ')');
                continue;
            }
            out.push('  '.repeat(depth) + field + ' → ' + kind);
            if (typeof value === 'object') {
                this._walk(value, depth + 1, seen, out);
            }
        }
    }

    _collaboratorsOf(node) {
        if (!node || typeof node !== 'object') return [];
        const out = [];
        for (const field of Object.keys(node)) {
            if (!field.startsWith('_')) continue;
            const value = node[field];
            if (value == null) continue;
            const t = typeof value;
            if (t !== 'object' && t !== 'function') continue;
            if (this._isPrimitiveContainer(value)) continue;
            out.push({ field, value });
        }
        return out;
    }

    _isPrimitiveContainer(v) {
        if (typeof v === 'function') return false;
        const ctorName = v.constructor && v.constructor.name;
        if (!ctorName)              return true;
        if (ctorName === 'Object')  return true;
        if (ctorName === 'Array')   return true;
        if (ctorName === 'Map')     return true;
        if (ctorName === 'Set')     return true;
        if (ctorName === 'Promise') return true;
        return false;
    }

    _kind(v) {
        if (v === null || v === undefined) return String(v);
        if (typeof v === 'function')        return 'class ' + (v.name || '<anonymous>');
        const ctor = v.constructor && v.constructor.name;
        return ctor || typeof v;
    }
}

DepGraphWalker.INSTANCE = new DepGraphWalker();
