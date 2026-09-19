package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.site.Trail;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static hue.captains.singapura.js.homing.site.catalogue.Fixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogueRouterTest {

    private static final CatalogueRouter AT_ROOT = CatalogueRouter.at(Path.ROOT, RootCatalogue.INSTANCE);
    private static final CatalogueRouter AT_CAT  = CatalogueRouter.at(Path.of("cat"), RootCatalogue.INSTANCE);

    @Test
    void mintsAddressesFromTheMount() {
        assertEquals("/",               AT_ROOT.hrefOf(RootCatalogue.INSTANCE));
        assertEquals("/alpha/deep",     AT_ROOT.hrefOf(DeepCatalogue.INSTANCE));
        assertEquals("/cat",            AT_CAT.hrefOf(RootCatalogue.INSTANCE));
        assertEquals("/cat/alpha/deep", AT_CAT.hrefOf(DeepCatalogue.INSTANCE));
        assertEquals(Optional.of("/cat/alpha/deep/two"), AT_CAT.hrefOf(P_TWO));
        assertEquals(Optional.empty(), AT_CAT.hrefOf(q -> null));
    }

    @Test
    void servesOnlyUnderTheMount() {
        assertTrue(AT_CAT.resolve(Path.ROOT).isEmpty());
        assertTrue(AT_CAT.resolve(Path.of("alpha")).isEmpty());
        assertTrue(AT_CAT.resolve(Path.of("cat")).isPresent());
        assertTrue(AT_CAT.resolve(Path.of("cat", "alpha")).isPresent());
        assertTrue(AT_CAT.resolve(Path.of("cat", "nope")).isEmpty());
        assertTrue(AT_ROOT.resolve(Path.ROOT).isPresent());
        assertTrue(AT_ROOT.resolve(Path.of("cat")).isEmpty());   // no vertex is called cat
    }

    @Test
    void aPlacedLeafIsToldItsTrail() {
        var page = AT_CAT.resolve(Path.of("cat", "alpha", "deep", "two")).orElseThrow();
        assertEquals("two|4|q", page.html(Query.of("v", "q")).body());   // Root / Alpha / Deep / Two
        // The same page, reached without a router, has no trail.
        assertEquals("two|0|", P_TWO.html(Query.NONE).body());
    }

    @Test
    void aPlainLeafIsServedAsItIs() {
        assertSame(P_PLAIN, AT_ROOT.resolve(Path.of("alpha", "plain-page")).orElseThrow());
    }

    @Test
    void theTrailIsThePath() {
        var r = AT_CAT.resolution(Path.of("cat", "alpha", "deep", "two")).orElseThrow();
        var trail = AT_CAT.trailFor(r);
        assertEquals(List.of("Root", "Alpha", "Deep", "Two"), trail.crumbs().stream().map(Trail.Crumb::text).toList());
        assertEquals(List.of("/cat", "/cat/alpha", "/cat/alpha/deep", "/cat/alpha/deep/two"),
                     trail.crumbs().stream().map(Trail.Crumb::href).toList());

        var vertex = AT_ROOT.resolution(Path.of("alpha")).orElseThrow();
        assertEquals(List.of("/", "/alpha"), AT_ROOT.trailFor(vertex).crumbs().stream().map(Trail.Crumb::href).toList());

        var miss = AT_ROOT.resolution(Path.of("nope")).orElseThrow();
        assertEquals(Trail.NONE, AT_ROOT.trailFor(miss));
    }

    @Test
    void theDefaultListingLinksEveryChild() {
        var body = AT_CAT.resolve(Path.of("cat", "alpha")).orElseThrow().html(Query.NONE).body();
        assertTrue(body.contains("<h1>Alpha"), body);
        assertTrue(body.contains("href=\"/cat/alpha/deep\""), body);
        assertTrue(body.contains("href=\"/cat/alpha/one\""), body);
        assertTrue(body.contains("href=\"/cat/alpha/plain-page\""), body);
        assertTrue(body.contains("<nav class=\"trail\"><a href=\"/cat\">Root</a><span>/</span>Alpha</nav>"), body);
    }

    @Test
    void aListingOfItsOwn() {
        Listing bare = (c, r) -> (trail, q) ->
                new hue.captains.singapura.js.homing.server.HtmlPageContent(c.name() + "@" + trail.depth());
        var router = AT_CAT.listing(bare);
        assertEquals("Deep@3", router.resolve(Path.of("cat", "alpha", "deep")).orElseThrow().html(Query.NONE).body());
    }
}
