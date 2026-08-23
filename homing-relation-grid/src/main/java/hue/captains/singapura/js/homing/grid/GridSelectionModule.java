package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 — {@code GridSelection}: the RFC 0049 selection pipeline one level
 * down. Intent held in identity space (cursor {@code {pk, column}}, ranges as
 * anchor/focus identity pairs); a TOTAL resolver recomputes the view-space
 * answer after every event (cursor self-heals by identity, then position
 * clamp, then origin); a reconciler hands the paint set to the layout. This
 * module NEVER touches DOM — the module-map boundary rule.
 *
 * <p>Settles decision D5 (v1 lean): a view remap resets the ranges; only the
 * cursor survives, by identity when visible, else by clamp — total, so some
 * cell is always the cursor while any cell exists (the exactly-one
 * invariant).</p>
 */
public record GridSelectionModule() implements DomModule<GridSelectionModule> {

    /** The single export — the {@code GridSelection} JS class. */
    public record GridSelection() implements Exportable._Constant<GridSelectionModule> {}

    public static final GridSelectionModule INSTANCE = new GridSelectionModule();

    @Override
    public ImportsFor<GridSelectionModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridSelectionModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridSelection()));
    }
}
