package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * {@code TabStrip}, the row of chips over a pane. {@code new TabStrip(branch,
 * {onAdd?, onDrop, onDragOut?})} builds the strip — chips with a label and a
 * cross, the tail with the add button and the count, the mark for a tab
 * offered from outside — and the drag that is a browser's, along a rail:
 * the chip pressed goes where the hand goes along the row, the others
 * stepping aside live, and settles on the slot it is nearest, never before
 * the pinned ones. It holds no tab state; the pane arranges, selects and
 * counts, and turns a drop into a move.
 */
public record TabStripModule() implements DomModule<TabStripModule> {

    /** The class. */
    public record TabStrip() implements BranchComponent<TabStripModule> {
        @Override public String summary() { return "The strip of chips on its own: selection by a press, and the hand that drags a chip along the rail; no chip takes native focus, and the keys over a strip are its pane's."; }
    }

    public static final TabStripModule INSTANCE = new TabStripModule();

    @Override
    public ImportsFor<TabStripModule> imports() {
        return ImportsFor.<TabStripModule>builder()
                .add(new ModuleImports<>(List.of(
                        new PaneStyles.mtp_strip(),
                        new PaneStyles.mtp_rail(),
                        new PaneStyles.mtp_strip_loose(),
                        new PaneStyles.mtp_strip_tail(),
                        new PaneStyles.mtp_rail_add(),
                        new PaneStyles.mtp_add_off(),
                        new PaneStyles.mtp_pill(),
                        new PaneStyles.mtp_drop_mark(),
                        new PaneStyles.mtp_chip(),
                        new PaneStyles.mtp_chip_label(),
                        new PaneStyles.mtp_strip_current(),
                        new PaneStyles.mtp_chip_mark(),
                        new PaneStyles.mtp_chip_mark_on(),
                        new PaneStyles.mtp_chip_lifted(),
                        new PaneStyles.mtp_chip_close(),
                        new PaneStyles.mtp_chip_seated(),
                        new PaneStyles.mtp_chip_dragging(),
                        new PaneStyles.mtp_chip_shifted()
                ), PaneStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new TabHandModule.TabHand()), TabHandModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new TabDragModule.TabDrag()), TabDragModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new TabWindowModule.TabWindow()), TabWindowModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TabStripModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TabStrip()));
    }
}
