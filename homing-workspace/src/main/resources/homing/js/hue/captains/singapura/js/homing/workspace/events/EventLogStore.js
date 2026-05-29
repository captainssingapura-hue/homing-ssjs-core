// =============================================================================
// EventLogStore — IndexedDB substrate for the workspace event log.
//
// RFC 0035 P2 — refactored from top-level-const + factory to class form so
// the constants live as static class fields (no script-scope pollution; no
// _DB_NAME collisions). The public method shape is unchanged; downstream
// callers see the same (kind, workspaceId)-keyed append / query / clear.
//
// Schema:
//   database name : "homing.eventlog"
//   object store  : "events"  — auto-increment integer "seq" key
//                   each row: { seq, kind, workspaceId, name, payload, t }
//                   indexed by composite key (kind, workspaceId, seq)
//
// This class is the LOW-LEVEL multi-tenant store. The per-workspace facade
// is {@link WorkspaceEventLog} (also refactored to class form) which binds
// (kind, workspaceId) at construction and conforms to the Java EventLog<P>
// contract from the RFC 0035 contract package.
//
// Async by design — IndexedDB has no synchronous API. Callers either await
// the promises or fire-and-forget; the recording path uses fire-and-forget.
// =============================================================================

class EventLogStore {

    static DB_NAME    = "homing.eventlog";
    static STORE_NAME = "events";
    static DB_VERSION = 1;

    constructor() {
        this._dbPromise = null;
    }

    /** Lazy-opened singleton IDB connection for this instance. */
    _db() {
        if (this._dbPromise === null) this._dbPromise = this._openDb();
        return this._dbPromise;
    }

    _openDb() {
        return new Promise((resolve, reject) => {
            if (typeof indexedDB === "undefined") {
                reject(new Error("EventLogStore: IndexedDB not available in this environment"));
                return;
            }
            const req = indexedDB.open(EventLogStore.DB_NAME, EventLogStore.DB_VERSION);
            req.onupgradeneeded = (e) => {
                const db = e.target.result;
                if (!db.objectStoreNames.contains(EventLogStore.STORE_NAME)) {
                    const store = db.createObjectStore(EventLogStore.STORE_NAME, { keyPath: "seq", autoIncrement: true });
                    // Composite index for per-workspace range queries.
                    store.createIndex("by_workspace", ["kind", "workspaceId", "seq"], { unique: true });
                }
            };
            req.onsuccess = (e) => resolve(e.target.result);
            req.onerror   = (e) => reject(e.target.error);
        });
    }

    /** Run `fn(store)` inside a transaction of the named mode. */
    _tx(mode, fn) {
        return this._db().then(db => new Promise((resolve, reject) => {
            const tx    = db.transaction(EventLogStore.STORE_NAME, mode);
            const store = tx.objectStore(EventLogStore.STORE_NAME);
            const req   = fn(store);
            req.onsuccess = () => resolve(req.result);
            req.onerror   = () => reject(req.error);
        }));
    }

    /**
     * Append one event. Returns a Promise that resolves to the assigned
     * seq number. Fire-and-forget is the expected call style on the
     * recording path; the promise is there for tests + diagnostics.
     */
    append(kind, workspaceId, name, payload) {
        EventLogStore._requireScope(kind, workspaceId);
        if (typeof name !== "string" || name.length === 0) {
            return Promise.reject(new TypeError("EventLogStore.append: name must be non-empty string"));
        }
        const row = {
            kind, workspaceId, name,
            payload: payload == null ? {} : payload,
            t: Date.now()
            // seq filled by autoIncrement; available on the request's result
        };
        return this._tx("readwrite", store => store.add(row));
    }

    /**
     * Range-query events for one workspace instance. Optional options:
     *   { fromSeq?: number, limit?: number }
     * fromSeq is exclusive (returns events with seq > fromSeq).
     */
    query(kind, workspaceId, opts) {
        EventLogStore._requireScope(kind, workspaceId);
        const fromSeq = opts && typeof opts.fromSeq === "number" ? opts.fromSeq : 0;
        const limit   = opts && typeof opts.limit   === "number" ? opts.limit   : Number.POSITIVE_INFINITY;
        return this._db().then(db => new Promise((resolve, reject) => {
            const out   = [];
            const tx    = db.transaction(EventLogStore.STORE_NAME, "readonly");
            const store = tx.objectStore(EventLogStore.STORE_NAME);
            const idx   = store.index("by_workspace");
            const lower = [kind, workspaceId, fromSeq + 1];
            const upper = [kind, workspaceId, Number.MAX_SAFE_INTEGER];
            const range = IDBKeyRange.bound(lower, upper, false, false);
            const req   = idx.openCursor(range);
            req.onsuccess = (e) => {
                const cursor = e.target.result;
                if (cursor && out.length < limit) {
                    out.push(cursor.value);
                    cursor.continue();
                } else {
                    resolve(out);
                }
            };
            req.onerror = () => reject(req.error);
        }));
    }

    /**
     * Drop every event for a workspace instance. Resolves to the count
     * deleted. Used by Reset State + future RFC 0034 P3 pruning.
     */
    clear(kind, workspaceId) {
        EventLogStore._requireScope(kind, workspaceId);
        return this._db().then(db => new Promise((resolve, reject) => {
            let count = 0;
            const tx    = db.transaction(EventLogStore.STORE_NAME, "readwrite");
            const store = tx.objectStore(EventLogStore.STORE_NAME);
            const idx   = store.index("by_workspace");
            const lower = [kind, workspaceId, Number.MIN_SAFE_INTEGER];
            const upper = [kind, workspaceId, Number.MAX_SAFE_INTEGER];
            const range = IDBKeyRange.bound(lower, upper, false, false);
            const req   = idx.openCursor(range);
            req.onsuccess = (e) => {
                const cursor = e.target.result;
                if (cursor) { cursor.delete(); count++; cursor.continue(); }
                else        { resolve(count); }
            };
            req.onerror = () => reject(req.error);
        }));
    }

    static _requireScope(kind, workspaceId) {
        if (typeof kind !== "string" || kind.length === 0) {
            throw new TypeError("EventLogStore: kind must be non-empty string");
        }
        if (typeof workspaceId !== "string" || workspaceId.length === 0) {
            throw new TypeError("EventLogStore: workspaceId must be non-empty string");
        }
    }
}

/**
 * Factory — matches the createX() naming convention so chrome call sites
 * read uniformly. Returns a fresh EventLogStore instance with its own
 * lazy IDB connection.
 */
function createEventLogStore() {
    return new EventLogStore();
}
