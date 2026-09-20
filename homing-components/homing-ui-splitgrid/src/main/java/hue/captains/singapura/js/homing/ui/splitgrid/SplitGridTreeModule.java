package hue.captains.singapura.js.homing.ui.splitgrid;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import java.util.List;

/**
 * {@code SplitGridTree}: the grid's arrangement as data, and the algebra on
 * it — validate a layout, copy it, subdivide a cell, remove one, find a cell's
 * place — with no element in sight. Headless, so the arrangement can be
 * reasoned about, persisted and replayed apart from any grid.
 */
public record SplitGridTreeModule() implements EsModule<SplitGridTreeModule> {

    /** The class of statics: {@code validate}, {@code copy}, {@code subdivide}, {@code remove}, {@code find}, {@code cells}. */
    public record SplitGridTree() implements Exportable._Constant<SplitGridTreeModule> {}

    public static final SplitGridTreeModule INSTANCE = new SplitGridTreeModule();

    @Override public ImportsFor<SplitGridTreeModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<SplitGridTreeModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new SplitGridTree()));
    }
}
