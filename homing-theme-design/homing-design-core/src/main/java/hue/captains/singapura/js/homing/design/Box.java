package hue.captains.singapura.js.homing.design;

/**
 * Box — the kind of box an element is, for the geometry a design owns:
 * density (inset, gap, extent) and shape (corner, rule). A component's own
 * layout says where a box goes; the design says how much air it has and
 * how its corners are cut. {@code Control} is a button or a field;
 * {@code Inline} a chip, tag or badge; {@code Container} a card, a pane or a panel;
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
        /**
         * An option: one row of a menu or a list, a control that is picked —
         * its air inside, its gap between mark, label and hint, a soft corner
         * for the wash the design lays under the cursor.
         */
        public record Option() implements Box {}
        /**
         * A slider: a control that sets a number by a knob on a track. Its
         * measure is its own — the track's length, and the least height a
         * knob needs, both grown by the size — its inset the air around the
         * parts, its gap between label, track and readout.
         */
        public record Slider() implements Box {
            /** The track: the groove the knob runs in — its thickness, its corner, its rule; a design sinks it. */
            public record Track() implements Box {}
            /**
             * The knob: what the hand takes — its extent, and its face's
             * silhouette: a corner, round or square, or a clip that cuts it
             * to a diamond, a hexagon, a pointer; raised, and ringed around
             * its box when it has the focus. The face is clipped, the box is
             * not, so the ring survives any cut; a cut edge carries no rule.
             */
            public record Knob() implements Box {}
            /**
             * The cap: what the hand takes on a vertical slider — a fader's,
             * wide across the track and low along it, so the finger has it;
             * its extent along and across, its corner, its ring on focus, and
             * the size of the mark on it. Always a face.
             */
            public record Cap() implements Box {}
        }
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
        /**
         * A panel: a container that brings a head and a body to whatever
         * holds it — mounted in a pane, set in a cell of a grid, or standing
         * on a page. Unlike a card it does not measure itself: it fills what
         * it is given, so what a design says about it is the air inside and
         * the line between its parts. Its corner and its edge are a
         * container's, since what holds it usually draws them; a panel that
         * stands alone wears them itself, the keys mark included.
         */
        public record Panel() implements Box {
            /**
             * The head: the bar that names the panel and carries what acts on
             * it — its air, the gap between the name and the controls, and the
             * line under it, which is where a design says what a panel is.
             */
            public record Head() implements Box {}
            /**
             * The body: the working surface under the head — the air around
             * what it shows and the gap between the things in it. It is the
             * part that scrolls.
             */
            public record Body() implements Box {}
        }
        /**
         * A menu: a container of options that opens at a point and closes
         * when it is done with — its corner and rule the container's, its
         * inset the air around the options, its gap between them, its extent
         * the least it is wide.
         */
        public record Menu() implements Box {}
    }

    record Section() implements Box {}
}
