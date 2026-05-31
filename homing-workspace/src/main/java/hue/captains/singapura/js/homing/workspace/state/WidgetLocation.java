package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;

/**
 * Where a widget instance currently lives in the workspace. The inverse
 * mapping that the redesign called for — each {@link WidgetInstance}
 * names its own location, rather than the workspace state holding a
 * {@code Map<PaneId, ListOfWidgetIds>} separately from the widgets
 * themselves. One source of truth per widget; the cross-axis
 * desynchronisation hazard disappears by construction.
 *
 * <h2>Sealed</h2>
 *
 * <p>Two variants: {@link InPane} (the common case, with tab position and
 * active flag) and {@link InModal} (the single transit slot — at most one
 * widget across the whole workspace). Future location kinds (e.g. a
 * stash / trash area) would be sealed extensions of this interface.</p>
 *
 * <h2>The slight coupling</h2>
 *
 * <p>Most events touch one location's fields ({@code tabIndex} on tab
 * reorder, {@code isActive} on tab activation). One event is special:
 * <b>drag-start</b> transitions a widget from {@link InPane} to
 * {@link InModal} (or back). The single drag-start handler owns this
 * transition; no other event handler may construct {@link InModal} or
 * remove a widget from a pane's tab order. Naming this asymmetry
 * explicitly is what keeps the rest of the design orthogonal.</p>
 *
 * @since RFC 0029 cycle 1
 */
public sealed interface WidgetLocation permits WidgetLocation.InPane, WidgetLocation.InModal {

    /**
     * Widget is in a tab strip — the common case. Carries the pane it
     * lives in, its position within that pane's tab strip, and whether
     * it's the currently active tab in that pane.
     *
     * @param paneId    the leaf pane this widget is in; must resolve in
     *                  the {@link WorkspaceState#layout()} tree
     * @param tabIndex  zero-based position in the pane's tab strip;
     *                  must be unique within {@code paneId}
     * @param isActive  whether this is the active tab in {@code paneId};
     *                  at most one widget per pane has this true
     */
    record InPane(PaneId paneId, int tabIndex, boolean isActive) implements WidgetLocation {
        public InPane {
            Objects.requireNonNull(paneId, "WidgetLocation.InPane.paneId");
            if (tabIndex < 0) {
                throw new IllegalArgumentException(
                        "WidgetLocation.InPane.tabIndex must be >= 0 (got " + tabIndex + ")");
            }
        }
    }

    /**
     * Widget is in the workspace's single transit modal — the drag-to-modal
     * landing slot. At most one widget across the whole workspace has this
     * location; the invariant is enforced in {@link WorkspaceState}'s
     * compact constructor.
     *
     * <p>By design carries no payload — being in the modal is the
     * entire claim. Single-slot, transient, no metadata.</p>
     */
    record InModal() implements WidgetLocation {
        public static final InModal INSTANCE = new InModal();
    }
}
