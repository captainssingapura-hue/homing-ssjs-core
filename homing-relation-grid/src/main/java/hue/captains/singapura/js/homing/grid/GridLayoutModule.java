package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 — {@code GridLayout}: the Relation Grid's <b>layout branch</b>.
 * Owns the {@code <table>} skeleton, the header band, and every {@code <td>}
 * CellSlot host (the FocusManager-wrapper pattern applied one level down).
 * Positional only — it addresses everything by {@code (i, j)} and never sees a
 * PK or a column name as identity; headers arrive as display labels.
 *
 * <p>It never touches cell content: slot children belong to the cells branch,
 * and after every {@code render()} the facade re-places content into the fresh
 * slots. Like MultiTabPane / SplitPane it is a PRIMITIVE — raw DOM internally,
 * styled by an injected stylesheet built purely from theme tokens.</p>
 */
public record GridLayoutModule() implements DomModule<GridLayoutModule> {

    /** The single export — the {@code GridLayout} JS class. */
    public record GridLayout() implements Exportable._Constant<GridLayoutModule> {}

    public static final GridLayoutModule INSTANCE = new GridLayoutModule();

    @Override
    public ImportsFor<GridLayoutModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridLayoutModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridLayout()));
    }
}
