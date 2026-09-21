package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;

import java.util.List;

/** The crate's catalogue of components: what it delivers, as components rather than as files. */
public record UiMenuComponents() implements C0_Components<UiMenuComponents> {

    public static final UiMenuComponents INSTANCE = new UiMenuComponents();

    @Override public String name() { return "Menus"; }
    @Override public String summary() { return "Context menus: the page's steward and one instance per kind."; }

    @Override public List<ComponentEntry<UiMenuComponents>> leaves() {
        return List.of(
                ComponentEntry.of(this, new ContextMenuStewardModule.ContextMenuSteward()),
                ComponentEntry.of(this, new ContextMenuModule.ContextMenu()));
    }
}
