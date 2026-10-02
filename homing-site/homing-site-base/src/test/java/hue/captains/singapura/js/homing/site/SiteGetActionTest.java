package hue.captains.singapura.js.homing.site;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The wire adapter over a two-page router: a hit renders with the query, a
 * miss is a ResourceNotFound naming the path, and a navigable that throws
 * fails the future rather than the handler.
 */
class SiteGetActionTest {

    private static final Navigable ECHO = q ->
            new HtmlPageContent("<p>" + Html.escape(q.first("say").orElse("nothing")) + "</p>");
    private static final Navigable BROKEN = q -> { throw new IllegalStateException("boom"); };

    private static final Router ROUTER = path -> switch (path.toString()) {
        case "/"       -> Optional.of(ECHO);
        case "/broken" -> Optional.of(BROKEN);
        default        -> Optional.empty();
    };

    private static final SiteGetAction ACTION = new SiteGetAction(ROUTER);

    @Test
    void aHitRendersWithTheQuery() throws Exception {
        var page = ACTION.execute(
                new SiteGetAction.Request(Path.ROOT, Query.of("say", "<hi>")),
                new EmptyParam.NoHeaders()).get();
        assertEquals("<p>&lt;hi&gt;</p>", page.body());
    }

    @Test
    void noQueryIsStillAPage() throws Exception {
        var page = ACTION.execute(
                new SiteGetAction.Request(Path.ROOT, Query.NONE),
                new EmptyParam.NoHeaders()).get();
        assertEquals("<p>nothing</p>", page.body());
    }

    @Test
    void aMissIsNotFoundNamingThePath() {
        var future = ACTION.execute(
                new SiteGetAction.Request(Path.of("no", "such page"), Query.NONE),
                new EmptyParam.NoHeaders());
        var ex = assertThrows(ExecutionException.class, future::get);
        var nf = assertInstanceOf(ResourceNotFound.class, ex.getCause());
        assertTrue(nf.internalError().desc().contains("/no/such%20page"), nf.internalError().desc());
    }

    @Test
    void aThrowingNavigableFailsTheFuture() {
        var future = ACTION.execute(
                new SiteGetAction.Request(Path.of("broken"), Query.NONE),
                new EmptyParam.NoHeaders());
        var ex = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(IllegalStateException.class, ex.getCause());
    }

    @Test
    void theHostRegistryIsTheOneRoute() {
        var site = new Site() {
            @Override public String name()   { return "t"; }
            @Override public Router router() { return ROUTER; }
        };
        var registry = SiteHost.registry(site);
        assertEquals(1, registry.getActions().size());
        assertInstanceOf(SiteGetAction.class, registry.getActions().get("/*"));
        assertEquals(Map.of(), registry.postActions());
    }
}
