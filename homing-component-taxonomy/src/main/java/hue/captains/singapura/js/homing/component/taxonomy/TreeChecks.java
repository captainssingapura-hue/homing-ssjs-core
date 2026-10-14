package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    /** A type with fewer than two values, a value twice, none said at all, or a design word for its rest or for a value it lacks. */
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
            Map<StateValue, String> words = t.words() == null ? Map.of() : t.words();
            words.forEach((v, w) -> {
                if (!values.contains(v) || v.equals(values.get(0)))
                    out.add(new TaxonomyProblem(Rule.BAD_STATE_VALUES, Names.qualified(t) + " gives a design word to " + v
                            + (values.contains(v) ? ", its rest - a look, not a word" : ", which is none of its values")));
                else if (w == null || !w.matches("[a-z][a-z0-9]*(-[a-z0-9]+)*"))
                    out.add(new TaxonomyProblem(Rule.BAD_STATE_VALUES, Names.qualified(t) + "'s " + v + " has the design word '" + w
                            + "': lower case, its words joined by hyphens"));
            });
        }
        return List.copyOf(out);
    }

    /**
     * A type of state with no level of importance - every state has one, intrinsic, on the tree of states - and a pseudo
     * state that is no flag, or whose pseudo-class is none.
     */
    List<TaxonomyProblem> levels(List<? extends StateType<?>> types) {
        var out = new ArrayList<TaxonomyProblem>();
        for (StateType<?> t : types) {
            if (Importances.INSTANCE.levelOf(t).isEmpty())
                out.add(new TaxonomyProblem(Rule.STATE_UNRANKED, Names.qualified(t) + " has no level of importance: every state "
                        + "implements one, Importance.Primary to Importance.Senary"));
            if (t instanceof Pseudo p) {
                List<StateValue> values = t.values();
                if (values != null && values.size() != 2)
                    out.add(new TaxonomyProblem(Rule.PSEUDO_BADLY, Names.qualified(t) + " is a pseudo state with " + values.size()
                            + " values: a pseudo state is a flag, its one value beside its rest the one its pseudo-class shows"));
                String pc = p.pseudoClass();
                if (pc == null || !pc.matches(":[a-z][a-z-]*"))
                    out.add(new TaxonomyProblem(Rule.PSEUDO_BADLY, Names.qualified(t) + "'s pseudo-class '" + pc + "' is none: ':hover', "
                            + "':focus-visible'"));
            }
        }
        return List.copyOf(out);
    }

    /**
     * A leaf - a component or a part - that lists a state twice or lists nothing, or overrides the level of a state it
     * does not list, or of one twice.
     */
    List<TaxonomyProblem> leafStates(List<? extends ComponentLeaf> leaves) {
        var out = new ArrayList<TaxonomyProblem>();
        for (ComponentLeaf leaf : leaves) {
            String who = leaf.token();
            var listed = new HashSet<StateType<?>>();
            for (StateType<?> s : leaf.states() == null ? List.<StateType<?>>of() : leaf.states()) {
                if (s == null) out.add(new TaxonomyProblem(Rule.STATES_LISTED_BADLY, who + " lists a state that is nothing"));
                else if (!listed.add(s)) out.add(new TaxonomyProblem(Rule.STATES_LISTED_BADLY, who + " lists " + Names.of(s) + " twice"));
            }
            var overridden = new HashSet<StateType<?>>();
            for (ImportanceOverride<?> o : leaf.importance() == null ? List.<ImportanceOverride<?>>of() : leaf.importance()) {
                if (o == null) {
                    out.add(new TaxonomyProblem(Rule.STATES_LISTED_BADLY, who + " overrides the level of nothing"));
                    continue;
                }
                if (!listed.contains(o.state()))
                    out.add(new TaxonomyProblem(Rule.STATES_LISTED_BADLY, who + " overrides the level of " + Names.of(o.state())
                            + ", which it does not list"));
                if (!overridden.add(o.state()))
                    out.add(new TaxonomyProblem(Rule.STATES_LISTED_BADLY, who + " overrides the level of " + Names.of(o.state()) + " twice"));
            }
        }
        return List.copyOf(out);
    }

    /** Two values of the tree answered by one design word - an established word, or a type's name and a value's. */
    List<TaxonomyProblem> wordsTwice(List<? extends StateType<?>> types) {
        var byWord = new LinkedHashMap<String, List<String>>();
        for (StateType<?> t : types) {
            List<StateValue> values = t.values();
            if (values == null || values.size() < 2) continue;
            for (StateValue v : values.subList(1, values.size())) {
                String w;
                try { w = t.word(v); } catch (IllegalArgumentException e) { continue; }
                byWord.computeIfAbsent(w, x -> new ArrayList<>()).add(Names.qualified(t) + "(" + v + ")");
            }
        }
        var out = new ArrayList<TaxonomyProblem>();
        byWord.forEach((word, same) -> {
            if (same.size() > 1)
                out.add(new TaxonomyProblem(Rule.DESIGN_WORD_TWICE, "the design word '" + word + "' answers " + String.join(" and ", same)));
        });
        return List.copyOf(out);
    }
}
