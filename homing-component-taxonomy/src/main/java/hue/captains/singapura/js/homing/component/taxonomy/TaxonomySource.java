package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A taxonomy as a repository declares it: its components and its role catalogue, read whole. A
 * tool that is told which taxonomy to work on by name - a command line, a workbench - reads it
 * through this: the class's {@code INSTANCE}, then {@link #read()}.
 */
public interface TaxonomySource {
    /** The taxonomy, read and refused nothing. */
    Taxonomy read();
}
