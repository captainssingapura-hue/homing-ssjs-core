package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A node of the role catalogue that may have nodes under it: the {@link AnyRole} root, or a
 * {@link RoleFamily}. Only a branch can be named as a family's parent, and only a family as a
 * role's, so a role is always a leaf, and never filed at the root.
 */
public sealed interface RoleBranch extends RoleNode permits AnyRole, RoleFamily {
}
