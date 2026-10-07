package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A role catalogue, as read: its branches under the root and its roles. It cannot be constructed
 * with two nodes of one name - the check is the constructor's, so no catalogue with a clash exists,
 * however it is made: by reading a taxonomy, or by a library checking its own words.
 *
 * @param branches the branches under the root, by level
 * @param roles    the roles
 */
public record RoleCatalogue(List<RoleBranch> branches, List<Role<?>> roles) implements ValueObject {

    public RoleCatalogue {
        branches = List.copyOf(branches);
        roles = List.copyOf(roles);
        var clashes = new CatalogueNames().clashes(branches, roles);
        if (!clashes.isEmpty()) throw new RefusedTaxonomy(clashes);
    }

    /** The branches and roles directly under a branch - the root's included - branches first. */
    public List<RoleNode> children(RoleBranch branch) {
        var out = new ArrayList<RoleNode>();
        for (RoleBranch b : branches) if (branch.equals(Levels.parentOf(b))) out.add(b);
        for (Role<?> r : roles) if (branch.equals(r.parent())) out.add(r);
        return List.copyOf(out);
    }
}
