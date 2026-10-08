package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch at level 1 of the taxonomy of components: its parent is at level 0.
 *
 * @param <P> the branch it sits under, one level up
 */
public non-sealed interface L1_ComponentBranch<P extends L0_ComponentBranch> extends ComponentBranch {

    /** The branch it sits under, one level up. */
    P parent();

    @Override default int level() { return 1; }
}
