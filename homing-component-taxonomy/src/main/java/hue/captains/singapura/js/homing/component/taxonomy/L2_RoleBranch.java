package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch at level 2 of the role catalogue: its parent is at level 1.
 *
 * @param <P> the branch it is filed under, one level up
 */
public non-sealed interface L2_RoleBranch<P extends L1_RoleBranch<?>> extends RoleBranch {

    /** The branch it is filed under, one level up. */
    P parent();

    @Override default int level() { return 2; }
}
