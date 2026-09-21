package hue.captains.singapura.js.homing.ui.icons;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;

import java.util.List;

/**
 * The icons' crate: the {@code Icon} element component, the generated table
 * of words and the styles that wear them. A component that shows a mark
 * requires this crate and asks for a word; a design answers every word.
 */
public final class UiIconsCrate implements Crate {
    public static final UiIconsCrate INSTANCE = new UiIconsCrate();
    private UiIconsCrate() {}

    @Override public String name() { return "homing-ui-icons"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE);
    }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(IconModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(IconsModule.INSTANCE),
                CrateEntry.of(IconStyles.INSTANCE));
    }
}
