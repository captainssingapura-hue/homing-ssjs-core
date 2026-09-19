package hue.captains.singapura.js.homing.site.catalogue;

import java.util.List;

/**
 * A level-4 catalogue: its parent is level 3, its sub-catalogues are
 * level 5.
 *
 * @param <P>    the parent's type
 * @param <Self> the implementing type
 */
public non-sealed interface L4_Catalogue<P extends L3_Catalogue<?, P>,
                                          Self extends L4_Catalogue<P, Self>>
        extends Catalogue<Self> {

    /** The catalogue this one sits under; it must list this one among its sub-catalogues. */
    P parent();

    @Override default List<? extends L5_Catalogue<Self, ?>> subCatalogues() { return List.of(); }
}
