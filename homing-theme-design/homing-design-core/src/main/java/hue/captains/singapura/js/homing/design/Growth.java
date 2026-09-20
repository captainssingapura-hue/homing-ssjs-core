package hue.captains.singapura.js.homing.design;

/**
 * An axis a length grows along: one number on the element, from −1 to 1, 0
 * the regular; every value a design lets grow along it is
 * {@code regular × pow(ratio, number)}. Exponential, so +1 and −1 are the
 * same proportion either way and the way between is even to the eye. The
 * design gives each property its own ratio per axis — the padding may grow
 * faster than the type, the line-height may hold at 1 — and a ratio of
 * exactly 1 is the explicit "this one stays".
 *
 * <p>{@link #SIZE} is how big a thing is: a button, a card, and their
 * parts. {@link #ASPECT} is how wide a box whose measure is its own: 0 is
 * square, +1 the widest the design allows, −1 the tallest. The two are
 * independent numbers on one element, beside the colour axis
 * {@link Extent}.</p>
 *
 * <p>A number is an element's, not a class's: a button sets its size with
 * {@code css.size(el, s)}, a card its aspect with {@code css.aspect(el, a)},
 * each writing the axis's {@link #var()}; a class that expects to, names
 * the pairs in {@code sizes()} or {@code aspects()}, and the deployment
 * holds the design to a ratio on that axis for every property of them.
 * Registered non-inheriting with an initial value of 0, so an element that
 * never sets one is regular, and a component nested in a grown one is not
 * grown by it.</p>
 */
public enum Growth {
    SIZE("--size", "-ratio"),
    ASPECT("--aspect", "-ratio-aspect");

    private final String var;
    private final String suffix;

    Growth(String var, String suffix) { this.var = var; this.suffix = suffix; }

    /** The custom property an element carries this number in. */
    public String var() { return var; }

    /** The suffix on the ratio's variable for this axis. */
    public String suffix() { return suffix; }

    /** The registration every sheet with a length growing along this axis carries: a number, not inherited, zero by default. */
    public String property() { return "@property " + var + " { syntax: \"<number>\"; inherits: false; initial-value: 0; }"; }

    /** The size axis's custom property, for the one-axis reading. */
    public static final String VAR = "--size";
    public static final String PROPERTY = "@property " + VAR + " { syntax: \"<number>\"; inherits: false; initial-value: 0; }";
    public static final String RATIO = "-ratio";
}
