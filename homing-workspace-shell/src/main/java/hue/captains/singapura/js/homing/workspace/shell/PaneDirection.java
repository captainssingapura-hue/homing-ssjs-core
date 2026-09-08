package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.state.Orientation;

/**
 * RFC 0060 D6 — which way a new pane is carved off an existing one.
 *
 * <p>An arrangement names a <b>direction</b>, never an orientation and never a
 * dimension. {@code 1×2} and {@code 2×1} are unreadable without first asking
 * which factor counts rows, and an author who guesses wrong gets a layout rotated
 * ninety degrees with nothing to say so. {@code RIGHT} cannot be misread.</p>
 *
 * <p>The axis follows from the direction, so {@link Orientation} is never
 * authored — it is derived here and nowhere else, which is what makes it
 * impossible to write down backwards.</p>
 *
 * @since RFC 0060
 */
public enum PaneDirection {

    /** New pane to the right of the one being split. */
    RIGHT(Orientation.HORIZONTAL, true),

    /** New pane to the left of the one being split. */
    LEFT(Orientation.HORIZONTAL, false),

    /** New pane below the one being split. */
    DOWN(Orientation.VERTICAL, true),

    /** New pane above the one being split. */
    UP(Orientation.VERTICAL, false);

    private final Orientation orientation;
    private final boolean     targetIsFirst;

    PaneDirection(Orientation orientation, boolean targetIsFirst) {
        this.orientation   = orientation;
        this.targetIsFirst = targetIsFirst;
    }

    /** The split axis this direction implies. */
    public Orientation orientation() { return orientation; }

    /**
     * Does the pane being split stay on the {@code first} side of the new split?
     * True for {@link #RIGHT} and {@link #DOWN} (the new pane lands after it),
     * false for {@link #LEFT} and {@link #UP}.
     *
     * <p>This is what turns "the share the split pane keeps" — the only reading an
     * author should have to hold — into {@code LayoutNode.Split}'s ratio, which is
     * the <i>first</i> child's share.</p>
     */
    public boolean targetIsFirst() { return targetIsFirst; }
}
