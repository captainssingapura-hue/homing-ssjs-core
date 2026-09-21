package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;

import java.util.List;

/**
 * The context menus' crate: the steward, the menu instance, the events, the
 * geometry and the styles, on the runtime ({@code ServerCrate}) and the
 * design targets. The steward and the menu are primitives, owning the DOM on
 * their branches; the events and the geometry are pure logic. A site's
 * registry module — its kinds as data — is the site's, declared beside it.
 */
public final class UiMenuCrate implements Crate {
    public static final UiMenuCrate INSTANCE = new UiMenuCrate();
    private UiMenuCrate() {}

    @Override public String name() { return "homing-ui-menu"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE);
    }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(ContextMenuStewardModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(ContextMenuModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(MenuEventsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(MenuGeometryModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(MenuStyles.INSTANCE));
    }
}
