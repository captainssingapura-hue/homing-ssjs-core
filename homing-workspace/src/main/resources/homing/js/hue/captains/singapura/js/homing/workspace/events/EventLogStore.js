// =============================================================================
// EventLogStore — IndexedDB wrapper for the per-workspace event log.
//
// RFC 0030 Phase 1. Record-only — events are appended, never replayed yet.
// Recovery still uses RFC 0029 snapshots through Phase 2.
//
// Schema:
//   database name : "homing.eventlog"
//   object store  : "events"  — auto-increment integer "seq" key
//                   each row: { seq, kind, workspaceId, name, payload, t }
//                   indexed by composite key (kind, workspaceId, seq)
//
// Per-workspace scoping — every API call carries (kind, workspaceId) so a
// single store can hold events for many workspace instances without bleed.
// Matches the RFC 0031 multi-workspace storage shape from day one; single-
// workspace use today simply passes the deterministic placeholder UUID the
// chrome computes for its (single) instance.
//
// Async by design — IndexedDB has no synchronous API. Callers either await
// the promises or fire-and-forget; the recording path uses fire-and-forget
// (we don't block widget mutations on storage writes). Errors are logged
// via console.warn; we never throw out of append().
// =============================================================================

const _DB_NAME      = "homing.eventlog";
const _STORE_NAME   = "events";
const _DB_VERSION   = 1;

function _openDb() {
    return new Promise(function (resolve, reject) {
        if (typeof indexedDB === "undefined") {
            reject(new Error("EventLogStore: IndexedDB not available in this environment"));
            return;
        }
        const req = indexedDB.open(_DB_NAME, _DB_VERSION);
        req.onupgradeneeded = function (e) {
            const db = e.target.result;
            if (!db.objectStoreNames.contains(_STORE_NAME)) {
                const store = db.createObjectStore(_STORE_NAME, { keyPath: "seq", autoIncrement: true });
                // Composite index for per-workspace range queries.
                store.createIndex("by_workspace", ["kind", "workspaceId", "seq"], { unique: true });
            }
        };
        req.onsuccess = function (e) { resolve(e.target.result); };
        req.onerror   = function (e) { reject(e.target.error); };
    });
}

function createEventLogStore() {
    let _dbPromise = null;
    function _db() {
        if (_dbPromise === null) _dbPromise = _openDb();
        return _dbPromise;
    }

    return Object.freeze({
        /**
         * Append one event. Returns a Promise that resolves to the assigned
         * seq number. Fire-and-forget is the expected call style on the
         * recording path; the promise is there for tests + diagnostics.
         */
        append(kind, workspaceId, name, payload) {
            if (typeof kind !== "string" || kind.length === 0) {
                return Promise.reject(new TypeError("EventLogStore.append: kind must be non-empty string"));
            }
            if (typeof workspaceId !== "string" || workspaceId.length === 0) {
                return Promise.reject(new TypeError("EventLogStore.append: workspaceId must be non-empty string"));
            }
            if (typeof name !== "string" || name.length === 0) {
                return Promise.reject(new TypeError("EventLogStore.append: name must be non-empty string"));
            }
            const row = {
                kind:        kind,
                workspaceId: workspaceId,
                name:        name,
                payload:     payload == null ? {} : payload,
                t:           Date.now()
                // seq filled by autoIncrement; available on the request's result
            };
            return _db().then(function (db) {
                return new Promise(function (resolve, reject) {
                    const tx    = db.transaction(_STORE_NAME, "readwrite");
                    const store = tx.objectStore(_STORE_NAME);
                    const req   = store.add(row);
                    req.onsuccess = function () { resolve(req.result); };
                    req.onerror   = function () { reject(req.error); };
                });
            });
        },

        /**
         * Range-query events for one workspace instance. Optional options:
         *   { fromSeq?: number, limit?: number }
         * fromSeq is exclusive (returns events with seq > fromSeq).
         * Returns a Promise<Array> sorted by seq ascending.
         */
        query(kind, workspaceId, opts) {
            const fromSeq = opts && typeof opts.fromSeq === "number" ? opts.fromSeq : 0;
            const limit   = opts && typeof opts.limit   === "number" ? opts.limit   : Number.POSITIVE_INFINITY;
            return _db().then(function (db) {
                return new Promise(function (resolve, reject) {
                    const out   = [];
                    const tx    = db.transaction(_STORE_NAME, "readonly");
                    const store = tx.objectStore(_STORE_NAME);
                    // We use the composite index but bounded by exact (kind,
                    // workspaceId) and seq > fromSeq. IDBKeyRange.bound on a
                    // composite key gives us this directly.
                    const idx   = store.index("by_workspace");
                    const lower = [kind, workspaceId, fromSeq + 1];
                    const upper = [kind, workspaceId, Number.MAX_SAFE_INTEGER];
                    const range = IDBKeyRange.bound(lower, upper, false, false);
                    const req   = idx.openCursor(range);
                    req.onsuccess = function (e) {
                        const cursor = e.target.result;
                        if (cursor && out.length < limit) {
                            out.push(cursor.value);
                            cursor.continue();
                        } else {
                            resolve(out);
                        }
                    };
                    req.onerror = function () { reject(req.error); };
                });
            });
        },

        /**
         * Drop every event for a workspace instance. Used by reset / migration
         * affordances. Returns a Promise<number> resolving to the count
         * deleted.
         */
        clear(kind, workspaceId) {
            return _db().then(function (db) {
                return new Promise(function (resolve, reject) {
                    let count = 0;
                    const tx    = db.transaction(_STORE_NAME, "readwrite");
                    const store = tx.objectStore(_STORE_NAME);
                    const idx   = store.index("by_workspace");
                    const lower = [kind, workspaceId, Number.MIN_SAFE_INTEGER];
                    const upper = [kind, workspaceId, Number.MAX_SAFE_INTEGER];
                    const range = IDBKeyRange.bound(lower, upper, false, false);
                    const req   = idx.openCursor(range);
                    req.onsuccess = function (e) {
                        const cursor = e.target.result;
                        if (cursor) { cursor.delete(); count++; cursor.continue(); }
                        else        { resolve(count); }
                    };
                    req.onerror = function () { reject(req.error); };
                });
            });
        }
    });
}
