package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.state.LayoutNode;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RFC 0060 — the arrangement a workspace starts in: its panes, and the widgets
 * in each.
 *
 * <p>Built as a <b>playbook</b> — the sequence of splits a person would have
 * performed, in the vocabulary the runtime already records when they do
 * ({@code SplitCreated(paneId, orientation, newRatio)}):</p>
 *
 * <pre>{@code
 * Arrangement.named("editor-and-output")
 *         .root("main")
 *         .splitWithRatio("main", PaneDirection.RIGHT, "side", 0.70)
 *         .splitEvenly("side", PaneDirection.DOWN, "output")
 *         .place("main",   "DocViewWidget")
 *         .place("output", "LogWidget")
 *         .build();
 * }</pre>
 *
 * <h2>The ratio is the share the split pane KEEPS</h2>
 *
 * <p>This is the one thing an author can get wrong, so the method says it: in the
 * example {@code main} keeps 0.70 of the whole and {@code side} takes 0.30, and
 * splitting {@code side} evenly then gives two panes of <b>0.15 each of the
 * workspace</b>. A ratio is local to its split, exactly as the event stores it and
 * exactly as a person experiences dragging a divider — which means these numbers
 * read as absolute and are not.</p>
 *
 * <p>{@link LayoutNode.Split} stores the <i>first</i> child's share, so a
 * {@link PaneDirection#LEFT} or {@link PaneDirection#UP} split is recorded as
 * {@code 1 - keep}. The author never sees that; it is exactly the arithmetic
 * mistake this type exists to absorb.</p>
 *
 * <h2>A seed, not a template</h2>
 *
 * <p>RFC 0060 D10 — an arrangement applies only to a workspace with no saved
 * state. A saved layout always wins, and editing a spec never reshapes an
 * existing workspace.</p>
 *
 * @param name    identifies the arrangement; shared ones are referenced by it
 * @param layout  the pane tree, binary throughout (D1)
 * @param widgets widget simpleNames per pane, in mount order
 *
 * @since RFC 0060
 */
public record Arrangement(String name, LayoutNode layout,
                          Map<PaneId, List<String>> widgets) implements ValueObject {

    public Arrangement {
        Objects.requireNonNull(name,    "Arrangement.name");
        Objects.requireNonNull(layout,  "Arrangement.layout");
        Objects.requireNonNull(widgets, "Arrangement.widgets");
        if (name.isBlank()) throw new IllegalArgumentException("Arrangement.name must not be blank");
        var copy = new LinkedHashMap<PaneId, List<String>>();
        widgets.forEach((k, v) -> copy.put(k, List.copyOf(v)));
        widgets = Map.copyOf(copy);
    }

    /** Every pane, in playbook order — the order panes were created. */
    public List<PaneId> panes() {
        var out = new ArrayList<PaneId>();
        collect(layout, out);
        return List.copyOf(out);
    }

    /** Widgets bound to one pane, or empty. */
    public List<String> widgetsIn(PaneId pane) {
        return widgets.getOrDefault(pane, List.of());
    }


    /**
     * This arrangement with widgets added to one pane — the method that makes a
     * shared design reusable (D7).
     *
     * <p>The starter set in {@link Arrangements} ships as pure geometry, because
     * an arrangement is only shareable if two workspaces can put different things
     * in it. A spec takes one and says what goes where:</p>
     *
     * <pre>{@code
     * Arrangements.IDE.with("editor", "DocViewWidget")
     *                 .with("terminal", "LogWidget");
     * }</pre>
     */
    public Arrangement with(String pane, String... widgetSimpleNames) {
        var id = new PaneId(pane);
        if (!panes().contains(id)) {
            throw new IllegalArgumentException(
                    "with: no pane named '" + pane + "' in arrangement '" + name + "' — panes are "
                  + panes().stream().map(PaneId::value).toList());
        }
        var next = new LinkedHashMap<PaneId, List<String>>(widgets);
        var list = new ArrayList<>(next.getOrDefault(id, List.of()));
        for (String w : widgetSimpleNames) {
            list.add(Objects.requireNonNull(w, "with: widget simpleName"));
        }
        next.put(id, list);
        return new Arrangement(name, layout, next);
    }

    /** This arrangement under a different name — for a downstream variant. */
    public Arrangement renamed(String newName) {
        return new Arrangement(newName, layout, widgets);
    }
    /** Total widgets across every pane — what {@code maxTabs()} is checked against (D12). */
    public int totalWidgets() {
        return widgets.values().stream().mapToInt(List::size).sum();
    }

    private static void collect(LayoutNode n, List<PaneId> out) {
        switch (n) {
            case LayoutNode.Leaf leaf -> out.add(leaf.paneId());
            case LayoutNode.Split s   -> { collect(s.first(), out); collect(s.second(), out); }
        }
    }

    // ── Playbook ─────────────────────────────────────────────────────────────

    /** Start a playbook. */
    public static Named named(String name) { return new Named(name); }

    /** Intermediate step: an arrangement needs its first pane before it can be split. */
    public record Named(String name) {
        public Named {
            Objects.requireNonNull(name, "Arrangement.named");
            if (name.isBlank()) throw new IllegalArgumentException("Arrangement.named must not be blank");
        }
        /** The whole workspace as one pane — every playbook starts here. */
        public Builder root(String paneName) {
            return new Builder(name, new LayoutNode.Leaf(new PaneId(paneName)));
        }
    }

    /** The playbook itself. Mutable while building; the {@link Arrangement} it yields is not. */
    public static final class Builder {

        private final String name;
        private LayoutNode layout;
        private final Map<PaneId, List<String>> widgets = new LinkedHashMap<>();

        private Builder(String name, LayoutNode layout) {
            this.name = name; this.layout = layout;
        }

        /**
         * Split {@code target} in half, putting the new pane in {@code direction}.
         * Sugar for {@link #splitWithRatio} at 0.5 — which is already what
         * {@code SplitPane.split} builds, so the even case needs no number (D5).
         */
        public Builder splitEvenly(String target, PaneDirection direction, String newPane) {
            return splitWithRatio(target, direction, newPane, 0.5);
        }

        /**
         * Split {@code target}, putting the new pane in {@code direction}.
         *
         * @param keep the share {@code target} KEEPS, strictly between 0 and 1 —
         *             see the class note; the new pane takes the remainder
         */
        public Builder splitWithRatio(String target, PaneDirection direction,
                                      String newPane, double keep) {
            Objects.requireNonNull(direction, "splitWithRatio.direction");
            var targetId = new PaneId(target);
            var newId    = new PaneId(newPane);
            if (!(keep > 0.0 && keep < 1.0)) {
                throw new IllegalArgumentException(
                        "splitWithRatio: '" + target + "' keeps " + keep
                      + " — must be strictly between 0 and 1");
            }
            if (find(layout, newId)) {
                throw new IllegalArgumentException("splitWithRatio: pane '" + newPane + "' already exists");
            }
            if (!find(layout, targetId)) {
                throw new IllegalArgumentException(
                        "splitWithRatio: no pane named '" + target + "' to split");
            }
            layout = replace(layout, targetId, direction, newId, keep);
            return this;
        }

        /** Bind widgets to a pane, in mount order. Repeated calls append. */
        public Builder place(String pane, String... widgetSimpleNames) {
            var id = new PaneId(pane);
            if (!find(layout, id)) {
                throw new IllegalArgumentException("place: no pane named '" + pane + "'");
            }
            var list = widgets.computeIfAbsent(id, k -> new ArrayList<>());
            for (String w : widgetSimpleNames) {
                list.add(Objects.requireNonNull(w, "place: widget simpleName"));
            }
            return this;
        }

        public Arrangement build() { return new Arrangement(name, layout, widgets); }

        // The split, applied: the target leaf becomes a split holding it and the
        // new pane. LayoutNode.Split stores the FIRST child's share, so a LEFT/UP
        // split records 1 - keep.
        private static LayoutNode replace(LayoutNode n, PaneId target,
                                          PaneDirection dir, PaneId added, double keep) {
            return switch (n) {
                case LayoutNode.Leaf leaf -> {
                    if (!leaf.paneId().equals(target)) yield leaf;
                    var kept  = new LayoutNode.Leaf(target);
                    var fresh = new LayoutNode.Leaf(added);
                    yield dir.targetIsFirst()
                            ? new LayoutNode.Split(dir.orientation(), keep, kept, fresh)
                            : new LayoutNode.Split(dir.orientation(), 1.0 - keep, fresh, kept);
                }
                case LayoutNode.Split s -> new LayoutNode.Split(
                        s.orientation(), s.ratio(),
                        replace(s.first(),  target, dir, added, keep),
                        replace(s.second(), target, dir, added, keep));
            };
        }

        private static boolean find(LayoutNode n, PaneId id) {
            return switch (n) {
                case LayoutNode.Leaf leaf -> leaf.paneId().equals(id);
                case LayoutNode.Split s   -> find(s.first(), id) || find(s.second(), id);
            };
        }
    }
}
