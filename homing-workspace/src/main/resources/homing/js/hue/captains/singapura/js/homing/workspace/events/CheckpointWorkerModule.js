// =============================================================================
// CheckpointWorkerModule — Web Worker for RFC 0034 P2 checkpoint writes
//                          + RFC 0034 P3 bounded event-log pruning.
//
// LMAX consumer-thread shape: receives {state, kind, workspaceId,
// lastEventSeq, prune?} from the main thread, serializes + writes a
// single checkpoint row, and (if requested) range-deletes the workspace's
// event-log entries with seq <= storedSeq. Main thread never blocks on
// serialization, I/O, or pruning.
//
// Self-contained: duplicates the small IDB schema constants from
// CheckpointStore + EventLogStore rather than importing across the
// worker boundary. The surface is small enough that the duplication is
// cheaper than cross-module import wiring inside a worker.
//
// Message contract:
//   IN  : { reqId, kind, workspaceId, state, lastEventSeq, prune? }
//   OUT : { reqId, ok: true,  storedSeq, prunedCount } |
//         { reqId, ok: false, error }
//
// prunedCount is null when prune was false; a number (possibly 0) when
// prune was true.
// =============================================================================

const CHECKPOINT_DB_NAME    = "homing.checkpoints";
const CHECKPOINT_STORE_NAME = "checkpoints";
const CHECKPOINT_DB_VERSION = 1;
const SCHEMA_VERSION        = 1;

const EVENT_DB_NAME         = "homing.eventlog";
const EVENT_STORE_NAME      = "events";
const EVENT_DB_VERSION      = 1;
const EVENT_INDEX_NAME      = "by_workspace";

let _checkpointDbPromise = null;
let _eventDbPromise      = null;

function _openCheckpointDb() {
    if (_checkpointDbPromise) return _checkpointDbPromise;
    _checkpointDbPromise = new Promise((resolve, reject) => {
        const req = indexedDB.open(CHECKPOINT_DB_NAME, CHECKPOINT_DB_VERSION);
        req.onupgradeneeded = (e) => {
            const db = e.target.result;
            if (!db.objectStoreNames.contains(CHECKPOINT_STORE_NAME)) {
                db.createObjectStore(CHECKPOINT_STORE_NAME, { keyPath: ["kind", "workspaceId"] });
            }
        };
        req.onsuccess = (e) => resolve(e.target.result);
        req.onerror   = (e) => reject(e.target.error);
    });
    return _checkpointDbPromise;
}

function _openEventDb() {
    if (_eventDbPromise) return _eventDbPromise;
    _eventDbPromise = new Promise((resolve, reject) => {
        const req = indexedDB.open(EVENT_DB_NAME, EVENT_DB_VERSION);
        // No onupgradeneeded — the main thread's EventLogStore owns
        // schema creation. If the DB doesn't exist yet (no events have
        // been emitted), opening it as a reader will create an empty
        // DB with no object store; pruning becomes a no-op since the
        // store-existence check below handles that.
        req.onsuccess = (e) => resolve(e.target.result);
        req.onerror   = (e) => reject(e.target.error);
    });
    return _eventDbPromise;
}

async function _writeCheckpointRow(kind, workspaceId, state, lastEventSeq) {
    const db = await _openCheckpointDb();
    const row = {
        kind:          kind,
        workspaceId:   workspaceId,
        schemaVersion: SCHEMA_VERSION,
        state:         state,
        lastEventSeq:  lastEventSeq,
        capturedAtMs:  Date.now()
    };
    return new Promise((resolve, reject) => {
        const tx    = db.transaction(CHECKPOINT_STORE_NAME, "readwrite");
        const store = tx.objectStore(CHECKPOINT_STORE_NAME);
        const req   = store.put(row);
        req.onsuccess = () => resolve();
        req.onerror   = () => reject(req.error);
    });
}

/**
 * Range-delete events of this workspace with seq <= upToSeq.
 *
 * Uses the by_workspace index ({@code [kind, workspaceId, seq]}, unique)
 * to scope the deletion to this workspace only — concurrent workspaces
 * sharing the same DB are untouched. Returns the number of rows deleted.
 *
 * Returns 0 if the events store doesn't exist yet (no events ever
 * recorded) or if no rows fall in the range — both are valid post-
 * conditions, not errors.
 */
async function _pruneEventsUpTo(kind, workspaceId, upToSeq) {
    const db = await _openEventDb();
    if (!db.objectStoreNames.contains(EVENT_STORE_NAME)) return 0;
    return new Promise((resolve, reject) => {
        const tx    = db.transaction(EVENT_STORE_NAME, "readwrite");
        const store = tx.objectStore(EVENT_STORE_NAME);
        const idx   = store.index(EVENT_INDEX_NAME);
        const range = IDBKeyRange.bound(
            [kind, workspaceId, -Infinity],
            [kind, workspaceId,  upToSeq],
            /* lowerOpen */ false, /* upperOpen */ false);
        const req = idx.openCursor(range);
        let deleted = 0;
        req.onsuccess = (e) => {
            const cur = e.target.result;
            if (cur) {
                cur.delete();
                deleted++;
                cur.continue();
            } else {
                // Wait for the transaction to commit before resolving;
                // an aborted tx after our reply would leak inconsistency.
                tx.oncomplete = () => resolve(deleted);
                tx.onerror    = () => reject(tx.error);
                tx.onabort    = () => reject(tx.error || new Error("prune tx aborted"));
            }
        };
        req.onerror = () => reject(req.error);
    });
}

self.onmessage = async (ev) => {
    const { reqId, kind, workspaceId, state, lastEventSeq, prune } = ev.data || {};
    if (typeof reqId !== 'number') {
        // Garbled message; can't reply with reqId. Best-effort log.
        console.warn('[CheckpointWorker] message missing reqId:', ev.data);
        return;
    }
    try {
        await _writeCheckpointRow(kind, workspaceId, state, lastEventSeq);
        let prunedCount = null;
        if (prune) {
            try {
                prunedCount = await _pruneEventsUpTo(kind, workspaceId, lastEventSeq);
            } catch (pruneErr) {
                // Prune failure is non-fatal — checkpoint succeeded; the
                // event log just keeps the rows that would have been
                // pruned. They get retried on the next successful write.
                console.warn('[CheckpointWorker] prune failed (checkpoint still committed):',
                    pruneErr && pruneErr.message ? pruneErr.message : pruneErr);
            }
        }
        self.postMessage({ reqId, ok: true, storedSeq: lastEventSeq, prunedCount });
    } catch (err) {
        self.postMessage({
            reqId,
            ok: false,
            error: String(err && err.message ? err.message : err)
        });
    }
};
