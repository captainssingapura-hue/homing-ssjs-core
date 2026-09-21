package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentVehicle;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.ui.elements.UiElementsCrate;
import hue.captains.singapura.js.homing.ui.floating.UiFloatingCrate;

import java.util.List;

/**
 * The dialog's crate: the dialog, its modality and its styles, on the
 * runtime ({@code ServerCrate}, the base's crate under its old name), the
 * design targets, the floating pane it is built on, and the elements whose buttons the action row is made of.
 */
public final class UiDialogCrate implements Crate, ComponentVehicle {

    public static final UiDialogCrate INSTANCE = new UiDialogCrate();

    private UiDialogCrate() {}

    @Override public String name() { return "homing-ui-dialog"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE, UiElementsCrate.INSTANCE, UiFloatingCrate.INSTANCE);
    }

    @Override public C0_Components<?> components() { return UiDialogComponents.INSTANCE; }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(DialogModule.INSTANCE),
                CrateEntry.of(ModalityModule.INSTANCE),
                CrateEntry.of(DialogStyles.INSTANCE));
    }
}
