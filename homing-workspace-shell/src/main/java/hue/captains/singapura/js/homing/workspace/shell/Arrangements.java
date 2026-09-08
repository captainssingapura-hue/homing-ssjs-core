package hue.captains.singapura.js.homing.workspace.shell;

import java.util.List;

import static hue.captains.singapura.js.homing.workspace.shell.PaneDirection.DOWN;
import static hue.captains.singapura.js.homing.workspace.shell.PaneDirection.LEFT;
import static hue.captains.singapura.js.homing.workspace.shell.PaneDirection.RIGHT;

/**
 * RFC 0060 D7 — the arrangements that ship with the framework.
 *
 * <p>All of them are <b>pure geometry</b>. An arrangement is only shareable if two
 * workspaces can put different things in it, so widgets are bound by the consumer
 * through {@link Arrangement#with(String, String...)}:</p>
 *
 * <pre>{@code
 * @Override public Arrangement arrangement() {
 *     return Arrangements.IDE
 *             .with("explorer", "TreeWidget")
 *             .with("editor",   "DocViewWidget")
 *             .with("terminal", "LogWidget");
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
public final class Arrangements {

    private Arrangements() {}

    /**
     * One pane, the whole space. <b>The framework default</b> (D8) — and the
     * honest starting point, since a workspace that has been told nothing should
     * not pre-commit the reader to a shape.
     */
    public static final Arrangement SINGLE =
            Arrangement.named("single").root("main").build();

    /**
     * Two equal columns. The comparison shape — diffs, before/after, source
     * beside output.
     */
    public static final Arrangement COLUMNS =
            Arrangement.named("columns")
                    .root("left")
                    .splitEvenly("left", RIGHT, "right")
                    .build();

    /** Two equal rows. The same comparison, stacked — long lines beat tall ones. */
    public static final Arrangement ROWS =
            Arrangement.named("rows")
                    .root("top")
                    .splitEvenly("top", DOWN, "bottom")
                    .build();

    /**
     * A 2×2 grid. The monitoring shape — four things watched at once, none of
     * them primary. This is the framework's current default, kept because it is
     * genuinely useful and demoted because it is a poor default.
     */
    public static final Arrangement QUAD =
            Arrangement.named("quad")
                    .root("top-left")
                    .splitEvenly("top-left", RIGHT, "top-right")
                    .splitEvenly("top-left", DOWN, "bottom-left")
                    .splitEvenly("top-right", DOWN, "bottom-right")
                    .build();

    /**
     * A main pane with a 30% companion on the right — the inspector shape.
     * Editor beside properties, document beside outline, canvas beside layers.
     */
    public static final Arrangement MAIN_AND_SIDE =
            Arrangement.named("main-and-side")
                    .root("main")
                    .splitWithRatio("main", RIGHT, "side", 0.70)
                    .build();

    /**
     * A main pane with a 30% companion below — the console shape, and probably
     * the most-used two-pane layout there is: editor over terminal, query over
     * results, code over test output.
     */
    public static final Arrangement MAIN_AND_OUTPUT =
            Arrangement.named("main-and-output")
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
    public static final Arrangement IDE =
            Arrangement.named("ide")
                    .root("editor")
                    .splitWithRatio("editor", LEFT, "explorer", 0.80)
                    .splitWithRatio("editor", DOWN, "terminal", 0.75)
                    .build();

    /**
     * Three columns at 20 / 30 / 50 — the mail and chat shape: folders, then a
     * message list, then the message. Outlook, Thunderbird, Slack, and most
     * feed readers.
     */
    public static final Arrangement TRIPLE_COLUMN =
            Arrangement.named("triple-column")
                    .root("nav")
                    .splitWithRatio("nav", RIGHT, "list", 0.20)
                    .splitWithRatio("list", RIGHT, "content", 0.375)
                    .build();

    /** Everything the framework ships, in the order this class declares them. */
    public static final List<Arrangement> ALL = List.of(
            SINGLE, COLUMNS, ROWS, QUAD, MAIN_AND_SIDE, MAIN_AND_OUTPUT, IDE, TRIPLE_COLUMN);
}
