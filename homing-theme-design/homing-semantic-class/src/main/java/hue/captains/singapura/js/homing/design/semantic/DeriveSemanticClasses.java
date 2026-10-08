package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentNode;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.Trees;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Derives the forest of semantic classes: every target leaf × every node a taxonomy reads,
 * target by target, each in the taxonomy's order. Nothing is declared for it. Two classes that
 * would carry one token are refused - the taxonomy already refuses two nodes with one, so this
 * holds the join of the two trees to the same rule.
 */
public record DeriveSemanticClasses() implements StatelessFunctionalObject {

    /** Over every leaf of the target tree. */
    public List<SemanticClass<Target, ComponentNode>> derive(Taxonomy taxonomy) {
        return derive(taxonomy, Trees.targetLeaves());
    }

    /** Over the target leaves given. */
    public List<SemanticClass<Target, ComponentNode>> derive(Taxonomy taxonomy, List<? extends Target> targets) {
        var out = new ArrayList<SemanticClass<Target, ComponentNode>>();
        for (Target t : targets)
            for (ComponentNode n : taxonomy.nodes())
                out.add(new SemanticClass<>(t, n));
        var byToken = new LinkedHashMap<String, SemanticClass<?, ?>>();
        for (var c : out) {
            var other = byToken.putIfAbsent(c.token(), c);
            if (other != null)
                throw new IllegalStateException("'" + c.token() + "' is derived twice: "
                        + other.component() + " on " + other.target() + ", and " + c.component() + " on " + c.target());
        }
        return List.copyOf(out);
    }
}
