package hue.captains.singapura.js.homing.workspace.state;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Persistent envelope for a single workspace — the artifact that
 * {@link WorkspaceStateStore} reads and writes, and that
 * {@link WorkspaceStateMigration} chains transform across schema bumps.
 *
 * <h2>Two storage maps, three conceptual axes</h2>
 *
 * <ol>
 *   <li><b>Layout</b> — {@link #layout()} — the SplitPane tree shape, an
 *       independent axis of evolution (split / merge / resize).</li>
 *   <li><b>Widget identity + linkage</b> — {@link #widgetsById()} — each
 *       {@link WidgetInstance} carries its own {@link WidgetLocation},
 *       so axes 2 (linkage) and 3 (identity + params) of the original
 *       three-axis sketch are unified here. One source of truth per
 *       widget; the cross-axis desynchronisation hazard goes away by
 *       construction.</li>
 *   <li><b>Chrome</b> — {@link #chrome()} — small workspace-wide
 *       framework settings.</li>
 * </ol>
 *
 * <p>The inverse mapping (widget → location) is the redesign that fell
 * out of the {@code Names Are Types} pass — every widget knows where it
 * lives. Tab order per pane is derived on demand by grouping
 * {@code widgetsById.values()} by {@link WidgetLocation.InPane#paneId()}
 * and sorting by {@link WidgetLocation.InPane#tabIndex()}.</p>
 *
 * <h2>The single transit modal slot</h2>
 *
 * <p>At most one widget across the whole workspace has
 * {@code location == WidgetLocation.InModal.INSTANCE}. This is the
 * single-slot drag-to-modal landing area, enforced structurally in this
 * record's compact constructor.</p>
 *
 * @param schemaVersion strict version of the envelope shape;
 *                      {@link WorkspaceStateMigration} chains forward to
 *                      the current version on load
 * @param workspaceKind registry key naming the kind of workspace this is
 * @param savedAt       wall-clock time at capture
 * @param layout        the SplitPane tree shape
 * @param widgetsById   widget identity + params + location, keyed by
 *                      stable instance ID
 * @param chrome        small workspace-wide settings
 * @since RFC 0029 cycle 1
 */
public record WorkspaceState(
        int                                    schemaVersion,
        WorkspaceKind                          workspaceKind,
        Instant                                savedAt,
        LayoutNode                             layout,
        Map<WidgetInstanceId, WidgetInstance>  widgetsById,
        ChromeState                            chrome
) {

    /** Current envelope schema version. */
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public WorkspaceState {
        Objects.requireNonNull(workspaceKind, "WorkspaceState.workspaceKind");
        Objects.requireNonNull(savedAt,       "WorkspaceState.savedAt");
        Objects.requireNonNull(layout,        "WorkspaceState.layout");
        Objects.requireNonNull(widgetsById,   "WorkspaceState.widgetsById");
        Objects.requireNonNull(chrome,        "WorkspaceState.chrome");
        if (schemaVersion < 1) {
            throw new IllegalArgumentException(
                    "WorkspaceState.schemaVersion must be >= 1 (got " + schemaVersion + ")");
        }
        widgetsById = Map.copyOf(widgetsById);

        // -- Cross-widget invariants ----------------------------------------
        var paneIdsInLayout = collectPaneIds(layout);
        var occupied = new HashSet<TabSlot>();              // (paneId, tabIndex) uniqueness
        var activeByPane = new HashSet<PaneId>();           // at-most-one-active per pane
        boolean modalSeen = false;                          // single-transit-modal slot

        for (var entry : widgetsById.entrySet()) {
            // Key must match the WidgetInstance's own id (no key/value drift)
            if (!entry.getKey().equals(entry.getValue().id())) {
                throw new IllegalArgumentException(
                        "WorkspaceState.widgetsById key " + entry.getKey()
                      + " does not match WidgetInstance.id " + entry.getValue().id());
            }
            var loc = entry.getValue().location();
            switch (loc) {
                case WidgetLocation.InPane p -> {
                    if (!paneIdsInLayout.contains(p.paneId())) {
                        throw new IllegalArgumentException(
                                "WidgetInstance " + entry.getKey()
                              + " references pane " + p.paneId()
                              + " that does not exist in the layout tree");
                    }
                    var slot = new TabSlot(p.paneId(), p.tabIndex());
                    if (!occupied.add(slot)) {
                        throw new IllegalArgumentException(
                                "Multiple widgets occupy the same tab slot: " + slot);
                    }
                    if (p.isActive() && !activeByPane.add(p.paneId())) {
                        throw new IllegalArgumentException(
                                "Multiple widgets marked active in pane " + p.paneId());
                    }
                }
                case WidgetLocation.InModal ignored -> {
                    if (modalSeen) {
                        throw new IllegalArgumentException(
                                "Multiple widgets in the single transit modal slot");
                    }
                    modalSeen = true;
                }
            }
        }
    }

    /** Internal: tuple for uniqueness checking in the constructor. */
    private record TabSlot(PaneId paneId, int tabIndex) {}

    /** Walk the layout tree and collect every leaf's paneId. */
    private static Set<PaneId> collectPaneIds(LayoutNode node) {
        var out = new HashSet<PaneId>();
        collectInto(node, out);
        return out;
    }

    private static void collectInto(LayoutNode node, Set<PaneId> out) {
        switch (node) {
            case LayoutNode.Leaf  l -> out.add(l.paneId());
            case LayoutNode.Split s -> { collectInto(s.first(), out); collectInto(s.second(), out); }
        }
    }
}
