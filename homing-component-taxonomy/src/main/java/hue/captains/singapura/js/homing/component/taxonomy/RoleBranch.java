package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch of the role catalogue, levelled as the taxonomy's are: the root at
 * {@link L0_RoleBranch level 0}, then {@link L1_RoleBranch} to {@link L8_RoleBranch}, each naming a
 * parent one level up. It groups the roles and branches under it, named for what they do for
 * their owner - saying something of it, letting the user act on it, shaping its room - and it is
 * open below the root: any library adds a branch or a role under any branch, at any level.
 */
public sealed interface RoleBranch extends RoleNode
        permits L0_RoleBranch,
                L1_RoleBranch, L2_RoleBranch, L3_RoleBranch, L4_RoleBranch,
                L5_RoleBranch, L6_RoleBranch, L7_RoleBranch, L8_RoleBranch {

    /** Its level: 0 for the root, one more than its parent's for every other. */
    int level();
}
