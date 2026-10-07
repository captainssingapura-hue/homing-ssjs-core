package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * The root level of the taxonomy of components: the one level with no parent, and the one
 * branch nobody adds - it permits the {@link Root} alone.
 */
public sealed interface L0_ComponentBranch extends ComponentBranch permits Root {

    @Override default int level() { return 0; }
}
