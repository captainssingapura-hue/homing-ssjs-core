package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.studio.base.DocRegistry;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDoc;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDocNormalizer;
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
 * {@code GET /doc-tree-content?id=<rootUuid>&path=<a/b/c>&seg=<i>} — serves the
 * raw bytes of a <b>resource-backed inline segment</b> (svg / table / image)
 * embedded in a doc rigid tree.
 *
 * <p>The companion to {@link DocTreeGetAction}: that action serves the tree's
 * {@code {structure, content}} JSON, and for each inline resource segment the
 * {@link DocTreeJsonWriter} emits a URL <i>here</i> instead of the resource's
 * standalone-viewer {@code url()}. The client renderers ({@code renderSvgSegment}
 * / {@code renderImageSegment} / {@code renderTableSegment}) fetch that URL
 * expecting the resource's <b>own content</b> (raw SVG markup, the ImageDoc JSON
 * envelope, the TableDoc JSON) — <i>not</i> the viewer app's HTML page — and then
 * apply the composed doc's own figure/caption styling. Pointing the renderers at
 * the viewer app was the RFC 0039 defect this endpoint fixes: fetching an HTML
 * page and injecting it either executed the viewer's {@code appMain} (the SVG
 * crash) or failed a {@code JSON.parse} (the image error).</p>
 *
 * <p>Addressing mirrors the doc tree's own positional identity: the segment is
 * located by the <b>registered root</b>'s uuid plus the content node's
 * child-index {@code path} and the segment's index {@code seg} within that node's
 * bundle — exactly the keys {@link DocTreeJsonWriter} writes. The action
 * re-derives the same {@link DocTree} (via the same normalizer the writer used)
 * and looks the segment up, so the embedded doc never needs separate UUID
 * registration — its addressability is its position inside the root, the same
 * carve-out {@code DocGetAction}'s leveled URL provides for the flat
 * {@code ComposedDoc} path.</p>
 *
 * @since homing-studio-base — RFC 0039 rigid-tree doc (inline-resource loading fix)
 */
public final class DocTreeContentGetAction
        implements GetAction<RoutingContext, DocTreeContentGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    /** Defensive cap on path depth (mirrors {@link DocTreeGetAction}). */
    private static final int MAX_LEVELS = 32;

    /**
     * @param id      registered root doc uuid
     * @param rawPath the raw {@code path} param verbatim — a name-path (V2,
     *                {@code "a/b/c"}) or child-index path (V1); {@code ""} = root
     * @param path    the child-index path parsed from {@code rawPath} (V1 only)
     * @param seg     the segment index within that node's bundle
     */
    public record Query(String id, String rawPath, List<Integer> path, int seg) implements Param._QueryString {}

    private final DocRegistry registry;

    public DocTreeContentGetAction(DocRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> {
            String id      = ctx.request().getParam("id");
            String rawPath = ctx.request().getParam("path");
            String rawSeg  = ctx.request().getParam("seg");
            List<Integer> path = new ArrayList<>();
            if (rawPath != null && !rawPath.isBlank()) {
                String[] parts = rawPath.split("/", MAX_LEVELS + 1);
                for (String part : parts) {
                    try { path.add(Integer.parseInt(part.trim())); }
                    catch (NumberFormatException e) { /* malformed level — lookup will 404 */ }
                }
            }
            int seg;
            try { seg = (rawSeg == null) ? -1 : Integer.parseInt(rawSeg.trim()); }
            catch (NumberFormatException e) { seg = -1; }
            return new Query(id, rawPath == null ? "" : rawPath, path, seg);
        };
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String rawId = (query == null) ? null : query.id();
        if (rawId == null || rawId.isBlank()) {
            return CompletableFuture.failedFuture(notFound("id", "Required query parameter 'id' was not provided"));
        }
        UUID id;
        try { id = UUID.fromString(rawId); }
        catch (IllegalArgumentException e) { return CompletableFuture.failedFuture(notFound(rawId, "Malformed UUID")); }

        Doc root = registry.resolve(id);
        if (root == null) {
            return CompletableFuture.failedFuture(notFound(rawId, "No Doc registered with this UUID"));
        }

        // A name-path doc (RigidDocV2) addresses content by nodeName-chain; the
        // index-path kinds by child-index path. Same segment extraction after.
        String locator;
        Optional<ContentProvider> provider;
        if (root instanceof DocTreeV2Source v2) {
            String namePath = (query.rawPath() == null) ? "" : query.rawPath();
            locator = namePath.isEmpty() ? "(root)" : namePath;
            provider = v2.toDocTreeV2().providerAt(namePath);
        } else {
            DocTree tree = toDocTree(root);
            if (tree == null) {
                return CompletableFuture.failedFuture(notFound(rawId,
                        "Doc kind '" + root.kind() + "' has no rigid-tree transform"));
            }
            locator = pathStr(query.path());
            provider = tree.providerAt(query.path());
        }
        if (provider.isEmpty()) {
            return CompletableFuture.failedFuture(notFound(locator, "No content at this doc-tree path"));
        }
        if (!(provider.get().content() instanceof ComposedLeaf leaf)) {
            return CompletableFuture.failedFuture(notFound(locator, "Node content is not a segment bundle"));
        }
        List<Segment> segs = leaf.contents();
        if (query.seg() < 0 || query.seg() >= segs.size()) {
            return CompletableFuture.failedFuture(notFound("seg=" + query.seg(),
                    "Segment index out of range (bundle size " + segs.size() + ")"));
        }
        Segment segment = segs.get(query.seg());
        Doc embedded = embeddedDocOf(segment);
        if (embedded == null) {
            return CompletableFuture.failedFuture(notFound("seg=" + query.seg(),
                    "Segment kind '" + segment.getClass().getSimpleName()
                    + "' is not a resource-backed inline doc"));
        }
        try {
            return CompletableFuture.completedFuture(new DocContent(embedded.contents(), embedded.contentType()));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(rawId, "Failed to load inline resource: " + e.getMessage()));
        }
    }

    /** The same normalizer dispatch {@link DocTreeGetAction} uses; {@code null} when the kind has no transform. */
    private static DocTree toDocTree(Doc doc) {
        if (doc instanceof DocTreeSource dts) {
            return dts.toDocTree();
        } else if (doc instanceof ComposedDoc cd) {
            return ComposedDocNormalizer.INSTANCE.toDocTree(cd);
        } else if (doc instanceof RigidDoc rd) {
            return RigidDocNormalizer.INSTANCE.toDocTree(rd);
        } else if ("doc".equals(doc.kind()) || "markdown".equals(doc.kind())) {
            return MarkdownDocNormalizer.INSTANCE.toDocTree(doc);
        }
        return null;
    }

    /** The embedded resource Doc a segment references, or {@code null} for text-shaped kinds. */
    private static Doc embeddedDocOf(Segment seg) {
        return switch (seg) {
            case SvgSegment v    -> v.doc();
            case TableSegment t  -> t.doc();
            case ImageSegment im -> im.doc();
            default              -> null;
        };
    }

    private static String pathStr(List<Integer> path) {
        var sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append('/');
            sb.append(path.get(i));
        }
        return sb.length() == 0 ? "(root)" : sb.toString();
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason));
    }
}
