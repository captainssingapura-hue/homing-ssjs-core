package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;

/**
 * What a design branch refuses to hold (RFC 0067, appendix Virtual Leaves and Designs): a component that
 * is not virtual, a virtual leaf with parts, or a branch of its own - a design is judged complete over
 * exactly its branch's leaves, so nothing else may sit there. And a virtual leaf plays no part: what a
 * page wears is a concrete leaf of it, and a part is cast. Checked where a taxonomy is read.
 */
record DesignBranchChecks() implements StatelessFunctionalObject {

    static final DesignBranchChecks INSTANCE = new DesignBranchChecks();

    List<TaxonomyProblem> problems(List<ComponentBranch> branches, List<Component<?>> components, List<Part<?, ?>> parts) {
        var out = new ArrayList<TaxonomyProblem>();
        for (ComponentBranch b : branches)
            if (Levels.parentOf(b) instanceof DesignBranch above)
                out.add(new TaxonomyProblem(Rule.BRANCH_IN_DESIGN_BRANCH, Names.of(b) + " is a branch under the design branch "
                        + Names.of(above) + ", which holds virtual leaves only"));
        for (Component<?> c : components) {
            boolean virtual = c instanceof VirtualComponent<?>;
            if (!virtual && c.parent() instanceof DesignBranch branch)
                out.add(new TaxonomyProblem(Rule.NOT_VIRTUAL, Names.of(c) + " sits under the design branch " + Names.of(branch)
                        + " and is not virtual: a design could never be complete over it"));
            if (virtual && !c.parts().isEmpty())
                out.add(new TaxonomyProblem(Rule.VIRTUAL_PARTS, Names.of(c) + " is virtual and declares parts: a virtual leaf has none,"
                        + " and its concrete leaves add none"));
        }
        for (Part<?, ?> p : parts)
            if (p.base() instanceof VirtualComponent<?>)
                out.add(new TaxonomyProblem(Rule.VIRTUAL_PLAYS_A_PART, Names.of(p.owner()) + "." + Names.of(p.role()) + " is played by the virtual leaf "
                        + Names.of(p.base()) + ": a part is cast, and a virtual leaf never is"));
        return List.copyOf(out);
    }
}
