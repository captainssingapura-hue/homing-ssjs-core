package hue.captains.singapura.js.homing.ui.splitgrid;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import java.util.List;

/**
 * The split grid's crate: the grid, its tree algebra, its events and its
 * styles, on the runtime and the design targets. The grid is a primitive,
 * owning the DOM on its branch; the tree and the events are pure logic.
 */
public final class UiSplitGridCrate implements Crate {
    public static final UiSplitGridCrate INSTANCE = new UiSplitGridCrate();
    private UiSplitGridCrate() {}

    @Override public String name() { return "homing-ui-splitgrid"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE);
    }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(SplitGridModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(SplitGridTreeModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(SplitGridEventsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(SplitGridStyles.INSTANCE));
    }
}
