package hue.captains.singapura.js.homing.ui.splitgrid;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentVehicle;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import java.util.List;

/**
 * The split grid's crate: the grid, the mirror, the tree algebra, the
 * geometry, the events and the styles, on the runtime and the design targets.
 * The grid and the mirror are primitives, owning the DOM on their branches;
 * the tree, the geometry and the events are pure logic.
 */
public final class UiSplitGridCrate implements Crate, ComponentVehicle {
    public static final UiSplitGridCrate INSTANCE = new UiSplitGridCrate();
    private UiSplitGridCrate() {}

    @Override public String name() { return "homing-ui-splitgrid"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE);
    }

    @Override public C0_Components<?> components() { return UiSplitGridComponents.INSTANCE; }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(SplitGridModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(SplitGridMirrorModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(SplitGridTreeModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(SplitGridGeometryModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(SplitGridEventsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(SplitGridStyles.INSTANCE));
    }
}
