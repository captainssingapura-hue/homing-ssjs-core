package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentVehicle;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;

import java.util.List;

/**
 * The panes' crate: the multi-tab pane, its strip, its events and their styles, on the runtime
 * ({@code ServerCrate}, the base's crate under its old name) and the design
 * targets. The pane, the strip and the hand on its chips are primitives, owning the DOM on their branch; the events and the drag's arithmetic are pure logic.
 */
public final class UiPanesCrate implements Crate, ComponentVehicle {

    public static final UiPanesCrate INSTANCE = new UiPanesCrate();

    private UiPanesCrate() {}

    @Override public String name() { return "homing-ui-panes"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE);
    }

    @Override public C0_Components<?> components() { return UiPanesComponents.INSTANCE; }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(MultiTabPaneModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(TabStripModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(TabHandModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(PaneEventsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(TabDragModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(PaneKeysModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(PaneMenusModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(PaneStyles.INSTANCE));
    }
}
