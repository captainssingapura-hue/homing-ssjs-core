package hue.captains.singapura.js.homing.site.catalogue;

import java.util.List;

/**
 * A level-2 catalogue: its parent is level 1, its sub-catalogues are
 * level 3.
 *
 * @param <P>    the parent's type
 * @param <Self> the implementing type
 */
public non-sealed interface L2_Catalogue<P extends L1_Catalogue<?, P>,
                                          Self extends L2_Catalogue<P, Self>>
        extends Catalogue<Self> {

    /** The catalogue this one sits under; it must list this one among its sub-catalogues. */
    P parent();

    @Override default List<? extends L3_Catalogue<Self, ?>> subCatalogues() { return List.of(); }
}
