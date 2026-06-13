package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /open?id=<doc-uuid>} — resolves a doc UUID to its <i>own</i>
 * authoritative viewer URL ({@code Doc.url()}, per-kind) and redirects there
 * in regular MPA mode.
 *
 * <p>The point of this endpoint is to keep the URL-resolution concern on the
 * studio side, where {@code Doc.url()} lives, instead of leaking it into the
 * generic tree substrate or re-deriving per-kind URL grammar in JS. The
 * Studio Workspace's tree only carries the node's {@code id} (the doc UUID);
 * the detail pane's "Open" button just hits {@code /open?id=<id>} and lets
 * the studio name the destination.</p>
 *
 * <p>The UUID → URL index is built once at construction by walking the root
 * {@link Catalogue}'s subtree (sub-catalogues + {@code OfDoc} leaves) — the
 * same traversal {@code CatalogueTreeGetAction} uses to serve the tree.
 * Wired downstream via {@code Fixtures.harnessGetActions()} with the studio's
 * root catalogue.</p>
 *
 * <p>Redirect mechanism mirrors {@code RootRedirectGetAction}: an HTML
 * meta-refresh + {@code location.replace} page (the action pipeline serves
 * {@code TypedContent} bodies, not 302 status codes). An unknown id redirects
 * to {@code /}.</p>
 *
 * @since homing-studio-workspace — Studio Workspace, leaf "Open" action
 */
public final class OpenDocGetAction
        implements GetAction<RoutingContext, OpenDocGetAction.Query, EmptyParam.NoHeaders, HtmlPageContent> {

    /** @param id the doc UUID (a tree leaf's node id). */
    public record Query(String id) implements Param._QueryString {}

    private final Map<String, String> urlByUuid;

    public OpenDocGetAction(Catalogue<?> root) {
        Objects.requireNonNull(root, "root catalogue");
        var m = new HashMap<String, String>();
        index(root, m);
        this.urlByUuid = Map.copyOf(m);
    }

    /** Walk the catalogue subtree, mapping each leaf doc's UUID to its url(). */
    private static void index(Catalogue<?> cat, Map<String, String> out) {
        for (Catalogue<?> sub : cat.subCatalogues()) index(sub, out);
        for (Entry<?> entry : cat.leaves()) {
            if (entry instanceof Entry.OfDoc<?, ?> od) {
                Doc doc = od.doc();
                if (doc.url() != null) out.put(doc.uuid().toString(), doc.url());
            }
        }
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("id"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<HtmlPageContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String id     = (query == null) ? null : query.id();
        String target = (id != null) ? urlByUuid.get(id) : null;
        if (target == null) target = "/";   // unknown id → home
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta http-equiv="refresh" content="0;url=%s">
                    <title>Opening…</title>
                </head>
                <body>
                    <script>window.location.replace(%s);</script>
                </body>
                </html>
                """.formatted(htmlAttrEscape(target), "\"" + jsStringEscape(target) + "\"");
        return CompletableFuture.completedFuture(new HtmlPageContent(html));
    }

    private static String htmlAttrEscape(String s) {
        return s.replace("&", "&amp;").replace("\"", "&quot;");
    }

    private static String jsStringEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
