package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch at level 6 of the role catalogue: its parent is at level 5.
 *
 * @param <P> the branch it is filed under, one level up
 */
public non-sealed interface L6_RoleBranch<P extends L5_RoleBranch<?>> extends RoleBranch {

    /** The branch it is filed under, one level up. */
    P parent();

    @Override default int level() { return 6; }
}
