package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A node of the tree of states: a {@link StateBranch} at its level, or a {@link StateType} at a
 * leaf. The states a component has - being enabled, being on - cut across components, so they
 * have a tree of their own, apart from the components, as the roles have theirs. Each node is a
 * stateless singleton: a type, never a value that changes. A change of a state is an event, and
 * has no node of its own: it is named on its type, if at all (RFC 0069, amended).
 */
public sealed interface StateNode extends StatelessFunctionalObject permits StateBranch, StateType {

    /** {@code Enabled} gives {@code enabled}. */
    default NodeName name() { return NodeName.ofType(getClass(), ""); }
}
