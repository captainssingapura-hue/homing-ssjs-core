package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.StampedParams;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocRegistry;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0051 — {@code GET /ref?doc=<uuid>}: resolve a reference to wherever its
 * doc actually lives.
 *
 * <p>A cross-reference knows a UUID and nothing else. It does not know whether
 * the target is prose, a composed doc, a rigid tree or an SVG — and it has no
 * way to find out, because the UUID carries no kind. Every client-side emitter
 * therefore guessed, and they all guessed the same way:
 * {@code /app?app=doc-reader&doc=<uuid>}. For a prose doc that was right. For
 * RFC 0040, a ComposedDoc, it opened the markdown reader on a JSON payload and
 * rendered the raw JSON as prose — a 45,000-character page of visible braces
 * where an 18,000-character document should be.</p>
 *
 * <p>This endpoint exists so the referrer stops choosing. It knows only what
 * a reference legitimately knows — the UUID — and the server, which owns both
 * the DocRegistry and the tree, answers with the address:</p>
 *
 * <ol>
 *   <li><b>Positioned</b> → its catalogue path. The authentic address, and the
 *       path route picks the viewer from the doc itself.</li>
 *   <li><b>Known but unpositioned</b> → the doc's own {@code url()}, which is
 *       its correct viewer even when it has no place in the tree.</li>
 *   <li><b>Unknown</b> → 404 naming the UUID, rather than a page that renders
 *       something misleading.</li>
 * </ol>
 *
 * <p>This is D2 put to work: a UUID is authoring-time identity, so resolving
 * one to a runtime address is the server's job by definition. It is also why
 * the fix belongs here rather than in each emitter — there are three of them
 * today and the next one would guess the same way.</p>
 */
public final class RefRedirectGetAction
        implements GetAction<RoutingContext, RefRedirectGetAction.Query,
                             EmptyParam.NoHeaders, HtmlPageContent> {

    /** The route this action mounts on. */
    public static final String ROUTE = "/ref";

    /** Build the reference URL for a doc UUID — the one place emitters call. */
    public static String hrefFor(String uuid) {
        return ROUTE + "?doc=" + java.net.URLEncoder.encode(
                uuid, java.nio.charset.StandardCharsets.UTF_8);
    }

    public record Query(String doc) implements Param._QueryString {}

    private final DocRegistry docs;
    private final CatalogueRegistry registry;

    public RefRedirectGetAction(DocRegistry docs, CatalogueRegistry registry) {
        this.docs     = Objects.requireNonNull(docs, "docs");
        this.registry = registry;   // may be null when no catalogues are registered
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("doc"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<HtmlPageContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String raw = query.doc();
        if (raw == null || raw.isBlank()) {
            return CompletableFuture.failedFuture(notFound("doc", "no reference given"));
        }
        UUID id;
        try {
            id = UUID.fromString(raw.trim());
        } catch (IllegalArgumentException notAUuid) {
            return CompletableFuture.failedFuture(notFound(raw, "not a document reference"));
        }
        Doc target = docs.resolve(id);
        if (target == null) {
            return CompletableFuture.failedFuture(
                    notFound(raw, "no document is registered under this reference"));
        }
        String to = null;
        if (registry != null) {
            CataloguePath path = registry.pathOf(target);
            if (path != null) to = path.toUrl();
        }
        // Unpositioned but real: the doc's own url() names its correct viewer,
        // which is the part the guessing emitters got wrong.
        if (to == null) to = target.url();
        return CompletableFuture.completedFuture(new HtmlPageContent(redirectHtml(to)));
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
