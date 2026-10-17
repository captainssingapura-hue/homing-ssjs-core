package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A node of the tree of states: a {@link StateBranch} at its level, or a {@link StateType} at a
 * leaf. The states a component has - being enabled, being on - cut across components, so they
 * have a tree of their own, apart from the components, as the roles have theirs. Each node is a
 * stateless singleton: a type, never a value that changes. A state's change is exactly the state,
 * so it has no node of its own in the tree of events.
 */
public sealed interface StateNode extends StatelessFunctionalObject permits StateBranch, StateType {

    /** {@code Enabled} gives {@code enabled}. */
    default NodeName name() { return NodeName.ofType(getClass(), ""); }
}
