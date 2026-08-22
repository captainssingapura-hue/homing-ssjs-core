package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 — the EffectiveTypes gating BULK EDITING: strong, fine-grained
 * value types with identity ({@code name} — homogeneity compares names, so
 * {@code price} ≠ {@code calories} even though both are numbers), the
 * opening characters that start a virtual session, the session mode
 * (instantaneous vs buffered), and VALIDATION ({@code parse}). Display
 * formatting stays in the cell; what a value may BE lives here.
 *
 * <p>Factories: {@code textType}, {@code numberType(name, {integer,min,max})},
 * {@code enumType(name, options)} (prefix-matching), and
 * {@code instantType(name, chars)} — the mine-tile mode where the opening
 * keystroke is the complete value.</p>
 */
public record GridCellTypesModule() implements DomModule<GridCellTypesModule> {

    public record textType()    implements Exportable._Constant<GridCellTypesModule> {}
    public record numberType()  implements Exportable._Constant<GridCellTypesModule> {}
    public record enumType()    implements Exportable._Constant<GridCellTypesModule> {}
    public record instantType() implements Exportable._Constant<GridCellTypesModule> {}

    public static final GridCellTypesModule INSTANCE = new GridCellTypesModule();

    @Override
    public ImportsFor<GridCellTypesModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridCellTypesModule> exports() {
        return new ExportsOf<>(INSTANCE,
                List.of(new textType(), new numberType(), new enumType(), new instantType()));
    }
}
