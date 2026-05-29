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
