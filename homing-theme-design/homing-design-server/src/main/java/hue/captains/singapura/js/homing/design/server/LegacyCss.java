package hue.captains.singapura.js.homing.design.server;

/**
 * Whether the design words' sheets are served - the old design stack, soft-removed. Emptied, every
 * target sheet is served with nothing in it: the words are still declared, bound by the designs and
 * checked by the gates, so everything compiles and the old stack can still be read as data - and
 * nothing on a page draws from it any more. What still depends on it shows, and is the work left
 * before the stack is deleted. Served brings it back, for comparing while that work is done.
 *
 * <p>Emptied unless the server is started with {@code -Dhoming.legacyCss=on}.</p>
 */
public enum LegacyCss {

    /** Every target sheet as the designs render it. */
    SERVED,

    /** Every target sheet served empty, saying why. */
    EMPTIED;

    /** The system property that brings the sheets back: {@code -Dhoming.legacyCss=on}. */
    public static final String PROPERTY = "homing.legacyCss";
}
