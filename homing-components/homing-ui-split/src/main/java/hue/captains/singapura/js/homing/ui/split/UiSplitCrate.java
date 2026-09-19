package hue.captains.singapura.js.homing.ui.split;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;

import java.util.List;

/**
 * The splitter's crate: the split pane, its events and its styles, on the
 * runtime ({@code ServerCrate}, the base's crate under its old name) and the
 * design targets. The pane is a primitive, owning the DOM on its branch;
 * the events are pure logic.
 */
public final class UiSplitCrate implements Crate {

    public static final UiSplitCrate INSTANCE = new UiSplitCrate();

    private UiSplitCrate() {}

    @Override public String name() { return "homing-ui-split"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE);
    }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(SplitPane.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(SplitEventsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(SplitStyles.INSTANCE));
    }
}
