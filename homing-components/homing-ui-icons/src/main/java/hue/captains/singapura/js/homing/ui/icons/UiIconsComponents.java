package hue.captains.singapura.js.homing.ui.icons;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;

import java.util.List;

/** The crate's catalogue of components: what it delivers, as components rather than as files. */
public record UiIconsComponents() implements C0_Components<UiIconsComponents> {

    public static final UiIconsComponents INSTANCE = new UiIconsComponents();

    @Override public String name() { return "Icons"; }
    @Override public String summary() { return "A mark that means a word; the design draws it."; }

    @Override public List<ComponentEntry<UiIconsComponents>> leaves() {
        return List.of(
                ComponentEntry.of(this, new IconModule.Icon()));
    }
}
