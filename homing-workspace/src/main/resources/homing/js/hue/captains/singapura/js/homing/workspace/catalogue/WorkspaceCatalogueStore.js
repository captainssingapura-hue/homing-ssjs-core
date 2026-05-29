// =============================================================================
// WorkspaceCatalogueStore — IndexedDB wrapper for the workspace catalogue.
//
// RFC 0031 V1. The metadata layer above per-workspace persistence (events +
// checkpoints). One row per workspace instance; per-kind unique constraint
// on `name`.
//
// Schema:
//   database name : "homing.catalogue"
//   object store  : "entries"
//                   primary key: [kind, id]
//                   each row: { id, kind, name, createdAt, lastOpenedAt, isDefault }
//                   index by_name: [kind, name] — unique
//
// All operations are async (IDB has no synchronous API). The chrome awaits
// resolveDefault / lookupByName / lookupByUuid at boot, then proceeds with
// the resolved entry's UUID as the workspace's identity.
//
// Class form per RFC 0035 P2 discipline — constants are static class
// fields; no top-level script-scope pollution; the public method set
// matches the Java WorkspaceCatalogue interface.
// =============================================================================

class WorkspaceCatalogueStore {

    static DB_NAME    = "homing.catalogue";
    static STORE_NAME = "entries";
    static DB_VERSION = 1;
    static NAME_INDEX = "by_name";

    constructor() {
        this._dbPromise = null;
    }

    /** Lazy-opened singleton IDB connection. */
    _db() {
        if (this._dbPromise === null) this._dbPromise = this._openDb();
        return this._dbPromise;
    }

    _openDb() {
        return new Promise((resolve, reject) => {
            if (typeof indexedDB === "undefined") {
                reject(new Error("WorkspaceCatalogueStore: IndexedDB not available"));
                return;
            }
            const req = indexedDB.open(WorkspaceCatalogueStore.DB_NAME, WorkspaceCatalogueStore.DB_VERSION);
            req.onupgradeneeded = (e) => {
                const db = e.target.result;
                if (!db.objectStoreNames.contains(WorkspaceCatalogueStore.STORE_NAME)) {
                    const store = db.createObjectStore(WorkspaceCatalogueStore.STORE_NAME,
                            { keyPath: ["kind", "id"] });
                    store.createIndex(WorkspaceCatalogueStore.NAME_INDEX,
                            ["kind", "name"], { unique: true });
                }
            };
            req.onsuccess = (e) => resolve(e.target.result);
            req.onerror   = (e) => reject(e.target.error);
        });
    }

    /** Run `fn(store)` in a transaction of the named mode. */
    _tx(mode, fn) {
        return this._db().then(db => new Promise((resolve, reject) => {
            const tx    = db.transaction(WorkspaceCatalogueStore.STORE_NAME, mode);
            const store = tx.objectStore(WorkspaceCatalogueStore.STORE_NAME);
            const req   = fn(store);
            req.onsuccess = () => resolve(req.result);
            req.onerror   = () => reject(req.error);
        }));
    }

    /** Read all entries for a kind via a cursor. */
    _listInternal(kind) {
        return this._db().then(db => new Promise((resolve, reject) => {
            const out   = [];
            const tx    = db.transaction(WorkspaceCatalogueStore.STORE_NAME, "readonly");
            const store = tx.objectStore(WorkspaceCatalogueStore.STORE_NAME);
            const range = IDBKeyRange.bound([kind, ""], [kind, "￿"]);
            const req   = store.openCursor(range);
            req.onsuccess = (e) => {
                const cur = e.target.result;
                if (cur) { out.push(cur.value); cur.continue(); }
                else     { resolve(out); }
            };
            req.onerror = () => reject(req.error);
        }));
    }

    /** Look up by composite primary key. */
    lookupByUuid(kind, id) {
        WorkspaceCatalogueStore._requireKind(kind);
        WorkspaceCatalogueStore._requireId(id);
        return this._tx("readonly", store => store.get([kind, id])).then(row => row || null);
    }

    /** Look up by (kind, name) via the unique index. */
    lookupByName(kind, name) {
        WorkspaceCatalogueStore._requireKind(kind);
        WorkspaceCatalogueStore._requireName(name);
        return this._db().then(db => new Promise((resolve, reject) => {
            const tx    = db.transaction(WorkspaceCatalogueStore.STORE_NAME, "readonly");
            const store = tx.objectStore(WorkspaceCatalogueStore.STORE_NAME);
            const idx   = store.index(WorkspaceCatalogueStore.NAME_INDEX);
            const req   = idx.get([kind, name]);
            req.onsuccess = () => resolve(req.result || null);
            req.onerror   = () => reject(req.error);
        }));
    }

    /**
     * Resolve {@code ?app=kind} — the isDefault entry, or the
     * most-recently-opened, or null if no entries exist for this kind.
     */
    resolveDefault(kind) {
        return this._listInternal(kind).then(entries => {
            if (entries.length === 0) return null;
            const explicit = entries.find(e => e.isDefault === true);
            if (explicit) return explicit;
            return entries.slice().sort((a, b) => b.lastOpenedAt - a.lastOpenedAt)[0];
        });
    }

    /** All entries for this kind, sorted by lastOpenedAt descending. */
    listByKind(kind) {
        return this._listInternal(kind).then(entries =>
            entries.slice().sort((a, b) => b.lastOpenedAt - a.lastOpenedAt));
    }

    /**
     * Create a new entry. Rejects with a TypeError if an entry with
     * the same (kind, name) already exists — IDB's unique index throws
     * a ConstraintError on the {@code add()} call.
     */
    create(entry) {
        WorkspaceCatalogueStore._requireEntry(entry);
        return this._tx("readwrite", store => store.add(entry))
                   .then(() => undefined);
    }

    /** Rename an existing entry; throws on collision with another in the same kind. */
    rename(kind, id, newName) {
        WorkspaceCatalogueStore._requireKind(kind);
        WorkspaceCatalogueStore._requireId(id);
        WorkspaceCatalogueStore._requireName(newName);
        return this._db().then(db => new Promise((resolve, reject) => {
            const tx    = db.transaction(WorkspaceCatalogueStore.STORE_NAME, "readwrite");
            const store = tx.objectStore(WorkspaceCatalogueStore.STORE_NAME);
            const getReq = store.get([kind, id]);
            getReq.onsuccess = () => {
                const row = getReq.result;
                if (!row) { reject(new Error("rename: no entry at (" + kind + ", " + id + ")")); return; }
                row.name = newName;
                const putReq = store.put(row);
                putReq.onsuccess = () => resolve();
                putReq.onerror   = () => reject(putReq.error);
            };
            getReq.onerror = () => reject(getReq.error);
        }));
    }

    /** Delete an entry. */
    delete(kind, id) {
        WorkspaceCatalogueStore._requireKind(kind);
        WorkspaceCatalogueStore._requireId(id);
        return this._tx("readwrite", store => store.delete([kind, id]))
                   .then(() => undefined);
    }

    /** Atomically clear any prior isDefault in the kind; mark this one. */
    setDefault(kind, id) {
        return this._listInternal(kind).then(entries => {
            const updates = entries.map(e => ({ ...e, isDefault: (e.kind === kind && e.id === id) }));
            return this._db().then(db => new Promise((resolve, reject) => {
                const tx    = db.transaction(WorkspaceCatalogueStore.STORE_NAME, "readwrite");
                const store = tx.objectStore(WorkspaceCatalogueStore.STORE_NAME);
                let remaining = updates.length;
                if (remaining === 0) { resolve(); return; }
                updates.forEach(u => {
                    const req = store.put(u);
                    req.onsuccess = () => { if (--remaining === 0) resolve(); };
                    req.onerror   = () => reject(req.error);
                });
            }));
        });
    }

    /** Update lastOpenedAt to now. */
    touch(kind, id) {
        return this._db().then(db => new Promise((resolve, reject) => {
            const tx    = db.transaction(WorkspaceCatalogueStore.STORE_NAME, "readwrite");
            const store = tx.objectStore(WorkspaceCatalogueStore.STORE_NAME);
            const getReq = store.get([kind, id]);
            getReq.onsuccess = () => {
                const row = getReq.result;
                if (!row) { resolve(); return; }   // silently no-op on missing
                row.lastOpenedAt = Date.now();
                const putReq = store.put(row);
                putReq.onsuccess = () => resolve();
                putReq.onerror   = () => reject(putReq.error);
            };
            getReq.onerror = () => reject(getReq.error);
        }));
    }

    // ─── Argument validation ─────────────────────────────────────────────

    static _requireKind(kind) {
        if (typeof kind !== "string" || kind.length === 0) {
            throw new TypeError("WorkspaceCatalogueStore: kind must be non-empty string");
        }
    }
    static _requireId(id) {
        if (typeof id !== "string" || id.length === 0) {
            throw new TypeError("WorkspaceCatalogueStore: id must be non-empty string");
        }
    }
    static _requireName(name) {
        if (typeof name !== "string" || name.length === 0) {
            throw new TypeError("WorkspaceCatalogueStore: name must be non-empty string");
        }
    }
    static _requireEntry(entry) {
        if (!entry) throw new TypeError("WorkspaceCatalogueStore.create: entry required");
        WorkspaceCatalogueStore._requireKind(entry.kind);
        WorkspaceCatalogueStore._requireId(entry.id);
        WorkspaceCatalogueStore._requireName(entry.name);
        if (typeof entry.createdAt    !== "number") throw new TypeError("entry.createdAt required (ms)");
        if (typeof entry.lastOpenedAt !== "number") throw new TypeError("entry.lastOpenedAt required (ms)");
        if (typeof entry.isDefault    !== "boolean") throw new TypeError("entry.isDefault required");
    }
}

/** Factory — matches the createX() naming convention in adjacent modules. */
function createWorkspaceCatalogueStore() {
    return new WorkspaceCatalogueStore();
}
