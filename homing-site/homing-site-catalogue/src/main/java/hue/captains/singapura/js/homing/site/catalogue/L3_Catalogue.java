package hue.captains.singapura.js.homing.site.catalogue;

import java.util.List;

/**
 * A level-3 catalogue: its parent is level 2, its sub-catalogues are
 * level 4.
 *
 * @param <P>    the parent's type
 * @param <Self> the implementing type
 */
public non-sealed interface L3_Catalogue<P extends L2_Catalogue<?, P>,
                                          Self extends L3_Catalogue<P, Self>>
        extends Catalogue<Self> {

    /** The catalogue this one sits under; it must list this one among its sub-catalogues. */
    P parent();

    @Override default List<? extends L4_Catalogue<Self, ?>> subCatalogues() { return List.of(); }
}
