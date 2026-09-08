package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
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
 * nobody — the same three-pane IDE layout serves a document studio and a trading
 * desk, which is why {@link PaneArrangements} ships shapes rather than
 * arrangements. An <b>allocation</b> names widget kinds, and widget kinds belong
 * to a workspace kind: a spec can only place what its own
 * {@code widgetEntries()} declares.</p>
 *
 * <p>So the shape is reusable and the allocation is not, and putting them in one
 * type made the reusable half unreachable — a consumer had to accept the widgets
 * baked into a shipped design or rebuild the playbook.</p>
 *
 * <pre>{@code
 * PaneArrangements.IDE.allocate()
 *         .place("explorer", "TreeWidget")
 *         .place("editor",   "DocContentWidget")
 *         .place("terminal", "SummaryWidget")
 *         .build();
 * }</pre>
 *
 * <h2>A seed, not a template</h2>
 *
 * <p>D10 — an arrangement applies only to a workspace with no saved state. A saved
 * layout always wins, and editing a spec never reshapes an existing workspace.</p>
 *
 * @param panes   the shape
 * @param widgets widget simpleNames per pane, in mount order
 *
 * @since RFC 0060
 */
public record Arrangement(PaneArrangement panes,
                          Map<PaneId, List<String>> widgets) implements ValueObject {

    public Arrangement {
        Objects.requireNonNull(panes,   "Arrangement.panes");
        Objects.requireNonNull(widgets, "Arrangement.widgets");
        var copy = new LinkedHashMap<PaneId, List<String>>();
        widgets.forEach((k, v) -> {
            if (!panes.hasPane(k)) {
                throw new IllegalArgumentException(
                        "Arrangement: pane '" + k.value() + "' is not in shape '" + panes.name()
                      + "' — panes are " + panes.panes().stream().map(PaneId::value).toList());
            }
            copy.put(k, List.copyOf(v));
        });
        widgets = Map.copyOf(copy);
    }

    /** The shape's name — what a shared arrangement is referenced by. */
    public String name() { return panes.name(); }

    /** Widgets bound to one pane, or empty. */
    public List<String> widgetsIn(PaneId pane) {
        return widgets.getOrDefault(pane, List.of());
    }

    /** Total widgets across every pane — what {@code maxTabs()} is checked against (D12). */
    public int totalWidgets() {
        return widgets.values().stream().mapToInt(List::size).sum();
    }

    /** A shape with nothing in it — the shape alone, allocated to no widgets. */
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
        private final Map<PaneId, List<String>> widgets = new LinkedHashMap<>();

        Builder(PaneArrangement panes) { this.panes = panes; }

        /** Bind widgets to a pane, in mount order. Repeated calls append. */
        public Builder place(String pane, String... widgetSimpleNames) {
            var id = new PaneId(pane);
            if (!panes.hasPane(id)) {
                throw new IllegalArgumentException(
                        "place: no pane named '" + pane + "' in shape '" + panes.name()
                      + "' — panes are " + panes.panes().stream().map(PaneId::value).toList());
            }
            var list = widgets.computeIfAbsent(id, k -> new ArrayList<>());
            for (String w : widgetSimpleNames) {
                list.add(Objects.requireNonNull(w, "place: widget simpleName"));
            }
            return this;
        }

        public Arrangement build() { return new Arrangement(panes, widgets); }
    }
}
