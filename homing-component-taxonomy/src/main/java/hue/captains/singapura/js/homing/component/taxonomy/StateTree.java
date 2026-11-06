package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.Scalar;

import java.util.ArrayList;
import java.util.List;

/**
 * A tree of states, as read: its branches under the root, and its types. It files the types, not
 * their values. It cannot be constructed with two nodes of one name, two values answered by one
 * design word, nor with a type whose values do not hold together - the checks are the constructor's, so no tree that breaks them exists, however
 * it is made.
 *
 * @param branches the branches under the root
 * @param types    the types: the reader's list in its order, then those only a component names
 */
public record StateTree(List<StateBranch> branches, List<StateType<?>> types) implements Scalar {

    public StateTree {
        branches = List.copyOf(branches);
        types = List.copyOf(types);
        var problems = new ArrayList<>(TreeChecks.INSTANCE.clashes("states", StateNodes.INSTANCE.of(branches, types), StateNode::name));
        problems.addAll(TreeChecks.INSTANCE.badValues(types));
        problems.addAll(TreeChecks.INSTANCE.wordsTwice(types));
        if (!problems.isEmpty()) throw new RefusedTaxonomy(problems);
    }

    /** The branch a node is filed under; none for the root. */
    public StateBranch parent(StateNode node) {
        return switch (node) {
            case StateType<?> t -> t.parent();
            case L1_StateBranch<?> b -> b.parent();
            case L0_StateBranch r -> null;
        };
    }

    /** The branches and types directly under a branch - the root's included - branches first. */
    public List<StateNode> children(StateBranch branch) {
        var out = new ArrayList<StateNode>();
        for (StateBranch b : branches) if (branch.equals(parent(b))) out.add(b);
        for (StateType<?> t : types) if (branch.equals(t.parent())) out.add(t);
        return List.copyOf(out);
    }

    /** What a node of the tree means: read, on a tree that was, from the section written for it. */
    public Meaning meaning(StateNode node) { return Meanings.INSTANCE.meant(node); }
}
