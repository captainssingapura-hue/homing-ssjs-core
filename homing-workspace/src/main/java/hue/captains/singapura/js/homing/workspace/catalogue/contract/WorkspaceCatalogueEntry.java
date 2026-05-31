package hue.captains.singapura.js.homing.workspace.catalogue.contract;

import hue.captains.singapura.js.homing.workspace.state.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceName;

import java.time.Instant;
import java.util.Objects;

/**
 * One row in the workspace catalogue — the cross-workspace metadata
 * index. Per RFC 0031 V1, each user-visible workspace instance has one
 * entry; the entry holds the metadata that {@link WorkspaceCatalogue}
 * operations need (identity, display handle, age, recency, default flag).
 *
 * <p>The per-instance state (events + checkpoints) lives elsewhere in
 * IDB keyed by {@code (kind, uuid)} — the catalogue is the metadata
 * layer above that state.</p>
 *
 * @param id           UUID identity — stable across renames; canonical reference
 * @param kind         the workspace kind this instance belongs to
 * @param name         user-facing handle — used in URL {@code ?name=} param
 * @param createdAt    wall-clock at entry creation
 * @param lastOpenedAt wall-clock at most recent {@code touch()} call
 * @param isDefault    true for at most one entry per kind — what {@code ?app=K} opens
 *
 * @since RFC 0031 V1
 */
public record WorkspaceCatalogueEntry(
        WorkspaceInstanceId id,
        WorkspaceKind       kind,
        WorkspaceName       name,
        Instant             createdAt,
        Instant             lastOpenedAt,
        boolean             isDefault
) {

    public WorkspaceCatalogueEntry {
        Objects.requireNonNull(id,           "WorkspaceCatalogueEntry.id");
        Objects.requireNonNull(kind,         "WorkspaceCatalogueEntry.kind");
        Objects.requireNonNull(name,         "WorkspaceCatalogueEntry.name");
        Objects.requireNonNull(createdAt,    "WorkspaceCatalogueEntry.createdAt");
        Objects.requireNonNull(lastOpenedAt, "WorkspaceCatalogueEntry.lastOpenedAt");
    }
}
