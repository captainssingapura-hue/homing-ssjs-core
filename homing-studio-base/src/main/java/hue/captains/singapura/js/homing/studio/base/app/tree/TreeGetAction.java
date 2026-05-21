package hue.captains.singapura.js.homing.studio.base.app.tree;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueRegistry;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0016 — {@code GET /tree?id=<tree-id>&path=<branch-path>} — serves a
 * {@link ContentTree}'s branch as JSON for the {@link TreeAppHost}'s
 * renderer to consume.
 *
 * <p>Response shape mirrors {@code CatalogueGetAction} so the same
 * frontend renderer can consume both:</p>
 *
 * <pre>{@code
 * {
 *   "name":    "...",
 *   "summary": "...",
 *   "brand":   { "label": "...", "homeUrl": "..." },
 *   "breadcrumbs": [ { "name": "Animals", "url": "..." }, ... ],
 *   "entries": [
 *     { "kind": "catalogue", "name": "Animals",  "summary": "...", "category": "...", "url": "/app?app=tree&id=animals&path=animals" },
 *     { "kind": "svg",       "name": "Turtle",   "summary": "...", "category": "ANIMAL", "url": "", "svg": "<svg>...</svg>" },
 *     ...
 *   ]
 * }
 * }</pre>
 *
 * @since RFC 0016
 */
public final class TreeGetAction
        implements GetAction<RoutingContext, TreeGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    /**
     * @param id   tree id (URL-safe slug; resolved via {@link TreeRegistry})
     * @param path optional path-segments (slash-separated) addressing a sub-branch;
     *             empty / missing → root branch
     */
    public record Query(String id, String path) implements Param._QueryString {}

    private final TreeRegistry registry;
    private final StudioBrand  brand;
    /** RFC 0016 → tree-breadcrumb bridge. When non-null, the catalogue
     *  chain hosting each tree is prepended to the tree-internal chain in
     *  the breadcrumb response, so a tree page's breadcrumb spans both
     *  the catalogue context (multi-studio · demo · …) AND the tree-internal
     *  path. Without this, the breadcrumb of the tree root shows only the
     *  root node — a known gap addressed mid-RFC-0024-P1b. */
    private final CatalogueRegistry catalogueRegistry;
    /** Tree id → the catalogue that contains the {@code TreeAppHost} navigable
     *  leaf for this tree. Computed by {@code Bootstrap} via scanning catalogue
     *  leaves. Empty when no catalogue hosts the tree (e.g. trees registered
     *  in fixtures but not surfaced via any catalogue leaf). */
    private final Map<String, Catalogue<?>> hostOfTree;

    public TreeGetAction(TreeRegistry registry, StudioBrand brand) {
        this(registry, brand, null, Map.of());
    }

    public TreeGetAction(TreeRegistry registry,
                         StudioBrand brand,
                         CatalogueRegistry catalogueRegistry,
                         Map<String, Catalogue<?>> hostOfTree) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.brand    = brand;  // nullable when no brand is configured
        this.catalogueRegistry = catalogueRegistry;  // nullable
        this.hostOfTree = (hostOfTree == null) ? Map.of() : Map.copyOf(hostOfTree);
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(
                ctx.request().getParam("id"),
                ctx.request().getParam("path"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String treeId = query.id();
        if (treeId == null || treeId.isBlank()) {
            return CompletableFuture.failedFuture(
                    notFound("id", "Required query parameter 'id' was not provided"));
        }
        ContentTree tree = registry.resolve(treeId);
        if (tree == null) {
            return CompletableFuture.failedFuture(notFound(treeId, "No ContentTree registered with this id"));
        }
        TreeNode node = registry.resolvePath(treeId, query.path());
        if (node == null) {
            return CompletableFuture.failedFuture(notFound(
                    treeId + "?" + query.path(), "Path does not resolve to a node"));
        }
        if (!(node instanceof TreeBranch branch)) {
            // Leaves don't have their own page; their content viewer renders them.
            // For Phase 1 we return 404 — the renderer is expected to not navigate
            // to leaf paths.
            return CompletableFuture.failedFuture(notFound(
                    treeId + "?" + query.path(),
                    "Path resolves to a leaf; leaves render inline in their parent branch"));
        }
        try {
            String body = serialize(tree, query.path(), branch);
            return CompletableFuture.completedFuture(new DocContent(body, "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(treeId,
                    "Failed to serialise tree: " + e.getMessage()));
        }
    }

    String serialize(ContentTree tree, String currentPath, TreeBranch branch) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"name\":")   .append(jstr(branch.name())).append(',');
        sb.append("\"summary\":").append(jstr(branch.summary())).append(',');

        // Brand block (optional; same shape as CatalogueGetAction).
        if (brand != null) {
            String logoSvg = (brand.logo() != null) ? brand.logo().resolve().orElse("") : "";
            sb.append("\"brand\":{")
              .append("\"label\":")  .append(jstr(brand.label())).append(',')
              .append("\"logo\":")   .append(jstr(logoSvg)).append(',')
              .append("\"homeUrl\":").append(jstr("/"))
              .append("},");
        }

        // Breadcrumbs. RFC 0016 → tree-breadcrumb bridge: when this tree
        // is hosted by a catalogue (a catalogue contains a TreeAppHost
        // navigable leaf for this tree id), prepend that catalogue's
        // chain (root → ... → host) BEFORE the tree-internal chain
        // (root TreeBranch → ... → addressed node). The host's own leaf
        // (the TreeAppHost navigable) is NOT included — the tree's root
        // node serves as the corresponding crumb.
        List<TreeNode> treeChain = registry.breadcrumbs(tree.id(), currentPath);
        sb.append("\"breadcrumbs\":[");

        // -- Catalogue prelude --
        boolean firstCrumb = true;
        Catalogue<?> host = (catalogueRegistry != null) ? hostOfTree.get(tree.id()) : null;
        if (host != null) {
            List<Catalogue<?>> catChain = catalogueRegistry.breadcrumbs(host);
            for (Catalogue<?> c : catChain) {
                if (!firstCrumb) sb.append(',');
                firstCrumb = false;
                // All catalogue prelude crumbs link back; even the host
                // (the catalogue we'd normally render here) gets a link
                // because the current page is the tree, not the host.
                @SuppressWarnings("unchecked")
                Class<? extends Catalogue<?>> cClass = (Class<? extends Catalogue<?>>) c.getClass();
                String icon = c.icon();
                String text = (icon == null || icon.isEmpty()) ? c.name() : icon + " " + c.name();
                sb.append("{\"name\":").append(jstr(text))
                  .append(",\"url\":") .append(jstr(CatalogueAppHost.urlFor(cClass)))
                  .append('}');
            }
        }

        // -- Tree-internal chain --
        StringBuilder cumulative = new StringBuilder();
        for (int i = 0; i < treeChain.size(); i++) {
            TreeNode n = treeChain.get(i);
            if (!firstCrumb) sb.append(',');
            firstCrumb = false;
            // i==0 is the root; subsequent nodes append their segment to the path.
            if (i > 0) {
                if (cumulative.length() > 0) cumulative.append('/');
                cumulative.append(n.segment());
            }
            String url = (i == treeChain.size() - 1) ? "" : treeUrl(tree.id(), cumulative.toString());
            sb.append("{\"name\":").append(jstr(n.name()))
              .append(",\"url\":") .append(jstr(url))
              .append('}');
        }
        sb.append("],");

        // Entries — children of current branch.
        sb.append("\"entries\":[");
        boolean first = true;
        for (TreeNode child : branch.children()) {
            if (!first) sb.append(',');
            first = false;
            String childPath = (currentPath == null || currentPath.isBlank())
                    ? child.segment()
                    : currentPath + "/" + child.segment();
            switch (child) {
                case TreeBranch sub -> {
                    sb.append("{\"kind\":\"catalogue\",")
                      .append("\"name\":")    .append(jstr(sub.name())).append(',')
                      .append("\"summary\":") .append(jstr(sub.summary())).append(',')
                      .append("\"category\":").append(jstr(sub.badge().isEmpty() ? "CATALOGUE" : sub.badge())).append(',')
                      .append("\"url\":")     .append(jstr(treeUrl(tree.id(), childPath)))
                      .append('}');
                }
                case TreeLeaf leaf -> {
                    // RFC 0015 polymorphic Doc viewer: each leaf wraps a Doc;
                    // the Doc supplies kind() and url() (its registered
                    // ContentViewer); the leaf may override display fields.
                    var doc = leaf.doc();
                    String name     = leaf.name().isEmpty()    ? doc.title()    : leaf.name();
                    String summary  = leaf.summary().isEmpty() ? doc.summary()  : leaf.summary();
                    String category = leaf.badge().isEmpty()   ? doc.category() : leaf.badge();
                    sb.append("{\"kind\":")    .append(jstr(doc.kind())).append(',')
                      .append("\"name\":")     .append(jstr(name)).append(',')
                      .append("\"summary\":")  .append(jstr(summary)).append(',')
                      .append("\"category\":") .append(jstr(category)).append(',')
                      .append("\"url\":")      .append(jstr(doc.url()))
                      .append('}');
                }
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String treeUrl(String id, String path) {
        if (path == null || path.isEmpty()) return "/app?app=tree&id=" + id;
        return "/app?app=tree&id=" + id + "&path=" + path;
    }

    private static String jstr(String v) {
        if (v == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason)
        );
    }
}
