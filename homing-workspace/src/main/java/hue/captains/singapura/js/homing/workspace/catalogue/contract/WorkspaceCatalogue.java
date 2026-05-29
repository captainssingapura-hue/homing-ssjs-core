package hue.captains.singapura.js.homing.workspace.catalogue.contract;

import hue.captains.singapura.js.homing.workspace.state.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceName;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Substrate-free contract for the workspace catalogue. The JS
 * implementation ({@code WorkspaceCatalogueStore.js}) conforms
 * structurally; the conformance test gates the build per the Diligent
 * Primitives doctrine.
 *
 * <p>The catalogue is a thin metadata layer above the per-workspace
 * persistence (events + checkpoints). Each operation is async via IDB;
 * boot orchestrates a single lookup, then proceeds with the resolved
 * {@code WorkspaceInstanceId} as the workspace's identity.</p>
 *
 * <p>Substrate-free per the {@code A Good Design Doesn't Pick Up Its
 * Substrate} meta-doctrine — the same interface renders to JS against
 * IndexedDB, to Java against JDBC, to C against flat files. The
 * contract just specifies what each method does, not how.</p>
 *
 * @since RFC 0031 V1
 */
public interface WorkspaceCatalogue {

    /** Look up the entry with the given UUID, scoped to a kind. */
    CompletableFuture<Optional<WorkspaceCatalogueEntry>> lookupByUuid(WorkspaceKind kind, WorkspaceInstanceId id);

    /** Look up the entry with the given name, scoped to a kind. */
    CompletableFuture<Optional<WorkspaceCatalogueEntry>> lookupByName(WorkspaceKind kind, WorkspaceName name);

    /**
     * Resolve {@code ?app=kind} (no name, no uuid) — the "default"
     * workspace for the kind. Returns the entry with
     * {@code isDefault=true} if one exists, otherwise the
     * most-recently-opened entry; empty if no entries exist for this
     * kind at all.
     */
    CompletableFuture<Optional<WorkspaceCatalogueEntry>> resolveDefault(WorkspaceKind kind);

    /** All entries for this kind, sorted by {@code lastOpenedAt} descending. */
    CompletableFuture<List<WorkspaceCatalogueEntry>> listByKind(WorkspaceKind kind);

    /**
     * Create a new entry. Throws (resolves exceptionally) if an entry
     * with the same {@code (kind, name)} already exists. Per-kind
     * uniqueness on name is enforced at write time.
     */
    CompletableFuture<Void> create(WorkspaceCatalogueEntry entry);

    /**
     * Rename an existing entry. Throws on collision with another
     * entry of the same kind. Identity ({@code uuid}) is unchanged.
     */
    CompletableFuture<Void> rename(WorkspaceKind kind, WorkspaceInstanceId id, WorkspaceName newName);

    /**
     * Delete an entry. Caller is responsible for cleaning up the
     * per-workspace persistence (event log + checkpoint) separately;
     * the catalogue only owns the metadata row.
     */
    CompletableFuture<Void> delete(WorkspaceKind kind, WorkspaceInstanceId id);

    /**
     * Mark this entry as the default for its kind. Atomically clears
     * the previous default (if any) in the same kind so that
     * {@code isDefault=true} remains a per-kind unique constraint.
     */
    CompletableFuture<Void> setDefault(WorkspaceKind kind, WorkspaceInstanceId id);

    /** Update {@code lastOpenedAt} to the current wall-clock. */
    CompletableFuture<Void> touch(WorkspaceKind kind, WorkspaceInstanceId id);
}
