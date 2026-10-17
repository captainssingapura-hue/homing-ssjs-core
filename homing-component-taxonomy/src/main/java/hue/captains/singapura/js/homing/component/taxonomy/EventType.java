package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A type of event, a leaf of the tree of events: what can happen to a component besides a change of
 * its states - a click. A shared, stateless singleton filed under a branch, the root's included. A
 * component names the events it offers ({@link Component#events()}).
 *
 * @param <P> the branch it is filed under
 */
public non-sealed interface EventType<P extends EventBranch> extends EventNode {

    /** The branch it is filed under. */
    P parent();
}
