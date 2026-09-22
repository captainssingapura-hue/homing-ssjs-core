package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code PaneMenus}: the menus a pane offers and where they are asked for —
 * a chip's, by right-click or by the menu key at the chip; and the strip's
 * own ground, whose kind is the page's, since what it offers is about the
 * room the pane sits in and not about the pane. Headless statics on a pane,
 * so the rules are read and tested without a browser.
 */
public record PaneMenusModule() implements EsModule<PaneMenusModule> {

    /** The class of statics: {@code forChip}, {@code byKey}, {@code onGround}. */
    public record PaneMenus() implements Exportable._Constant<PaneMenusModule> {}

    public static final PaneMenusModule INSTANCE = new PaneMenusModule();

    @Override public ImportsFor<PaneMenusModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<PaneMenusModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PaneMenus()));
    }
}
