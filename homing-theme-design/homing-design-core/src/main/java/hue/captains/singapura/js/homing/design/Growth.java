package hue.captains.singapura.js.homing.design;

/**
 * The size of an element, on one fixed axis from −1 to 1: 0 is the regular
 * size, 1 the biggest, −1 the smallest. Exponential: every length a design
 * lets grow is {@code regular × pow(ratio, size)}, so +1 and −1 are the same
 * proportion either way and the way between is even to the eye. The design
 * gives each length its own ratio — the padding may grow faster than the
 * type, the line-height may hold at 1 — and a ratio of exactly 1 is the
 * explicit "this one stays".
 *
 * <p>The size is an element's number, not a class's: a button sets its own
 * with {@code css.size(el, s)}, which writes {@link #VAR}; a class that
 * expects to, names the pairs in {@code sizes()}, and the deployment holds
 * the design to a ratio for every property of them. Registered
 * non-inheriting with an initial value of 0, so an element that never sets
 * it is regular, and a component nested in a grown one is not grown by it.</p>
 *
 * <p>The colour axis is {@link Extent}; the two are independent numbers on
 * one element.</p>
 */
public final class Growth {
    private Growth() {}

    /** The custom property an element carries its size in. */
    public static final String VAR = "--size";

    /** The registration every sheet with a growing length carries: a number, not inherited, zero by default. */
    public static final String PROPERTY = "@property " + VAR + " { syntax: \"<number>\"; inherits: false; initial-value: 0; }";

    /** The suffix on the ratio's variable. */
    public static final String RATIO = "-ratio";
}
