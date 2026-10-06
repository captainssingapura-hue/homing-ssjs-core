package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * The root of the taxonomy: any component at all. Every kind and component falls back to it in
 * the end, so a design that answers the root on a target has answered every component on it.
 */
public record Root() implements Branch {

    public static final Root INSTANCE = new Root();
}
