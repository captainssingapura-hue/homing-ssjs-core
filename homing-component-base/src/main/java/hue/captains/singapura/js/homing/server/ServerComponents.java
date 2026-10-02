package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardStewardModule;

import java.util.List;

/** The base's catalogue: the one component it delivers, the keyboard party's steward. */
public record ServerComponents() implements C0_Components<ServerComponents> {

    public static final ServerComponents INSTANCE = new ServerComponents();

    @Override public String name() { return "Base"; }
    @Override public String summary() { return "What every page has: the keyboard party's steward, one per page, on a branch of the page's own."; }

    @Override public List<ComponentEntry<ServerComponents>> leaves() {
        return List.of(ComponentEntry.of(this, new KeyboardStewardModule.KeyboardSteward()));
    }
}
