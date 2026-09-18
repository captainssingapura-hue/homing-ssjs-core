package hue.captains.singapura.js.homing.design;

/**
 * Box — the kind of box an element is, for the geometry a design owns:
 * density (inset, gap, extent) and shape (corner, rule). A component's own
 * layout says where a box goes; the design says how much air it has and
 * how its corners are cut. {@code Control} is a button or a field;
 * {@code Inline} a chip, tag or badge; {@code Container} a card or panel;
 * {@code Section} a region of the page.
 */
public interface Box extends Semantic {

    record Control() implements Box {}

    record Inline() implements Box {}

    record Container() implements Box {}

    record Section() implements Box {}
}
