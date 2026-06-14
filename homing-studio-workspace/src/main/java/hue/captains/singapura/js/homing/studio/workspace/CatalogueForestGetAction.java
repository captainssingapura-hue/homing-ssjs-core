package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.tree.CatalogueNormalizer;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0040 — the normalized-pipeline counterpart of {@link CatalogueTreeGetAction}.
 * Serves the same {@code TreeNode} JSON the generic {@code TreeRenderer}
 * consumes, but produces it through {@link CatalogueNormalizer} →
 * {@link NormalizedNode} (which grafts {@code OfStudio} portals into the
 * studio "synthetic forest") rather than the legacy {@code CatalogueTreeAdapter}.
 *
 * <p>Built alongside the original so the Navigator can run on the new
 * substrate without disturbing the old path; the migration flips wiring over
 * once the tree view is proven. Drop-in: same {@code ?id=<slug>} re-rooting
 * contract and same JSON shape.</p>
 *
 * @since homing-studio-workspace — RFC 0040 normalize pipeline
 */
public final class CatalogueForestGetAction
        implements GetAction<RoutingContext, CatalogueForestGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    /** @param id optional name-slug selecting a sub-catalogue to re-root at. */
    public record Query(String id) implements Param._QueryString {}

    private final Catalogue<?>       root;
    private final CatalogueNormalizer normalizer = CatalogueNormalizer.INSTANCE;
    private final TreeNodeJsonWriter  writer     = new TreeNodeJsonWriter();

    public CatalogueForestGetAction(Catalogue<?> root) {
        this.root = Objects.requireNonNull(root, "root catalogue");
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
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        try {
            Catalogue<?> target = resolveTarget(query == null ? null : query.id());
            NormalizedNode node = normalizer.normalize(target);
            String body = writer.write(node);
            return CompletableFuture.completedFuture(
                    new DocContent(body, "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(
                    "catalogue-tree", "Failed to serialise catalogue forest: " + e.getMessage()));
        }
    }

    private Catalogue<?> resolveTarget(String id) {
        if (id == null || id.isBlank()) return root;
        String wanted = slug(id);
        Catalogue<?> found = findBySlug(root, wanted);
        return (found != null) ? found : root;
    }

    private Catalogue<?> findBySlug(Catalogue<?> at, String wanted) {
        if (slug(at.name()).equals(wanted)) return at;
        for (Catalogue<?> sub : at.subCatalogues()) {
            Catalogue<?> hit = findBySlug(sub, wanted);
            if (hit != null) return hit;
        }
        return null;
    }

    private static String slug(String name) {
        if (name == null || name.isBlank()) return "node";
        var sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = Character.toLowerCase(name.charAt(i));
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) sb.append(c);
            else if (c == ' ' || c == '-' || c == '_') sb.append('-');
        }
        return sb.isEmpty() ? "node" : sb.toString();
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason)
        );
    }
}
