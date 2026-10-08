package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * One word, one meaning, across the whole role catalogue: no two of its nodes - the root, a branch,
 * a role, at any level, under any parent - answer to one name. A library adding a branch or a role
 * picks a word the catalogue does not already use. Every clash is named, each once.
 */
public record CatalogueNames() implements StatelessFunctionalObject {

    /** Every name answered to by more than one node of the catalogue: the root, the branches and the roles given. */
    public List<TaxonomyProblem> clashes(Collection<? extends RoleBranch> branches, Collection<? extends Role<?>> roles) {
        var nodes = new LinkedHashSet<RoleNode>();
        nodes.add(RoleRoot.INSTANCE);
        nodes.addAll(branches);
        nodes.addAll(roles);
        var byName = new LinkedHashMap<String, List<RoleNode>>();
        for (RoleNode n : nodes) byName.computeIfAbsent(n.name().value(), x -> new ArrayList<>()).add(n);
        var out = new ArrayList<TaxonomyProblem>();
        byName.forEach((name, same) -> {
            if (same.size() > 1)
                out.add(new TaxonomyProblem(Rule.CATALOGUE_NAME_TWICE, "'" + name + "' names " + same.size()
                        + " nodes of the role catalogue: " + String.join(" and ", same.stream().map(CatalogueNames::said).toList())));
        });
        return List.copyOf(out);
    }

    private static String said(RoleNode n) {
        return (n instanceof Role<?> ? "the role " : "the branch ") + Names.qualified(n);
    }
}
