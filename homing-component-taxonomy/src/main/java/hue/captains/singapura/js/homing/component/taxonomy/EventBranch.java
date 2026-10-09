package hue.captains.singapura.js.homing.component.taxonomy;

/** A branch of the tree of events, at its level: the root, or one under it. Levels are added as the tree grows. */
public sealed interface EventBranch extends EventNode permits L0_EventBranch, L1_EventBranch {

    /** Its level: 0 for the root, one more than its parent's for every other. */
    int level();
}
