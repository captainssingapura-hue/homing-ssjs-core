// =============================================================================
// PersistenceAttacher — Phase 4 of the workspace-shell chrome.
//
// Thin wrapper around WorkspaceStatePersistence.attach. Returns the
// constructed persistence layer (or null on failure, logged).
//
// Explicit Substrate doctrine:
//   - Instance methods on INSTANCE singleton (no static).
//   - attach is SYNC.
//   - ALL collaborators in constructor: _persistence.
//
// Instance fields (graph-traversable):
//   _persistence : WorkspaceStatePersistence — defaults to the global
// =============================================================================

class PersistenceAttacher {

    constructor(deps) {
        deps = deps || {};
        this._persistence = deps.persistence || WorkspaceStatePersistence;
    }

    /**
     * Attach the persistence layer for a workspace kind. Returns the
     * layer (whose .store + .workspaceKind props serve later phases),
     * or null if attach throws (logged).
     *
     * Per-call data only: workspaceKind, storage. Collaborator
     * (_persistence) is fixed at construct time.
     */
    attach(opts) {
        if (!opts || !opts.workspaceKind) {
            throw new Error('[PersistenceAttacher] opts.workspaceKind required');
        }
        const storage = (opts.storage !== undefined)
                      ? opts.storage
                      : (typeof window !== 'undefined' && window.localStorage)
                            ? window.localStorage
                            : null;
        if (!storage) {
            console.warn('[PersistenceAttacher] no storage available '
                       + '(no window.localStorage, none supplied) — skipping attach.');
            return null;
        }
        try {
            return this._persistence.attach({
                workspaceKind: opts.workspaceKind,
                storage:       storage,
                paramsCodecs:  {}    // codecs registered separately by CodecRegistrar
            });
        } catch (err) {
            console.error('[PersistenceAttacher] attach failed for kind "'
                        + opts.workspaceKind + '":', err);
            return null;
        }
    }
}

PersistenceAttacher.INSTANCE = new PersistenceAttacher();
