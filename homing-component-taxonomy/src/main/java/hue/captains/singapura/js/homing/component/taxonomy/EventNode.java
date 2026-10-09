package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A node of the tree of events: an {@link EventBranch} at its level, or an {@link EventType} at a
 * leaf. It holds what happens to a component that is not a change of one of its states - a click,
 * a hover - since a state's change is exactly the state. Each node is a stateless singleton.
 */
public sealed interface EventNode extends StatelessFunctionalObject permits EventBranch, EventType {

    /** {@code Click} gives {@code click}. */
    default NodeName name() { return NodeName.ofType(getClass(), ""); }
}
