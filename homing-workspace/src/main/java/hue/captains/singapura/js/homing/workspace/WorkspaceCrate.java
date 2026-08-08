package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.studio.base.StudioBaseCrate;
import hue.captains.singapura.js.homing.workspace.catalogue.WorkspaceCatalogueModule;
import hue.captains.singapura.js.homing.workspace.events.CheckpointStoreModule;
import hue.captains.singapura.js.homing.workspace.events.CheckpointWorkerModule;
import hue.captains.singapura.js.homing.workspace.events.WorkspaceEventLogModule;
import hue.captains.singapura.js.homing.workspace.party.LayoutSecretaryModule;
import hue.captains.singapura.js.homing.workspace.party.PartyModule;
import hue.captains.singapura.js.homing.workspace.persistence.WidgetParamsCodecRegistryModule;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-workspace}: the workspace
 * primitives (picker, layout, catalogue, event log, party bus, checkpoint
 * stores, widget-params codec registry). Requires the core-js substrate and
 * studio-base (styles/DOM primitives its modules import).
 */
public final class WorkspaceCrate implements Crate {

    public static final WorkspaceCrate INSTANCE = new WorkspaceCrate();

    private WorkspaceCrate() {}

    @Override public String name() { return "homing-workspace"; }

    @Override public List<Crate> requires() {
        return List.of(CoreJsCrate.INSTANCE, StudioBaseCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WidgetPickerModule.INSTANCE),
                CrateEntry.of(WidgetPickerStyles.INSTANCE),
                CrateEntry.of(WorkspaceLayoutModule.INSTANCE),
                CrateEntry.of(WorkspaceLayoutStyles.INSTANCE),
                CrateEntry.of(WorkspaceCatalogueModule.INSTANCE),
                CrateEntry.of(CheckpointStoreModule.INSTANCE),
                CrateEntry.of(CheckpointWorkerModule.INSTANCE),
                CrateEntry.of(WorkspaceEventLogModule.INSTANCE),
                CrateEntry.of(LayoutSecretaryModule.INSTANCE),
                CrateEntry.of(PartyModule.INSTANCE),
                CrateEntry.of(WidgetParamsCodecRegistryModule.INSTANCE));
    }
}
