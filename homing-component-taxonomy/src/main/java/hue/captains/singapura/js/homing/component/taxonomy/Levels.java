package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A node's parent, read off its level - one exhaustive switch over the sealed levels of each tree,
 * so a level added without its case does not compile. None for a root.
 */
final class Levels {

    private Levels() {}

    /** The branch a node of the taxonomy sits under; none for the root. */
    static ComponentBranch parentOf(Taxon t) {
        return switch (t) {
            case Component<?> c          -> c.parent();
            case L0_ComponentBranch root -> null;
            case L1_ComponentBranch<?> b -> b.parent();
            case L2_ComponentBranch<?> b -> b.parent();
            case L3_ComponentBranch<?> b -> b.parent();
            case L4_ComponentBranch<?> b -> b.parent();
            case L5_ComponentBranch<?> b -> b.parent();
            case L6_ComponentBranch<?> b -> b.parent();
            case L7_ComponentBranch<?> b -> b.parent();
            case L8_ComponentBranch<?> b -> b.parent();
        };
    }

    /** The branch a node of the role catalogue is filed under; none for the root. */
    static RoleBranch parentOf(RoleNode n) {
        return switch (n) {
            case Role<?> r          -> r.parent();
            case L0_RoleBranch root -> null;
            case L1_RoleBranch<?> b -> b.parent();
            case L2_RoleBranch<?> b -> b.parent();
            case L3_RoleBranch<?> b -> b.parent();
            case L4_RoleBranch<?> b -> b.parent();
            case L5_RoleBranch<?> b -> b.parent();
            case L6_RoleBranch<?> b -> b.parent();
            case L7_RoleBranch<?> b -> b.parent();
            case L8_RoleBranch<?> b -> b.parent();
        };
    }
}
