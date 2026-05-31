package hue.captains.singapura.js.homing.workspace.events.contract;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Periodic snapshot store, bound to one {@code (kind, workspaceId)} pair.
 * One row at a time — writes overwrite the previous checkpoint. The
 * state type {@code S} is the workspace-specific aggregate (typically
 * {@link hue.captains.singapura.js.homing.workspace.state.WorkspaceState}).
 *
 * <p>Substrate-free contract; per RFC 0034, the JS implementation lives
 * in IndexedDB (P1: main thread; P2: Web Worker).</p>
 *
 * <p>The {@link #schemaVersion()} value gates compatibility — readers
 * compare a loaded checkpoint's {@link Checkpoint#schemaVersion()} against
 * the store's current version and treat mismatches as "no checkpoint"
 * (fall back to full event-log replay).</p>
 *
 * @param <S> state type stored in the snapshot envelope
 *
 * @since RFC 0035 P1
 */
public interface CheckpointStore<S> {

    /** Current envelope schema version. Bump when the on-disk shape changes. */
    int schemaVersion();

    /** Write or replace this workspace's checkpoint. Atomic single-row put. */
    CompletableFuture<Void> write(Checkpoint<S> checkpoint);

    /**
     * Read this workspace's latest checkpoint, or empty if none exists or
     * the loaded envelope's schemaVersion doesn't match {@link
     * #schemaVersion()}. Schema-mismatched envelopes are treated as
     * absent so boot can fall back gracefully.
     */
    CompletableFuture<Optional<Checkpoint<S>>> read();

    /** Drop this workspace's checkpoint. Used by Reset State. */
    CompletableFuture<Void> clear();
}
