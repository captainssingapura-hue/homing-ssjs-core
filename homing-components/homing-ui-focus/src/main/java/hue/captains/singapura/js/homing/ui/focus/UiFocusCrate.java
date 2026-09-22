package hue.captains.singapura.js.homing.ui.focus;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentVehicle;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;

import java.util.List;

/**
 * The focus crate: the monitor that shows the page's logical-focus tree and
 * who holds the keys, and the styles it wears. Tooling for a page, not a
 * component that takes keys: it reads the focus party and the steward.
 */
public final class UiFocusCrate implements Crate, ComponentVehicle {
    public static final UiFocusCrate INSTANCE = new UiFocusCrate();
    private UiFocusCrate() {}

    @Override public String name() { return "homing-ui-focus"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE);
    }

    @Override public C0_Components<?> components() { return UiFocusComponents.INSTANCE; }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(FocusMonitorModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(FocusStyles.INSTANCE));
    }
}
