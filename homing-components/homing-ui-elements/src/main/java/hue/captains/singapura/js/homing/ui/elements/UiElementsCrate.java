package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentVehicle;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.ui.icons.UiIconsCrate;

import java.util.List;

/**
 * The elements' crate: the builders and their styles, on the runtime
 * ({@code ServerCrate} — the component base's crate, under its old name
 * until the old structure retires) and the design substrate's targets.
 */
public final class UiElementsCrate implements Crate, ComponentVehicle {

    public static final UiElementsCrate INSTANCE = new UiElementsCrate();

    private UiElementsCrate() {}

    @Override public String name() { return "homing-ui-elements"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE, UiIconsCrate.INSTANCE);
    }

    @Override public C0_Components<?> components() { return UiElementsComponents.INSTANCE; }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(Elements.INSTANCE),
                CrateEntry.of(SliderModule.INSTANCE),
                CrateEntry.of(ElementStyles.INSTANCE));
    }
}
