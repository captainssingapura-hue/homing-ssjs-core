package hue.captains.singapura.js.homing.component.taxonomy;

/** A branch of the tree of states at level 1, under the root. */
public non-sealed interface L1_StateBranch<P extends L0_StateBranch> extends StateBranch {

    /** The branch it is filed under, one level up. */
    P parent();

    @Override default int level() { return 1; }
}
