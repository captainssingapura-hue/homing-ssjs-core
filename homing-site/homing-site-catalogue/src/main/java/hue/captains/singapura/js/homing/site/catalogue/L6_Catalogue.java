package hue.captains.singapura.js.homing.site.catalogue;

import java.util.List;

/**
 * A level-6 catalogue: its parent is level 5, its sub-catalogues are
 * level 7.
 *
 * @param <P>    the parent's type
 * @param <Self> the implementing type
 */
public non-sealed interface L6_Catalogue<P extends L5_Catalogue<?, P>,
                                          Self extends L6_Catalogue<P, Self>>
        extends Catalogue<Self> {

    /** The catalogue this one sits under; it must list this one among its sub-catalogues. */
    P parent();

    @Override default List<? extends L7_Catalogue<Self, ?>> subCatalogues() { return List.of(); }
}
