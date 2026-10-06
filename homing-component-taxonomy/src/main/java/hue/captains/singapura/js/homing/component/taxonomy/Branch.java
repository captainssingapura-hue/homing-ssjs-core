package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A node that may have nodes under it: the {@link Root}, or a {@link Kind}. Only a branch can
 * be named as a parent, so a {@link Component} - which is not one - is always a leaf: that only
 * a leaf is concrete is the compiler's to hold.
 */
public sealed interface Branch extends Taxon permits Root, Kind {
}
