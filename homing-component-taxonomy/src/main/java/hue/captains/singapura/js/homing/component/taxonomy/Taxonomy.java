package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyFinding.Sign;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A taxonomy as read: every kind and component reached from the components declared - up
 * through their parents, across through the components that play their parts - every part, each
 * slot with its owner appended, and the role catalogue the slots name. What a design walks, what
 * derives the design classes, and what the workbench shows.
 *
 * @param kinds      the kinds, parents before their children
 * @param components the components, in the order they were reached
 * @param parts      the parts, each component's in its slots' order
 * @param branches   the role catalogue's branches under its root, parents before their children
 * @param roles      the roles, those named by a slot in the order first named, then those given and named by none
 */
public record Taxonomy(List<Kind<?>> kinds, List<Component<?>> components, List<Part<?, ?>> parts,
                       List<RoleBranch<?>> branches, List<Role<?>> roles) implements ValueObject {

    public Taxonomy {
        kinds = List.copyOf(kinds);
        components = List.copyOf(components);
        parts = List.copyOf(parts);
        branches = List.copyOf(branches);
        roles = List.copyOf(roles);
    }

    /** Every node a design can be asked about: the root, the kinds, the components, then the parts. */
    public List<ComponentNode> nodes() {
        var out = new ArrayList<ComponentNode>();
        out.add(Root.INSTANCE);
        out.addAll(kinds);
        out.addAll(components);
        out.addAll(parts);
        return List.copyOf(out);
    }

    /** The kinds and components directly under a branch, kinds first, each in reading order. */
    public List<Taxon> children(Branch branch) {
        var out = new ArrayList<Taxon>();
        for (Kind<?> k : kinds) if (k.parent().equals(branch)) out.add(k);
        for (Component<?> c : components) if (c.parent().equals(branch)) out.add(c);
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
        for (Branch b = parentOf(t); b != null; b = parentOf(b)) out.add(b);
        return List.copyOf(out);
    }

    /** A node's token, unique across a taxonomy: the reader refuses two nodes with one. */
    public String token(ComponentNode node) { return node.token(); }

    // ── the role catalogue ─────────────────────────────────────────────────

    /** The branches and roles directly under a branch of the catalogue - the root's included - branches first. */
    public List<RoleNode> children(RoleBranch<?> branch) {
        var out = new ArrayList<RoleNode>();
        for (RoleBranch<?> b : branches) if (b.parent().equals(branch)) out.add(b);
        for (Role<?> r : roles) if (r.parent().equals(branch)) out.add(r);
        return List.copyOf(out);
    }

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
                out.add(new TaxonomyFinding(Sign.ALL_OPTIONAL, Names.of(c) + ": every part is optional - a kind missing its plain leaf?"));
        }
        for (Role<?> r : roles) {
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

    /** The branch a node sits under; none for the root. */
    static Branch parentOf(Taxon t) {
        return switch (t) {
            case Root r         -> null;
            case Kind<?> k      -> k.parent();
            case Component<?> c -> c.parent();
        };
    }
}
