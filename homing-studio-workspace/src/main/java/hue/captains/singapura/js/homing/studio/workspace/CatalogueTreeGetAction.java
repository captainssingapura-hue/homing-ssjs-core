package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.tree.CatalogueTreeAdapter;
import hue.captains.singapura.js.homing.studio.base.tree.CatalogueTreeNode;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /catalogue-tree?id=<slug>} — serializes a {@link Catalogue}
 * subtree as the canonical {@code TreeNode} JSON the generic
 * {@code TreeRenderer} (homing-core-js) consumes. The Studio Workspace's
 * {@link TreeWidget} fetches this to draw the studio navigation tree.
 *
 * <p>Stateless except for the configured {@code root} catalogue — typically
 * the studio's home L0. The {@code id} query parameter is optional:</p>
 *
 * <ul>
 *   <li><b>blank / missing</b> — serialize the whole tree rooted at
 *       {@code root} (the studio's full catalogue forest).</li>
 *   <li><b>present</b> — re-root the serialization at the first catalogue
 *       in {@code root}'s subtree whose name-slug matches {@code id};
 *       falls back to {@code root} when no match (robust for the demo —
 *       a stale id renders the whole tree rather than 404ing).</li>
 * </ul>
 *
 * <p>Wired downstream, not in studio-base's {@code Bootstrap}: the action
 * needs the studio's root {@link Catalogue}, which the downstream studio
 * already declares. Register via {@code Fixtures.harnessGetActions()}:</p>
 *
 * <pre>{@code
 * Map.of("/catalogue-tree", new CatalogueTreeGetAction(MyStudioHome.INSTANCE))
 * }</pre>
 *
 * <p>Functional Object: the adapter + writer are stateless singletons;
 * this action holds only its immutable root. Mirrors the response-shape
 * conventions of {@code TreeGetAction} but emits the <i>substrate</i>
 * TreeNode JSON (level + typed dimensions + children), not the legacy
 * per-branch entry list.</p>
 *
 * @since homing-studio-workspace — Studio Workspace, first widget (tree view)
 */
public final class CatalogueTreeGetAction
        implements GetAction<RoutingContext, CatalogueTreeGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    /** @param id optional name-slug selecting a sub-catalogue to re-root at;
     *            blank / missing → the configured root. */
    public record Query(String id) implements Param._QueryString {}

    private final Catalogue<?>           root;
    private final CatalogueTreeAdapter   adapter = CatalogueTreeAdapter.INSTANCE;
    private final TreeNodeJsonWriter     writer  = new TreeNodeJsonWriter();

    public CatalogueTreeGetAction(Catalogue<?> root) {
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
            CatalogueTreeNode node = adapter.adapt(target);
            String body = writer.write(node);
            return CompletableFuture.completedFuture(
                    new DocContent(body, "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(
                    "catalogue-tree", "Failed to serialise catalogue tree: " + e.getMessage()));
        }
    }

    // ── target resolution ────────────────────────────────────────────────

    private Catalogue<?> resolveTarget(String id) {
        if (id == null || id.isBlank()) return root;
        String wanted = slug(id);
        Catalogue<?> found = findBySlug(root, wanted);
        return (found != null) ? found : root;
    }

    /** Depth-first search for a catalogue in {@code at}'s subtree whose
     *  name-slug equals {@code wanted}. Returns {@code null} when absent. */
    private Catalogue<?> findBySlug(Catalogue<?> at, String wanted) {
        if (slug(at.name()).equals(wanted)) return at;
        for (Catalogue<?> sub : at.subCatalogues()) {
            Catalogue<?> hit = findBySlug(sub, wanted);
            if (hit != null) return hit;
        }
        return null;
    }

    /** Lowercase, URL-safe slug from a display name. Mirrors
     *  {@code CatalogueTreeAdapter}'s slug rules so ids round-trip. */
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
