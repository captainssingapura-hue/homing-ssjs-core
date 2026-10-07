package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch at level 8 of the taxonomy of components: its parent is at level 7. This is the deepest level.
 *
 * @param <P> the branch it sits under, one level up
 */
public non-sealed interface L8_ComponentBranch<P extends L7_ComponentBranch<?>> extends ComponentBranch {

    /** The branch it sits under, one level up. */
    P parent();

    @Override default int level() { return 8; }
}
