// =============================================================================
// WorkspaceStatePersister — debounced auto-save coordinator for RFC 0029.
//
// Factory-shaped. The persister itself owns nothing but a pending-timer
// reference and the debounce wiring; the saveFn (what actually happens
// at save time — capture → encode → store.save) is injected, and the
// scheduler is injected too so tests can advance time deterministically.
//
// Two entry points the workspace shell calls into:
//
//   triggerSave()  — debounced. Called by every state-affecting event
//                    (SplitPane resize/split/merge, MTP tab add/close/
//                    move/switch, widget param notify, ribbon toggles).
//                    Bursts coalesce; the actual save fires only once
//                    after the debounce window settles.
//
//   forceSave()    — immediate. Called by the Force Save ribbon control
//                    and (in future) by the page-unload handler. Cancels
//                    any pending debounced save and runs the saveFn now.
//
// The persister returns isPending() so the UI (or a status pill) can
// surface whether unsaved changes exist; useful for both diagnostics and
// the "you have unsaved changes" prompt on tab close.
//
// Doctrine alignment:
//   - Functional Object — the returned object is stateless from the
//     caller's view; bursts of triggerSave coalesce naturally.
//   - CodeGen as Functions — this module is hand-written; an automated
//     generator could equally produce it, conforming to the same
//     interface. Tests don't know which.
// =============================================================================

function createWorkspaceStatePersister(opts) {
    if (!opts || typeof opts.saveFn !== 'function') {
        throw new TypeError(
            "createWorkspaceStatePersister: opts.saveFn (function) is required");
    }
    if (!opts.scheduler
        || typeof opts.scheduler.schedule !== 'function'
        || typeof opts.scheduler.cancel !== 'function') {
        throw new TypeError(
            "createWorkspaceStatePersister: opts.scheduler must have schedule() + cancel()");
    }

    const debounceMs = opts.debounceMs ?? 500;
    const saveFn     = opts.saveFn;
    const scheduler  = opts.scheduler;

    let pendingTicket = null;
    let lastError     = null;

    /** Run the saveFn now, swallowing the pending debounce timer (if any). */
    function forceSave() {
        if (pendingTicket !== null) {
            scheduler.cancel(pendingTicket);
            pendingTicket = null;
        }
        try {
            saveFn();
            lastError = null;
        } catch (e) {
            lastError = e;
            throw e;
        }
    }

    /** Schedule a save for {@code debounceMs} from now. Bursts coalesce. */
    function triggerSave() {
        if (pendingTicket !== null) {
            scheduler.cancel(pendingTicket);
        }
        pendingTicket = scheduler.schedule(() => {
            pendingTicket = null;
            try {
                saveFn();
                lastError = null;
            } catch (e) {
                lastError = e;
                // Don't re-throw — async callback; the error survives in lastError
                // and via the scheduler's own error path (which is platform-specific).
            }
        }, debounceMs);
    }

    return Object.freeze({
        triggerSave,
        forceSave,
        isPending() { return pendingTicket !== null; },
        get lastError() { return lastError; }
    });
}

// ─────────────────────────────────────────────────────────────────────────────
// Production scheduler — wraps browser setTimeout / clearTimeout. Importing
// modules in browsers use this; tests provide their own scheduler.
// ─────────────────────────────────────────────────────────────────────────────

const browserScheduler = Object.freeze({
    schedule(fn, delayMs) { return setTimeout(fn, delayMs); },
    cancel(ticket)        { clearTimeout(ticket); }
});
