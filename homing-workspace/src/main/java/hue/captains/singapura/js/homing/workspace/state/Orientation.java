package hue.captains.singapura.js.homing.workspace.state;

/**
 * Orientation of a SplitPane split. Mirrors the SplitPane primitive's own
 * orientation vocabulary so the persistence layer doesn't introduce a
 * parallel grammar — captured state uses the same name the live primitive
 * uses.
 *
 * @since RFC 0029 cycle 1
 */
public enum Orientation {
    /** Split into a left pane and a right pane. */
    HORIZONTAL,

    /** Split into a top pane and a bottom pane. */
    VERTICAL
}
