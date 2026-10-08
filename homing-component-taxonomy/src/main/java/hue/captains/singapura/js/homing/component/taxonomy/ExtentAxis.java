package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * An axis a component varies along by degree: one number on an element, from −1 to 1, set and
 * changed live. A degree is never a component of its own and never a state - a big button is a
 * button, a wide card a card, a danger turned to safety the same danger - so it adds no node and
 * no state to the taxonomy.
 *
 * <p>The axes are closed, and core's: each drives a plane of its own - colour the colour plane,
 * size the lengths, aspect the proportion - so no two of them ever bind one property, and none
 * clashes with a state. A node declares the axes it adds ({@link Taxon#extents()}); every node
 * under it has them too, and adds its own but never drops one, so what is varied along a branch
 * is varied the same, to the eye and to the hand, by every leaf under it. A part takes its axes
 * from its own component, never from its owner: a square card may hold a long button.</p>
 */
public enum ExtentAxis {

    /**
     * How much of its colour's meaning a component carries: 1, its rest, the meaning at full; 0
     * neutral, none of it; −1 the meaning turned the other way - danger at −1 is safety.
     */
    COLOUR(1),

    /** How big a component is: 0, its rest, regular; 1 the biggest the design allows; −1 the smallest. */
    SIZE(0),

    /** How wide a component whose measure is its own: 0, its rest, as the design has it; 1 the widest; −1 the tallest. */
    ASPECT(0);

    private final int rest;

    ExtentAxis(int rest) { this.rest = rest; }

    /** The number an element is at until it is set. */
    public int rest() { return rest; }
}
