package hue.captains.singapura.js.homing.ui.split;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;

import java.util.List;

/** The crate's catalogue of components: what it delivers, as components rather than as files. */
public record UiSplitComponents() implements C0_Components<UiSplitComponents> {

    public static final UiSplitComponents INSTANCE = new UiSplitComponents();

    @Override public String name() { return "Split"; }
    @Override public String summary() { return "Two slots and the divider between them."; }

    @Override public List<ComponentEntry<UiSplitComponents>> leaves() {
        return List.of(
                ComponentEntry.of(this, new SplitPaneModule.SplitPane()));
    }
}
