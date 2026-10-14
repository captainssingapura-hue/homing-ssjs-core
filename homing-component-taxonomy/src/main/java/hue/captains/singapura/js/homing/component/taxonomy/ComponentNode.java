package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.Immutable;

/**
 * A node of the taxonomy of components: one of the tree's own nodes ({@link Taxon} - the root,
 * a branch, a component), or a {@link Part} - a role a component declares, played by another
 * component. Every node is something a design can be asked about; a design class is a target
 * paired with one of them.
 */
public sealed interface ComponentNode extends Immutable permits Taxon, Part {

    /** The node's own name: from its type for the tree's nodes, from its role for a part. */
    NodeName name();

    /**
     * The node's token: its own name for the root, a branch or a component ({@code plain-button});
     * for a part, its owner's and its role's ({@code confirmation-confirm}). A reader refuses two nodes with
     * one token.
     */
    default String token() { return name().value(); }
}
