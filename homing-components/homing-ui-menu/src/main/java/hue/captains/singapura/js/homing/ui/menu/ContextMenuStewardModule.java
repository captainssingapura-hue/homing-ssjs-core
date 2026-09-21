package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
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
    public record ContextMenuSteward() implements Exportable._Constant<ContextMenuStewardModule> {}

    public static final ContextMenuStewardModule INSTANCE = new ContextMenuStewardModule();

    @Override
    public ImportsFor<ContextMenuStewardModule> imports() {
        return ImportsFor.<ContextMenuStewardModule>builder()
                .add(new ModuleImports<>(List.of(new MenuStyles.cm_layer()), MenuStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContextMenuModule.ContextMenu()), ContextMenuModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new MenuEventsModule.MenuEvents()), MenuEventsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ContextMenuStewardModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ContextMenuSteward()));
    }
}
