package hue.captains.singapura.js.homing.workspace.codecs;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.workspace.WorkspaceCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-workspace-codecs}: the
 * build-time-generated workspace-state codecs module. Requires the workspace
 * crate whose typed records it serializes.
 */
public final class WorkspaceCodecsCrate implements Crate {

    public static final WorkspaceCodecsCrate INSTANCE = new WorkspaceCodecsCrate();

    private WorkspaceCodecsCrate() {}

    @Override public String name() { return "homing-workspace-codecs"; }

    @Override public List<Crate> requires() {
        return List.of(WorkspaceCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WorkspaceStateCodecsModule.INSTANCE, JsModuleType.PURE_LOGIC));
    }
}
