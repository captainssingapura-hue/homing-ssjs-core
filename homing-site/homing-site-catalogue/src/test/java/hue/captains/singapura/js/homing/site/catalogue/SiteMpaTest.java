package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Placed;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.Trail;
import hue.captains.singapura.js.homing.site.mpa.Brand;
import hue.captains.singapura.js.homing.site.mpa.Mpa;
import hue.captains.singapura.tao.http.action.ActionRegistry;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A site's pages are made with the site's MPA: its tree is read with it, and
 * every catalogue - its own and every tree it grafts - makes its leaves from
 * it, so every page wears the one chrome, whichever tree placed it. The same
 * tree read by another site wears that site's. A tree read with no MPA serves
 * pages that need none, and refuses - saying why - a catalogue that needs one.
 */
class SiteMpaTest {

    /** An MPA that can say its brand, and nothing else - enough to tell which site made a page. */
    record BrandOnly(String label) implements Mpa {
        @Override public Brand brand() { return new Brand(label, "/"); }
        @Override public ThemeRegistry themes() { throw new UnsupportedOperationException(); }
        @Override public String moduleUrl(EsModule<?> module) { throw new UnsupportedOperationException(); }
        @Override public <P extends AppModule._Param, M extends AppModule<P, M>> Placed page(M app, P params) { throw new UnsupportedOperationException(); }
        @Override public ActionRegistry<RoutingContext> registry(Site site) { throw new UnsupportedOperationException(); }
    }

    /** A page made with a site's MPA: it wears the site's brand. */
    record SitePage(String brand, String title) implements Placed {
        @Override public HtmlPageContent html(Trail trail, Query q) { return new HtmlPageContent("[" + brand + "] " + title); }
    }

    /** An app's tree, written alone: its pages made with whichever MPA the site hands in. */
    record ClockCatalogue() implements L0_Catalogue<ClockCatalogue> {
        static final ClockCatalogue INSTANCE = new ClockCatalogue();
        @Override public String name() { return "Clock"; }
        @Override public List<Leaf<ClockCatalogue>> leaves(Mpa mpa) {
            return List.of(Leaf.of(this, "Now", "", new SitePage(mpa.brand().label(), "now")));
        }
    }

    /** A site's root: a page of its own made with the MPA too, and the clock grafted. */
    record DeskCatalogue() implements L0_Catalogue<DeskCatalogue> {
        static final DeskCatalogue INSTANCE = new DeskCatalogue();
        @Override public String name() { return "Desk"; }
        @Override public List<Graft<DeskCatalogue>> grafts() { return List.of(Graft.of(this, ClockCatalogue.INSTANCE)); }
        @Override public List<Leaf<DeskCatalogue>> leaves(Mpa mpa) {
            return List.of(Leaf.of(this, "Home", "", new SitePage(mpa.brand().label(), "home")));
        }
    }

    static final Mpa DESK = new BrandOnly("Desk site"), LAB = new BrandOnly("Lab site");

    @Test
    void everyPageOfASiteIsMadeWithItsMpa_whicheverTreePlacedIt() {
        var router = CatalogueRouter.at(Path.ROOT, DeskCatalogue.INSTANCE, DESK);
        assertEquals("[Desk site] home", router.resolve(Path.of("home")).orElseThrow().html(Query.NONE).body());
        assertEquals("[Desk site] now", router.resolve(Path.of("clock", "now")).orElseThrow().html(Query.NONE).body());
        assertSame(DESK, router.tree().mpa().orElseThrow());
    }

    @Test
    void theSameTreeReadByAnotherSite_wearsThatSitesChrome() {
        var lab = CatalogueRouter.at(Path.of("lab"), ClockCatalogue.INSTANCE, LAB);
        assertEquals("[Lab site] now", lab.resolve(Path.of("lab", "now")).orElseThrow().html(Query.NONE).body());
    }

    @Test
    void theListingShowsThePagesTheTreeRead_andLinksThem() {
        var tree = CatalogueTree.of(DeskCatalogue.INSTANCE, DESK);
        assertEquals(List.of(new SitePage("Desk site", "now")), tree.leavesOf(ClockCatalogue.INSTANCE).stream().map(Leaf::page).toList());
        assertEquals(Optional.of(Path.of("clock", "now")), tree.pathOf(new SitePage("Desk site", "now")));
        String listing = CatalogueRouter.at(Path.ROOT, tree).resolve(Path.of("clock")).orElseThrow().html(Query.NONE).body();
        assertTrue(listing.contains("href=\"/clock/now\""), listing);
    }

    @Test
    void aTreeReadWithNoMpa_refusesACatalogueThatNeedsOne_sayingWhy() {
        var e = assertThrows(IllegalStateException.class, () -> CatalogueTree.of(DeskCatalogue.INSTANCE));
        assertTrue(e.getMessage().contains("CatalogueTree.of(root, mpa)"), e.getMessage());
        assertTrue(CatalogueTree.of(GraftTest.NotesCatalogue.INSTANCE).mpa().isEmpty(), "a tree of pages that need no site needs no MPA");
    }
}
