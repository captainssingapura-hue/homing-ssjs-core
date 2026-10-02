package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.tree.NodeIdentity;

/**
 * Who a node of a composed component tree is — as opposed to where it sits,
 * which is its segment. The root is the composition; a vehicle is its
 * crate's name; a catalogue node and a component are their classes, so the
 * same class reached through two vehicles would be one identity, which the
 * disjointness check refuses.
 */
public sealed interface ComponentNodeIdentity extends NodeIdentity {
    record OfRoot(String name) implements ComponentNodeIdentity {}
    record OfVehicle(String crate) implements ComponentNodeIdentity {}
    record OfCatalogue(Class<? extends ComponentCatalogue<?>> catalogue) implements ComponentNodeIdentity {}
    record OfComponent(Class<? extends UiComponent<?>> component) implements ComponentNodeIdentity {}
}
