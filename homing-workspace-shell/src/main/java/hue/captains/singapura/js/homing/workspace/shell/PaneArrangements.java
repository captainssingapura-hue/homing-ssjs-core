package hue.captains.singapura.js.homing.workspace.shell;

import java.util.List;

import static hue.captains.singapura.js.homing.workspace.shell.PaneDirection.DOWN;
import static hue.captains.singapura.js.homing.workspace.shell.PaneDirection.LEFT;
import static hue.captains.singapura.js.homing.workspace.shell.PaneDirection.RIGHT;

/**
 * RFC 0060 D7 — the pane SHAPES that ship with the framework.
 *
 * <p>Every one is <b>pure geometry</b>: named panes and how the space divides,
 * with no widgets and no workspace kind. That is what makes them shareable — the
 * same three-pane IDE shape serves a document studio and a trading desk. A
 * consumer allocates widgets into a shape to get an {@link Arrangement}:</p>
 *
 * <pre>{@code
 * @Override public Arrangement arrangement() {
 *     return PaneArrangements.IDE.allocate()
 *             .place(Ide.EXPLORER, TreeWidget.class)
 *             .place(Ide.EDITOR,   DocViewWidget.class)
 *             .place(Ide.TERMINAL, LogWidget.class)
 *             .build();
 * }
 * }</pre>
 *
 * <p>The set is chosen by how common the shape is in tools people already use,
 * not by what the split grammar can express — the grammar can express anything,
 * which is exactly why the shipped list should be short and recognisable. A
 * workspace wanting something else writes its own playbook; nothing here is
 * privileged.</p>
 *
 * <p>Percentages below are shares of the whole workspace, which is <b>not</b> how
 * they are written: a playbook's ratio is the share the split pane keeps, so the
 * numbers in the source are local to their split. {@code ArrangementTest} pins
 * the resulting geometry so the two readings cannot drift.</p>
 *
 * @since RFC 0060
 */
public final class PaneArrangements {

    private PaneArrangements() {}

    /**
     * One pane, the whole space. <b>The framework default</b> (D8) — and the
     * honest starting point, since a workspace that has been told nothing should
     * not pre-commit the reader to a shape.
     */
    public static final PaneArrangement SINGLE =
            PaneArrangement.named("single").root("main").build();

    /**
     * Two equal columns. The comparison shape — diffs, before/after, source
     * beside output.
     */
    public static final PaneArrangement COLUMNS =
            PaneArrangement.named("columns")
                    .root("left")
                    .splitEvenly("left", RIGHT, "right")
                    .build();

    /** Two equal rows. The same comparison, stacked — long lines beat tall ones. */
    public static final PaneArrangement ROWS =
            PaneArrangement.named("rows")
                    .root("top")
                    .splitEvenly("top", DOWN, "bottom")
                    .build();

    /**
     * A 2×2 grid. The monitoring shape — four things watched at once, none of
     * them primary. This is the framework's current default, kept because it is
     * genuinely useful and demoted because it is a poor default.
     */
    public static final PaneArrangement QUAD =
            PaneArrangement.named("quad")
                    .root("top-left")
                    .splitEvenly("top-left", RIGHT, "top-right")
                    .splitEvenly("top-left", DOWN, "bottom-left")
                    .splitEvenly("top-right", DOWN, "bottom-right")
                    .build();

    /**
     * A main pane with a 30% companion on the right — the inspector shape.
     * Editor beside properties, document beside outline, canvas beside layers.
     */
    public static final PaneArrangement MAIN_AND_SIDE =
            PaneArrangement.named("main-and-side")
                    .root("main")
                    .splitWithRatio("main", RIGHT, "side", 0.70)
                    .build();

    /**
     * A main pane with a 30% companion below — the console shape, and probably
     * the most-used two-pane layout there is: editor over terminal, query over
     * results, code over test output.
     */
    public static final PaneArrangement MAIN_AND_OUTPUT =
            PaneArrangement.named("main-and-output")
                    .root("main")
                    .splitWithRatio("main", DOWN, "output", 0.70)
                    .build();

    /**
     * Explorer 20% on the left, editor 60%, terminal 20% beneath the editor —
     * the shape VS Code and the JetBrains IDEs open in, and the reason the
     * three-pane case had to be expressible at all.
     *
     * <p>Written as two splits: the editor gives 20% to an explorer on its left,
     * then keeps 75% of what remains, which is 60% of the whole.</p>
     */
    public static final PaneArrangement IDE =
            PaneArrangement.named("ide")
                    .root("editor")
                    .splitWithRatio("editor", LEFT, "explorer", 0.80)
                    .splitWithRatio("editor", DOWN, "terminal", 0.75)
                    .build();

    /**
     * Three columns at 20 / 30 / 50 — the mail and chat shape: folders, then a
     * message list, then the message. Outlook, Thunderbird, Slack, and most
     * feed readers.
     */
    public static final PaneArrangement TRIPLE_COLUMN =
            PaneArrangement.named("triple-column")
                    .root("nav")
                    .splitWithRatio("nav", RIGHT, "list", 0.20)
                    .splitWithRatio("list", RIGHT, "content", 0.375)
                    .build();


    // ── The panes, by name ───────────────────────────────────────────────────
    // Each constant is built through PaneArrangement.pane(), which validates it
    // against the shape — so a typo fails at class initialisation naming the
    // panes that exist, and these cannot drift from the playbooks above.
    //
    // They are ShapePanes, not PaneIds: they say what a SHAPE calls a pane, and
    // are the live slot ids only at seed time. See ShapePane.

    /** {@link #COLUMNS}' panes. */
    public static final class Columns {
        private Columns() {}
        public static final ShapePane LEFT  = COLUMNS.pane("left");
        public static final ShapePane RIGHT = COLUMNS.pane("right");
    }

    /** {@link #ROWS}' panes. */
    public static final class Rows {
        private Rows() {}
        public static final ShapePane TOP    = ROWS.pane("top");
        public static final ShapePane BOTTOM = ROWS.pane("bottom");
    }

    /** {@link #SINGLE}'s pane. */
    public static final class Single {
        private Single() {}
        public static final ShapePane MAIN = SINGLE.pane("main");
    }

    /** {@link #QUAD}'s panes. */
    public static final class Quad {
        private Quad() {}
        public static final ShapePane TOP_LEFT     = QUAD.pane("top-left");
        public static final ShapePane TOP_RIGHT    = QUAD.pane("top-right");
        public static final ShapePane BOTTOM_LEFT  = QUAD.pane("bottom-left");
        public static final ShapePane BOTTOM_RIGHT = QUAD.pane("bottom-right");
    }

    /** {@link #MAIN_AND_SIDE}'s panes. */
    public static final class MainAndSide {
        private MainAndSide() {}
        public static final ShapePane MAIN = MAIN_AND_SIDE.pane("main");
        public static final ShapePane SIDE = MAIN_AND_SIDE.pane("side");
    }

    /** {@link #MAIN_AND_OUTPUT}'s panes. */
    public static final class MainAndOutput {
        private MainAndOutput() {}
        public static final ShapePane MAIN   = MAIN_AND_OUTPUT.pane("main");
        public static final ShapePane OUTPUT = MAIN_AND_OUTPUT.pane("output");
    }

    /** {@link #IDE}'s panes. */
    public static final class Ide {
        private Ide() {}
        public static final ShapePane EXPLORER = IDE.pane("explorer");
        public static final ShapePane EDITOR   = IDE.pane("editor");
        public static final ShapePane TERMINAL = IDE.pane("terminal");
    }

    /** {@link #TRIPLE_COLUMN}'s panes. */
    public static final class TripleColumn {
        private TripleColumn() {}
        public static final ShapePane NAV     = TRIPLE_COLUMN.pane("nav");
        public static final ShapePane LIST    = TRIPLE_COLUMN.pane("list");
        public static final ShapePane CONTENT = TRIPLE_COLUMN.pane("content");
    }

    /** Everything the framework ships, in the order this class declares them. */
    public static final List<PaneArrangement> ALL = List.of(
            SINGLE, COLUMNS, ROWS, QUAD, MAIN_AND_SIDE, MAIN_AND_OUTPUT, IDE, TRIPLE_COLUMN);
}
