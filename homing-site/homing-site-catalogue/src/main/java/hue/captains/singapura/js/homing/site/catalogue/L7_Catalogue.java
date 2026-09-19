package hue.captains.singapura.js.homing.site.catalogue;

import java.util.List;

/**
 * A level-7 catalogue: its parent is level 6, its sub-catalogues are
 * level 8.
 *
 * @param <P>    the parent's type
 * @param <Self> the implementing type
 */
public non-sealed interface L7_Catalogue<P extends L6_Catalogue<?, P>,
                                          Self extends L7_Catalogue<P, Self>>
        extends Catalogue<Self> {

    /** The catalogue this one sits under; it must list this one among its sub-catalogues. */
    P parent();

    @Override default List<? extends L8_Catalogue<Self, ?>> subCatalogues() { return List.of(); }
}
