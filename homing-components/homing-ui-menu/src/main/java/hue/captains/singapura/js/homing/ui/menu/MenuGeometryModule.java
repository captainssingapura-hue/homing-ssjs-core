package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import java.util.List;

/**
 * {@code MenuGeometry}: where a menu goes — a frame at a point kept within
 * the viewport, a submenu beside its row — and where its cursor goes, the
 * next enabled row with wrap. Headless, so the steward draws what it says
 * and the rules are tested without a browser.
 */
public record MenuGeometryModule() implements EsModule<MenuGeometryModule> {

    /** The class of statics: {@code place}, {@code beside}, {@code step}. */
    public record MenuGeometry() implements Exportable._Constant<MenuGeometryModule> {}

    public static final MenuGeometryModule INSTANCE = new MenuGeometryModule();

    @Override public ImportsFor<MenuGeometryModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<MenuGeometryModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new MenuGeometry()));
    }
}
