package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050-ext2 — {@code GridViewState}: the VIRTUAL LAYER as first-class,
 * serializable state. Owns everything the user arranged — column order,
 * hidden set, widths, sort, filters — as held intent (the Phase 3 rule) and
 * recomputes the view maps from it. Filters are DECLARATIVE
 * {@code {column, op, operand}} criteria compiled at apply time, because a
 * predicate function cannot be serialized; the raw-predicate escape hatch
 * remains, marked ephemeral (never in a snapshot, never restored). Widths
 * key on {@code columnName}, so they follow their column through hide /
 * show / reorder for free. {@code snapshot()} / {@code apply(vs)} is the
 * round-trip — drift-tolerant, recording intent and never the permutation
 * it produced, so a restore re-derives against current data. No DOM.
 */
public record GridViewStateModule() implements DomModule<GridViewStateModule> {

    /** The single export — the {@code GridViewState} JS class. */
    public record GridViewState() implements Exportable._Constant<GridViewStateModule> {}

    public static final GridViewStateModule INSTANCE = new GridViewStateModule();

    @Override
    public ImportsFor<GridViewStateModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridViewStateModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridViewState()));
    }
}
