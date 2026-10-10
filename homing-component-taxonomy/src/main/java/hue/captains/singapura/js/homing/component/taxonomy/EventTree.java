package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A tree of events, as read: its branches under the root, and its types. It cannot be constructed
 * with two nodes of one name - the check is the constructor's, so no tree with a clash exists,
 * however it is made.
 *
 * @param branches the branches under the root
 * @param types    the types: the reader's list in its order, then those only a component offers
 */
public record EventTree(List<EventBranch> branches, List<EventType<?>> types) implements ValueObject {

    public EventTree {
        branches = List.copyOf(branches);
        types = List.copyOf(types);
        var problems = TreeChecks.INSTANCE.clashes("events", nodes(branches, types), EventNode::name);
        if (!problems.isEmpty()) throw new RefusedTaxonomy(problems);
    }

    /** Every node: the root, the branches, the types. */
    static List<EventNode> nodes(List<? extends EventBranch> branches, List<? extends EventType<?>> types) {
        var out = new ArrayList<EventNode>();
        out.add(EventRoot.INSTANCE);
        out.addAll(branches);
        out.addAll(types);
        return out;
    }

    /** The branch a node is filed under; none for the root. */
    public EventBranch parent(EventNode node) {
        return switch (node) {
            case EventType<?> t -> t.parent();
            case L1_EventBranch<?> b -> b.parent();
            case L0_EventBranch r -> null;
        };
    }

    /** The branches and types directly under a branch - the root's included - branches first. */
    public List<EventNode> children(EventBranch branch) {
        var out = new ArrayList<EventNode>();
        for (EventBranch b : branches) if (branch.equals(parent(b))) out.add(b);
        for (EventType<?> t : types) if (branch.equals(t.parent())) out.add(t);
        return List.copyOf(out);
    }

    /** What a node of the tree means: read, on a tree that was, from the section written for it. */
    public Meaning meaning(EventNode node) { return Meanings.INSTANCE.meant(node); }
}
