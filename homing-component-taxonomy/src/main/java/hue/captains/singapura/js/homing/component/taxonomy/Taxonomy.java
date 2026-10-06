package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A taxonomy as read: every kind and component reached from the components declared - up
 * through their parents, across through the components that play their roles - and every part,
 * each role with its owner appended. What a design walks, and what derives the design classes.
 *
 * @param kinds      the kinds, parents before their children
 * @param components the components, in the order they were reached
 * @param parts      the parts, each component's in its roles' order
 */
public record Taxonomy(List<Kind<?>> kinds, List<Component<?>> components, List<Part<?, ?>> parts)
        implements ValueObject {

    public Taxonomy {
        kinds = List.copyOf(kinds);
        components = List.copyOf(components);
        parts = List.copyOf(parts);
    }

    /** Every node: the root, the kinds, the components, then the parts. */
    public List<ComponentNode> nodes() {
        var out = new ArrayList<ComponentNode>();
        out.add(Root.INSTANCE);
        out.addAll(kinds);
        out.addAll(components);
        out.addAll(parts);
        return List.copyOf(out);
    }

    /** The kinds and components directly under a branch, kinds first, each in reading order. */
    public List<Taxon> children(Branch branch) {
        var out = new ArrayList<Taxon>();
        for (Kind<?> k : kinds) if (k.parent().equals(branch)) out.add(k);
        for (Component<?> c : components) if (c.parent().equals(branch)) out.add(c);
        return List.copyOf(out);
    }

    /** The parts a component declares, in its roles' order. */
    public List<Part<?, ?>> partsOf(Component<?> owner) {
        return parts.stream().filter(p -> p.belongsTo().equals(owner)).toList();
    }

    /**
     * The chain a design walks for a node, most specific first: the node, its parent, and so on
     * up to the root. A part's chain is the part, then its base's - never its owner's: a part is
     * answered as what it is, never as where it is.
     */
    public List<ComponentNode> fallback(ComponentNode node) {
        var out = new ArrayList<ComponentNode>();
        out.add(node);
        Taxon t = node instanceof Part<?, ?> p ? p.base() : (Taxon) node;
        if (node instanceof Part<?, ?>) out.add(t);
        for (Branch b = parentOf(t); b != null; b = parentOf(b)) out.add(b);
        return List.copyOf(out);
    }

    /** A node's token, unique across a taxonomy: the reader refuses two nodes with one. */
    public String token(ComponentNode node) { return node.token(); }

    /** The branch a node sits under; none for the root. */
    static Branch parentOf(Taxon t) {
        return switch (t) {
            case Root r         -> null;
            case Kind<?> k      -> k.parent();
            case Component<?> c -> c.parent();
        };
    }
}
