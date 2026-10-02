package hue.captains.singapura.js.homing.component;

import java.util.List;

/**
 * A group within a family, and the last level: nothing can name a C2 as
 * its parent, so a vehicle's catalogue is three deep by construction.
 *
 * @param <P>    the concrete family
 * @param <Self> the concrete group's own type
 */
public non-sealed interface C2_Components<P extends C1_Components<?, P>, Self extends C2_Components<P, Self>> extends ComponentCatalogue<Self> {
    P parent();
    @Override default List<? extends ComponentCatalogue<?>> subCatalogues() { return List.of(); }
}
