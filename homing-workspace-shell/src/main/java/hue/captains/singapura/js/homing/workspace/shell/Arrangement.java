package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RFC 0060 — the full arrangement a workspace starts in: a {@link PaneArrangement}
 * (the shape) plus the <b>widget allocation</b> (what goes in each pane).
 *
 * <h2>Why the two are separate</h2>
 *
 * <p>They answer to different owners. A <b>shape</b> is geometry and belongs to
 * nobody — the same three-pane IDE shape serves a document studio and a trading
 * desk, which is why {@link PaneArrangements} ships shapes rather than
 * arrangements. An <b>allocation</b> names widget kinds, and widget kinds belong
 * to a workspace kind: a spec can only place what its own
 * {@code widgetEntries()} declares.</p>
 *
 * <p>With both in one type the reusable half was unreachable — a consumer had to
 * accept the widgets baked into a shipped design or rebuild the playbook.</p>
 *
 * <pre>{@code
 * PaneArrangements.IDE.allocate()
 *         .place(Ide.EXPLORER, TreeWidget.class)
 *         .place(Ide.EDITOR,   DocContentWidget.class)
 *         .build();
 * }</pre>
 *
 * <h2>Both sides of a placement are typed</h2>
 *
 * <p>The pane is a {@link ShapePane}, obtained from the shape that owns it, so it
 * cannot name a pane the shape lacks and cannot be confused with a live
 * {@link PaneId}. The widget is its {@code Class}, which a spec already uses to
 * declare it in {@code WidgetEntry.of(TreeWidget.class, …)}. Both were strings,
 * and a string here fails the way strings fail — the pane comes up empty and the
 * shell logs a warning nobody reads.</p>
 *
 * <h2>A seed, not a template</h2>
 *
 * <p>D10 — an arrangement applies only to a workspace with no saved state. A saved
 * layout always wins, and editing a spec never reshapes an existing workspace.</p>
 *
 * @param panes   the shape
 * @param widgets widget classes per pane, in mount order
 *
 * @since RFC 0060
 */
public record Arrangement(PaneArrangement panes,
                          Map<ShapePane, List<Class<? extends WorkspaceWidget<?, ?>>>> widgets)
        implements ValueObject {

    public Arrangement {
        Objects.requireNonNull(panes,   "Arrangement.panes");
        Objects.requireNonNull(widgets, "Arrangement.widgets");
        var copy = new LinkedHashMap<ShapePane, List<Class<? extends WorkspaceWidget<?, ?>>>>();
        widgets.forEach((pane, list) -> {
            if (!panes.name().equals(pane.shapeName()) || !panes.hasPane(pane.asSeededPaneId())) {
                throw new IllegalArgumentException(
                        "Arrangement: " + pane + " does not belong to shape '" + panes.name()
                      + "' — panes are " + panes.panes().stream().map(PaneId::value).toList());
            }
            copy.put(pane, List.copyOf(list));
        });
        // NOT Map.copyOf — it hashes, and the declaration order of placements is
        // what an error message and a walk over the allocation should follow.
        // WidgetEntry.defaults notes the same trap for the same reason.
        widgets = Collections.unmodifiableMap(copy);
    }

    /** The shape's name — what a shared arrangement is referenced by. */
    public String name() { return panes.name(); }

    /** Widgets bound to one pane, or empty. */
    public List<Class<? extends WorkspaceWidget<?, ?>>> widgetsIn(ShapePane pane) {
        return widgets.getOrDefault(pane, List.of());
    }

    /** Widget simpleNames for one pane — the wire form the shell mounts by. */
    public List<String> widgetNamesIn(ShapePane pane) {
        return widgetsIn(pane).stream().map(Class::getSimpleName).toList();
    }

    /** Total widgets across every pane — what {@code maxTabs()} is checked against (D12). */
    public int totalWidgets() {
        return widgets.values().stream().mapToInt(List::size).sum();
    }

    /** A shape with nothing in it. */
    public static Arrangement of(PaneArrangement shape) {
        return new Arrangement(shape, Map.of());
    }

    /** Continue allocating — starts from what is already placed. */
    public Builder allocate() {
        var b = new Builder(panes);
        widgets.forEach((pane, list) -> b.widgets.put(pane, new ArrayList<>(list)));
        return b;
    }

    /** Allocation. Mutable while building; what it yields is not. */
    public static final class Builder {

        private final PaneArrangement panes;
        private final Map<ShapePane, List<Class<? extends WorkspaceWidget<?, ?>>>> widgets
                = new LinkedHashMap<>();

        Builder(PaneArrangement panes) { this.panes = panes; }

        /** Bind widgets to a pane, in mount order. Repeated calls append. */
        @SafeVarargs
        public final Builder place(ShapePane pane,
                                   Class<? extends WorkspaceWidget<?, ?>>... widgetClasses) {
            Objects.requireNonNull(pane, "place.pane");
            if (!panes.name().equals(pane.shapeName())) {
                throw new IllegalArgumentException(
                        "place: " + pane + " belongs to a different shape than '"
                      + panes.name() + "'");
            }
            if (!panes.hasPane(pane.asSeededPaneId())) {
                throw new IllegalArgumentException(
                        "place: no pane " + pane + " in shape '" + panes.name()
                      + "' — panes are " + panes.panes().stream().map(PaneId::value).toList());
            }
            var list = widgets.computeIfAbsent(pane, k -> new ArrayList<>());
            for (var w : widgetClasses) {
                list.add(Objects.requireNonNull(w, "place: widget class"));
            }
            return this;
        }

        public Arrangement build() { return new Arrangement(panes, widgets); }
    }
}
