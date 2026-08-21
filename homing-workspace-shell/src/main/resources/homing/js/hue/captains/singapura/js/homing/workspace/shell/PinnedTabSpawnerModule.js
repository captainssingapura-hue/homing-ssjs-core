// =============================================================================
// PinnedTabSpawner — Phase 14 of the workspace-shell chrome.
//
// Reads spec.pinnedSpawns and spawns each into a target slot.
//
// Explicit Substrate doctrine:
//   - Instance methods on INSTANCE singleton (no static).
//   - No async sugar.
//   - ALL collaborators in constructor; the per-call methods receive
//     only data (mtp, branch, spec, ctx, slotId), never collaborator
//     overrides.
//
// Instance fields (graph-traversable):
//   _mounter : WidgetMounter — defaults to WidgetMounter.INSTANCE
// =============================================================================

class PinnedTabSpawner {

    constructor(deps) {
        deps = deps || {};
        this._mounter = deps.mounter || WidgetMounter.INSTANCE;
    }

    /**
     * Spawn every widget the spec marks as pinned. Returns Promise<Tab[]>.
     * Synchronous side effects complete before any .then yields.
     *
     * Optional per-call collaborators (passed through opts because they
     * are per-workspace, not per-INSTANCE — so they would otherwise
     * pollute the singleton):
     *   tabRegistry — Phase 12; register(uuid, tab, slotId, kind) on mount.
     *   recorder    — Phase 6; emit('WidgetSpawnedPinned', payload) so
     *                 Phase 9 replay can re-create this widget.
     */
    spawnAll(opts) {
        if (!opts || !opts.mtp)            throw new Error('[PinnedTabSpawner] opts.mtp required');
        if (!opts.widgetsBranch)           throw new Error('[PinnedTabSpawner] opts.widgetsBranch required');
        if (!opts.spec)                    throw new Error('[PinnedTabSpawner] opts.spec required');

        const slotId = opts.slotId || 'tl';
        const pickedKinds = Array.isArray(opts.spec.pinnedSpawns)
                          ? opts.spec.pinnedSpawns : [];
        if (pickedKinds.length === 0) return Promise.resolve([]);

        const byName = {};
        for (const e of (opts.spec.entries || [])) byName[e.simpleName] = e;

        const startedSpawns = [];
        for (const kind of pickedKinds) {
            const entry = byName[kind];
            if (!entry) {
                console.warn('[PinnedTabSpawner] pinned widget kind "' + kind
                           + '" not present in spec.entries — skipped.');
                continue;
            }
            startedSpawns.push(this._beginSpawn(
                    opts.mtp, opts.widgetsBranch, slotId, entry,
                    opts.workspaceCtx, opts.tabRegistry || null,
                    opts.recorder || null));
        }

        // RFC 0048 decision #2 — boot is SHALLOW: pinned tabs are added but no pane
        // is entered/activated. (The shell's _restoreWorkspaceActive keeps it null.)
        // A pane becomes active only via a deliberate deep-select (enterDeep).

        return Promise.all(startedSpawns.map(s => s.controllerReady.catch(_ => null)))
                      .then(_ => startedSpawns.map(s => s.tab));
    }

    /**
     * SYNCHRONOUS — creates the tab, calls mtp.addTab, kicks off the
     * mounter. Returns { tab, controllerReady }.
     */
    _beginSpawn(mtp, widgetsBranch, slotId, entry, workspaceCtx,
                tabRegistry, recorder) {
        const tabId   = entry.simpleName + ':pinned';
        const wBranch = widgetsBranch.createBranch('p-' + entry.simpleName);
        const owner   = Object.freeze({ toString: () => 'pinned:' + entry.simpleName });
        wBranch.activate(owner);

        const loading = document.createElement('div');
        loading.style.cssText = 'padding:20px;color:#666;font-family:sans-serif;';
        loading.textContent   = 'Loading ' + entry.label + '…';
        loading.setAttribute('tabindex', '0');   // focusable: keep focus in the pane + announce
        loading.setAttribute('role', 'status');

        const holder = { contentEl: null };
        const tab = {
            id:       tabId,
            title:    entry.label,
            pinned:   true,
            render:   function (contentEl) {
                holder.contentEl = contentEl;
                contentEl.appendChild(loading);
            },
            setActive: function (active) {},
            onClose:   function () { try { wBranch.dissolve(); } catch (e) {} },
            widgetKind:         entry.simpleName,
            widgetInstanceUuid: entry.simpleName + ':pinned'
        };
        mtp.addTab(slotId, tab);

        const mounter = this._mounter;
        const controllerReady = mounter.resolve(entry).then(function (mod) {
            const controller = mounter.mount(mod, wBranch, entry,
                                             entry.defaults || {}, workspaceCtx);
            mounter.attach(controller, tab, holder);
            // Phase 12 — record the live tab so replay can find it later.
            if (tabRegistry) {
                try {
                    tabRegistry.register({
                        widgetInstanceUuid: tab.widgetInstanceUuid,
                        tab:                tab,
                        slotId:             slotId,
                        widgetKind:         entry.simpleName,
                        controller:         controller
                    });
                } catch (e) {
                    console.error('[PinnedTabSpawner] tabRegistry.register threw:', e);
                }
            }
            // Phase 6 — emit so Phase 9 replay can re-spawn this widget.
            //   paneId is the pane-tree path the layout codec speaks; resolve the
            //   real destination via the MTP slot→paneId / tab-index bridges
            //   (paneIdOf / tabIndexOf), same as the picker + move paths, so a
            //   pinned widget restores into the pane it was actually spawned in.
            if (recorder && typeof recorder.emit === 'function') {
                const toPaneId = (mtp.paneIdOf && mtp.paneIdOf(slotId)) || '_';
                const rawIdx   = mtp.tabIndexOf ? mtp.tabIndexOf(slotId, tabId) : -1;
                recorder.emit('WidgetSpawnedPinned', {
                    widgetInstanceId: tab.widgetInstanceUuid,
                    widgetKind:       entry.simpleName,
                    title:            entry.label,
                    to: { paneId: toPaneId, tabIndex: rawIdx < 0 ? 0 : rawIdx }
                });
            }
            // RFC 0049 — no boot-time activation check: boot is shallow, and
            // setActive(true) is fired by the tab's FocusManager when the
            // coordinator enters the pane, never here.
            return controller;
        }).catch(function (err) {
            console.error('[PinnedTabSpawner] mount failed for', entry.simpleName, ':', err);
            if (holder.contentEl) {
                loading.textContent = 'Failed to load ' + entry.label
                                    + ': ' + (err && err.message ? err.message : err);
            }
            throw err;
        });

        return { tab: tab, controllerReady: controllerReady };
    }
}

PinnedTabSpawner.INSTANCE = new PinnedTabSpawner();
