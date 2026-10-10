package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * What the trees of states and of events refuse to be made of: two nodes of one tree that answer to
 * one name - one word, one meaning, within each tree - and a type of state whose values do not hold
 * together. Checked where a tree is constructed, and where a taxonomy is read, so a read reports
 * every problem at once.
 */
record TreeChecks() implements StatelessFunctionalObject {

    static final TreeChecks INSTANCE = new TreeChecks();

    /** Every name more than one of the nodes answers to - the root, the branches, the types - each said once. */
    <N> List<TaxonomyProblem> clashes(String tree, List<? extends N> nodes, Function<N, NodeName> nameOf) {
        var byName = new LinkedHashMap<String, List<N>>();
        for (N n : nodes) byName.computeIfAbsent(nameOf.apply(n).value(), x -> new ArrayList<>()).add(n);
        var out = new ArrayList<TaxonomyProblem>();
        byName.forEach((name, same) -> {
            if (same.size() > 1)
                out.add(new TaxonomyProblem(Rule.TREE_NAME_TWICE, "'" + name + "' names " + same.size() + " nodes of the tree of "
                        + tree + ": " + String.join(" and ", same.stream().map(Names::qualified).toList())));
        });
        return List.copyOf(out);
    }

    /** A type with fewer than two values, a value twice, or none said at all. */
    List<TaxonomyProblem> badValues(List<? extends StateType<?>> types) {
        var out = new ArrayList<TaxonomyProblem>();
        for (StateType<?> t : types) {
            List<StateValue> values = t.values();
            if (values == null || values.size() < 2) {
                out.add(new TaxonomyProblem(Rule.BAD_STATE_VALUES, Names.qualified(t) + " has "
                        + (values == null ? "no values" : values.size() + " value" + (values.size() == 1 ? "" : "s"))
                        + ": a type of state is one way or another, so two values at least"));
                continue;
            }
            var seen = new HashSet<StateValue>();
            for (StateValue v : values)
                if (v == null) out.add(new TaxonomyProblem(Rule.BAD_STATE_VALUES, Names.qualified(t) + " has a value that is nothing"));
                else if (!seen.add(v)) out.add(new TaxonomyProblem(Rule.BAD_STATE_VALUES, Names.qualified(t) + " has the value " + v + " twice"));
        }
        return List.copyOf(out);
    }
}
