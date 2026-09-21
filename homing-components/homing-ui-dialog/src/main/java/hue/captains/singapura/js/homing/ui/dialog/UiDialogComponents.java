package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;

import java.util.List;

/** The crate's catalogue of components: what it delivers, as components rather than as files. */
public record UiDialogComponents() implements C0_Components<UiDialogComponents> {

    public static final UiDialogComponents INSTANCE = new UiDialogComponents();

    @Override public String name() { return "Dialog"; }
    @Override public String summary() { return "A pane that owns the screen, or does not."; }

    @Override public List<ComponentEntry<UiDialogComponents>> leaves() {
        return List.of(
                ComponentEntry.of(this, new DialogModule.Dialog()));
    }
}
