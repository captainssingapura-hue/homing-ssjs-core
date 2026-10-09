package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A type of state, a leaf of the tree of states: what a component may be - enabled, on. A shared,
 * stateless singleton filed under a branch, the root's included. A component names the states it
 * has ({@link Component#states()}); each has its change, an event no tree needs to hold.
 *
 * <pre>{@code
 * public record Enabled() implements StateType<StateRoot> {
 *     public static final Enabled INSTANCE = new Enabled();
 *     @Override public StateRoot parent() { return StateRoot.INSTANCE; }
 * }
 * }</pre>
 *
 * @param <P> the branch it is filed under
 */
public non-sealed interface StateType<P extends StateBranch> extends StateNode {

    /** The branch it is filed under. */
    P parent();
}
