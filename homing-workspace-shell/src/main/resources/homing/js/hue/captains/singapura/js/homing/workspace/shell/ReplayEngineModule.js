// =============================================================================
// ReplayEngine — virtual fold over recorded events into a final state.
//
// boot(opts):
//
//   1. Read checkpoint (if store provided)
//   2. Decode checkpoint via opts.decodeCheckpoint(row) → initialState
//      (caller-supplied; falls back to opts.initialState() if checkpoint
//      missing or decode fails)
//   3. Query event log starting from (cp.lastEventSeq + 1 || 0)
//   4. recorder.setReplaying(true) for the duration
//   5. Fold: for each event call state = opts.fold(state, event); the
//      caller's fold IS the reducer that mutates / replaces the in-memory
//      model. No DOM, no MTP, no per-step side effects beyond the fold.
//   6. Empty log + no checkpoint → state = opts.onEmpty() (caller-supplied
//      seed; typically: spawn pinned widgets into a fresh model)
//   7. Production mode: drop fence; slow-motion deferred to separate app
//
// Returns Promise<summary>:
//   { finalState, replayed, lastSeq, restoredFromCheckpoint, fellThroughToEmpty }
//
// Differences from physical-replay design:
//   - No `handlers` map. The single `fold` callback receives every event.
//   - No `ctx.halt`. A bad event isn't fatal — the fold can choose to
//     ignore unknown event names. Fatal disagreement is detected by the
//     caller comparing finalState to expected invariants (out of scope here).
//   - Caller projects finalState ONCE after boot — engine emits no
//     per-step DOM work. spawn-then-close pairs cost nothing at paint.
//
// Explicit Substrate doctrine:
//   - INSTANCE singleton; no static.
//   - No async sugar — naive Promise chains.
//   - ALL collaborators in constructor: _clock.
// =============================================================================

class ReplayEngine {

    constructor(deps) {
        deps = deps || {};
        this._clock = deps.clock || function () { return Date.now(); };
    }

    /**
     * Boot the workspace from checkpoint + event log. Returns
     * Promise<summary>. Never throws on its own — failures are absorbed
     * into the summary so the caller can surface them in the UI.
     */
    boot(opts) {
        if (!opts)                throw new Error('[ReplayEngine] opts required');
        if (!opts.workspaceKind)  throw new Error('[ReplayEngine] opts.workspaceKind required');
        if (!opts.workspaceId)    throw new Error('[ReplayEngine] opts.workspaceId required');
        if (!opts.recorder)       throw new Error('[ReplayEngine] opts.recorder required');
        if (typeof opts.fold !== 'function') {
            throw new Error('[ReplayEngine] opts.fold (function) required');
        }
        if (typeof opts.initialState !== 'function') {
            throw new Error('[ReplayEngine] opts.initialState (function) required');
        }
        const self = this;

        // Fence ON synchronously. Held through the WHOLE boot — projection
        // (caller's responsibility, post-resolve) runs while the fence is
        // still down, so any emits from spawn-path side effects drop
        // silently. Caller MUST flip the fence off (recorder.setReplaying(false))
        // when projection completes.
        try { opts.recorder.setReplaying(true); }
        catch (e) { console.error('[ReplayEngine] setReplaying threw:', e); }

        // ── Step 1+2: checkpoint read + decode ──────────────────────────
        const cpPromise = (opts.checkpointStore && typeof opts.checkpointStore.read === 'function')
            ? opts.checkpointStore.read(opts.workspaceKind, opts.workspaceId)
                  .catch(function (e) {
                      console.warn('[ReplayEngine] checkpoint read failed '
                                 + '— treating as no checkpoint:',
                          e && e.message ? e.message : e);
                      return null;
                  })
            : Promise.resolve(null);

        return cpPromise.then(function (cpRow) {
            let restoredFromCheckpoint = false;
            let state = null;
            if (cpRow && typeof opts.decodeCheckpoint === 'function') {
                try {
                    state = opts.decodeCheckpoint(cpRow);
                    if (state != null) restoredFromCheckpoint = true;
                } catch (e) {
                    console.warn('[ReplayEngine] decodeCheckpoint threw '
                               + '— starting from fresh state:',
                        e && e.message ? e.message : e);
                    state = null;
                }
            }
            if (state == null) state = opts.initialState();
            const fromSeq = (restoredFromCheckpoint
                          && cpRow && typeof cpRow.lastEventSeq === 'number')
                          ? cpRow.lastEventSeq : 0;

            // ── Step 3: event log query ─────────────────────────────────
            const logPromise = (opts.eventLog && typeof opts.eventLog.query === 'function')
                ? opts.eventLog.query()
                      .catch(function (e) {
                          console.warn('[ReplayEngine] event log query failed '
                                     + '— empty queue:',
                              e && e.message ? e.message : e);
                          return [];
                      })
                : Promise.resolve([]);

            return logPromise.then(function (rows) {
                // Filter: skip events at or below checkpoint cutoff.
                const queue = [];
                for (const row of (rows || [])) {
                    if (typeof row.seq === 'number' && row.seq <= fromSeq) continue;
                    queue.push(row);
                }
                console.log('[ReplayEngine] reconstructing:',
                            queue.length, 'event(s) of', (rows || []).length,
                            'recorded; cp seq', fromSeq,
                            '; checkpoint-seeded:', restoredFromCheckpoint);

                // ── Step 6: empty queue + no checkpoint → onEmpty ──────
                let fellThroughToEmpty = false;
                if (queue.length === 0 && !restoredFromCheckpoint) {
                    fellThroughToEmpty = true;
                    if (typeof opts.onEmpty === 'function') {
                        try { state = opts.onEmpty(state); }
                        catch (e) {
                            console.warn('[ReplayEngine] onEmpty threw:',
                                e && e.message ? e.message : e);
                        }
                    }
                    return {
                        finalState:             state,
                        replayed:               0,
                        lastSeq:                fromSeq,
                        restoredFromCheckpoint: false,
                        fellThroughToEmpty:     true
                    };
                }

                // ── Step 5: fold events into state ─────────────────────
                let lastSeq = fromSeq;
                const onProgress = (typeof opts.onProgress === 'function')
                                 ? opts.onProgress : null;
                for (let i = 0; i < queue.length; i++) {
                    const ev = queue[i];
                    try {
                        state = opts.fold(state, ev);
                        if (typeof ev.seq === 'number') lastSeq = ev.seq;
                    } catch (e) {
                        console.warn('[ReplayEngine] fold threw on', ev.name, ':',
                            e && e.message ? e.message : e);
                    }
                    if (onProgress) {
                        try { onProgress({ idx: i + 1, total: queue.length,
                                           name: ev.name, seq: ev.seq }); }
                        catch (e) { console.warn('[ReplayEngine] onProgress threw:', e); }
                    }
                }
                return {
                    finalState:             state,
                    replayed:               queue.length,
                    lastSeq:                lastSeq,
                    restoredFromCheckpoint: restoredFromCheckpoint,
                    fellThroughToEmpty:     false
                };
            });
        });
    }
}

ReplayEngine.INSTANCE = new ReplayEngine();
