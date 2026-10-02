package hue.captains.singapura.js.homing.site.demo.library;

import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.site.catalogue.CatalogueTree;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The tree as declared, every address as minted, and the pages as served — no server. */
class LibrarySiteTest {

    private static final CatalogueTree TREE = LibrarySite.ROUTER.tree();

    @Test
    void fourCataloguesOverThreeLevels() {
        assertEquals(List.of(LibraryCatalogue.INSTANCE, FictionCatalogue.INSTANCE,
                             ScienceCatalogue.INSTANCE, PhysicsCatalogue.INSTANCE), TREE.all());
        assertEquals(2, CatalogueTree.levelOf(PhysicsCatalogue.INSTANCE));
    }

    @Test
    void everyAddressIsMinted() {
        assertEquals("/",                LibrarySite.ROUTER.hrefOf(LibraryCatalogue.INSTANCE));
        assertEquals("/fiction",         LibrarySite.ROUTER.hrefOf(FictionCatalogue.INSTANCE));
        assertEquals("/science/physics", LibrarySite.ROUTER.hrefOf(PhysicsCatalogue.INSTANCE));
        assertEquals(Optional.of("/fiction/middlemarch"),  LibrarySite.ROUTER.hrefOf(FictionCatalogue.MIDDLEMARCH));
        assertEquals(Optional.of("/science/physics/qed"),  LibrarySite.ROUTER.hrefOf(PhysicsCatalogue.FEYNMAN));
        assertEquals(Optional.of("/about-this-library"),   LibrarySite.ROUTER.hrefOf(LibraryCatalogue.ABOUT));
    }

    @Test
    void aBookKnowsWhereItIs() {
        var body = LibrarySite.ROUTER.resolve(Path.of("science", "physics", "qed")).orElseThrow().html(Query.NONE).body();
        assertTrue(body.contains("<nav class=\"trail\"><a href=\"/\">Library</a><span>/</span><a href=\"/science\">Science</a>"
                               + "<span>/</span><a href=\"/science/physics\">Physics</a><span>/</span>QED</nav>"), body);
        assertTrue(body.contains("4 crumbs deep"), body);
        assertTrue(body.contains("arrows and honesty"), body);
    }

    @Test
    void aBookReadsItsOwnQuery() {
        var body = LibrarySite.ROUTER.resolve(Path.of("fiction", "solaris")).orElseThrow().html(Query.of("format", "brief")).body();
        assertTrue(!body.contains("ocean that thinks"), body);
        assertTrue(body.contains("href=\"?format=full\""), body);
    }

    @Test
    void thePlainPageIsServedAsItIs() {
        assertSame(LibraryCatalogue.ABOUT, LibrarySite.ROUTER.resolve(Path.of("about-this-library")).orElseThrow());
    }

    @Test
    void theRootListsTheShelves() {
        var body = LibrarySite.ROUTER.resolve(Path.ROOT).orElseThrow().html(Query.NONE).body();
        assertTrue(body.contains("href=\"/fiction\""), body);
        assertTrue(body.contains("href=\"/science\""), body);
        assertTrue(body.contains("href=\"/about-this-library\""), body);
        assertTrue(!body.contains("<nav class=\"trail\">"), body);   // the root has no trail to draw
    }

    @Test
    void misses() {
        assertTrue(LibrarySite.ROUTER.resolve(Path.of("history")).isEmpty());
        assertTrue(LibrarySite.ROUTER.resolve(Path.of("fiction", "solaris", "chapter-1")).isEmpty());
    }
}
