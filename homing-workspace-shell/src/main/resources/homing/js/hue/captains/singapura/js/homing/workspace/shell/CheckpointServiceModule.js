// =============================================================================
// CheckpointService — Phase 8 of the workspace-shell chrome.
//
// Periodic snapshots of live workspace state → CheckpointStore. Three
// cadence triggers, all gated by recorder.inspect() (replaying or
// !writeLockHeld → skip):
//
//   event-count : recorder.eventsSinceLastCheckpoint() ≥ eventCountM
//   timer       : setInterval(intervalMs); skipped if cadence==0
//   unload      : window.pagehide + beforeunload best-effort flush
//
// Two write paths:
//   WORKER (RFC 0034 P2, default when supported):
//     postMessage to a CheckpointWorkerModule instance; worker serialises
//     + writes + (optionally) prunes events. Main thread unblocks once
//     postMessage returns. Reply tracked via reqId; reply triggers
//     cadence reset + console log. On worker init failure or postMessage
//     throw (non-cloneable payload), falls back to main-thread path for
//     that one capture; worker stays alive for next time unless onerror
//     fires too.
//
//   MAIN-THREAD (V1 fallback):
//     Direct store.write on the JS event-loop thread. Prunes inline
//     (post-write .then) when opt-in is set.
//
// Pruning is gated by window.__homing_enableEventPruning__ (opt-in
// today; flips to default-on when checkpoint state captures widget
// positions losslessly — see WorkspaceStateModel snapshot work).
//
// Returns controller:
//   captureNow(reason)→Promise<seq|null>
//   stop()
//   inspect()
//   store()  — for ReplayEngine to share
//
// Explicit Substrate doctrine:
//   - INSTANCE singleton + per-call attach.
//   - No async sugar — naive Promise chains.
//   - ALL collaborators in constructor: _checkpointStoreFactory, _timer,
//     _window, _WorkerCtor, _workerUrl.
// =============================================================================

class CheckpointService {

    constructor(deps) {
        deps = deps || {};
        if (Object.prototype.hasOwnProperty.call(deps, 'checkpointStoreFactory')) {
            this._checkpointStoreFactory = deps.checkpointStoreFactory;
        } else {
            this._checkpointStoreFactory = (typeof createCheckpointStore === 'function')
                ? createCheckpointStore : null;
        }
        this._timer = deps.timer || {
            setInterval:   (typeof setInterval   !== 'undefined')
                                ? function (fn, ms) { return setInterval(fn, ms); }
                                : null,
            clearInterval: (typeof clearInterval !== 'undefined')
                                ? function (id)     { return clearInterval(id); }
                                : null
        };
        this._window = deps.window
            || (typeof window !== 'undefined' ? window : null);
        // Worker constructor + URL — both overridable for tests. Default
        // wires the framework's CheckpointWorkerModule via the standard
        // /module?class=… route. Explicit null disables the worker path
        // entirely (caller forced main-thread).
        if (Object.prototype.hasOwnProperty.call(deps, 'WorkerCtor')) {
            this._WorkerCtor = deps.WorkerCtor;
        } else {
            this._WorkerCtor = (typeof Worker !== 'undefined') ? Worker : null;
        }
        this._workerUrl = deps.workerUrl
            || ((this._window && this._window.location)
                ? this._window.location.origin
                    + '/module?class=hue.captains.singapura.js.homing.workspace.events.CheckpointWorkerModule'
                : null);
    }

    /**
     * Attach the service for one workspace. Wires the cadence triggers
     * and returns a controller. Idempotent on stop() — second call is
     * a no-op.
     */
    attach(opts) {
        if (!opts)                throw new Error('[CheckpointService] opts required');
        if (!opts.workspaceKind)  throw new Error('[CheckpointService] opts.workspaceKind required');
        if (!opts.workspaceId)    throw new Error('[CheckpointService] opts.workspaceId required');
        if (!opts.recorder)       throw new Error('[CheckpointService] opts.recorder required');
        if (typeof opts.captureState !== 'function') {
            throw new Error('[CheckpointService] opts.captureState (function) required');
        }
        const self         = this;
        const intervalMs   = (typeof opts.intervalMs === 'number') ? opts.intervalMs : 30_000;
        const eventCountM  = (typeof opts.eventCountM === 'number') ? opts.eventCountM : 50;
        let   stopped      = false;
        let   timerHandle  = null;
        let   pageHideHandler     = null;
        let   beforeUnloadHandler = null;

        // Build the underlying store. Used by the main-thread write
        // fallback path AND exposed via store() for ReplayEngine reads.
        // The Worker has its own IDB connection (lives in worker thread)
        // and writes independently.
        let store = null;
        if (this._checkpointStoreFactory) {
            try { store = this._checkpointStoreFactory(); }
            catch (e) {
                console.warn('[CheckpointService] store factory threw '
                           + '(writes disabled):',
                    e && e.message ? e.message : e);
            }
        }

        // ── Worker setup (RFC 0034 P2) ──────────────────────────────────
        // Try to spawn once at attach time. On any failure, worker stays
        // null and all writes go through the main-thread path. A worker
        // .onerror later flips useWorker=false to drain pending +
        // permanently fall back without churning init retries.
        let worker        = null;
        let useWorker     = false;
        let workerReqSeq  = 0;
        const workerPending = new Map();   // reqId → { resolve, reason }
        if (this._WorkerCtor && this._workerUrl) {
            try {
                worker = new this._WorkerCtor(this._workerUrl, { type: 'module' });
                worker.onmessage = function (ev) {
                    const data  = ev.data || {};
                    const entry = workerPending.get(data.reqId);
                    if (!entry) return;
                    workerPending.delete(data.reqId);
                    if (data.ok) {
                        stats.writesOk++;
                        stats.workerWrites++;
                        opts.recorder.resetCheckpointCadence();
                        if (typeof data.prunedCount === 'number') {
                            stats.pruned += data.prunedCount;
                        }
                        const pruneSuffix = (typeof data.prunedCount === 'number')
                            ? ' (pruned ' + data.prunedCount + ' event row(s))'
                            : '';
                        console.log('[CheckpointService] worker checkpoint ('
                                  + entry.reason + ') — seq ' + data.storedSeq
                                  + pruneSuffix);
                        entry.resolve(data.storedSeq);
                    } else {
                        stats.writesFailed++;
                        console.warn('[CheckpointService] worker checkpoint failed ('
                                  + entry.reason + '):', data.error);
                        entry.resolve(null);
                    }
                };
                worker.onerror = function (err) {
                    console.warn('[CheckpointService] worker errored '
                               + '— falling back to main thread:',
                        err && err.message ? err.message : err);
                    useWorker = false;
                    // Drain pending so callers don't hang.
                    workerPending.forEach(function (entry) { entry.resolve(null); });
                    workerPending.clear();
                };
                useWorker = true;
                console.log('[CheckpointService] checkpoint worker initialized');
            } catch (e) {
                console.warn('[CheckpointService] worker init failed '
                           + '— main-thread fallback:',
                    e && e.message ? e.message : e);
                worker = null;
            }
        }
        if (!worker) {
            console.log('[CheckpointService] no worker — main-thread checkpoint path');
        }

        const stats = {
            writesAttempted: 0,
            writesOk:        0,
            writesFailed:    0,
            pruned:          0,
            workerWrites:    0,
            mainThreadWrites: 0
        };

        function gateOpen() {
            const snap = opts.recorder.inspect();
            return !snap.replaying && snap.writeLockHeld;
        }

        function shouldPrune() {
            return (typeof window !== 'undefined'
                 && !!window.__homing_enableEventPruning__);
        }

        function captureNow(reason) {
            if (stopped)  return Promise.resolve(null);
            if (!store)   return Promise.resolve(null);
            if (!gateOpen()) return Promise.resolve(null);
            let wire;
            try { wire = opts.captureState(); }
            catch (e) {
                console.warn('[CheckpointService] captureState threw:',
                    e && e.message ? e.message : e);
                return Promise.resolve(null);
            }
            if (wire == null) return Promise.resolve(null);
            const seqAtCapture = opts.recorder.lastEmittedSeq();
            stats.writesAttempted++;
            const doPrune = shouldPrune();
            // Worker path first if available. postMessage can throw on
            // non-cloneable payloads (Maps, Sets, functions) — that one
            // capture falls through to main thread; worker stays alive.
            if (useWorker && worker) {
                const reqId = ++workerReqSeq;
                try {
                    return new Promise(function (resolve) {
                        workerPending.set(reqId, { resolve: resolve, reason: reason });
                        worker.postMessage({
                            reqId:        reqId,
                            kind:         opts.workspaceKind,
                            workspaceId:  opts.workspaceId,
                            state:        wire,
                            lastEventSeq: seqAtCapture,
                            prune:        doPrune
                        });
                    });
                } catch (e) {
                    workerPending.delete(reqId);
                    console.warn('[CheckpointService] worker postMessage threw ('
                              + reason + ') — main-thread fallback for this write:',
                        e && e.message ? e.message : e);
                    // Fall through to main thread.
                }
            }
            return writeMainThread(wire, seqAtCapture, reason, doPrune);
        }

        function writeMainThread(wire, seqAtCapture, reason, doPrune) {
            return store.write(opts.workspaceKind, opts.workspaceId, wire, seqAtCapture)
                .then(function () {
                    stats.writesOk++;
                    stats.mainThreadWrites++;
                    opts.recorder.resetCheckpointCadence();
                    console.log('[CheckpointService] main-thread checkpoint ('
                              + reason + ') — seq ' + seqAtCapture);
                    if (!doPrune) return seqAtCapture;
                    if (opts.eventLog && typeof opts.eventLog.prune === 'function'
                                      && seqAtCapture > 0) {
                        opts.eventLog.prune(seqAtCapture).then(function (n) {
                            stats.pruned += n;
                            if (n > 0) {
                                console.log('[CheckpointService] pruned ' + n
                                          + ' event(s) up to seq ' + seqAtCapture);
                            }
                        }).catch(function (e) {
                            console.warn('[CheckpointService] prune failed:',
                                e && e.message ? e.message : e);
                        });
                    }
                    return seqAtCapture;
                })
                .catch(function (e) {
                    stats.writesFailed++;
                    console.warn('[CheckpointService] main-thread write failed ('
                              + reason + '):',
                        e && e.message ? e.message : e);
                    return null;
                });
        }

        // event-count cadence.
        opts.recorder.setOnAfterEmit(function (_seq) {
            if (stopped) return;
            if (opts.recorder.eventsSinceLastCheckpoint() < eventCountM) return;
            captureNow('event-count');
        });

        // timer cadence.
        if (this._timer.setInterval) {
            timerHandle = this._timer.setInterval(function () {
                if (stopped) return;
                if (opts.recorder.eventsSinceLastCheckpoint() === 0) return;
                captureNow('timer');
            }, intervalMs);
        }

        // unload cadence.
        if (this._window && typeof this._window.addEventListener === 'function') {
            pageHideHandler = function () {
                if (stopped) return;
                if (opts.recorder.eventsSinceLastCheckpoint() === 0) return;
                captureNow('unload');
            };
            beforeUnloadHandler = pageHideHandler;
            this._window.addEventListener('pagehide',     pageHideHandler);
            this._window.addEventListener('beforeunload', beforeUnloadHandler);
        }

        return {
            captureNow: captureNow,
            stop:       function () {
                if (stopped) return;
                stopped = true;
                try { opts.recorder.setOnAfterEmit(null); } catch (e) {}
                if (timerHandle != null && self._timer.clearInterval) {
                    try { self._timer.clearInterval(timerHandle); } catch (e) {}
                    timerHandle = null;
                }
                if (self._window && pageHideHandler) {
                    try {
                        self._window.removeEventListener('pagehide',     pageHideHandler);
                        self._window.removeEventListener('beforeunload', beforeUnloadHandler);
                    } catch (e) {}
                    pageHideHandler     = null;
                    beforeUnloadHandler = null;
                }
                // Terminate worker — any in-flight write is allowed to
                // commit (IDB transactions in workers commit independent
                // of worker lifetime). Termination just frees the thread.
                if (worker) {
                    try { worker.terminate(); } catch (e) {}
                    worker = null;
                    useWorker = false;
                    workerPending.forEach(function (entry) { entry.resolve(null); });
                    workerPending.clear();
                }
            },
            inspect:    function () {
                return {
                    hasStore:         !!store,
                    hasWorker:        !!worker,
                    useWorker:        useWorker,
                    intervalMs:       intervalMs,
                    eventCountM:      eventCountM,
                    stopped:          stopped,
                    writesAttempted:  stats.writesAttempted,
                    writesOk:         stats.writesOk,
                    writesFailed:     stats.writesFailed,
                    workerWrites:     stats.workerWrites,
                    mainThreadWrites: stats.mainThreadWrites,
                    pruned:           stats.pruned
                };
            },
            store:      function () { return store; }
        };
    }
}

CheckpointService.INSTANCE = new CheckpointService();
