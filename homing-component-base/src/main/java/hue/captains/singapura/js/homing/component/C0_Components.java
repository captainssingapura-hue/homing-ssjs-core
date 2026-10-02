package hue.captains.singapura.js.homing.component;

import java.util.List;

/**
 * The root of a vehicle's catalogue: what a crate delivers. A crate class
 * that ships components implements {@link ComponentVehicle} and answers
 * with one of these.
 *
 * @param <Self> the concrete root's own type
 */
public non-sealed interface C0_Components<Self extends C0_Components<Self>> extends ComponentCatalogue<Self> {
    @Override default List<? extends C1_Components<Self, ?>> subCatalogues() { return List.of(); }
}
