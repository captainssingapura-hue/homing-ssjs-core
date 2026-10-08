package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch at level 3 of the taxonomy of components: its parent is at level 2.
 *
 * @param <P> the branch it sits under, one level up
 */
public non-sealed interface L3_ComponentBranch<P extends L2_ComponentBranch<?>> extends ComponentBranch {

    /** The branch it sits under, one level up. */
    P parent();

    @Override default int level() { return 3; }
}
