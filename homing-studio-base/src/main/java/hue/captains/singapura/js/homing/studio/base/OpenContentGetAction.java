package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.tree.ForestPathResolver;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /open-content?treeId=<id>&l0=<i>&l1=<i>&…} — the path-keyed
 * content lookup behind RFC 0040's leveled Open. Walks the catalogue forest
 * by the child-index path to the {@link Doc} sitting there and returns its
 * bytes ({@link Doc#contents()} + {@link Doc#contentType()}).
 *
 * <p>The Navigator stamps no uuid; a leaf's identity is its structural path
 * ({@code l0,l1,…}). Because the resolved node already holds a direct
 * {@code Doc} reference ({@link ForestPathResolver}), no uuid index and no
 * per-kind fetch indirection is needed — the widget hosting an opened doc
 * fetches this endpoint with the same path locator it was opened by. The
 * legacy {@code /doc?id=<uuid>} fetch keeps working in parallel; this is the
 * positional sibling.</p>
 *
 * <p>{@code treeId} is reserved for future multi-tree addressing; today a
 * single forest is rooted at the studio's {@code home()}.</p>
 *
 * @since homing-studio-base — RFC 0040 leveled Open
 */
public final class OpenContentGetAction
        implements GetAction<RoutingContext, OpenContentGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    /** Defensive cap on path depth (mirrors {@code DocGetAction}). */
    private static final int MAX_LEVELS = 32;

    /** @param path the leveled child-index path ({@code [l0, l1, …]}). */
    public record Query(List<Integer> path) implements Param._QueryString {}

    private final Catalogue<?> root;

    public OpenContentGetAction(Catalogue<?> root) {
        this.root = Objects.requireNonNull(root, "root catalogue");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> {
            var path = new ArrayList<Integer>();
            for (int n = 0; n < MAX_LEVELS; n++) {
                String v = ctx.request().getParam("l" + n);
                if (v == null) break;
                try { path.add(Integer.parseInt(v)); }
                catch (NumberFormatException e) { break; }
            }
            return new Query(path);
        };
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        List<Integer> path = (query == null) ? List.of() : query.path();
        Optional<ForestPathResolver.Resolved> resolved =
                ForestPathResolver.INSTANCE.resolve(root, path);
        if (resolved.isEmpty()) {
            return CompletableFuture.failedFuture(notFound(
                    pathStr(path), "No doc at this tree path"));
        }
        Doc doc = resolved.get().doc();
        try {
            return CompletableFuture.completedFuture(
                    new DocContent(doc.contents(), doc.contentType()));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(pathStr(path),
                    "Failed to load doc contents: " + e.getMessage()));
        }
    }

    private static String pathStr(List<Integer> path) {
        var sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('l').append(i).append('=').append(path.get(i));
        }
        return sb.length() == 0 ? "(empty)" : sb.toString();
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason)
        );
    }
}
