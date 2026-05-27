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
