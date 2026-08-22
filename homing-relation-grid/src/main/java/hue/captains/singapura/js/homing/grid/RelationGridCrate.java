package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-relation-grid}: the Relation
 * Grid primitive family (RFC 0050). One Crate per Maven module; every grid
 * module is declared here with its type, so conformance is <b>ambient from
 * the first line</b> — strict-for-new gates each module on the build that
 * introduces it (the RFC 0050 journey's Phase 0 standing constraint).
 *
 * <p>Phase 1: {@code GridViewMaps} (PURE_LOGIC — the identity/position seam).
 * Phase 2: the two-branch render family — {@code GridLayout} (layout branch),
 * {@code GridCells} (cells branch), {@code StockCells}, and the
 * {@code RelationGrid} facade, all PRIMITIVE (raw DOM internally, like
 * MultiTabPane / SplitPane; the cells branch object is handed in at runtime).
 * Selection / keyboard / edit / bulk-ops modules land phase by phase.</p>
 */
public final class RelationGridCrate implements Crate {

    public static final RelationGridCrate INSTANCE = new RelationGridCrate();

    private RelationGridCrate() {}

    @Override public String name() { return "homing-relation-grid"; }

    @Override public List<Crate> requires() {
        return List.of();   // Phase 1 is pure logic; DOM-facing phases add requires
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(GridViewMapsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(GridSelectionModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(GridKeyboardModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(GridLayoutModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(GridCellsModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(StockCellsModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(RelationGridModule.INSTANCE, StandardJsModuleType.PRIMITIVE));
    }
}
