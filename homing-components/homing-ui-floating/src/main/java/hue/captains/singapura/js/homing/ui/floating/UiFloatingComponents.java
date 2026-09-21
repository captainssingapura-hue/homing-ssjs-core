package hue.captains.singapura.js.homing.ui.floating;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;

import java.util.List;

/** The crate's catalogue of components: what it delivers, as components rather than as files. */
public record UiFloatingComponents() implements C0_Components<UiFloatingComponents> {

    public static final UiFloatingComponents INSTANCE = new UiFloatingComponents();

    @Override public String name() { return "Floating"; }
    @Override public String summary() { return "The desk, and the panes that float on it."; }

    @Override public List<ComponentEntry<UiFloatingComponents>> leaves() {
        return List.of(
                ComponentEntry.of(this, new DeskModule.Desk()),
                ComponentEntry.of(this, new FloatingPaneModule.FloatingPane()));
    }
}
