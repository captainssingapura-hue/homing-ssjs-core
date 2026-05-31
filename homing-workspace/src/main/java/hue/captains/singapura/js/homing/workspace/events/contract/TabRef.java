package hue.captains.singapura.js.homing.workspace.events.contract;

import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.js.homing.workspace.state.WidgetInstanceId;

import java.util.Objects;

/**
 * Reference to one workspace-active tab — the widget instance plus where
 * it sits. Used by the {@link WorkspaceEventPayload.WorkspaceActiveChanged}
 * variant's {@code from} and {@code to} fields.
 *
 * <p>Carries the widget UUID alongside the structural path so the replay
 * vocabulary can locate the tab by uuid regardless of where it sits at
 * the time of replay (paneId paths can shift as splits/merges land in
 * intervening events).</p>
 *
 * @param widgetInstanceId UUID of the widget under the tab
 * @param paneId           pane path where the tab sat when this ref was captured
 * @param tabIndex         strip position when captured
 *
 * @since RFC 0035 P1
 */
public record TabRef(
        WidgetInstanceId widgetInstanceId,
        PaneId           paneId,
        int              tabIndex
) {

    public TabRef {
        Objects.requireNonNull(widgetInstanceId, "TabRef.widgetInstanceId");
        Objects.requireNonNull(paneId,           "TabRef.paneId");
        if (tabIndex < 0) {
            throw new IllegalArgumentException("TabRef.tabIndex: must be non-negative, got " + tabIndex);
        }
    }
}
