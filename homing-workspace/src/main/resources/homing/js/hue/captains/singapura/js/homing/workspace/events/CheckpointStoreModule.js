// =============================================================================
// CheckpointStore — IndexedDB wrapper for periodic workspace snapshots.
//
// RFC 0034 P1 — main-thread cadence-driven write. P2 will move the write
// into a Web Worker (LMAX consumer-thread shape); the class API is stable.
//
// Schema:
//   database name : "homing.checkpoints"
//   object store  : "checkpoints" — composite key (kind, workspaceId)
//                   each row: {
//                     kind, workspaceId, lastEventSeq,
//                     capturedAtMs, schemaVersion, state
//                   }
//
// One row per workspace — write replaces previous checkpoint atomically.
// The latest checkpoint is the only one boot needs; older ones are dead
// weight, so we put() over the same key rather than appending history.
//
// Separate database from EventLogStore so each module owns its IDB lifecycle
// (and to keep top-level identifiers from colliding when both bundles ship
// in the same chrome script). P3's cross-store pruning may coalesce later;
// the public surface (write/read/clear, plus SCHEMA_VERSION) stays the same.
// =============================================================================

class CheckpointStore {

    static DB_NAME        = "homing.checkpoints";
    static STORE_NAME     = "checkpoints";
    static DB_VERSION     = 1;
    static SCHEMA_VERSION = 1;

    constructor() {
        this._dbPromise = null;
    }

    /** Expose schema as an instance property so callers don't need the class. */
    get SCHEMA_VERSION() { return CheckpointStore.SCHEMA_VERSION; }

    /** Lazy-opened singleton IDB connection for this instance. */
    _db() {
        if (this._dbPromise === null) this._dbPromise = this._openDb();
        return this._dbPromise;
    }

    _openDb() {
        return new Promise((resolve, reject) => {
            if (typeof indexedDB === "undefined") {
                reject(new Error("CheckpointStore: IndexedDB not available in this environment"));
                return;
            }
            const req = indexedDB.open(CheckpointStore.DB_NAME, CheckpointStore.DB_VERSION);
            req.onupgradeneeded = (e) => {
                const db = e.target.result;
                if (!db.objectStoreNames.contains(CheckpointStore.STORE_NAME)) {
                    db.createObjectStore(CheckpointStore.STORE_NAME, { keyPath: ["kind", "workspaceId"] });
                }
            };
            req.onsuccess = (e) => resolve(e.target.result);
            req.onerror   = (e) => reject(e.target.error);
        });
    }

    /** Run `fn(store)` inside a transaction of the named mode. */
    _tx(mode, fn) {
        return this._db().then(db => new Promise((resolve, reject) => {
            const tx    = db.transaction(CheckpointStore.STORE_NAME, mode);
            const store = tx.objectStore(CheckpointStore.STORE_NAME);
            const req   = fn(store);
            req.onsuccess = () => resolve(req.result);
            req.onerror   = () => reject(req.error);
        }));
    }

    /**
     * Write or replace this workspace's checkpoint. Atomic single-row put;
     * any previous checkpoint at the same (kind, workspaceId) key is
     * overwritten in one transaction.
     */
    write(kind, workspaceId, state, lastEventSeq) {
        CheckpointStore._requireKey(kind, workspaceId);
        if (typeof lastEventSeq !== "number" || !isFinite(lastEventSeq) || lastEventSeq < 0) {
            return Promise.reject(new TypeError("CheckpointStore.write: lastEventSeq must be non-negative finite number"));
        }
        const row = {
            kind, workspaceId, lastEventSeq,
            capturedAtMs:  Date.now(),
            schemaVersion: CheckpointStore.SCHEMA_VERSION,
            state:         state == null ? {} : state
        };
        return this._tx("readwrite", store => store.put(row)).then(() => undefined);
    }

    /**
     * Read this workspace's latest checkpoint, or null if none exists.
     * Caller checks schemaVersion before consuming; mismatched versions
     * are treated as "no checkpoint" so boot falls back to event-log
     * replay from zero.
     */
    read(kind, workspaceId) {
        CheckpointStore._requireKey(kind, workspaceId);
        return this._tx("readonly", store => store.get([kind, workspaceId]))
                   .then(row => row || null);
    }

    /**
     * Delete this workspace's checkpoint. Used by Reset State and as a
     * manual DevTools escape hatch.
     */
    clear(kind, workspaceId) {
        CheckpointStore._requireKey(kind, workspaceId);
        return this._tx("readwrite", store => store.delete([kind, workspaceId]))
                   .then(() => undefined);
    }

    static _requireKey(kind, workspaceId) {
        if (typeof kind !== "string" || kind.length === 0) {
            throw new TypeError("CheckpointStore: kind must be non-empty string");
        }
        if (typeof workspaceId !== "string" || workspaceId.length === 0) {
            throw new TypeError("CheckpointStore: workspaceId must be non-empty string");
        }
    }
}

/**
 * Factory — matches the createEventLogStore() naming convention so chrome
 * call sites read uniformly. Returns a fresh CheckpointStore instance with
 * its own lazy IDB connection.
 */
function createCheckpointStore() {
    return new CheckpointStore();
}
