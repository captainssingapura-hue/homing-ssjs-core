package hue.captains.singapura.js.homing.component.taxonomy;

/** A branch of the tree of events at level 1, under the root. */
public non-sealed interface L1_EventBranch<P extends L0_EventBranch> extends EventBranch {

    /** The branch it is filed under, one level up. */
    P parent();

    @Override default int level() { return 1; }
}
