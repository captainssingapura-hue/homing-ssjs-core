package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A type of state, a leaf of the tree of states: one respect in which a component may be one way or
 * another - Enabled (true or false), Elevation (flat, elevated or sunken). The tree files the types,
 * never their values: a component that has a type is in exactly one of its values at a time, so it
 * has one value of each type it names ({@link Component#states()}). A shared, stateless singleton
 * filed under a branch, the root's included; each has its change, an event no tree needs to hold.
 * A type that is yes or no is a {@link Flag}.
 *
 * <pre>{@code
 * public record Elevation() implements StateType<Physical> {
 *     public static final Elevation INSTANCE = new Elevation();
 *     @Override public Physical parent() { return Physical.INSTANCE; }
 *     @Override public List<StateValue> values() {
 *         return List.of(new StateValue("flat"), new StateValue("elevated"), new StateValue("sunken"));
 *     }
 * }
 * }</pre>
 *
 * @param <P> the branch it is filed under
 */
public non-sealed interface StateType<P extends StateBranch> extends StateNode {

    /** The branch it is filed under. */
    P parent();

    /**
     * Its values, which exclude one another, the one at rest first: the value a look is drawn for
     * when it says nothing of the state. Two at least, each once - held when the tree is read.
     */
    List<StateValue> values();
}
