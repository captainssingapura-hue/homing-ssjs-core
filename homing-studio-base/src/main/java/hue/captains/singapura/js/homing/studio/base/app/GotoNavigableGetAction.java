package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.StampedParams;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0051 — {@code GET /goto?app=<simpleName>&<args>}: send me to this
 * navigable, wherever it lives.
 *
 * <p>One action for the whole {@code (app, args)} space, which by D1 is
 * exactly the space of navigables — so this resolves a doc reference, an app
 * tile or a catalogue by the same route, because they are the same kind of
 * thing.</p>
 *
 * <p><b>Why this exists separately from {@code /app}.</b> {@code /app}
 * renders; it also redirects a positioned address so old links self-correct
 * (D4), but that is a property of the rendering route, not its purpose. A
 * link that means "go to this navigable" should say so, rather than say
 * "render this app page" and lean on the bounce. Naming the operation also
 * keeps the two independently changeable: if the render route ever stopped
 * redirecting, every managed reference would still be correct.</p>
 *
 * <p>Three outcomes, all of them honest:</p>
 * <ol>
 *   <li><b>Positioned</b> → its catalogue path, the authentic address.</li>
 *   <li><b>Not positioned</b> → the flat render URL. A workspace shell or a
 *       picker has no place in the tree, and inventing one would be worse
 *       than sending the caller to where the thing actually renders. An
 *       address carrying more than a node's identity — {@code &phase=6} on a
 *       plan — lands here too, and correctly: it names no node, and this
 *       route answers only "where does this node live".</li>
 *   <li><b>No app named</b> → 404, rather than a page about nothing.</li>
 * </ol>
 */
public final class GotoNavigableGetAction
        implements GetAction<RoutingContext, GotoNavigableGetAction.Query,
                             EmptyParam.NoHeaders, HtmlPageContent> {

    /** The route this action mounts on. */
    public static final String ROUTE = "/goto";

    /**
     * The "go to this navigable" URL for a flat render address.
     *
     * <p>Takes the {@code (app, args)} a navigable already states as its own
     * {@code url()} and re-points it at this action, so callers never
     * hand-assemble the query and cannot drift from the render form.</p>
     */
    public static String hrefFor(String flatUrl) {
        if (flatUrl == null) return null;
        Map<String, List<String>> args = QueryString.parse(flatUrl);
        if (QueryString.first(args, "app") == null) return flatUrl;   // not an (app, args) URL
        return ROUTE + QueryString.encodeSuffix(args);
    }

    public record Query(String app, Map<String, List<String>> args) implements Param._QueryString {
        public Query {
            args = (args == null) ? Map.of() : args;
        }
    }

    private final CatalogueRegistry registry;

    public GotoNavigableGetAction(CatalogueRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("app"),
                                QueryString.parse(ctx.request().query()));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<HtmlPageContent> execute(Query query, EmptyParam.NoHeaders headers) {
        if (query.app() == null || query.app().isBlank()) {
            return CompletableFuture.failedFuture(notFound("app", "no navigable named"));
        }
        // RFC 0051 — /goto addresses NODES. The args are taken whole and
        // matched against the index; nothing is narrowed away first, so an
        // address carrying more than a node's identity simply names no node
        // and falls to the flat render alongside the genuinely unpositioned.
        //
        // This used to keep an identity keyspace per app so it could split the
        // args in two, redirect on the identity half and re-append the rest —
        // carrying ?phase= across the hop. Nothing ever sent one: both emitters
        // build their href from DocViewers.addressOf(doc).flat(), a canonical
        // binding with no refinement in it. So the split existed for hand-typed
        // URLs alone, and paid for it with a keyspace derived as a union over
        // placements — where one tile placed at a refinement would have made
        // that refinement identity-bearing for every other placement of the
        // same app, silently.
        CataloguePath path = registry.pathForFlat(query.app(), query.args());
        String target = (path != null)
                ? path.toUrl()
                : "/app" + QueryString.encodeSuffix(query.args());
        return CompletableFuture.completedFuture(new HtmlPageContent(redirectHtml(target)));
    }

    private static String redirectHtml(String target) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta http-equiv="refresh" content="0;url=%s">
                    <title>Redirecting…</title>
                </head>
                <body><script>window.location.replace(%s);</script></body>
                </html>
                """.formatted(htmlAttrEscape(target), StampedParams.jsString(target));
    }

    private static String htmlAttrEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static ResourceNotFound notFound(String resource, String why) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, resource + ": " + why),
                new ResourceNotFound._ExternalError(resource, why));
    }
}
