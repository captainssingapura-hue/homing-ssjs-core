package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch at level 8 of the role catalogue: its parent is at level 7. This is the deepest level.
 *
 * @param <P> the branch it is filed under, one level up
 */
public non-sealed interface L8_RoleBranch<P extends L7_RoleBranch<?>> extends RoleBranch {

    /** The branch it is filed under, one level up. */
    P parent();

    @Override default int level() { return 8; }
}
