package hue.captains.singapura.js.homing.component.taxonomy;

/** The root of the role catalogue: the one node at level 0, which nobody adds. */
public record RoleRoot() implements L0_RoleBranch {

    public static final RoleRoot INSTANCE = new RoleRoot();
}
