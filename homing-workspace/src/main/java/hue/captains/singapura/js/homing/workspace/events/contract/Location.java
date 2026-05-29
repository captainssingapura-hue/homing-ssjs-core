package hue.captains.singapura.js.homing.workspace.events.contract;

import hue.captains.singapura.js.homing.workspace.state.PaneId;

import java.util.Objects;

/**
 * Where a tab sits inside the workspace — a structural pane path plus the
 * index of the tab within the pane's strip. Used as a field type in
 * several {@link WorkspaceEventPayload} variants ({@code
 * WidgetSpawnedFromPicker.to}, {@code TabClosed.from}, {@code
 * TabMoved.from / to}).
 *
 * <p>Hoisted to a package-level record (rather than nested inside each
 * payload variant) because the same {@code (paneId, tabIndex)} shape
 * appears in many places — define once, reuse via record composition.</p>
 *
 * @param paneId   structural path of the pane (e.g. "_1_1", "_2_2_1")
 * @param tabIndex zero-based position in the pane's tab strip
 *
 * @since RFC 0035 P1
 */
public record Location(PaneId paneId, int tabIndex) {

    public Location {
        Objects.requireNonNull(paneId, "Location.paneId");
        if (tabIndex < 0) {
            throw new IllegalArgumentException("Location.tabIndex: must be non-negative, got " + tabIndex);
        }
    }
}
