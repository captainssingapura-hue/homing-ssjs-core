// =============================================================================
// WorkspaceEventLog — high-level facade over EventLogStore for one workspace
// instance.
//
// RFC 0030 Phase 1 (record-only). Bind once at chrome boot with the
// (workspaceKind, workspaceInstanceId) pair; thereafter the chrome calls
// emit(name, payload) on every state-affecting action. Recording is
// fire-and-forget; errors land in console.warn and the live UI never
// blocks on storage.
//
// Usage in the chrome's bodyJs():
//
//   const eventLog = WorkspaceEventLog.attach({
//       workspaceKind: "AnimalsPlayground",
//       workspaceId:   "<deterministic-placeholder-uuid>"
//   });
//   eventLog.emit("TabAdded", { paneId: "tl", tabId: "...", widgetKind: "..." });
//
// Per RFC 0031 multi-workspace, the workspaceId becomes a real per-instance
// UUID; until then we pass a deterministic placeholder so the storage
// shape never has to migrate.
// =============================================================================

const WorkspaceEventLog = Object.freeze({

    attach(opts) {
        if (!opts || typeof opts.workspaceKind !== "string" || opts.workspaceKind.length === 0) {
            throw new TypeError("WorkspaceEventLog.attach: opts.workspaceKind required (non-empty string)");
        }
        if (typeof opts.workspaceId !== "string" || opts.workspaceId.length === 0) {
            throw new TypeError("WorkspaceEventLog.attach: opts.workspaceId required (non-empty string)");
        }

        const kind        = opts.workspaceKind;
        const workspaceId = opts.workspaceId;
        const store       = createEventLogStore();

        // Monotonic local counter for the session — useful for ordering
        // events emitted in the same millisecond. The authoritative seq
        // comes back from IndexedDB; this is just for in-session tracing.
        let _local = 0;

        return Object.freeze({
            workspaceKind: kind,
            workspaceId:   workspaceId,

            /**
             * Append an event to the log. Fire-and-forget on the recording
             * path; the promise resolves to the assigned seq for callers
             * that want it (mostly tests).
             */
            emit(name, payload) {
                _local++;
                return store.append(kind, workspaceId, name, payload).catch(function (e) {
                    console.warn("[WorkspaceEventLog] emit failed for", name, "—", e && e.message ? e.message : e);
                });
            },

            /**
             * Range-query this instance's events. Optional { fromSeq, limit }.
             * Useful for diagnostics; Phase 2's replay path will call this
             * with fromSeq = checkpoint's lastEventSeq.
             */
            query(opts) {
                return store.query(kind, workspaceId, opts);
            },

            /**
             * Drop every event for this instance. Used by reset workspace.
             */
            clear() {
                return store.clear(kind, workspaceId);
            },

            /** Diagnostics: how many events have been emit()ed this session. */
            localEmitCount() { return _local; }
        });
    }
});
