package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyFinding.Sign;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * A taxonomy as read: every branch and component reached from the components declared - up
 * through their parents, across through the components that play their parts - every part, each
 * slot with its owner appended, and the role catalogue the slots name. What a design walks, what
 * derives the design classes, and what the workbench shows.
 *
 * @param branches   the branches under the root, by level, each level in the order first reached
 * @param components the components, in the order they were reached
 * @param parts      the parts, each component's in its slots' order
 * @param catalogue  the role catalogue: its branches by level, and its roles - those named by a slot in the order
 *                   first named, then those given and named by none
 */
public record Taxonomy(List<ComponentBranch> branches, List<Component<?>> components, List<Part<?, ?>> parts,
                       RoleCatalogue catalogue) implements ValueObject {

    public Taxonomy {
        branches = List.copyOf(branches);
        components = List.copyOf(components);
        parts = List.copyOf(parts);
        Objects.requireNonNull(catalogue, "Taxonomy.catalogue");
    }

    /** Every node a design can be asked about: the root, the branches, the components, then the parts. */
    public List<ComponentNode> nodes() {
        var out = new ArrayList<ComponentNode>();
        out.add(Root.INSTANCE);
        out.addAll(branches);
        out.addAll(components);
        out.addAll(parts);
        return List.copyOf(out);
    }

    /** The branches and components directly under a branch - the root's included - branches first, each in reading order. */
    public List<Taxon> children(ComponentBranch branch) {
        var out = new ArrayList<Taxon>();
        for (ComponentBranch b : branches) if (branch.equals(Levels.parentOf(b))) out.add(b);
        for (Component<?> c : components) if (branch.equals(c.parent())) out.add(c);
        return List.copyOf(out);
    }

    /** The parts a component declares, in its slots' order. */
    public List<Part<?, ?>> partsOf(Component<?> owner) {
        return parts.stream().filter(p -> p.owner().equals(owner)).toList();
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
        for (ComponentBranch b = Levels.parentOf(t); b != null; b = Levels.parentOf(b)) out.add(b);
        return List.copyOf(out);
    }

    /** A node's token, unique across a taxonomy: the reader refuses two nodes with one. */
    public String token(ComponentNode node) { return node.token(); }

    // ── the role catalogue ─────────────────────────────────────────────────

    /** The role catalogue's branches, by level. */
    public List<RoleBranch> roleBranches() { return catalogue.branches(); }

    /** The role catalogue's roles. */
    public List<Role<?>> roles() { return catalogue.roles(); }

    /** The branches and roles directly under a branch of the catalogue - the root's included - branches first. */
    public List<RoleNode> children(RoleBranch branch) { return catalogue.children(branch); }

    /** Every part that names a role: where it is used, what plays it there, how many. */
    public List<Part<?, ?>> partsNaming(Role<?> role) {
        return parts.stream().filter(p -> p.role().equals(role)).toList();
    }

    // ── signs ──────────────────────────────────────────────────────────────

    /** What reading notices and refuses nothing for. */
    public List<TaxonomyFinding> findings() {
        var out = new ArrayList<TaxonomyFinding>();
        for (Component<?> c : components) {
            var own = partsOf(c);
            if (!own.isEmpty() && own.stream().allMatch(p -> p.cardinality().least() == 0))
                out.add(new TaxonomyFinding(Sign.ALL_OPTIONAL, Names.of(c) + ": every part is optional - a branch missing its plain leaf?"));
        }
        for (Role<?> r : roles()) {
            var uses = partsNaming(r);
            if (uses.isEmpty()) {
                out.add(new TaxonomyFinding(Sign.ROLE_UNNAMED, Names.of(r) + " is named by no component"));
                continue;
            }
            var bases = new LinkedHashSet<Component<?>>();
            for (Part<?, ?> p : uses) bases.add(p.base());
            if (bases.size() > 1)
                out.add(new TaxonomyFinding(Sign.ROLE_PLAYED_VARIOUSLY, Names.of(r) + " is played by "
                        + String.join(", ", bases.stream().map(b -> Names.of(b) + " in "
                                + String.join(" and ", uses.stream().filter(p -> p.base().equals(b)).map(p -> Names.of(p.owner())).toList()))
                        .toList())));
        }
        return List.copyOf(out);
    }
}
