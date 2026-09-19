package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;

import java.util.List;

/**
 * The panes' crate: the multi-tab pane, its strip and their styles, on the runtime
 * ({@code ServerCrate}, the base's crate under its old name) and the design
 * targets. Both are primitives: they own the DOM on its branch.
 */
public final class UiPanesCrate implements Crate {

    public static final UiPanesCrate INSTANCE = new UiPanesCrate();

    private UiPanesCrate() {}

    @Override public String name() { return "homing-ui-panes"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE);
    }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(MultiTabPane.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(TabStrip.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(PaneStyles.INSTANCE));
    }
}
