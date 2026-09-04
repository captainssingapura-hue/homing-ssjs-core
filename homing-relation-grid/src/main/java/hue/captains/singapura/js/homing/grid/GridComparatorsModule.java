package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050-ext6 — the stock comparators a Relation declares for its columns
 * through {@code columnMeta(column).compare}. A comparator is MANDATORY to
 * sort: {@code GridViewState} refuses {@code sortBy} on a column whose meta
 * supplies none, and there is deliberately no fallback to raw {@code <} —
 * the bug these retire sorted enums alphabetically, text by code point, and
 * nulls wherever they fell.
 *
 * <p>Three orderings: {@code compareNumbers} (numeric, NaN last);
 * {@code compareText} (natural — case- and accent-folded, digit runs by
 * value, so {@code item 2} precedes {@code item 10}; deliberately not a
 * locale collator, so it orders identically headless and in every browser);
 * {@code compareByOrder(options)} (an enum's declared order). The grid, not
 * the comparator, handles absent values — set aside, appended last in both
 * directions — and direction.</p>
 */
public record GridComparatorsModule() implements DomModule<GridComparatorsModule> {

    public record compareNumbers() implements Exportable._Constant<GridComparatorsModule> {}
    public record compareText()    implements Exportable._Constant<GridComparatorsModule> {}
    public record compareByOrder() implements Exportable._Constant<GridComparatorsModule> {}

    public static final GridComparatorsModule INSTANCE = new GridComparatorsModule();

    @Override
    public ImportsFor<GridComparatorsModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridComparatorsModule> exports() {
        return new ExportsOf<>(INSTANCE,
                List.of(new compareNumbers(), new compareText(), new compareByOrder()));
    }
}
