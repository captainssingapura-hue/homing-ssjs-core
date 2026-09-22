package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * {@code PaneKeys}: the pane's keys while it holds them, as one function over
 * the pane's own surface — the arrows and Home/End changing the active tab,
 * Shift+arrows reordering along the rail, Shift+Down asking to detach, Enter
 * having the widget activate itself, Escape yielding, the menu key. What a
 * key means for a container of tabs, and nothing about how the pane is
 * built; the pane's {@code keyDown} is this. Headless.
 */
public record PaneKeysModule() implements EsModule<PaneKeysModule> {

    /** The class of statics: {@code keyDown(pane, ev)}. */
    public record PaneKeys() implements Exportable._Constant<PaneKeysModule> {}

    public static final PaneKeysModule INSTANCE = new PaneKeysModule();

    @Override
    public ImportsFor<PaneKeysModule> imports() {
        return ImportsFor.<PaneKeysModule>builder()
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PaneKeysModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PaneKeys()));
    }
}
