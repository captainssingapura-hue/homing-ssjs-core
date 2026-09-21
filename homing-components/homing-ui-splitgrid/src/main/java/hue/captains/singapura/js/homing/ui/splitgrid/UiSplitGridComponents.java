package hue.captains.singapura.js.homing.ui.splitgrid;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;

import java.util.List;

/** The crate's catalogue of components: what it delivers, as components rather than as files. */
public record UiSplitGridComponents() implements C0_Components<UiSplitGridComponents> {

    public static final UiSplitGridComponents INSTANCE = new UiSplitGridComponents();

    @Override public String name() { return "Split grid"; }
    @Override public String summary() { return "A grid split and re-split, and its mirror."; }

    @Override public List<ComponentEntry<UiSplitGridComponents>> leaves() {
        return List.of(
                ComponentEntry.of(this, new SplitGridModule.SplitGrid()),
                ComponentEntry.of(this, new SplitGridMirrorModule.SplitGridMirror()));
    }
}
