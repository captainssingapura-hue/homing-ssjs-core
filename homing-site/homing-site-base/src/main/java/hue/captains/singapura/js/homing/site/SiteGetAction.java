package hue.captains.singapura.js.homing.site;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * GET {@code /*} — the one action a site needs. The request's path goes to
 * the {@link Router}; the navigable it hands back gets the request's
 * {@link Query}; the page comes out. A miss is a 404 naming the path.
 *
 * <p>The action is the whole of the wire adapter, and it is small because the
 * contracts underneath already speak in the wire's terms: a path is what the
 * request line carries, a query is what follows the {@code ?}, and HTML is
 * what goes back. Nothing is looked up by name, decoded through a codec or
 * stamped into a page here — a navigable that needs any of that does it
 * itself, which is what keeps this one action fit for every site.</p>
 */
public final class SiteGetAction
        implements GetAction<RoutingContext, SiteGetAction.Request, EmptyParam.NoHeaders, HtmlPageContent> {

    /** The catch-all: every path on the host, the root included. */
    public static final String ROUTE = "/*";

    /** The address, split the way the contracts want it. */
    public record Request(Path path, Query query) implements Param._QueryString {}

    private final Router router;

    public SiteGetAction(Router router) {
        this.router = Objects.requireNonNull(router, "SiteGetAction.router");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Request> queryStrMarshaller() {
        // normalizedPath: dot-segments resolved, so "/about/../secret" asks the
        // router for /secret and nothing the router did not mean to answer.
        return ctx -> new Request(Path.parse(ctx.normalizedPath()), Query.parse(ctx.request().query()));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<HtmlPageContent> execute(Request request, EmptyParam.NoHeaders headers) {
        var found = router.resolve(request.path());
        if (found.isEmpty()) {
            return CompletableFuture.failedFuture(notFound(request.path()));
        }
        try {
            return CompletableFuture.completedFuture(found.get().html(request.query()));
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private static ResourceNotFound notFound(Path path) {
        String where = path.toString();
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, "No navigable at " + where),
                new ResourceNotFound._ExternalError(where, "No page at this path"));
    }
}
