package hue.captains.singapura.js.homing.ui.split;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The splitter. {@code mountSplitPane({branch, host, layout, minPanePx?,
 * onEvent?})} renders a tree of panes — side by side or stacked, each split
 * sharing its space by ratio, a draggable divider between neighbours — on a
 * child of the caller's branch, and returns the leaves by slot id as the
 * hosts for whatever fills them, the layout as it stands, and
 * {@code setRatios}.
 *
 * <p>The tree is the caller's and fixed for the splitter's life; the shares
 * are the user's, and every change is one {@link SplitEvent} on
 * {@code onEvent}. Each share is a custom property on its child, read by the
 * child's class; the divider is the design's spine or divider line with a
 * handle around it. Splitting and merging at runtime come with the
 * multi-tab pane that needs them. Built fresh beside the studio's
 * {@code SplitPaneModule}, which stays as it is.</p>
 */
public record SplitPane() implements DomModule<SplitPane> {

    public record mountSplitPane() implements Exportable._Constant<SplitPane> {}

    public static final SplitPane INSTANCE = new SplitPane();

    @Override
    public ImportsFor<SplitPane> imports() {
        return ImportsFor.<SplitPane>builder()
                .add(new ModuleImports<>(List.of(new SplitEventsModule.SplitEvents()), SplitEventsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new SplitStyles.sp_root(),
                        new SplitStyles.sp_split(),
                        new SplitStyles.sp_split_h(),
                        new SplitStyles.sp_split_v(),
                        new SplitStyles.sp_child(),
                        new SplitStyles.sp_child_h(),
                        new SplitStyles.sp_child_v(),
                        new SplitStyles.sp_leaf(),
                        new SplitStyles.sp_divider(),
                        new SplitStyles.sp_divider_h(),
                        new SplitStyles.sp_divider_v(),
                        new SplitStyles.sp_divider_dragging()
                ), SplitStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<SplitPane> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new mountSplitPane()));
    }
}
