package hue.captains.singapura.js.homing.workspace.state;

import java.util.Comparator;
import java.util.Objects;

/**
 * RFC 0029 cycle 3 — the restore pass. Replays a {@link WorkspaceState}
 * snapshot against a {@link WorkspaceBuilder}, reconstructing the live
 * workspace in a deterministic order.
 *
 * <h2>Order of operations</h2>
 *
 * <ol>
 *   <li>{@link WorkspaceBuilder#resetLayout(LayoutNode) Layout} — first,
 *       so the panes exist before widgets need to land in them.</li>
 *   <li>{@link WorkspaceBuilder#openInPane Widgets in panes} — second,
 *       sorted by {@code paneId} (lexicographic) then {@code tabIndex}
 *       (ascending). Sorting makes the construction order
 *       reproducible across runs and makes tab strips assemble in
 *       left-to-right order regardless of map iteration whims.</li>
 *   <li>{@link WorkspaceBuilder#openInModal Modal widget} — after pane
 *       widgets, so the modal opens against a fully-assembled
 *       workspace background.</li>
 *   <li>{@link WorkspaceBuilder#applyChrome Chrome} — last, so theme
 *       switching and fullscreen toggling don't thrash construction.</li>
 * </ol>
 *
 * <h2>Pure</h2>
 *
 * <p>Pure-functional algorithm over the snapshot + builder; no I/O, no
 * globals, no time. The builder is the only side-effect surface; tests
 * use recording builders to assert on the call sequence.</p>
 *
 * @since RFC 0029 cycle 3
 */
public final class WorkspaceRestore {

    private WorkspaceRestore() {}

    /**
     * Replay {@code state} against {@code builder} in the canonical
     * restore order described above.
     *
     * <p>The {@code state} argument has already passed
     * {@link WorkspaceState}'s compact-constructor validation, so the
     * algorithm trusts its cross-widget invariants (single-modal, single
     * active per pane, pane references resolve, no slot collisions).
     * The builder receives a clean sequence of construction calls.</p>
     */
    public static void restoreState(WorkspaceState state, WorkspaceBuilder builder) {
        Objects.requireNonNull(state,   "restoreState: state");
        Objects.requireNonNull(builder, "restoreState: builder");

        // 1. Layout first — panes must exist before widgets land in them.
        builder.resetLayout(state.layout());

        // 2. Widgets in deterministic order: InPane first (sorted), InModal last.
        state.widgetsById().values().stream()
                .sorted(RESTORE_ORDER)
                .forEach(instance -> {
                    switch (instance.location()) {
                        case WidgetLocation.InPane  p -> builder.openInPane(instance, p);
                        case WidgetLocation.InModal m -> builder.openInModal(instance, m);
                    }
                });

        // 3. Chrome last — theme/fullscreen apply once the workspace is built.
        builder.applyChrome(state.chrome());
    }

    /**
     * Canonical restore ordering — {@link WidgetLocation.InPane} entries
     * before {@link WidgetLocation.InModal}; within {@code InPane}, by
     * {@link PaneId} then {@code tabIndex}.
     */
    private static final Comparator<WidgetInstance> RESTORE_ORDER = (a, b) -> switch (a.location()) {
        case WidgetLocation.InPane pa -> switch (b.location()) {
            case WidgetLocation.InPane pb -> {
                int paneCmp = pa.paneId().value().compareTo(pb.paneId().value());
                yield paneCmp != 0 ? paneCmp : Integer.compare(pa.tabIndex(), pb.tabIndex());
            }
            case WidgetLocation.InModal m -> -1;    // pane sorts before modal
        };
        case WidgetLocation.InModal ma -> switch (b.location()) {
            case WidgetLocation.InPane pb -> 1;     // modal sorts after pane
            case WidgetLocation.InModal mb -> 0;    // at most one — degenerate equal
        };
    };
}
