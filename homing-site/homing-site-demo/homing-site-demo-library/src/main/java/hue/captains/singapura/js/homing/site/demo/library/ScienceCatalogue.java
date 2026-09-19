package hue.captains.singapura.js.homing.site.demo.library;

import hue.captains.singapura.js.homing.site.catalogue.L1_Catalogue;
import hue.captains.singapura.js.homing.site.catalogue.L2_Catalogue;
import hue.captains.singapura.js.homing.site.catalogue.Leaf;

import java.util.List;

/** A shelf with one book of its own and a section under it. */
public record ScienceCatalogue() implements L1_Catalogue<LibraryCatalogue, ScienceCatalogue> {

    public static final ScienceCatalogue INSTANCE = new ScienceCatalogue();

    static final BookPage ORIGIN = new BookPage("On the Origin of Species", "Charles Darwin", 1859,
            "One long argument, from pigeons to the tree of life.");

    @Override public LibraryCatalogue parent() { return LibraryCatalogue.INSTANCE; }
    @Override public String name()    { return "Science"; }
    @Override public String summary() { return "A shelf with a section of its own."; }
    @Override public String badge()   { return "SHELF"; }
    @Override public String icon()    { return "🔬"; }

    @Override
    public List<? extends L2_Catalogue<ScienceCatalogue, ?>> subCatalogues() {
        return List.of(PhysicsCatalogue.INSTANCE);
    }

    @Override
    public List<Leaf<ScienceCatalogue>> leaves() {
        return List.of(Leaf.of(this, ORIGIN.title(), ORIGIN.author() + ", " + ORIGIN.year(), ORIGIN).badge("BOOK"));
    }
}
