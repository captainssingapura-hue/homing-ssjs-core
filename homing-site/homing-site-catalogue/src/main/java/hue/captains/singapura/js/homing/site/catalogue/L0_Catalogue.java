package hue.captains.singapura.js.homing.site.catalogue;

import java.util.List;

/**
 * The root of a catalogue tree: the one level with no parent. A tree has
 * exactly one, and every path starts from it.
 *
 * @param <Self> the implementing type
 */
public non-sealed interface L0_Catalogue<Self extends L0_Catalogue<Self>>
        extends Catalogue<Self> {

    @Override default List<? extends L1_Catalogue<Self, ?>> subCatalogues() { return List.of(); }
}
