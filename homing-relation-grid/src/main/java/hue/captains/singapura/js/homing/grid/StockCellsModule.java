package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 — the stock cell renderers: {@code TextCell} and {@code NumberCell},
 * the plain cells most columns use. Each implements the
 * {@link hue.captains.singapura.js.homing.grid.contract.GridCellContract}
 * required set; the edit trio and {@code onAction} arrive with Phase 5.
 * {@code NumberCell} keeps the raw number for {@code getValueToCopy()} — a
 * copied price pastes as a number, never as its display formatting.
 */
public record StockCellsModule() implements DomModule<StockCellsModule> {

    /** {@code TextCell} — plain text rendering of any value. */
    public record TextCell() implements Exportable._Constant<StockCellsModule> {}

    /** {@code NumberCell} — numeric cell with optional display formatter. */
    public record NumberCell() implements Exportable._Constant<StockCellsModule> {}

    public static final StockCellsModule INSTANCE = new StockCellsModule();

    @Override
    public ImportsFor<StockCellsModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<StockCellsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TextCell(), new NumberCell()));
    }
}
