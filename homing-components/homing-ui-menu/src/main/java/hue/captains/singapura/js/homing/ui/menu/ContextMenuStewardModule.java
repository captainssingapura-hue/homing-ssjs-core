package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * {@code ContextMenuSteward}, one per page: the registry of kinds — a Java
 * {@link ContextMenuRegistry}'s stamped {@code MENUS}, or kinds defined
 * through its builder — one instance per kind minted at the kind's first
 * open, the layer minted at the first open of any, and the one menu active,
 * which is the fundamental invariant. Lazy: nothing minted at construction,
 * nothing listened to while no menu is open. The press that closes a menu
 * is swallowed whole. A primitive, owning the layer on its branch.
 */
public record ContextMenuStewardModule() implements DomModule<ContextMenuStewardModule> {

    /** The class. */
    public record ContextMenuSteward() implements BranchComponent<ContextMenuStewardModule>, NeedKeyboard {
        @Override public String summary() { return "One per page: the kinds, one instance each, the layer, and the one menu active."; }
        /** Claimed on open, given back on close, closed when taken: the open menu's keys. */
        public static final List<KeyBinding> KEYS = List.of(
                KeyBinding.of(Key.ESCAPE, "the menu closed"), KeyBinding.of(Key.TAB, "held: the focus stays in the menu"),
                KeyBinding.of(Key.ARROW_DOWN, "the next row"), KeyBinding.of(Key.ARROW_UP, "the previous row"),
                KeyBinding.of(Key.HOME, "the first row"), KeyBinding.of(Key.END, "the last row"),
                KeyBinding.of(Key.ARROW_RIGHT, "into the row's submenu"), KeyBinding.of(Key.ARROW_LEFT, "back out of a submenu"),
                KeyBinding.of(Key.ENTER, "the row activated"), KeyBinding.of(Key.SPACE, "the row activated"));
        @Override public List<KeyBinding> keys() { return KEYS; }
    }

    public static final ContextMenuStewardModule INSTANCE = new ContextMenuStewardModule();

    @Override
    public ImportsFor<ContextMenuStewardModule> imports() {
        return ImportsFor.<ContextMenuStewardModule>builder()
                .add(new ModuleImports<>(List.of(new MenuStyles.cm_layer(), new MenuStyles.cm_specimen()), MenuStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContextMenuModule.ContextMenu()), ContextMenuModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new MenuEventsModule.MenuEvents()), MenuEventsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new MenuTreeModule.MenuTree()), MenuTreeModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ContextMenuStewardModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ContextMenuSteward()));
    }
}
