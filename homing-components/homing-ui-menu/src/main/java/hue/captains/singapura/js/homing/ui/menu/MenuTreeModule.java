package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The shape of a context menu's tree in JS, and the walk over it, with no
 * DOM: the record a {@link ContextMenuRegistry} stamps — {@code { kind,
 * nodes }}, a node {@code { id, label, icon?, hint?, section?, nodes? }},
 * three levels at most — checked for what Java refuses, so a kind declared
 * through the steward's builder is held to the same rules as one declared
 * in Java. Pure logic.
 */
public record MenuTreeModule() implements EsModule<MenuTreeModule> {

    /** The class of statics: {@code MAX_DEPTH}, {@code check}, {@code rows}, {@code find}, {@code path}, {@code divided}. */
    public record MenuTree() implements Exportable._Constant<MenuTreeModule> {}

    public static final MenuTreeModule INSTANCE = new MenuTreeModule();

    @Override public ImportsFor<MenuTreeModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<MenuTreeModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new MenuTree()));
    }
}
