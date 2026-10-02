package hue.captains.singapura.js.homing.site.catalogue;

import java.util.List;

/**
 * A level-8 catalogue: its parent is level 7, its sub-catalogues are
 * level 9. This is the deepest level; a catalogue here lists no sub-catalogues of a narrower type.
 *
 * @param <P>    the parent's type
 * @param <Self> the implementing type
 */
public non-sealed interface L8_Catalogue<P extends L7_Catalogue<?, P>,
                                          Self extends L8_Catalogue<P, Self>>
        extends Catalogue<Self> {

    /** The catalogue this one sits under; it must list this one among its sub-catalogues. */
    P parent();

    @Override default List<? extends Catalogue<?>> subCatalogues() { return List.of(); }
}
