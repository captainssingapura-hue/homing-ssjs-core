package hue.captains.singapura.js.homing.site.demo.library;

import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.catalogue.L0_Catalogue;
import hue.captains.singapura.js.homing.site.catalogue.L1_Catalogue;
import hue.captains.singapura.js.homing.site.catalogue.Leaf;

import java.util.List;

/**
 * The root: two shelves and a page about the place. The about page is a
 * plain {@link Navigable}, not a placed one — it is served exactly as
 * written and never learns where it sits.
 */
public record LibraryCatalogue() implements L0_Catalogue<LibraryCatalogue> {

    public static final LibraryCatalogue INSTANCE = new LibraryCatalogue();

    static final Navigable ABOUT = q -> Shell.page("About", """
            <h1>About this library</h1>
            <p>A site whose whole router is a catalogue tree. The root is at <code>/</code>,
               each shelf at its slug, each book under its shelf, and every address on the
               site was minted from the tree, never typed.</p>
            <p>This page is a plain navigable: the router serves it as it is, and it has no
               trail to draw because it was never told where it sits. The books are placed
               pages, and are.</p>
            <p><a href="/">Back to the library</a></p>
            """);

    @Override public String name()    { return "Library"; }
    @Override public String summary() { return "Four catalogues over three levels, books as leaves, mounted at the root."; }
    @Override public String icon()    { return "🏛️"; }

    @Override
    public List<? extends L1_Catalogue<LibraryCatalogue, ?>> subCatalogues() {
        return List.of(FictionCatalogue.INSTANCE, ScienceCatalogue.INSTANCE);
    }

    @Override
    public List<Leaf<LibraryCatalogue>> leaves() {
        return List.of(Leaf.of(this, "About this library", "What this site is and how it is routed.", ABOUT)
                           .badge("PLAIN"));
    }
}
