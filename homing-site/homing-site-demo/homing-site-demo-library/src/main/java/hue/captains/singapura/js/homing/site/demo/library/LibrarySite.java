package hue.captains.singapura.js.homing.site.demo.library;

import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.catalogue.CatalogueRouter;

/**
 * The library as a site: its router IS the catalogue router, mounted at the
 * root, so {@code /} is the library's own listing and every other address
 * is a slug walk. Nothing else is routed; a path the tree cannot follow is
 * the host's 404.
 */
public record LibrarySite() implements Site {

    public static final LibrarySite INSTANCE = new LibrarySite();

    /** Read once: the tree is checked when the site is made, not when a request arrives. */
    public static final CatalogueRouter ROUTER = CatalogueRouter.at(Path.ROOT, LibraryCatalogue.INSTANCE);

    @Override public String name()   { return "library"; }
    @Override public Router router() { return ROUTER; }
}
