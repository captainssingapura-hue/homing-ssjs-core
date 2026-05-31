package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /doc?id=<uuid>&l1=<id1>&l2=<id2>&...} — serves the bytes of a
 * typed {@link Doc}, walking a leveled-tree path if levels are supplied.
 *
 * <p>Per <a href="../../../../../../../../../../docs/rfcs/0004-typed-docs-and-doc-visibility.md">
 * RFC 0004</a>, the wire identity of a registered Doc is its {@link UUID}. The action looks
 * up the {@code id} UUID in the {@link DocRegistry}, then descends through any provided
 * {@code lN} level identifiers via {@link Doc#resolveChild(String)}. Each {@code lN} value
 * is passed verbatim to the resolver for the doc at depth {@code N-1} — the framework
 * doesn't interpret level identifiers; the doc kind at that level does.</p>
 *
 * <p>The leveled-tree URL shape lets the framework address <em>embedded</em> sub-docs by
 * their containment path within a registered root, without requiring the embedded docs to
 * be registered separately. The act of being referenced in a parent's content IS the
 * embedded doc's addressability — eliminating the "I forgot to register the table doc"
 * 404 class.</p>
 *
 * <p><b>Security</b>: user-supplied input never reaches a filesystem path. Only Docs
 * registered at boot are reachable as roots; unknown UUID is 404. Level identifiers
 * are opaque strings passed to the doc-kind's resolver, which validates them
 * type-locally (e.g., ComposedDoc parses an integer index and bounds-checks).</p>
 *
 * @since RFC 0004; leveled-tree addressing added post-RFC-0034
 */
public class DocGetAction
        implements GetAction<RoutingContext, DocGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    /** Max levels honored per request. Defensive cap against pathological URLs. */
    private static final int MAX_LEVELS = 32;

    public record Query(String id, List<String> path) implements Param._QueryString {}

    private final DocRegistry registry;

    public DocGetAction(DocRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> {
            String id = ctx.request().getParam("id");
            List<String> path = new ArrayList<>();
            for (int n = 1; n <= MAX_LEVELS; n++) {
                String levelId = ctx.request().getParam("l" + n);
                if (levelId == null) break;
                path.add(levelId);
            }
            return new Query(id, path);
        };
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String raw = query.id();
        if (raw == null || raw.isBlank()) {
            return CompletableFuture.failedFuture(
                    notFound("id", "Required query parameter 'id' was not provided"));
        }
        UUID id;
        try {
            id = UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return CompletableFuture.failedFuture(notFound(raw, "Malformed UUID"));
        }
        Doc root = registry.resolve(id);
        if (root == null) {
            return CompletableFuture.failedFuture(notFound(raw, "No Doc registered with this UUID"));
        }
        // Walk levels — each lN value is opaque; the doc at depth N-1 resolves it.
        Doc current = root;
        for (int i = 0; i < query.path().size(); i++) {
            String levelId = query.path().get(i);
            Optional<Doc> next = current.resolveChild(levelId);
            if (next.isEmpty()) {
                int levelNum = i + 1;
                return CompletableFuture.failedFuture(notFound(
                        "l" + levelNum + "=" + levelId,
                        "Level " + levelNum + " resolution failed for " + current.getClass().getSimpleName()
                        + " (no child for identifier '" + levelId + "')"));
            }
            current = next.get();
        }
        try {
            String body = current.contents(raw, query.path());
            return CompletableFuture.completedFuture(new DocContent(body, current.contentType()));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(raw,
                    "Failed to load Doc contents: " + e.getMessage()));
        }
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason)
        );
    }
}
