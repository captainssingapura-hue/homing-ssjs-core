package hue.captains.singapura.js.homing.ui.splitgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import java.util.List;

/**
 * {@code SplitGrid}: a tree of rows and columns of cells sharing their space
 * by ratio — the tracks — with a draggable divider between neighbours. A
 * container in the relation grid's sense: the owner mints what goes in a
 * cell ({@code cell(id)} is the box to fill), the grid arranges the cells,
 * subdivides and removes them on request, and reports every change of
 * arrangement as one {@code SplitGridEvents} object. It never knows what a
 * cell holds. A branch component.
 */
public record SplitGridModule() implements DomModule<SplitGridModule> {

    /** The class: {@code new SplitGrid(branch, {host, layout, minCellPx?, onEvent?})}; {@code cell}, {@code cells}, {@code layout}, {@code setRatios}, {@code subdivide}, {@code remove}, {@code dispose}. */
    public record SplitGrid() implements Exportable._Constant<SplitGridModule> {}

    public static final SplitGridModule INSTANCE = new SplitGridModule();

    @Override
    public ImportsFor<SplitGridModule> imports() {
        return ImportsFor.<SplitGridModule>builder()
                .add(new ModuleImports<>(List.of(new SplitGridEventsModule.SplitGridEvents()), SplitGridEventsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new SplitGridTreeModule.SplitGridTree()), SplitGridTreeModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new SplitGridStyles.sg_root(),
                        new SplitGridStyles.sg_split(),
                        new SplitGridStyles.sg_split_h(),
                        new SplitGridStyles.sg_split_v(),
                        new SplitGridStyles.sg_child(),
                        new SplitGridStyles.sg_child_h(),
                        new SplitGridStyles.sg_child_v(),
                        new SplitGridStyles.sg_cell(),
                        new SplitGridStyles.sg_divider(),
                        new SplitGridStyles.sg_divider_h(),
                        new SplitGridStyles.sg_divider_v(),
                        new SplitGridStyles.sg_divider_lit()
                ), SplitGridStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<SplitGridModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new SplitGrid()));
    }
}
