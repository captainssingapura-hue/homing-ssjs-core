package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 — {@code GridViewMaps}: the Relation Grid's <b>identity/position
 * seam</b>, as pure logic. The ONLY place {@code (i, j)} meets
 * {@code (PK, columnName)}: two maps ({@code i → PK} row view,
 * {@code j → columnName} column view) with view operations as pure remaps —
 * sort/filter reassign the row view, hide/reorder the column view; the
 * Relation itself never moves. Base mutations (a row entering/leaving the
 * Relation) are the only operations that change what exists.
 *
 * <p>No DOM, no imports, no infrastructure — the class unit-tests without a
 * browser, and everything identity-side above it never sees coordinates while
 * everything layout-side never sees keys (the RFC 0050 module-map boundary
 * rule this module exists to enforce).</p>
 */
public record GridViewMapsModule() implements DomModule<GridViewMapsModule> {

    /** The single export — the {@code GridViewMaps} JS class. */
    public record GridViewMaps() implements Exportable._Constant<GridViewMapsModule> {}

    public static final GridViewMapsModule INSTANCE = new GridViewMapsModule();

    @Override
    public ImportsFor<GridViewMapsModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridViewMapsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridViewMaps()));
    }
}
