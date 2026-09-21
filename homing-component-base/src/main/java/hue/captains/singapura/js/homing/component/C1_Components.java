package hue.captains.singapura.js.homing.component;

import java.util.List;

/**
 * A family within a vehicle: listed under its {@link C0_Components} root;
 * its sub-catalogues, if any, are {@link C2_Components}.
 *
 * @param <P>    the concrete root
 * @param <Self> the concrete family's own type
 */
public non-sealed interface C1_Components<P extends C0_Components<P>, Self extends C1_Components<P, Self>> extends ComponentCatalogue<Self> {
    P parent();
    @Override default List<? extends C2_Components<Self, ?>> subCatalogues() { return List.of(); }
}
