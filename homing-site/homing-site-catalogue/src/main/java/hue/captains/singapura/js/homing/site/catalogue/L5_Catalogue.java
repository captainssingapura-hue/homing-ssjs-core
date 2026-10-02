package hue.captains.singapura.js.homing.site.catalogue;

import java.util.List;

/**
 * A level-5 catalogue: its parent is level 4, its sub-catalogues are
 * level 6.
 *
 * @param <P>    the parent's type
 * @param <Self> the implementing type
 */
public non-sealed interface L5_Catalogue<P extends L4_Catalogue<?, P>,
                                          Self extends L5_Catalogue<P, Self>>
        extends Catalogue<Self> {

    /** The catalogue this one sits under; it must list this one among its sub-catalogues. */
    P parent();

    @Override default List<? extends L6_Catalogue<Self, ?>> subCatalogues() { return List.of(); }
}
