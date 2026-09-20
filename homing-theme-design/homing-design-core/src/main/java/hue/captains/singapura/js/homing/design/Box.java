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
        /**
         * A tab: a control in a row that selects what is shown, like a button
         * with three differences. Its measure is its own, not its label's — a
         * hard frame, every tab in the row the same, the label ellipsised
         * within, as a browser's tabs are; its proportion is the design's and
         * wide, the element's aspect widening or narrowing it from there; and
         * its corners are cut where it meets the row, not where it sits on it.
         */
        public record Tab() implements Box {}
    }

    record Inline() implements Box {}

    record Container() implements Box {
        /** A card: a container whose measure is its own, and whose content fits it. Its kinds will refine it; for now, one. */
        public record Card() implements Box {
            /** The regular card: a heading, a bounded body, a footer; hover, press and focus as the design gives them; what a press does is the caller's. */
            public record Base() implements Box {}
        }
        /** A pane: a container that holds a widget by the base's contract — a tab's, a split's leaf, a floating one. Its air is its chrome's; the widget fills the rest. */
        public record Pane() implements Box {
            /** The floating pane: raised above the page, moved by its head, sized by its user; the active one is the ring drawn now. */
            public record Floating() implements Box {}
        }
    }

    record Section() implements Box {}
}
