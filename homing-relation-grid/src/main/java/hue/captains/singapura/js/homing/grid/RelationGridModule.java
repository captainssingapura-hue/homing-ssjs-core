package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0050 — {@code RelationGrid}: the facade composing the grid family, and
 * the ONLY place the two DomOpsParty branches meet. Orchestration only — map
 * logic lives in {@code GridViewMaps}, chrome in {@code GridLayout}, cell
 * ownership in {@code GridCells}; the facade reads the adapter, threads the
 * pieces, and re-places cell content into freshly-minted slots on every view
 * change. Direct updates ({@code updateCell}) go straight to the cell,
 * bypassing the positional lookup entirely.
 *
 * <p>Phase 2 surface: static render, refresh, the direct update path, and
 * base row add/remove (the only cell death). View commands, selection,
 * editing, and bulk ops land in Phases 3–6 per the RFC 0050 journey.</p>
 */
public record RelationGridModule() implements DomModule<RelationGridModule> {

    /** The single export — the {@code RelationGrid} JS class. */
    public record RelationGrid() implements Exportable._Constant<RelationGridModule> {}

    public static final RelationGridModule INSTANCE = new RelationGridModule();

    @Override
    public ImportsFor<RelationGridModule> imports() {
        return ImportsFor.<RelationGridModule>builder()
                .add(new ModuleImports<>(
                        List.of(new GridViewMapsModule.GridViewMaps()),
                        GridViewMapsModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new GridLayoutModule.GridLayout()),
                        GridLayoutModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new GridCellsModule.GridCells()),
                        GridCellsModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new StockCellsModule.TextCell(),
                                new StockCellsModule.NumberCell(),
                                new StockCellsModule.EnumCell()),
                        StockCellsModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new GridSelectionModule.GridSelection()),
                        GridSelectionModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new GridKeyboardModule.GridKeyboard()),
                        GridKeyboardModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new GridEditControllerModule.GridEditController()),
                        GridEditControllerModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<RelationGridModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelationGrid()));
    }
}
