package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A node of the lexicon: a {@link WordBranch} at its level, or a {@link Word} at a leaf. The words
 * a component may show as a picture - close, check, there is more beneath - cut across components,
 * so they have a taxonomy of their own, apart from the components, as the roles, the states and
 * the events have theirs. Each node is a stateless singleton.
 */
public sealed interface WordNode extends StatelessFunctionalObject permits WordBranch, Word {
    /** {@code Close} gives {@code close}. */
    default NodeName name() { return NodeName.ofType(getClass(), ""); }
}
