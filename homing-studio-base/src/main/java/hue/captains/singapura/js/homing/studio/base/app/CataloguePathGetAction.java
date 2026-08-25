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
    public record PathQuery(CataloguePath path, String theme, String locale)
            implements Param._QueryString {}

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
                ctx.request().getParam("locale"));
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
                // The leaf's own flat URL says which app opens it and with
                // what args — the one place that mapping already lives. It is
                // read rather than re-derived so this route cannot develop its
                // own opinion about how a doc kind is viewed. (D8 retires
                // url(); when it does, this reads the codec instead.)
                Map<String, List<String>> args = QueryString.parse(doc.url());
                String app = QueryString.first(args, "app");
                if (app == null) {
                    yield CompletableFuture.failedFuture(notFound(path.toUrl(), "leaf " + doc.title() + " has no app in its url()"));
                }
                yield render(app, args, query);
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

    /** Hand the resolved node to the page action as a flat (app, args). */
    private CompletableFuture<HtmlPageContent> render(
            String app, Map<String, List<String>> args, PathQuery query) {

        return pageAction.execute(
                new AppQuery(app, null, query.theme(), query.locale(), args),
                new EmptyParam.NoHeaders());
    }
}
