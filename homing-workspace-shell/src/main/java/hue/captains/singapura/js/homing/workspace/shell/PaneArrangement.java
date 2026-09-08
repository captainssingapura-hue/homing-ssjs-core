package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.state.LayoutNode;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RFC 0060 — <b>geometry only</b>: named panes and how the space divides between
 * them. No widgets, and no opinion about which workspace it serves.
 *
 * <p>That separation is the point. A pane arrangement is about <i>shape</i>, so it
 * is reusable across every workspace kind — the same three-pane IDE layout serves
 * a document studio and a trading desk. Widget allocation is the part that
 * belongs to a kind, and it lives in {@link Arrangement}, which holds one of
 * these.</p>
 *
 * <p>Being a pure {@link ValueObject} of shape, it is also the thing you can
 * <b>draw</b> — a thumbnail is a walk over the layout with no workspace, no
 * widget registry and no session required.</p>
 *
 * <h2>Built as a playbook</h2>
 *
 * <pre>{@code
 * PaneArrangement.named("editor-and-output")
 *         .root("main")
 *         .splitWithRatio("main", PaneDirection.RIGHT, "side", 0.70)
 *         .splitEvenly("side", PaneDirection.DOWN, "output")
 *         .build();
 * }</pre>
 *
 * <p>The sequence of splits a person would have performed, in the vocabulary the
 * runtime already records when they do ({@code SplitCreated(paneId, orientation,
 * newRatio)}).</p>
 *
 * <h2>The ratio is the share the split pane KEEPS</h2>
 *
 * <p>The one thing an author can get wrong, so the method says it: above,
 * {@code main} keeps 0.70 of the whole and {@code side} takes 0.30 — and
 * splitting {@code side} evenly then gives two panes of <b>0.15 each of the
 * workspace</b>. A ratio is local to its split, exactly as the event stores it
 * and exactly as a person experiences dragging a divider, which means these
 * numbers read as absolute and are not.</p>
 *
 * <p>{@link LayoutNode.Split} stores the <i>first</i> child's share, so a
 * {@link PaneDirection#LEFT} or {@link PaneDirection#UP} split is recorded as
 * {@code 1 - keep}. The author never sees that; absorbing it is why this type
 * exists rather than a raw tree.</p>
 *
 * @param name   identifies the shape; shared ones are referenced by it
 * @param layout the pane tree, binary throughout (D1)
 *
 * @since RFC 0060
 */
public record PaneArrangement(String name, LayoutNode layout) implements ValueObject {

    public PaneArrangement {
        Objects.requireNonNull(name,   "PaneArrangement.name");
        Objects.requireNonNull(layout, "PaneArrangement.layout");
        if (name.isBlank()) throw new IllegalArgumentException("PaneArrangement.name must not be blank");
    }

    /** Every pane, in playbook order — the order panes were created. */
    public List<PaneId> panes() {
        var out = new ArrayList<PaneId>();
        collect(layout, out);
        return List.copyOf(out);
    }

    /** Does this shape have a pane by that name? */
    public boolean hasPane(PaneId pane) { return panes().contains(pane); }

    /** How many panes the shape divides into — and therefore {@code count - 1} dividers. */
    public int paneCount() { return panes().size(); }


    /**
     * Start allocating widgets into this shape — the step from geometry (shared)
     * to a full {@link Arrangement} (a workspace kind's own).
     */
    public Arrangement.Builder allocate() { return new Arrangement.Builder(this); }

    /** This shape with no widgets in it at all. */
    public Arrangement empty() { return Arrangement.of(this); }

    private static void collect(LayoutNode n, List<PaneId> out) {
        switch (n) {
            case LayoutNode.Leaf leaf -> out.add(leaf.paneId());
            case LayoutNode.Split s   -> { collect(s.first(), out); collect(s.second(), out); }
        }
    }

    // ── Playbook ─────────────────────────────────────────────────────────────

    /** Start a playbook. */
    public static Named named(String name) { return new Named(name); }

    /** Intermediate step: a shape needs its first pane before it can be split. */
    public record Named(String name) {
        public Named {
            Objects.requireNonNull(name, "PaneArrangement.named");
            if (name.isBlank()) throw new IllegalArgumentException("PaneArrangement.named must not be blank");
        }
        /** The whole workspace as one pane — every playbook starts here. */
        public Builder root(String paneName) {
            return new Builder(name, new LayoutNode.Leaf(new PaneId(paneName)));
        }
    }

    /** The playbook. Mutable while building; what it yields is not. */
    public static final class Builder {

        private final String name;
        private LayoutNode layout;

        private Builder(String name, LayoutNode layout) {
            this.name = name; this.layout = layout;
        }

        /**
         * Split {@code target} in half, putting the new pane in {@code direction}.
         * Sugar for {@link #splitWithRatio} at 0.5 — already what
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

        public PaneArrangement build() { return new PaneArrangement(name, layout); }

        // The target leaf becomes a split holding it and the new pane.
        // LayoutNode.Split stores the FIRST child's share, so LEFT/UP records 1 - keep.
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
