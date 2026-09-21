package hue.captains.singapura.js.homing.ui.splitgrid;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import java.util.List;

/**
 * {@code SplitGridMirror}: a grid's arrangement drawn at a scale from the
 * geometry, with a cursor the keyboard moves from cell to cell by the
 * workspace's rule. It never touches the grid: the owner tells it the layout
 * and the box, and reads the cursor from {@code CursorMoved}. A widget
 * switcher in the making: a shortcut can raise it scaled, the arrows pick a
 * pane, and the owner makes that pane active. A branch component.
 */
public record SplitGridMirrorModule() implements DomModule<SplitGridMirrorModule> {

    /** The class: {@code new SplitGridMirror(branch, {host, scale?, onEvent?})}; {@code reflect(layout, box)}, {@code scale}, {@code cursor}, {@code rects}, {@code dispose}. */
    public record SplitGridMirror() implements BranchComponent<SplitGridMirrorModule> {
        @Override public String summary() { return "The arrangement of a grid, drawn at a scale, with a cursor the keyboard moves from cell to cell."; }
    }

    public static final SplitGridMirrorModule INSTANCE = new SplitGridMirrorModule();

    @Override
    public ImportsFor<SplitGridMirrorModule> imports() {
        return ImportsFor.<SplitGridMirrorModule>builder()
                .add(new ModuleImports<>(List.of(new SplitGridEventsModule.SplitGridEvents()), SplitGridEventsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new SplitGridTreeModule.SplitGridTree()), SplitGridTreeModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new SplitGridGeometryModule.SplitGridGeometry()), SplitGridGeometryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new SplitGridStyles.sgm_root(),
                        new SplitGridStyles.sgm_cell(),
                        new SplitGridStyles.sgm_cell_current()
                ), SplitGridStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<SplitGridMirrorModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new SplitGridMirror()));
    }
}
