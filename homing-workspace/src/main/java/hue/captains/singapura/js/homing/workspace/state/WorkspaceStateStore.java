package hue.captains.singapura.js.homing.workspace.state;

import java.util.List;
import java.util.Optional;

/**
 * SPI for persisting {@link WorkspaceState}. The framework ships exactly
 * two implementations:
 *
 * <ul>
 *   <li>{@code LocalStorageStore} — default, writes to the browser's
 *       {@code localStorage}; {@link #isRemote()} returns {@code false}.</li>
 *   <li>{@code FileExportStore} — explicit Export / Import via Blob URL
 *       download and file picker; {@link #isRemote()} returns
 *       {@code false} (the file lives on the user's machine).</li>
 * </ul>
 *
 * <p>Downstream applications may plug their own store implementations
 * via this SPI. When a store with {@link #isRemote()} returning
 * {@code true} is active, the framework's workspace shell renders a
 * persistent banner indicating that the user's state is leaving the
 * device, per the {@code State Belongs to the User} doctrine.</p>
 *
 * <p>All identifier-shaped parameters are typed
 * ({@link WorkspaceKind}, not raw {@code String}), per the
 * {@code Names Are Types} doctrine.</p>
 *
 * <p>The Java-side interface is the contract description; the actual
 * implementations live in JS. Methods are synchronous in this contract
 * for clarity — the JS side wraps in {@code Promise} as needed for the
 * async surfaces (file picker, network).</p>
 *
 * @since RFC 0029 cycle 1
 */
public interface WorkspaceStateStore {

    /**
     * Persist the state. Replaces any prior state for the same
     * {@code workspaceKind}. Implementations should be idempotent —
     * the same state saved twice is the same state on disk.
     */
    void save(WorkspaceState state);

    /**
     * Load the most-recent state for the given {@code workspaceKind},
     * if any. Returns {@link Optional#empty()} when no prior state
     * exists.
     */
    Optional<WorkspaceState> load(WorkspaceKind workspaceKind);

    /**
     * Forget the saved state for the given {@code workspaceKind}.
     * Used by "reset workspace" actions; after {@code clear}, a
     * subsequent {@link #load} returns {@link Optional#empty()}.
     */
    void clear(WorkspaceKind workspaceKind);

    /**
     * Names of every workspace with saved state in this store. Used by
     * "pick a workspace to restore" UIs (future, multi-workspace).
     */
    List<WorkspaceKind> listSavedKinds();

    /**
     * {@code true} if this store transmits the user's workspace state
     * across the network. Framework-shipped stores all return
     * {@code false}. Downstream implementations that move state to a
     * server return {@code true}, which drives the persistent
     * "state leaving device" banner.
     *
     * <p>A future conformance test will fail the build if a
     * framework-internal store implementation ever returns {@code true}
     * from this method — the doctrine binds the framework
     * mechanically, not just by convention.</p>
     */
    boolean isRemote();
}
