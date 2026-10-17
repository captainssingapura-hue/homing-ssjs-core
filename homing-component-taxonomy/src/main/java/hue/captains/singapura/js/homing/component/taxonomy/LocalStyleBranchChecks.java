package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;

/**
 * What a local-style branch refuses to hold (RFC 0067, appendix Virtual Leaves and Designs): a component that
 * is not virtual, a virtual leaf with parts, or a branch of its own - a local style is judged complete over
 * exactly its branch's leaves, so nothing else may sit there. And a virtual leaf plays no part: what a
 * page wears is a concrete leaf of it, and a part is cast. Checked where a taxonomy is read.
 */
record LocalStyleBranchChecks() implements StatelessFunctionalObject {

    static final LocalStyleBranchChecks INSTANCE = new LocalStyleBranchChecks();

    List<TaxonomyProblem> problems(List<ComponentBranch> branches, List<Component<?>> components, List<Part<?, ?>> parts) {
        var out = new ArrayList<TaxonomyProblem>();
        for (ComponentBranch b : branches)
            if (Levels.parentOf(b) instanceof LocalStyleBranch above)
                out.add(new TaxonomyProblem(Rule.BRANCH_IN_LOCAL_STYLE_BRANCH, Names.of(b) + " is a branch under the local-style branch "
                        + Names.of(above) + ", which holds virtual leaves only"));
        for (Component<?> c : components) {
            boolean virtual = c instanceof VirtualComponent<?>;
            if (!virtual && c.parent() instanceof LocalStyleBranch branch)
                out.add(new TaxonomyProblem(Rule.NOT_VIRTUAL, Names.of(c) + " sits under the local-style branch " + Names.of(branch)
                        + " and is not virtual: a local style could never be complete over it"));
            if (virtual && !c.parts().isEmpty())
                out.add(new TaxonomyProblem(Rule.VIRTUAL_PARTS, Names.of(c) + " is virtual and declares parts: a virtual leaf has none,"
                        + " and its concrete leaves add none"));
        }
        for (Part<?, ?> p : parts)
            for (Component<?> base : p.bases())
                if (base instanceof VirtualComponent<?>)
                    out.add(new TaxonomyProblem(Rule.VIRTUAL_PLAYS_A_PART, Names.of(p.owner()) + "." + Names.of(p.role()) + " is played by the virtual leaf "
                            + Names.of(base) + ": a part is cast, and a virtual leaf never is"));
        return List.copyOf(out);
    }
}
