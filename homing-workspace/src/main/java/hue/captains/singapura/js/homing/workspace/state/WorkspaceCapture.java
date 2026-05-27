package hue.captains.singapura.js.homing.workspace.state;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * RFC 0029 cycle 2 — the capture pass. Walks a {@link LiveWorkspace}
 * (read-only) and produces a typed {@link WorkspaceState} snapshot
 * ready for persistence.
 *
 * <h2>Per-axis composition</h2>
 *
 * <p>Three pure helpers, one per axis — exposed as first-class entry
 * points so the workspace shell can wire per-axis capture caches
 * against per-axis events (the {@code totally different set of events}
 * insight from the design pass):</p>
 *
 * <ul>
 *   <li>{@link #captureLayout(LiveWorkspace)} — fires on SplitPane events</li>
 *   <li>{@link #captureWidgets(LiveWorkspace)} — fires on MultiTabPane events
 *       and on per-widget {@code notifyParamsChanged}</li>
 *   <li>{@link #captureChrome(LiveWorkspace)} — fires on theme / fullscreen toggles</li>
 * </ul>
 *
 * <p>{@link #captureState(LiveWorkspace, Instant)} composes all three with
 * the schema version and a capture timestamp. The compact constructor of
 * {@link WorkspaceState} performs cross-widget validation (single modal
 * slot, single active tab per pane, no tab-slot collisions, pane
 * references resolve in the layout, key/value id agreement) — those
 * invariants are the load-bearing checks; the capture pass is otherwise
 * mechanical collection.</p>
 *
 * <h2>Pure</h2>
 *
 * <p>No I/O, no DOM access, no globals. The capture pass is a pure
 * function of its {@link LiveWorkspace} argument and the supplied
 * {@link Instant}. Tests construct mock workspaces as records and assert
 * on the resulting snapshot.</p>
 *
 * @since RFC 0029 cycle 2
 */
public final class WorkspaceCapture {

    private WorkspaceCapture() {}

    /** Axis 1 — layout tree. Pass-through; the live SplitPane owns the shape. */
    public static LayoutNode captureLayout(LiveWorkspace live) {
        Objects.requireNonNull(live, "captureLayout: live");
        return live.layout();
    }

    /**
     * Axes 2 + 3 — widget identity + linkage. Materialises a typed
     * {@link WidgetInstance} per {@link LiveWidget}, keyed by stable id.
     * Duplicate ids fail loudly with a clear message (rather than the
     * generic {@code Collectors.toMap} duplicate-key exception).
     *
     * <p>Returns a {@link LinkedHashMap} so the iteration order matches
     * the live workspace's iteration order — useful for deterministic
     * test output and for diff-friendly serialisation in Cycle 4.</p>
     */
    public static Map<WidgetInstanceId, WidgetInstance> captureWidgets(LiveWorkspace live) {
        Objects.requireNonNull(live, "captureWidgets: live");
        var out = new LinkedHashMap<WidgetInstanceId, WidgetInstance>();
        for (LiveWidget lw : live.widgets()) {
            Objects.requireNonNull(lw, "captureWidgets: null widget in LiveWorkspace.widgets()");
            var instance = new WidgetInstance(
                    lw.id(), lw.kind(), lw.params(), lw.title(), lw.location());
            if (out.put(lw.id(), instance) != null) {
                throw new IllegalStateException(
                        "Duplicate widget instance id in LiveWorkspace.widgets(): " + lw.id());
            }
        }
        return out;
    }

    /** Chrome axis — pass-through. */
    public static ChromeState captureChrome(LiveWorkspace live) {
        Objects.requireNonNull(live, "captureChrome: live");
        return live.chrome();
    }

    /**
     * Compose all three axes into a typed {@link WorkspaceState}. The
     * envelope's compact constructor performs cross-widget validation —
     * if the live workspace's state violates an invariant (two widgets
     * in the modal, a widget referencing a ghost pane, …), construction
     * fails loudly here rather than silently producing a corrupt
     * snapshot.
     *
     * @param live the live workspace to capture
     * @param now  the wall-clock time to record as the capture timestamp
     */
    public static WorkspaceState captureState(LiveWorkspace live, Instant now) {
        Objects.requireNonNull(live, "captureState: live");
        Objects.requireNonNull(now,  "captureState: now");
        return new WorkspaceState(
                WorkspaceState.CURRENT_SCHEMA_VERSION,
                live.kind(),
                now,
                captureLayout(live),
                captureWidgets(live),
                captureChrome(live));
    }
}
