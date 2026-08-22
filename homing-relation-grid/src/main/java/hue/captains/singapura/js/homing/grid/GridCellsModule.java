package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 — {@code GridCells}: the Relation Grid's <b>cells branch</b>. Owns
 * the DOM realization of every ActualCell for the cell's whole life,
 * addressed purely by {@code (PK, columnName)} — it never sees {@code (i, j)}
 * and never touches the layout; the facade hands it layout-owned slot
 * elements to place into (the single merge point of the two branches).
 *
 * <p>The DomOpsParty flagship use case: elements are minted through a
 * <b>handed-in branch</b> (a runtime object, hence no module import),
 * placement is one {@code appendChild} that preserves state, detach is not
 * disposal, and a cell dies only when its row leaves the Relation.</p>
 */
public record GridCellsModule() implements DomModule<GridCellsModule> {

    /** The single export — the {@code GridCells} JS class. */
    public record GridCells() implements Exportable._Constant<GridCellsModule> {}

    public static final GridCellsModule INSTANCE = new GridCellsModule();

    @Override
    public ImportsFor<GridCellsModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<GridCellsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new GridCells()));
    }
}
