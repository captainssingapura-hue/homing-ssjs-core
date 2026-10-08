package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch at level 7 of the taxonomy of components: its parent is at level 6.
 *
 * @param <P> the branch it sits under, one level up
 */
public non-sealed interface L7_ComponentBranch<P extends L6_ComponentBranch<?>> extends ComponentBranch {

    /** The branch it sits under, one level up. */
    P parent();

    @Override default int level() { return 7; }
}
