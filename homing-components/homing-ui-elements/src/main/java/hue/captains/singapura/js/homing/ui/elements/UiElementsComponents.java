package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;

import java.util.List;

/** The crate's catalogue of components: what it delivers, as components rather than as files. */
public record UiElementsComponents() implements C0_Components<UiElementsComponents> {

    public static final UiElementsComponents INSTANCE = new UiElementsComponents();

    @Override public String name() { return "Elements"; }
    @Override public String summary() { return "The smallest things: a button, a card and a slider, each made through its builder."; }

    @Override public List<ComponentEntry<UiElementsComponents>> leaves() {
        return List.of(
                ComponentEntry.of(this, new Elements.Button()),
                ComponentEntry.of(this, new Elements.Card()),
                ComponentEntry.of(this, new SliderModule.Slider()));
    }
}
