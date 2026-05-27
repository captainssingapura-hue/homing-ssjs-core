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
