package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.studio.base.DocRegistry;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0039 — serves the doc rigid-tree payload for a {@link ComposedDoc}: the
 * two-part {@code {structure, content}} JSON (structure via
 * {@link ComposedDocNormalizer} + {@link DocTreeJsonWriter}). The
 * {@code DocTreeWidget} fetches this and mounts {@code renderDocTree}.
 *
 * <p>Two locators, one payload — symmetric with how {@code ComposedWidget}
 * accepts both {@code /doc?id=} and {@code /open-content?l0=…}:</p>
 * <ul>
 *   <li>{@code GET /doc-tree?id=<uuid>} — direct uuid lookup via the
 *       {@link DocRegistry}; the standalone {@code doc-tree-viewer} uses this.</li>
 *   <li>{@code GET /doc-tree?l0=<i>&l1=<i>&…} — the leveled tree-path locator
 *       (RFC 0040), resolved against the studio forest by
 *       {@link ForestPathResolver}; the Navigator's leveled Open uses this so a
 *       composed leaf renders through the rigid-tree pipeline in
 *       {@code SingleWidgetWorkspace}.</li>
 * </ul>
 *
 * <p>Pilot scope: only {@link ComposedDoc} is supported (the first {@code
 * TreeNormalizer} front-end). A non-composed doc returns 404 — other doc kinds
 * adopt the transform later, as RFC 0039's migration prescribes. Parallel to
 * the legacy {@code /doc?id=} that {@code ComposedWidget} consumes; both run
 * until the migration flips.</p>
 *
 * @since homing-studio-base — RFC 0039 rigid-tree doc
 */
public final class DocTreeGetAction
        implements GetAction<RoutingContext, DocTreeGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    /** Defensive cap on path depth (mirrors {@code OpenContentGetAction}). */
    private static final int MAX_LEVELS = 32;

    /**
     * @param id   uuid locator (the {@code doc-tree-viewer}'s direct address), or null
     * @param path the leveled child-index path ({@code [l0, l1, …]}); empty when uuid is used
     */
    public record Query(String id, List<Integer> path) implements Param._QueryString {}

    private final DocRegistry registry;
    private final Catalogue<?> root;

    public DocTreeGetAction(DocRegistry registry, Catalogue<?> root) {
        this.registry = Objects.requireNonNull(registry, "registry");
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
            return new Query(ctx.request().getParam("id"), path);
        };
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String rawId = (query == null) ? null : query.id();
        List<Integer> path = (query == null) ? List.of() : query.path();

        // Locator 1 — uuid (the standalone doc-tree-viewer).
        if (rawId != null && !rawId.isBlank()) {
            UUID id;
            try { id = UUID.fromString(rawId); }
            catch (IllegalArgumentException e) { return CompletableFuture.failedFuture(notFound(rawId, "Malformed UUID")); }
            Doc doc = registry.resolve(id);
            if (doc == null) {
                return CompletableFuture.failedFuture(notFound(rawId, "No Doc registered with this UUID"));
            }
            return treePayload(doc, rawId);
        }

        // Locator 2 — leveled tree path (the Navigator's leveled Open).
        if (!path.isEmpty()) {
            Optional<ForestPathResolver.Resolved> resolved =
                    ForestPathResolver.INSTANCE.resolve(root, path);
            if (resolved.isEmpty()) {
                return CompletableFuture.failedFuture(notFound(pathStr(path), "No doc at this tree path"));
            }
            return treePayload(resolved.get().doc(), pathStr(path));
        }

        return CompletableFuture.failedFuture(notFound("(none)",
                "Required locator missing — supply 'id' or a leveled path 'l0,l1,…'"));
    }

    /**
     * Shared tail: emit a resolved {@link Doc}'s rigid-tree JSON. Two doc kinds
     * normalize to a {@link DocTree} today — a {@link ComposedDoc} (RFC 0039,
     * via its titled-fold normalizer) and a {@code RigidDoc} (RFC 0042, authored
     * as a tree directly). Any other kind 404s.
     */
    private CompletableFuture<DocContent> treePayload(Doc doc, String locator) {
        try {
            String json;
            String rootId = doc.uuid().toString();
            if (doc instanceof DocTreeSource dts) {
                // A Doc that computes its own tree (e.g. a catalogue-mirror),
                // possibly lazily — takes precedence over the kind dispatch.
                json = DocTreeJsonWriter.INSTANCE.write(dts.toDocTree(), rootId);
            } else if (doc instanceof ComposedDoc cd) {
                json = DocTreeJsonWriter.INSTANCE.write(ComposedDocNormalizer.INSTANCE.toDocTree(cd), rootId);
            } else if (doc instanceof hue.captains.singapura.js.homing.studio.base.rigid.RigidDoc rd) {
                json = DocTreeJsonWriter.INSTANCE.write(
                        hue.captains.singapura.js.homing.studio.base.rigid.RigidDocNormalizer.INSTANCE.toDocTree(rd), rootId);
            } else if ("doc".equals(doc.kind()) || "markdown".equals(doc.kind())) {
                // Legacy markdown-bodied Doc (ClasspathMarkdownDoc, MarkdownDoc, …):
                // render its raw markdown as one flat node — no migration needed.
                json = DocTreeJsonWriter.INSTANCE.write(MarkdownDocNormalizer.INSTANCE.toDocTree(doc), rootId);
            } else {
                return CompletableFuture.failedFuture(notFound(locator,
                        "Doc kind '" + doc.kind() + "' has no rigid-tree transform — supported: "
                      + "ComposedDoc, RigidDoc, and markdown docs (kind 'doc'/'markdown')"));
            }
            return CompletableFuture.completedFuture(new DocContent(json, "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(locator, "Failed to build doc tree: " + e.getMessage()));
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
                new ResourceNotFound._ExternalError(resource, reason));
    }
}
