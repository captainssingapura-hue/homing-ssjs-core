package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import java.util.List;

/**
 * {@code MenuEvents}: the closed vocabulary of context menus as data — a
 * class of static factories mirroring the sealed sum {@link MenuEvent}
 * record for record: Opened, Picked, Closed. Pure logic.
 */
public record MenuEventsModule() implements EsModule<MenuEventsModule> {

    /** The class of factories. */
    public record MenuEvents() implements Exportable._Constant<MenuEventsModule> {}

    public static final MenuEventsModule INSTANCE = new MenuEventsModule();

    @Override public ImportsFor<MenuEventsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<MenuEventsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new MenuEvents()));
    }
}
