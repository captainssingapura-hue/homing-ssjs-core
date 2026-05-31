// =============================================================================
// EventEmitter — Phase 6 of the workspace-shell chrome.
//
// EventEmitter is the factory (holds substrate collaborators); attach()
// produces an EventRecorder that downstream callsites use.
//
// Boot sequence:
//   1. EventEmitter.attach({workspaceKind, workspaceId, workspaceName})
//   2. WorkspaceEventLog.attach forms a (kind, id)-scoped event log
//   3. Emit SessionStarted boot event directly to the log (fences not
//      yet in place — this is the FIRST event of the session)
//   4. Return EventRecorder wrapping the log
//
// EventRecorder.emit(name, payload) — fenced:
//   if replaying          → drop (replay must not pollute its source log)
//   if !writeLockHeld     → drop (read-only mode; RFC 0031 V2.1)
//   if !log               → drop (attach failed; recording disabled)
//   else log.emit().then(seq => track + onAfterEmit(seq))
//
// Fence flippers are exposed for later phases to wire:
//   setReplaying(bool)         — Phase 9 ReplayEngine
//   setWriteLockHeld(bool)     — Phase 7 WriteLockGuard
//   setOnAfterEmit(fn)         — Phase 8 CheckpointService (cadence trigger)
//
// State accessors for Phase 8:
//   lastEmittedSeq()              — stamped into next checkpoint
//   eventsSinceLastCheckpoint()   — cadence counter
//   resetCheckpointCadence()      — call after a checkpoint write
//
// Explicit Substrate doctrine:
//   - Instance methods on INSTANCE singleton (no static).
//   - No async sugar — naive Promise chains.
//   - ALL collaborators in constructor: _workspaceEventLog, _clock,
//     _RecorderCtor. Per-call attach() receives only data.
//
// Instance fields (graph-traversable):
//   EventEmitter:
//     _workspaceEventLog : WorkspaceEventLog class (with .attach static)
//     _clock             : () => number
//     _RecorderCtor      : EventRecorder class
//   EventRecorder:
//     _log               : event-log instance | null
//     _onAfterEmit       : (seq) => void
// =============================================================================

class EventRecorder {

    constructor(opts) {
        opts = opts || {};
        this._log         = opts.log || null;
        this._onAfterEmit = opts.onAfterEmit || function (_seq) {};
        this._lastEmittedSeq           = 0;
        this._eventsSinceLastCheckpoint = 0;
        // Default open — Phase 7 (WriteLockGuard) flips this off when a
        // sibling window holds the lock. Until Phase 7 lands, recording
        // proceeds normally so V2 can be exercised end-to-end.
        this._writeLockHeld = true;
        this._replaying     = false;
    }

    /**
     * Fenced emit. Returns Promise<seq|null> — null when fenced; the
     * seq the event log assigned otherwise. Never throws on its own;
     * propagates only what the underlying log rejects with.
     */
    emit(name, payload) {
        if (this._replaying)     return Promise.resolve(null);
        if (!this._writeLockHeld) return Promise.resolve(null);
        if (!this._log)           return Promise.resolve(null);
        const self = this;
        return this._log.emit(name, payload || {}).then(function (seq) {
            if (typeof seq === 'number' && seq > self._lastEmittedSeq) {
                self._lastEmittedSeq = seq;
            }
            self._eventsSinceLastCheckpoint++;
            try { self._onAfterEmit(seq); }
            catch (e) {
                console.error('[EventRecorder] onAfterEmit threw:', e);
            }
            return seq;
        });
    }

    /** Phase 9 — replay engine flips this on while walking the log. */
    setReplaying(b) { this._replaying = !!b; }

    /** Phase 7 — write-lock guard flips when lock acquired/lost. */
    setWriteLockHeld(b) { this._writeLockHeld = !!b; }

    /** Phase 8 — cadence trigger; called after each successful emit. */
    setOnAfterEmit(fn) { this._onAfterEmit = fn || function (_seq) {}; }

    /** Exposed for Phase 9 ReplayEngine — query() walks the same log
     *  the recorder emits to. Returns null if attach failed. */
    log() { return this._log; }

    lastEmittedSeq()             { return this._lastEmittedSeq; }
    eventsSinceLastCheckpoint()  { return this._eventsSinceLastCheckpoint; }
    /** Phase 8 — called after a checkpoint write captures everything up
     *  to lastEmittedSeq. Resets the cadence counter. */
    resetCheckpointCadence()     { this._eventsSinceLastCheckpoint = 0; }

    /** Inspect-able state (Diligent Secretaries pillar 2). */
    inspect() {
        return {
            hasLog:                     !!this._log,
            replaying:                  this._replaying,
            writeLockHeld:              this._writeLockHeld,
            lastEmittedSeq:             this._lastEmittedSeq,
            eventsSinceLastCheckpoint:  this._eventsSinceLastCheckpoint
        };
    }
}

class EventEmitter {

    constructor(deps) {
        deps = deps || {};
        // WorkspaceEventLog is a class with a static .attach. Hold the
        // class as the dep so tests can swap it for a stub. typeof guard
        // keeps INSTANCE construction at module load safe in test envs
        // that don't pre-load the real log.
        this._workspaceEventLog = deps.workspaceEventLog
            || (typeof WorkspaceEventLog !== 'undefined' ? WorkspaceEventLog : null);
        this._clock         = deps.clock || function () { return Date.now(); };
        this._RecorderCtor  = deps.RecorderCtor || EventRecorder;
    }

    /**
     * Attach an event log scoped to (workspaceKind, workspaceId) and
     * emit the SessionStarted boot event. Returns an EventRecorder —
     * with .log=null if attach failed, so downstream callsites can
     * always recorder.emit() unconditionally.
     */
    attach(opts) {
        if (!opts)                throw new Error('[EventEmitter] opts required');
        if (!opts.workspaceKind)  throw new Error('[EventEmitter] opts.workspaceKind required');
        if (!opts.workspaceId)    throw new Error('[EventEmitter] opts.workspaceId required');
        if (!opts.workspaceName)  throw new Error('[EventEmitter] opts.workspaceName required');

        let log = null;
        if (this._workspaceEventLog && typeof this._workspaceEventLog.attach === 'function') {
            try {
                log = this._workspaceEventLog.attach({
                    workspaceKind: opts.workspaceKind,
                    workspaceId:   opts.workspaceId
                });
            } catch (e) {
                console.warn('[EventEmitter] event log attach failed '
                           + '(recording disabled):',
                    e && e.message ? e.message : e);
                log = null;
            }
        } else {
            console.warn('[EventEmitter] no WorkspaceEventLog available '
                       + '— recording disabled');
        }

        const recorder = new this._RecorderCtor({ log: log });

        // SessionStarted — boot event, emitted directly via the log so
        // it lands BEFORE any fence the recorder might apply later. V1
        // fires-and-forgets this (no .then-tracking of its seq); we
        // mirror that — boot is its own beat, not part of the normal
        // emit cadence.
        if (log) {
            try {
                log.emit('SessionStarted', {
                    href:          (typeof location !== 'undefined') ? location.href : null,
                    restored:      false,
                    startedAt:     this._clock(),
                    workspaceName: opts.workspaceName
                });
            } catch (e) {
                console.warn('[EventEmitter] SessionStarted emit failed:',
                    e && e.message ? e.message : e);
            }
        }
        return recorder;
    }
}

EventEmitter.INSTANCE = new EventEmitter();
