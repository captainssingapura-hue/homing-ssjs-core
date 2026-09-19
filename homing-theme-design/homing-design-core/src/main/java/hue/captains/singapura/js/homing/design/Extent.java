package hue.captains.singapura.js.homing.design;

/**
 * The extent of a colour word: how much of the meaning is present, on one
 * fixed axis from −1 to 1. The word every design writes is the meaning at
 * its full extent, {@link #FULL}; a design that wants the word to scale
 * anchors it at {@link #ZERO} — neutral: <i>none of this meaning</i>, which
 * is not always transparent, so it is the design's to say — and at
 * {@link #NEG} — the meaning turned the other way: success at −1 is failure,
 * danger at −1 is safety. Each semantic is its own axis, with an opposite of
 * its own; no two semantics are declared opposites of each other, and a
 * design that colours failure as it colours danger is choosing to. The
 * browser interpolates between the anchors it has, in oklab — rectangular, so
 * the way from a low-chroma neutral to a saturated pole never turns through a
 * third hue — choosing the pole by the sign of the extent and walking from the
 * neutral by its magnitude: a colour at −½ is the negative pole at half
 * strength, never a hue between the two poles.
 *
 * <p>The extent is an element's number, not a class's: a cell sets its own
 * with {@code css.extent(el, t)}, which writes {@link #VAR}; a class that
 * expects to, names the pairs in {@code extents()}, and the deployment holds
 * the design to all three anchors for them. Registered non-inheriting with
 * an initial value of 1, so an element that never sets it wears the full
 * word, and a child never takes its parent's number.</p>
 */
public enum Extent {
    /** −1: the meaning turned the other way. */
    NEG(-1, "-neg"),
    /** 0: neutral — none of this meaning, whatever that looks like here. */
    ZERO(0, "-zero"),
    /** 1: the meaning at full — the word itself. */
    FULL(1, "");

    /** The custom property an element carries its extent in. */
    public static final String VAR = "--extent";

    /** The registration every sheet with a scaled word carries: a number, not inherited, one by default. */
    public static final String PROPERTY = "@property " + VAR + " { syntax: \"<number>\"; inherits: false; initial-value: 1; }";

    private final int value;
    private final String suffix;

    Extent(int value, String suffix) { this.value = value; this.suffix = suffix; }

    public int value() { return value; }

    /** The suffix on the anchor's variable: none for the word itself, {@code -zero}, {@code -neg}. */
    public String suffix() { return suffix; }
}
