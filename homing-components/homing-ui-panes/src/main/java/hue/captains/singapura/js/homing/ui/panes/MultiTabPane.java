package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * One pane of tabs. {@code mountMultiTabPane(opts)} builds a strip of chips
 * over one content area on a child of the caller's branch, appends it to
 * the host and returns the pane: {@code addTab}, {@code attachTab},
 * {@code removeTab}, {@code detachTab}, {@code switchTab}, {@code moveTab},
 * the readers, {@code dispose}.
 *
 * <p>A tab holds a widget by the base's contract — what {@code
 * construct(branch, params)} returned, {@code {root, setActive?, dispose?}}
 * — whose root the pane appends once and never detaches; a switch hides
 * and shows panels. The pane disposes the widget on a real close and never
 * on a detach, and never calls {@code setActive}: that is the holder's,
 * told through {@code onTabActivated}.</p>
 *
 * <p>The callbacks are the studio pane's, by name and by argument shape —
 * {@code onTabAdded(slotId, tab, index)}, {@code onTabRemoved(slotId, tab,
 * fromIndex)}, {@code onTabMoved(srcSlotId, tab, srcIndex, destSlotId,
 * destIndex)}, {@code onTabActivated(slotId, tabId)}, {@code
 * onTabAttached(slotId, tab, index)}, {@code onAddTab(slotId)} — so what
 * records that pane's mutations records this one's. Built fresh beside
 * {@code MultiTabPaneModule}, which stays as it is; the strip, its chips and
 * the drag that reorders are {@link TabStrip}'s. The split of panes and the
 * drag between them are rounds of their own.</p>
 */
public record MultiTabPane() implements DomModule<MultiTabPane> {

    public record mountMultiTabPane() implements Exportable._Constant<MultiTabPane> {}

    public static final MultiTabPane INSTANCE = new MultiTabPane();

    @Override
    public ImportsFor<MultiTabPane> imports() {
        return ImportsFor.<MultiTabPane>builder()
                .add(new ModuleImports<>(List.of(new TabStrip.createTabStrip()), TabStrip.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PaneStyles.mtp_pane(),
                        new PaneStyles.mtp_content(),
                        new PaneStyles.mtp_tab_content(),
                        new PaneStyles.mtp_tab_content_hidden(),
                        new PaneStyles.mtp_empty()
                ), PaneStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MultiTabPane> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new mountMultiTabPane()));
    }
}
