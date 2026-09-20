package hue.captains.singapura.js.homing.ui.docking;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.ui.floating.UiFloatingCrate;
import hue.captains.singapura.js.homing.ui.panes.UiPanesCrate;
import java.util.List;

/**
 * The docking crate: the protocol and its events, on the runtime, the design
 * targets, the floating crate whose desk it owns and the panes crate whose
 * multi-tab pane is the dock. Docking is a primitive, owning the DOM on its
 * branch through the desk; the events are pure logic. No styles of its own:
 * the dock's drop-target look is the pane's, the float's is the desk's.
 */
public final class UiDockingCrate implements Crate {
    public static final UiDockingCrate INSTANCE = new UiDockingCrate();
    private UiDockingCrate() {}

    @Override public String name() { return "homing-ui-docking"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE, UiFloatingCrate.INSTANCE, UiPanesCrate.INSTANCE);
    }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(DockingModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(DockEventsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
