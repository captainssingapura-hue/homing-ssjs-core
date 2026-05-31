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
     * Delete events for a workspace instance with seq ≤ upToSeq. Resolves
     * to the count deleted. Used by RFC 0034 P3 post-checkpoint pruning:
     * once a checkpoint captures every event up to seq N, those events are
     * redundant and can be dropped from the log to keep replay fast.
     */
    prune(kind, workspaceId, upToSeq) {
        EventLogStore._requireScope(kind, workspaceId);
        if (typeof upToSeq !== "number" || !isFinite(upToSeq) || upToSeq < 0) {
            throw new TypeError("EventLogStore.prune: upToSeq must be non-negative finite number");
        }
        return this._db().then(db => new Promise((resolve, reject) => {
            let count = 0;
            const tx    = db.transaction(EventLogStore.STORE_NAME, "readwrite");
            const store = tx.objectStore(EventLogStore.STORE_NAME);
            const idx   = store.index("by_workspace");
            const lower = [kind, workspaceId, Number.MIN_SAFE_INTEGER];
            const upper = [kind, workspaceId, upToSeq];
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
// =============================================================================
// WorkspaceEventLog — per-workspace facade over EventLogStore.
//
// RFC 0035 P2 — refactored from `Object.freeze({attach: ...})` IIFE to class
// form. The class IS the substrate-free EventLog<WorkspaceEventPayload>
// contract from the Java contract package — bound to one (kind, workspaceId)
// pair at construction time, exposes append / query / clear as the public
// method set.
//
// Per-workspace scoping per RFC 0031 multi-workspace, the (kind, workspaceId)
// pair keys every IDB row. Single-workspace use today passes a deterministic
// placeholder UUID (see WorkspaceInstanceId.placeholderFor on the Java side).
//
// Usage in a chrome's bodyJs():
//
//   const eventLog = new WorkspaceEventLog({
//       workspaceKind: "AnimalsPlayground",
//       workspaceId:   "<deterministic-placeholder-uuid>"
//   });
//   eventLog.append("TabAdded", { paneId: "tl", tabId: "...", widgetKind: "..." });
//
// Phase 1 record-only — errors during recording land in console.warn and
// the live UI never blocks on storage.
// =============================================================================

class WorkspaceEventLog {

    constructor(opts) {
        if (!opts || typeof opts.workspaceKind !== "string" || opts.workspaceKind.length === 0) {
            throw new TypeError("WorkspaceEventLog: opts.workspaceKind required (non-empty string)");
        }
        if (typeof opts.workspaceId !== "string" || opts.workspaceId.length === 0) {
            throw new TypeError("WorkspaceEventLog: opts.workspaceId required (non-empty string)");
        }
        this.workspaceKind = opts.workspaceKind;
        this.workspaceId   = opts.workspaceId;
        this._store        = opts.store || createEventLogStore();

        // Monotonic local counter for the session — useful for ordering
        // events emitted in the same millisecond. The authoritative seq
        // comes back from IndexedDB; this is just for in-session tracing.
        this._localCount = 0;
    }

    /**
     * Append one event. Returns a Promise that resolves to the assigned
     * seq number. Fire-and-forget on the recording path; the promise is
     * there for tests that want the seq + chrome callsites that need it
     * for cadence tracking (RFC 0034).
     *
     * Conforms to Java EventLog<P>.append(EventName, P): the JS arguments
     * mirror the contract's parameter order. EventName arrives as a string
     * (the typed payload's NAME constant on the Java side; the JS author
     * holds the same convention).
     */
    append(name, payload) {
        this._localCount++;
        return this._store.append(this.workspaceKind, this.workspaceId, name, payload || {})
                   .catch(e => {
                       console.warn("[WorkspaceEventLog] append failed for", name, "—",
                                    e && e.message ? e.message : e);
                       throw e;
                   });
    }

    /**
     * Range-query this workspace's events. Optional { fromSeq, limit }.
     * Conforms to Java EventLog<P>.query(EventQuery) with the EventQuery
     * record's fields passed as a plain object.
     */
    query(opts) {
        return this._store.query(this.workspaceKind, this.workspaceId, opts);
    }

    /**
     * Drop every event for this workspace. Used by Reset State.
     * Resolves to the count deleted.
     */
    /** Delete events with seq ≤ upToSeq. See EventLogStore.prune. */
    prune(upToSeq) {
        return this._store.prune(this.workspaceKind, this.workspaceId, upToSeq);
    }

    clear() {
        return this._store.clear(this.workspaceKind, this.workspaceId);
    }

    /** Diagnostics: how many events have been append()ed this session. */
    localEmitCount() { return this._localCount; }

    // ─── Legacy emit() alias ─────────────────────────────────────────────
    // Pre-RFC-0035 chromes called eventLog.emit(name, payload). The method
    // is identical to append(); kept until all chromes migrate to the
    // contract-aligned name.

    /** @deprecated use {@link append}. */
    emit(name, payload) { return this.append(name, payload); }

    /**
     * Legacy {@code WorkspaceEventLog.attach({...})} call shape preserved
     * as a static factory. Pre-RFC-0035 chromes used the IIFE form; this
     * static lets them keep working unchanged. New code uses
     * {@code new WorkspaceEventLog(opts)} directly.
     *
     * @deprecated use {@code new WorkspaceEventLog(opts)}.
     */
    static attach(opts) { return new WorkspaceEventLog(opts); }
}
