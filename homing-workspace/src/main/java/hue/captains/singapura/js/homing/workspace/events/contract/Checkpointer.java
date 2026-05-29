package hue.captains.singapura.js.homing.workspace.events.contract;

import java.util.concurrent.CompletableFuture;

/**
 * Cadence orchestrator that sits above {@link EventLog} and {@link
 * CheckpointStore}. Owns the hybrid trigger policy ({@link
 * CadencePolicy}), the capture function (provided at construction time
 * by the chrome — the only piece that can't be substrate-free, since
 * capture walks the live workspace), and the post-write bookkeeping
 * (event-count reset, diagnostic logging).
 *
 * <p>Substrate-free contract; per RFC 0034, the JS implementation can
 * run capture on main thread + I/O on a Web Worker (the LMAX shape) or
 * everything on main thread (fallback). Both modes implement this same
 * interface.</p>
 *
 * @param <S> state type captured into the snapshot envelope
 *
 * @since RFC 0035 P1
 */
public interface Checkpointer<S> {

    /** Begin the cadence loop. Idempotent — calling {@code start} twice is a no-op. */
    void start();

    /** Stop the cadence loop. Outstanding writes are allowed to finish. */
    void stop();

    /**
     * Force-write a checkpoint immediately, regardless of cadence. Used
     * by {@code beforeunload}, manual DevTools triggers, and tests that
     * want to assert a checkpoint exists at a known seq.
     */
    CompletableFuture<Void> writeNow(CheckpointTrigger trigger);
}
