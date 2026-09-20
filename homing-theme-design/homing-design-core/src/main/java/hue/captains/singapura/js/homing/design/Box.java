package hue.captains.singapura.js.homing.design;

/**
 * Box — the kind of box an element is, for the geometry a design owns:
 * density (inset, gap, extent) and shape (corner, rule). A component's own
 * layout says where a box goes; the design says how much air it has and
 * how its corners are cut. {@code Control} is a button or a field;
 * {@code Inline} a chip, tag or badge; {@code Container} a card or panel;
 * {@code Section} a region of the page.
 *
 * <p>A word may be refined: a leaf nested in a leaf says what its parent
 * says, more precisely — {@code Control.Button.Base} is a control, a button,
 * the one that completes a task. A component wears the most precise word it
 * has; a design answers at whichever level it has an opinion, and the
 * deployment walks up from the word worn to the first answer. The token is
 * the path, {@code control-button-base}, so a refinement never shadows a
 * word elsewhere in the tree.</p>
 */
public interface Box extends Semantic {

    record Control() implements Box {
        /** A button: a control that fires an action. Its purposes will refine it; for now, one. */
        public record Button() implements Box {
            /** The button that completes a task — OK, Save, Apply, and the plain action beside them. */
            public record Base() implements Box {}
        }
    }

    record Inline() implements Box {}

    record Container() implements Box {}

    record Section() implements Box {}
}
