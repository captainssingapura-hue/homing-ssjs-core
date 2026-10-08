package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * The root level of the role catalogue: the one level with no parent, and the one branch nobody
 * adds - it permits the {@link RoleRoot} alone.
 */
public sealed interface L0_RoleBranch extends RoleBranch permits RoleRoot {

    @Override default int level() { return 0; }
}
