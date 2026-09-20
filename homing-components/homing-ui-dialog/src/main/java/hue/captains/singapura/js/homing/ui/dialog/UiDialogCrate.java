package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.ui.elements.UiElementsCrate;

import java.util.List;

/**
 * The dialog's crate: the dialog, its modality and its styles, on the
 * runtime ({@code ServerCrate}, the base's crate under its old name), the
 * design targets, and the elements whose buttons the action row is made of.
 */
public final class UiDialogCrate implements Crate {

    public static final UiDialogCrate INSTANCE = new UiDialogCrate();

    private UiDialogCrate() {}

    @Override public String name() { return "homing-ui-dialog"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, DesignCrate.INSTANCE, UiElementsCrate.INSTANCE);
    }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(DialogModule.INSTANCE),
                CrateEntry.of(ModalityModule.INSTANCE),
                CrateEntry.of(DialogStyles.INSTANCE));
    }
}
