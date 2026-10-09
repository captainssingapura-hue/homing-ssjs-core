package hue.captains.singapura.js.homing.component.taxonomy;

/** A branch of the tree of states, at its level: the root, or one under it. Levels are added as the tree grows. */
public sealed interface StateBranch extends StateNode permits L0_StateBranch, L1_StateBranch {

    /** Its level: 0 for the root, one more than its parent's for every other. */
    int level();
}
