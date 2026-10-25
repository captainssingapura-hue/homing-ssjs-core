package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * The check on a taxonomy's meanings, run at build in the test of each jar that declares nodes - never on a run-time
 * path, as jOntology's enforcer is not: every node the taxonomy holds means something, every section of their files
 * is headed as one of those nodes would be, and no two nodes mean the same. It reaches the nodes from the taxonomy
 * read, so a heading is checked against the headings they would have - a set difference - and no class is ever
 * looked up by its name.
 */
public record MeaningsCheck() implements StatelessFunctionalObject {

    public static final MeaningsCheck INSTANCE = new MeaningsCheck();

    /**
     * What is wrong with the meanings of the nodes a taxonomy holds: a node with none; a file with words outside
     * every section, a section twice, or a section headed as no node of that file the taxonomy holds; two nodes that
     * mean the same.
     */
    public List<TaxonomyProblem> problems(Taxonomy taxonomy) {
        var out = new ArrayList<TaxonomyProblem>();
        var files = new LinkedHashMap<Class<?>, Set<String>>();   // each file, by its class: the headings its nodes would have
        var said = new LinkedHashMap<String, List<Object>>();
        for (Object node : nodes(taxonomy)) {
            Class<?> top = Meanings.outermost(node.getClass());
            files.computeIfAbsent(top, MeaningsCheck::own).add(Meanings.heading(node.getClass()));
            var meaning = Meanings.INSTANCE.of(node);
            if (meaning.isEmpty()) {
                out.add(new TaxonomyProblem(Rule.NO_MEANING, Names.qualified(node) + " means nothing: no section '## "
                        + Meanings.heading(node.getClass()) + "' with words in it, in " + Meanings.path(top)));
                continue;
            }
            said.computeIfAbsent(Meanings.normalized(meaning.get().markdown()), x -> new ArrayList<>()).add(node);
        }
        files.forEach((top, headings) -> {
            var text = Meanings.read(top);
            if (text.isEmpty()) return;
            var read = Meanings.sections(text.get());
            for (String astray : read.astray())
                out.add(new TaxonomyProblem(Rule.MEANING_ASTRAY, Meanings.path(top) + ": " + astray));
            for (String heading : read.sections().keySet())
                if (!headings.contains(heading))
                    out.add(new TaxonomyProblem(Rule.MEANING_ASTRAY, Meanings.path(top) + ": '## " + heading
                            + "' names no node " + top.getSimpleName() + " declares"));
        });
        said.values().forEach(same -> {
            if (same.size() > 1)
                out.add(new TaxonomyProblem(Rule.MEANING_TWICE, String.join(" and ", same.stream().map(Names::qualified).toList())
                        + " mean the same: one node, or two meanings"));
        });
        return out;
    }

    /** Every node a taxonomy holds that must mean something: each tree's root when anything of the tree is held, its branches, its leaves. */
    private static List<Object> nodes(Taxonomy t) {
        var out = new ArrayList<Object>();
        if (!t.components().isEmpty()) out.add(Root.INSTANCE);
        out.addAll(t.branches());
        out.addAll(t.components());
        if (!t.roles().isEmpty()) out.add(RoleRoot.INSTANCE);
        out.addAll(t.roleBranches());
        out.addAll(t.roles());
        if (!t.states().types().isEmpty()) out.addAll(StateTree.nodes(t.states().branches(), t.states().types()));
        return out;
    }

    /** The headings a file has whatever is held: the class's own name, when the class is itself a node. */
    private static Set<String> own(Class<?> top) {
        var out = new HashSet<String>();
        if (Taxon.class.isAssignableFrom(top) || RoleNode.class.isAssignableFrom(top) || StateNode.class.isAssignableFrom(top))
            out.add(top.getSimpleName());
        return out;
    }
}
