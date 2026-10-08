package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * A node of the tree itself: a {@link ComponentBranch} at its level, or a {@link Component} at a
 * leaf. Each is a stateless singleton record - held to it by jOntology when the taxonomy is read -
 * used as a plain object, and named after its type.
 */
public sealed interface Taxon extends ComponentNode, StatelessFunctionalObject permits ComponentBranch, Component {

    /** {@code PlainButton} gives {@code plain-button}. */
    @Override default NodeName name() { return NodeName.ofType(getClass(), ""); }

    /**
     * The axes this node adds, along which it varies by degree; none by default. Every node under
     * it has them too, so a node declares only what its branches do not: an axis declared twice
     * along one lineage is refused when the taxonomy is read.
     */
    default List<ExtentAxis> extents() { return List.of(); }
}
