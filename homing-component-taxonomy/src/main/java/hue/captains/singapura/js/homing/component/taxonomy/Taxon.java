package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A node of the tree itself: a {@link Branch} - the {@link Root} or a {@link Kind} - or a
 * {@link Component} at a leaf. Each is a stateless singleton record, used as a plain object,
 * named after its type.
 */
public sealed interface Taxon extends ComponentNode, StatelessFunctionalObject permits Branch, Component {

    /** {@code PlainButton} gives {@code plain-button}. */
    @Override default NodeName name() { return NodeName.ofType(getClass(), ""); }
}
