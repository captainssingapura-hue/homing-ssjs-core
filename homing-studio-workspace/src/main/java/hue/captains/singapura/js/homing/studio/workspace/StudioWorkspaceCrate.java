package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.libs.LibsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.studio.base.StudioBaseCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-studio-workspace}: the Studio
 * Workspace widgets (Navigator tree, Summary, Document content) and the
 * navigation-party secretary. Requires the core-js renderer + studio-base
 * doc-render stack its widgets import.
 */
public final class StudioWorkspaceCrate implements Crate {

    public static final StudioWorkspaceCrate INSTANCE = new StudioWorkspaceCrate();

    private StudioWorkspaceCrate() {}

    @Override public String name() { return "homing-studio-workspace"; }

    @Override public List<Crate> requires() {
        return List.of(CoreJsCrate.INSTANCE, ServerCrate.INSTANCE,
                LibsCrate.INSTANCE, StudioBaseCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(DocContentWidget.INSTANCE),
                CrateEntry.of(NavigatorSecretaryModule.INSTANCE),
                CrateEntry.of(SummaryWidget.INSTANCE),
                CrateEntry.of(TreeWidget.INSTANCE));
    }
}
