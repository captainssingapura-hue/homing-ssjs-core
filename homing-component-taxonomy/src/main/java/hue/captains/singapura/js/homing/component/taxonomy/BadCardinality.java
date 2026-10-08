package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A count that is no cardinality: none, a range whose most is below its least, or one whose
 * least and most are equal. Thrown where it is said; reading a taxonomy reports it as a problem
 * of the component that said it, beside every other.
 */
public final class BadCardinality extends IllegalArgumentException {

    public BadCardinality(String says) { super(says); }
}
