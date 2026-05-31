// =============================================================================
// LocalStorageStore — JS-side implementation of the WorkspaceStateStore SPI
// for the browser localStorage backend.
//
// RFC 0029 Cycle 4 (real-world test path).
//
// Factory-shaped so the storage backing is a parameter: production code
// passes `window.localStorage`; tests pass a mock that satisfies the same
// shape (length, key(i), getItem, setItem, removeItem, clear).
//
// Operates on wire-form data (typed-codec output), not on typed values
// directly. Caller responsibility:
//
//   const wire = WorkspaceStateCodec.transformTo(typedState);
//   store.save(typedState.workspaceKind.value, wire);
//   ...
//   const wire     = store.load("AnimalsPlayground");
//   const restored = wire ? WorkspaceStateCodec.transformFrom(wire) : null;
//
// Keeps the store generic and the codec concern cleanly separated — per
// the "Codec is the pair" ontology decision (no third wrapper).
//
// State Belongs to the User doctrine:  isRemote() returns false. The
// framework refuses to ship a remote-store implementation; this file is
// the local-default reference. Downstream studios that want server-sync
// plug their own implementation; framework code never goes remote.
// =============================================================================

function createLocalStorageStore(storage) {
    if (!storage || typeof storage.getItem !== 'function') {
        throw new TypeError(
            "createLocalStorageStore: storage parameter must satisfy the localStorage interface");
    }

    // All workspace state keys share this prefix so listSavedKinds can scan
    // without interfering with other localStorage uses on the same origin.
    const KEY_PREFIX = "homing.workspace.";

    function keyFor(workspaceKind) {
        if (typeof workspaceKind !== 'string' || workspaceKind.length === 0) {
            throw new TypeError(
                "LocalStorageStore: workspaceKind must be a non-empty string");
        }
        return KEY_PREFIX + workspaceKind;
    }

    return Object.freeze({
        /**
         * Persist the wire-form state under the given workspaceKind.
         * Replaces any prior state. Idempotent — saving the same wire
         * form twice is structurally equivalent to saving it once.
         */
        save(workspaceKind, wire) {
            if (wire === undefined) {
                throw new TypeError("LocalStorageStore.save: wire must not be undefined");
            }
            storage.setItem(keyFor(workspaceKind), JSON.stringify(wire));
        },

        /**
         * Load the wire-form state for the given workspaceKind, if any.
         * Returns `null` when no prior state exists (first run, after
         * clear, after export-import for a different kind).
         */
        load(workspaceKind) {
            const raw = storage.getItem(keyFor(workspaceKind));
            return raw == null ? null : JSON.parse(raw);
        },

        /**
         * Forget the saved state for the given workspaceKind. Subsequent
         * load returns null. Used by "reset workspace" affordances.
         */
        clear(workspaceKind) {
            storage.removeItem(keyFor(workspaceKind));
        },

        /**
         * The workspaceKinds with saved state currently in this store.
         * Order is the storage's native iteration order (not sorted).
         * Used by future "pick a workspace to restore" UIs.
         */
        listSavedKinds() {
            const out = [];
            for (let i = 0; i < storage.length; i++) {
                const k = storage.key(i);
                if (k && k.indexOf(KEY_PREFIX) === 0) {
                    out.push(k.substring(KEY_PREFIX.length));
                }
            }
            return out;
        },

        /**
         * Always false for LocalStorageStore — the State Belongs to the
         * User doctrine binds the framework to local-only defaults. The
         * workspace shell uses this to decide whether to render the
         * "state leaving device" banner.
         */
        isRemote() {
            return false;
        }
    });
}
// =============================================================================
// captureLiveWorkspace — view → WorkspaceState helper for RFC 0029.
//
// Takes an abstract "workspace view" object that exposes the four
// axes of workspace state (kind / layout / widgets / chrome), and
// constructs a typed WorkspaceState. The view's shape is the contract
// the actual workspace shell implements at integration time; the
// capture function itself doesn't know how the view's data is sourced
// (DOM walk, internal cache, computed property — all opaque).
//
// Per the RFC 0029 design pass: the three orthogonal axes are captured
// via the same single call here, with the workspace shell responsible
// for keeping its own per-axis caches up to date as events fire. The
// shell calls captureLiveWorkspace(view) → encode → persister.save just
// when the debounced save timer fires; bursts of axis-specific updates
// coalesce into one composite read.
// =============================================================================

function captureLiveWorkspace(view) {
    if (!view) {
        throw new TypeError("captureLiveWorkspace: view is required");
    }
    if (typeof view.workspaceKind !== 'function') {
        throw new TypeError("captureLiveWorkspace: view.workspaceKind() is required");
    }
    if (typeof view.layout !== 'function') {
        throw new TypeError("captureLiveWorkspace: view.layout() is required");
    }
    if (typeof view.widgets !== 'function') {
        throw new TypeError("captureLiveWorkspace: view.widgets() is required");
    }
    if (typeof view.chrome !== 'function') {
        throw new TypeError("captureLiveWorkspace: view.chrome() is required");
    }

    const widgetsById = new Map();
    for (const w of view.widgets()) {
        if (!(w instanceof WidgetInstance)) {
            throw new TypeError(
                "captureLiveWorkspace: view.widgets() must yield WidgetInstance values");
        }
        widgetsById.set(w.id, w);
    }

    return new WorkspaceState(
        WorkspaceState.CURRENT_SCHEMA_VERSION,
        view.workspaceKind(),
        new Date(),                  // wall-clock capture timestamp
        view.layout(),
        widgetsById,
        view.chrome()
    );
}
// =============================================================================
// WorkspaceStatePersister — debounced auto-save coordinator for RFC 0029.
//
// Factory-shaped. The persister itself owns nothing but a pending-timer
// reference and the debounce wiring; the saveFn (what actually happens
// at save time — capture → encode → store.save) is injected, and the
// scheduler is injected too so tests can advance time deterministically.
//
// Two entry points the workspace shell calls into:
//
//   triggerSave()  — debounced. Called by every state-affecting event
//                    (SplitPane resize/split/merge, MTP tab add/close/
//                    move/switch, widget param notify, ribbon toggles).
//                    Bursts coalesce; the actual save fires only once
//                    after the debounce window settles.
//
//   forceSave()    — immediate. Called by the Force Save ribbon control
//                    and (in future) by the page-unload handler. Cancels
//                    any pending debounced save and runs the saveFn now.
//
// The persister returns isPending() so the UI (or a status pill) can
// surface whether unsaved changes exist; useful for both diagnostics and
// the "you have unsaved changes" prompt on tab close.
//
// Doctrine alignment:
//   - Functional Object — the returned object is stateless from the
//     caller's view; bursts of triggerSave coalesce naturally.
//   - CodeGen as Functions — this module is hand-written; an automated
//     generator could equally produce it, conforming to the same
//     interface. Tests don't know which.
// =============================================================================

function createWorkspaceStatePersister(opts) {
    if (!opts || typeof opts.saveFn !== 'function') {
        throw new TypeError(
            "createWorkspaceStatePersister: opts.saveFn (function) is required");
    }
    if (!opts.scheduler
        || typeof opts.scheduler.schedule !== 'function'
        || typeof opts.scheduler.cancel !== 'function') {
        throw new TypeError(
            "createWorkspaceStatePersister: opts.scheduler must have schedule() + cancel()");
    }

    const debounceMs = opts.debounceMs ?? 500;
    const saveFn     = opts.saveFn;
    const scheduler  = opts.scheduler;

    let pendingTicket = null;
    let lastError     = null;

    /** Run the saveFn now, swallowing the pending debounce timer (if any). */
    function forceSave() {
        if (pendingTicket !== null) {
            scheduler.cancel(pendingTicket);
            pendingTicket = null;
        }
        try {
            saveFn();
            lastError = null;
        } catch (e) {
            lastError = e;
            throw e;
        }
    }

    /** Schedule a save for {@code debounceMs} from now. Bursts coalesce. */
    function triggerSave() {
        if (pendingTicket !== null) {
            scheduler.cancel(pendingTicket);
        }
        pendingTicket = scheduler.schedule(() => {
            pendingTicket = null;
            try {
                saveFn();
                lastError = null;
            } catch (e) {
                lastError = e;
                // Don't re-throw — async callback; the error survives in lastError
                // and via the scheduler's own error path (which is platform-specific).
            }
        }, debounceMs);
    }

    return Object.freeze({
        triggerSave,
        forceSave,
        isPending() { return pendingTicket !== null; },
        get lastError() { return lastError; }
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// Production scheduler — wraps browser setTimeout / clearTimeout. Importing
// modules in browsers use this; tests provide their own scheduler.
// ─────────────────────────────────────────────────────────────────────────────

const browserScheduler = Object.freeze({
    schedule(fn, delayMs) { return setTimeout(fn, delayMs); },
    cancel(ticket)        { clearTimeout(ticket); }
});
// =============================================================================
// WorkspaceStatePersistence — high-level attach API for downstream chromes.
//
// One call. Returns a { create, tryRestore } handle the chrome uses to
// instantiate persisters and to restore prior state at boot. Hides the
// store + scheduler wiring; chromes only need to provide:
//
//   - workspaceKind  — the WorkspaceKind value (string) used as the
//                      store key
//   - storage        — backing storage (window.localStorage in browsers,
//                      a mock in tests); must satisfy the localStorage
//                      interface
//   - paramsCodecs   — { [widgetKindString]: TransformationFunctions }
//                      map; the chrome registers its widget kinds'
//                      Params codecs here so WidgetInstance can encode/
//                      decode polymorphically
//   - scheduler      — optional; defaults to browserScheduler
//   - debounceMs     — optional; defaults to 500
//
// Then:
//
//   const persister = persistenceLayer.create(captureFn);
//   persister.triggerSave();   // on any state-affecting event
//   persister.forceSave();     // on a Force Save button
//
//   const restored = persistenceLayer.tryRestore();
//   if (restored) { /* rebuild live workspace from restored */ }
//
// Doctrine alignment:
//   - State Belongs to the User — the facade only accepts a storage
//     backing the caller provides; never reaches for window globals
//     itself. Tests pass mock storage; production passes
//     window.localStorage. Future server stores plug here per the SPI.
//   - CodeGen as Functions — paramsCodecs are Functional Object pairs;
//     provenance (hand-written or generated) is invisible at this API.
// =============================================================================

const WorkspaceStatePersistence = Object.freeze({

    attach(opts) {
        if (!opts || typeof opts.workspaceKind !== 'string' || opts.workspaceKind.length === 0) {
            throw new TypeError("WorkspaceStatePersistence.attach: opts.workspaceKind required (non-empty string)");
        }
        if (!opts.storage || typeof opts.storage.getItem !== 'function') {
            throw new TypeError("WorkspaceStatePersistence.attach: opts.storage must satisfy the localStorage interface");
        }

        // Register every supplied widget-kind Params codec. The registry
        // is global within the JS context; multiple workspaces sharing the
        // same context (rare but legal) share the same registry.
        const paramsCodecs = opts.paramsCodecs || {};
        for (const widgetKindStr of Object.keys(paramsCodecs)) {
            WidgetParamsCodecRegistry.register(widgetKindStr, paramsCodecs[widgetKindStr]);
        }

        const store      = createLocalStorageStore(opts.storage);
        const scheduler  = opts.scheduler || (typeof browserScheduler !== 'undefined' ? browserScheduler : null);
        if (!scheduler) {
            throw new TypeError("WorkspaceStatePersistence.attach: scheduler required (no browserScheduler available)");
        }
        const debounceMs = opts.debounceMs ?? 500;
        const workspaceKindStr = opts.workspaceKind;

        return Object.freeze({
            /**
             * Build a persister for the given captureFn. The captureFn
             * runs at save time, walks the live workspace, and returns
             * a typed WorkspaceState (or null to skip the save).
             */
            create(captureFn) {
                if (typeof captureFn !== 'function') {
                    throw new TypeError("persistenceLayer.create: captureFn must be a function");
                }
                const saveFn = () => {
                    const state = captureFn();
                    if (state == null) return;
                    const wire = WorkspaceStateCodec.transformTo(state);
                    store.save(workspaceKindStr, wire);
                };
                return createWorkspaceStatePersister({ saveFn, scheduler, debounceMs });
            },

            /**
             * Attempt to restore prior workspace state, if any. Returns
             * the typed WorkspaceState, or null when no saved state
             * exists. Decoding throws if the saved wire form fails any
             * invariant or codec validation — the caller decides whether
             * to handle that or let it propagate.
             */
            tryRestore() {
                const wire = store.load(workspaceKindStr);
                return wire ? WorkspaceStateCodec.transformFrom(wire) : null;
            },

            /** Direct store access for clear/listSavedKinds/isRemote semantics. */
            store,

            /** The workspaceKind string the facade was attached for. */
            workspaceKind: workspaceKindStr
        });
    }
});
