package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The row of chips over a pane. {@code createTabStrip(branch, {onAdd?,
 * onDrop})} builds the strip — chips with a label and a cross, the tail
 * with the add button and the count, the drop mark — and the drag that
 * reorders: a drop lands at the count of the other chips whose middle is
 * left of the pointer, never before the pinned ones. It holds no tab
 * state; the pane arranges, selects and counts, and turns a drop into a
 * move.
 */
public record TabStrip() implements DomModule<TabStrip> {

    public record createTabStrip() implements Exportable._Constant<TabStrip> {}

    public static final TabStrip INSTANCE = new TabStrip();

    @Override
    public ImportsFor<TabStrip> imports() {
        return ImportsFor.<TabStrip>builder()
                .add(new ModuleImports<>(List.of(
                        new PaneStyles.mtp_strip(),
                        new PaneStyles.mtp_strip_tail(),
                        new PaneStyles.mtp_add(),
                        new PaneStyles.mtp_add_off(),
                        new PaneStyles.mtp_pill(),
                        new PaneStyles.mtp_drop_mark(),
                        new PaneStyles.mtp_chip(),
                        new PaneStyles.mtp_chip_label(),
                        new PaneStyles.mtp_chip_close(),
                        new PaneStyles.mtp_chip_dragging()
                ), PaneStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TabStrip> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createTabStrip()));
    }
}
