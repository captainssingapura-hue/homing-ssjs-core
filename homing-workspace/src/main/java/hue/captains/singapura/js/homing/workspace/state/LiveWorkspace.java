package hue.captains.singapura.js.homing.workspace.state;

import java.util.Collection;

/**
 * The observable surface of a running workspace — what
 * {@link WorkspaceCapture} reads to build a {@link WorkspaceState}
 * snapshot.
 *
 * <p>Read-only by design: the capture pass never mutates the live
 * workspace. The interface partitions cleanly along the three
 * conceptual axes — {@link #layout()} (axis 1), {@link #widgets()}
 * (axes 2 + 3 unified per the inverse-mapping redesign),
 * {@link #chrome()} (chrome state) — so the workspace shell can wire
 * per-axis capture caches against per-axis events (SplitPane events for
 * layout, MultiTabPane events for linkage, widget
 * {@code notifyParamsChanged} for identity+params, ribbon events for
 * chrome).</p>
 *
 * <p>Live workspaces in the running framework implement this; mock
 * implementations (records, in tests) exercise the capture logic
 * without a DOM. The capture function is total: any
 * {@code LiveWorkspace} that satisfies the cross-widget invariants of
 * {@link WorkspaceState} produces a valid snapshot.</p>
 *
 * @since RFC 0029 cycle 2
 */
public interface LiveWorkspace {

    /** Registry-key naming what kind of workspace this is. */
    WorkspaceKind kind();

    /** The SplitPane tree as it currently is. */
    LayoutNode layout();

    /**
     * Every widget instance currently held by the workspace — in any pane
     * or in the transit modal. Order is not significant; the capture pass
     * groups by {@link WidgetLocation} as needed.
     */
    Collection<LiveWidget> widgets();

    /** Current chrome state (active theme, fullscreen flag, …). */
    ChromeState chrome();
}
