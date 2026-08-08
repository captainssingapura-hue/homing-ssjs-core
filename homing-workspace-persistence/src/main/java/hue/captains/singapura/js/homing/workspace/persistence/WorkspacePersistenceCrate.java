package hue.captains.singapura.js.homing.workspace.persistence;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.workspace.WorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-workspace-persistence}: the
 * workspace-state persistence module. Requires the workspace + codecs crates it
 * builds on.
 */
public final class WorkspacePersistenceCrate implements Crate {

    public static final WorkspacePersistenceCrate INSTANCE = new WorkspacePersistenceCrate();

    private WorkspacePersistenceCrate() {}

    @Override public String name() { return "homing-workspace-persistence"; }

    @Override public List<Crate> requires() {
        return List.of(WorkspaceCrate.INSTANCE, WorkspaceCodecsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WorkspaceStatePersistenceModule.INSTANCE));
    }
}
