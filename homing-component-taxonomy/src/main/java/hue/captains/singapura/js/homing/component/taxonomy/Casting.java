package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A role played by a component - {@code (DangerButton, Go)}, a danger button as Go - the unit the
 * design language is realized in (RFC 0067, appendix Castings). Standalone, a component on its
 * own, is the role catalogue's root. The owner is never in it: two owners casting one component
 * in one role get the same thing.
 *
 * <p>A component that is cast is a leaf and so is its role; the nodes its look falls back to are
 * castings too, of a branch or in a broader role - {@code (Button, Committing)} - which nothing is
 * cast as, and which a design may answer.</p>
 *
 * @param component the component, or a branch of the taxonomy on the way to the root
 * @param role      the role, a branch of the catalogue on the way to its root, or the root itself:
 *                  Standalone
 */
public record Casting(Taxon component, RoleNode role) implements ValueObject {

    public Casting {
        Objects.requireNonNull(component, "Casting.component");
        Objects.requireNonNull(role, "Casting.role");
    }

    /** A component on its own: cast as Standalone, the role catalogue's root. */
    public Casting(Taxon component) { this(component, RoleRoot.INSTANCE); }

    /** Whether it is cast as Standalone. */
    public boolean standalone() { return role instanceof RoleRoot; }

    /** The same component on its own. */
    public Casting alone() { return standalone() ? this : new Casting(component); }

    /**
     * Whether its role only places its part: filed, at any depth, under a branch that is
     * {@link RoleBranch#functional() functional}.
     */
    public boolean functional() {
        for (RoleNode n = role; n != null; n = Levels.parentOf(n))
            if (n instanceof RoleBranch b && b.functional()) return true;
        return false;
    }

    /**
     * The castings its look is asked in, most precise first: along its role's lineage at its
     * component, then the same along each of its component's parents, ending at the root on its
     * own - {@code (PlainButton, Go) → (PlainButton, Committing) → … → (PlainButton, Standalone)
     * → (Button, Go) → … → (Root, Standalone)}. A functional role is set aside: such a casting
     * looks as its component on its own does.
     */
    public List<Casting> lineage(Taxonomy taxonomy) {
        List<RoleNode> roles = functional() ? List.of(RoleRoot.INSTANCE) : roleLineage();
        var out = new ArrayList<Casting>();
        for (ComponentNode c : taxonomy.fallback(component))
            for (RoleNode r : roles) out.add(new Casting((Taxon) c, r));
        return List.copyOf(out);
    }

    /** Its role, then each branch the role is filed under, ending at the root. */
    public List<RoleNode> roleLineage() {
        var out = new ArrayList<RoleNode>();
        for (RoleNode n = role; n != null; n = Levels.parentOf(n)) out.add(n);
        return List.copyOf(out);
    }

    @Override public String toString() {
        return standalone() ? component.name().value() : component.name().value() + " as " + role.name().value();
    }
}
