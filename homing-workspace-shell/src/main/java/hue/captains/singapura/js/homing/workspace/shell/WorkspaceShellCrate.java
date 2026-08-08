package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.studio.base.StudioBaseCrate;
import hue.captains.singapura.js.homing.workspace.WorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;
import hue.captains.singapura.js.homing.workspace.persistence.WorkspacePersistenceCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-workspace-shell}: the universal
 * workspace-chrome substrate (orchestrator + the layout / party / codec /
 * checkpoint / replay / mounter sub-modules and the GenericWorkspace app).
 * Requires the workspace stack below it plus the core-js + studio-base substrate.
 */
public final class WorkspaceShellCrate implements Crate {

    public static final WorkspaceShellCrate INSTANCE = new WorkspaceShellCrate();

    private WorkspaceShellCrate() {}

    @Override public String name() { return "homing-workspace-shell"; }

    @Override public List<Crate> requires() {
        return List.of(
                CoreJsCrate.INSTANCE,
                ServerCrate.INSTANCE,
                StudioBaseCrate.INSTANCE,
                WorkspaceCrate.INSTANCE,
                WorkspaceCodecsCrate.INSTANCE,
                WorkspacePersistenceCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(CheckpointServiceModule.INSTANCE),
                CrateEntry.of(CodecRegistrarModule.INSTANCE),
                CrateEntry.of(EventEmitterModule.INSTANCE),
                CrateEntry.of(GenericWorkspace.INSTANCE),
                CrateEntry.of(GenericWorkspaceChrome.INSTANCE),
                CrateEntry.of(LayoutCodecModule.INSTANCE),
                CrateEntry.of(PartyBootstrapModule.INSTANCE),
                CrateEntry.of(PersistenceAttacherModule.INSTANCE),
                CrateEntry.of(PickerTabFlowModule.INSTANCE),
                CrateEntry.of(PinnedTabSpawnerModule.INSTANCE),
                CrateEntry.of(ReplayEngineModule.INSTANCE),
                CrateEntry.of(TabRegistryModule.INSTANCE),
                CrateEntry.of(WidgetMounterModule.INSTANCE),
                CrateEntry.of(WorkspaceControlModalModule.INSTANCE),
                CrateEntry.of(WorkspaceDirectoryModule.INSTANCE),
                CrateEntry.of(WorkspaceShellChromeModule.INSTANCE),
                CrateEntry.of(WorkspaceStateModelModule.INSTANCE),
                CrateEntry.of(WriteLockGuardModule.INSTANCE));
    }
}
