package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;

/**
 * One of the captured axes of {@link WorkspaceState}: pure split
 * structure. The captured tree mirrors SplitPane's live tree — recursive
 * splits at internal nodes, leaf panes (addressable by {@link PaneId}) at
 * the leaves.
 *
 * <p>Sealed so the runtime can pattern-match exhaustively when rebuilding
 * the live SplitPane tree on restore. {@link Leaf}s carry the
 * {@link PaneId} that each widget's {@link WidgetLocation.InPane}
 * references; {@link Split}s carry orientation + ratio + the two child
 * sub-trees.</p>
 *
 * @since RFC 0029 cycle 1
 */
public sealed interface LayoutNode permits LayoutNode.Leaf, LayoutNode.Split {

    /**
     * Leaf pane — the addressable terminal of the layout tree. Its
     * {@link PaneId} is what widgets reference via
     * {@link WidgetLocation.InPane#paneId()}.
     *
     * @param paneId stable pane identifier (workspace-scoped)
     */
    record Leaf(PaneId paneId) implements LayoutNode {
        public Leaf {
            Objects.requireNonNull(paneId, "LayoutNode.Leaf.paneId");
        }
    }

    /**
     * Internal split node — two child sub-trees laid out along the
     * orientation axis, with the {@code ratio} naming the first child's
     * share of the available space.
     *
     * @param orientation horizontal (left + right) or vertical (top + bottom)
     * @param ratio       first child's share, strictly between 0.0 and 1.0
     * @param first       the left / top sub-tree
     * @param second      the right / bottom sub-tree
     */
    record Split(Orientation orientation, double ratio,
                 LayoutNode first, LayoutNode second) implements LayoutNode {
        public Split {
            Objects.requireNonNull(orientation, "LayoutNode.Split.orientation");
            Objects.requireNonNull(first,       "LayoutNode.Split.first");
            Objects.requireNonNull(second,      "LayoutNode.Split.second");
            if (!(ratio > 0.0 && ratio < 1.0)) {
                throw new IllegalArgumentException(
                        "LayoutNode.Split.ratio must be strictly between 0.0 and 1.0 (got " + ratio + ")");
            }
        }
    }
}
