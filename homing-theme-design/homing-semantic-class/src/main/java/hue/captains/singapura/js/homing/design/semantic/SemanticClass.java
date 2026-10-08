package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentNode;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.Trees;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * A semantic class: a target leaf paired with a node of the taxonomy of components - what a
 * design is asked about. A plain value of two objects, equal by value, declared by nobody: the
 * forest of semantic classes is the product of the targets and the taxonomy.
 *
 * <p>Its path reads root first - {@code Color.Ink › Confirmation.Title} - and its token is the node's
 * and the target's: {@code confirmation-title-color-ink}.</p>
 *
 * @param target    a leaf of the closed target tree
 * @param component a node of the taxonomy: the root, a kind, a component or a part
 * @param <T>       the target leaf
 * @param <C>       the node
 */
public record SemanticClass<T extends Target, C extends ComponentNode>(T target, C component) implements ValueObject {

    public SemanticClass {
        Objects.requireNonNull(target, "SemanticClass.target");
        Objects.requireNonNull(component, "SemanticClass.component");
        Trees.requireTargetLeaf(target.getClass());
    }

    /** The node's token and the target's: {@code confirmation-confirm-color-surface}. */
    public String token() { return component.token() + "-" + target.token(); }

    /**
     * The classes a design is asked in, most specific first: this one, then the same target on
     * each node of the component's chain up to the root - a part's through its base, never its
     * owner.
     */
    public List<SemanticClass<T, ComponentNode>> fallback(Taxonomy taxonomy) {
        return taxonomy.fallback(component).stream().map(n -> new SemanticClass<T, ComponentNode>(target, n)).toList();
    }

    @Override public String toString() { return token(); }
}
