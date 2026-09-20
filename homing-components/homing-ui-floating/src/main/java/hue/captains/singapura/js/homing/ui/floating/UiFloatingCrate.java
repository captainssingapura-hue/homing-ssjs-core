package hue.captains.singapura.js.homing.ui.floating;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import java.util.List;

/**
 * The floating crate: the pane, the desk, their events and their styles, on
 * the runtime and the design targets. The pane and the desk are primitives,
 * owning the DOM on their branches; the events are pure logic.
 */
public final class UiFloatingCrate implements Crate {
    public static final UiFloatingCrate INSTANCE = new UiFloatingCrate();
    private UiFloatingCrate() {}

    @Override public String name() { return "homing-ui-floating"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE);
    }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(FloatingPaneModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(DeskModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(FloatEventsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(FloatingStyles.INSTANCE));
    }
}
