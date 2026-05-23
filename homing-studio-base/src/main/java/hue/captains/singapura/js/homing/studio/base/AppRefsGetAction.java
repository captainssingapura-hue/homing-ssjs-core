package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.app.AppDoc;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /app-refs?app=<simpleName>} — serves the breadcrumb chain
 * for an AppModule-launched page (Navigable entries, RFC 0015).
 *
 * <p>Counterpart to {@link DocRefsGetAction} for pages reached via
 * {@code /app?app=<simpleName>} without an {@code ?id=&lt;uuid&gt;}
 * parameter — the URL identifies the AppModule, not the Doc it
 * implicitly represents. This endpoint resolves the matching {@link
 * AppDoc} (the typed wrapper created when the Navigable was registered
 * in a catalogue's {@code leaves()}), then serialises its breadcrumb
 * chain via {@link CatalogueRegistry#breadcrumbsForDoc(UUID)}.</p>
 *
 * <p>The response JSON shape matches {@link DocRefsGetAction} exactly,
 * so {@code StandardMPA}'s chrome can use the same code path to render
 * either path's breadcrumbs.</p>
 *
 * <p>If the AppDoc UUID is already known (e.g. embedded server-side at
 * HTML render time), callers should hit {@code /doc-refs?id=&lt;uuid&gt;}
 * directly. This endpoint exists for the common case where the chrome
 * only knows the app's simpleName.</p>
 *
 * @since RFC 0025 Phase L2.2 — added so workspace-style AppModules
 *        (MultiTabPane, future Workspace shell) get the same breadcrumb
 *        chrome as doc-based viewers without per-app wiring.
 */
public class AppRefsGetAction
        implements GetAction<RoutingContext, AppRefsGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String app) implements Param._QueryString {}

    private final DocRegistry         docRegistry;
    private final CatalogueRegistry   catalogueRegistry;   // nullable — empty breadcrumbs if missing
    private final Map<String, UUID>   appDocBySimpleName;  // pre-indexed at construction

    public AppRefsGetAction(DocRegistry docRegistry, CatalogueRegistry catalogueRegistry) {
        this.docRegistry       = Objects.requireNonNull(docRegistry, "docRegistry");
        this.catalogueRegistry = catalogueRegistry;
        this.appDocBySimpleName = indexAppDocs(docRegistry);
    }

    /**
     * Walk the DocRegistry once to build a {@code simpleName → UUID} map of
     * AppDocs. When two AppDocs wrap the same AppModule class (multi-
     * catalogue placement of the same Navigable), the first one wins —
     * acceptable since AppDoc equality collapses identical Navigables,
     * and distinct display framings would point at the same breadcrumb
     * source (the AppModule's primary catalogue placement is arbitrary
     * across registrations).
     */
    private static Map<String, UUID> indexAppDocs(DocRegistry registry) {
        var out = new java.util.HashMap<String, UUID>();
        for (Doc d : registry.all()) {
            if (d instanceof AppDoc<?, ?> ad) {
                out.putIfAbsent(ad.nav().app().simpleName(), ad.uuid());
            }
        }
        return Map.copyOf(out);
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("app"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String simpleName = query.app();
        if (simpleName == null || simpleName.isBlank()) {
            return CompletableFuture.failedFuture(
                    notFound("app", "Required query parameter 'app' was not provided"));
        }
        UUID uuid = appDocBySimpleName.get(simpleName);
        if (uuid == null) {
            // Not an error — many apps aren't surfaced via a Navigable (the
            // catalogue browser host, plan host, etc.). Empty response lets
            // the chrome fall back to its default crumb chain.
            return CompletableFuture.completedFuture(
                    new DocContent("{\"title\":null,\"breadcrumbs\":[],\"references\":[]}",
                                   "application/json; charset=utf-8"));
        }
        Doc doc = docRegistry.resolve(uuid);
        if (doc == null) {
            return CompletableFuture.failedFuture(notFound(simpleName,
                    "AppDoc UUID indexed but missing from DocRegistry — registry inconsistency"));
        }
        try {
            String body = DocRefsGetAction.serialize(doc, catalogueRegistry);
            return CompletableFuture.completedFuture(
                    new DocContent(body, "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(simpleName,
                    "Failed to serialise app refs: " + e.getMessage()));
        }
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason)
        );
    }
}
