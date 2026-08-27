package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.server.AppHtmlGetAction;
import hue.captains.singapura.js.homing.server.AppQuery;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0051 — GET {@code /cat/<path>}: the authentic address.
 *
 * <p>Phase 2 made a position expressible and readable; this is what makes one
 * fetchable. The path resolves to a node and the node is rendered — no
 * redirect, so the address bar keeps showing the path the user asked for,
 * which is the whole point of parity.</p>
 *
 * <p><b>Renders by delegation.</b> The node is turned back into the flat
 * {@code (app, args)} the existing page action already knows how to serve,
 * and that action produces the HTML. A second renderer would be a second
 * chance for the two routes to disagree about what a page looks like — and
 * "both routes emit an identical page shape" is a phase criterion, so it is
 * cheaper to make them literally the same code than to test that two
 * renderers agree.</p>
 */
public final class CataloguePathGetAction
        implements GetAction<RoutingContext, CataloguePathGetAction.PathQuery,
                             EmptyParam.NoHeaders, HtmlPageContent> {

    /** The route this action mounts on. Vert.x wildcard: everything under /cat. */
    public static final String ROUTE = "/" + CataloguePath.ROOT + "/*";
    /** The bare root, which the wildcard route does not itself match. */
    public static final String ROOT_ROUTE = "/" + CataloguePath.ROOT;

    /**
     * @param path  the requested path, or null when the URL was not one
     * @param theme carried through so a themed link keeps its theme
     * @param locale likewise
     */
    public record PathQuery(CataloguePath path, String theme, String locale,
                            Map<String, List<String>> query)
            implements Param._QueryString {

        public PathQuery {
            query = (query == null) ? Map.of() : query;
        }
    }

    private final CatalogueRegistry registry;
    private final AppHtmlGetAction pageAction;

    public CataloguePathGetAction(CatalogueRegistry registry, AppHtmlGetAction pageAction) {
        this.registry = java.util.Objects.requireNonNull(registry, "registry");
        this.pageAction = java.util.Objects.requireNonNull(pageAction, "pageAction");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, PathQuery> queryStrMarshaller() {
        return ctx -> new PathQuery(
                CataloguePath.parse(ctx.request().path()),
                ctx.request().getParam("theme"),
                ctx.request().getParam("locale"),
                QueryString.parse(ctx.request().query()));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<HtmlPageContent> execute(PathQuery query, EmptyParam.NoHeaders headers) {
        if (query.path() == null) {
            return CompletableFuture.failedFuture(
                    notFound("path", "not a catalogue path"));
        }
        PathResolution resolved = registry.resolve(query.path());
        return switch (resolved) {
            case PathResolution.ToCatalogue(var path, var catalogue) ->
                    render(CatalogueAppHost.INSTANCE.simpleName(),
                           QueryString.of("id", catalogue.getClass().getName()), query);

            case PathResolution.ToLeaf(var path, var parent, Doc doc) -> {
                // RFC 0051 Phase 6 — which app opens this leaf, and with what
                // args, read as the structured pair. It used to be recovered by
                // parsing the leaf's own minted url(): a string the framework
                // had just built, taken apart to get back what went into it.
                //
                // The pair is still DERIVED here (from the doc's type) rather
                // than carried by the leaf. When Entry holds the binding, this
                // reads it straight off the node and the derivation goes too.
                var address = DocViewers.addressOf(doc);
                yield render(address.app(), address.args(), query);
            }

            case PathResolution.Miss miss -> CompletableFuture.failedFuture(
                    notFound(miss.path().toUrl(),
                            switch (miss.reason()) {
                                case NO_SUCH_CHILD -> "no child named '" + miss.at() + "' here";
                                case PAST_A_LEAF   -> "'" + miss.at() + "' is under a leaf, which has no children";
                            }));
        };
    }

    /** A 404 that says which segment failed and why - the typed miss carries
     *  the reason, so the response can too. */
    private static ResourceNotFound notFound(String resource, String why) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, resource + ": " + why),
                new ResourceNotFound._ExternalError(resource, why));
    }

    /**
     * Hand the resolved node to the page action as a flat (app, args).
     *
     * <p>The request's own query is merged in UNDER the node's args, and the
     * order is the whole point. A path names the node; the query only refines
     * how that node is shown — {@code ?phase=2} on a plan, {@code ?context=}
     * on a catalogue. Without the merge those are silently dropped, which is
     * how {@code /cat/.../rfc0051-plan-data?phase=2} rendered the wrong phase
     * while the flat URL rendered the right one.</p>
     *
     * <p>The node's args win on conflict, so a request cannot smuggle in a
     * different {@code id} and make one path serve another node's content.
     * The path is the identity; the query is decoration.</p>
     */
    private CompletableFuture<HtmlPageContent> render(
            String app, Map<String, List<String>> args, PathQuery query) {

        var merged = QueryString.params();
        query.query().forEach((k, values) -> values.forEach(v -> QueryString.put(merged, k, v)));
        args.forEach((k, values) -> {
            merged.remove(k);
            values.forEach(v -> QueryString.put(merged, k, v));
        });
        return pageAction.execute(
                new AppQuery(app, null, query.theme(), query.locale(), merged),
                new EmptyParam.NoHeaders());
    }
}
