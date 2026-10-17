package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A state the browser alone decides - the pointer over a thing, the focus shown on it, the press - which a design draws
 * with its pseudo-class, nested in the leaf's class, never with a class of its own (RFC 0067, appendix Two Classes per
 * Leaf). Every other state is the application's, worn as a class while it holds. Said by the type of state: {@code
 * record Hovered() implements Flag<Interaction>, Importance.Quinary, Pseudo}. A pseudo state is a flag: one value beside
 * its rest, the one the pseudo-class shows.
 */
public interface Pseudo {

    /** The pseudo-class it is drawn with: {@code :hover}. */
    String pseudoClass();
}
