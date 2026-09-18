package hue.captains.singapura.js.homing.design;

/**
 * Structure — the visible lines of the page that belong to no component's
 * content. {@code Divider} is the rule between things; {@code Backdrop} is
 * the scene behind everything, when a design has one — an asset, a surface,
 * a filter, never an event handler.
 */
public interface Structure extends Semantic {

    record Divider() implements Structure {}

    /** The hairline: the quiet one-pixel edge between things that touch. */
    record Hairline() implements Structure {}

    /** The bar: an accent along the leading edge of a card, a row, a callout — the rule that says "this is one of those". */
    record Bar() implements Structure {}

    /** The spine: a one-pixel line along the leading edge of a column — a contents list, a nav. */
    record Spine() implements Structure {}

    /** The marker: a short leading-edge line that appears when its row is hovered or current, and is absent otherwise. */
    record Marker() implements Structure {}

    /** The cap: the quiet line above a footer, a meta row — the hairline's upper twin. */
    record Cap() implements Structure {}

    /** The rail: a one-pixel line along the trailing edge of a column — a nav beside its detail. */
    record Rail() implements Structure {}

    /**
     * The lattice: the lines between the cells of a grid. Every cell draws its
     * trailing edge and its bottom edge, and the cells tile into the lines —
     * so a grid keeps its lattice under a sticky header, which a collapsed
     * border cannot. A design says how heavy the lines are, or that there are
     * none.
     */
    record Lattice() implements Structure {}

    record Backdrop() implements Structure {}
}
