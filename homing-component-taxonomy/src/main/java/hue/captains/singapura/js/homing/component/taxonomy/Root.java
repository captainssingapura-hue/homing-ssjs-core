package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * The root of the taxonomy: any component at all. Every branch and component falls back to it in
 * the end, so a design that answers the root on a target has answered every component on it. It
 * is the one node at level 0, and nobody adds another.
 */
public record Root() implements L0_ComponentBranch {

    public static final Root INSTANCE = new Root();
}
