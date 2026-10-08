package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch of the taxonomy of components, levelled as the catalogue is: the root at
 * {@link L0_ComponentBranch level 0}, then {@link L1_ComponentBranch} to {@link L8_ComponentBranch},
 * each naming a parent one level up. The shape is in the types: a branch cannot be declared at a
 * depth its type does not allow, so a chain of parents can never come back on itself, and none
 * runs deeper than level 8. A branch is abstract - never realized, never worn on its own - and
 * open below the root: any repository adds a branch under any branch, by naming it.
 */
public sealed interface ComponentBranch extends Taxon
        permits L0_ComponentBranch,
                L1_ComponentBranch, L2_ComponentBranch, L3_ComponentBranch, L4_ComponentBranch,
                L5_ComponentBranch, L6_ComponentBranch, L7_ComponentBranch, L8_ComponentBranch {

    /** Its level: 0 for the root, one more than its parent's for every other. */
    int level();
}
