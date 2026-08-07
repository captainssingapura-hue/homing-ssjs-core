package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.rules.ModuleRef;
import hue.captains.singapura.js.homing.conformance.rules.ModuleRegistry;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.studio.base.tree.CategoryValue;
import hue.captains.singapura.js.homing.studio.base.tree.KindValue;
import hue.captains.singapura.js.homing.tree.Category;
import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.Kind;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.Summary;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import hue.captains.singapura.js.homing.tree.dims.NameValue;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0044 — the conformance workspace's tree feed: serves the JS module
 * inventory as canonical {@code TreeNode} JSON at {@code GET /module-tree},
 * consumed by {@link ModuleTreeWidget}'s {@code TreeRenderer}.
 *
 * <p>Reuses the tree-view serializer ({@link TreeNodeJsonWriter} over
 * {@link NormalizedNode}) — the SAME substrate the studio Navigator rides —
 * but builds its nodes straight from a {@link ModuleRegistry}, so the modules
 * never masquerade as catalogue entries or Docs. Three levels: root (L0) →
 * package (L1) → module leaf (L2). The renderer surfaces each node's
 * {@code displayLabel} / {@code kind} / {@code summary} dimensions onto the
 * selection payload; a module leaf carries its FQCN on {@code summary}, which
 * is how a detail widget learns which module was selected.</p>
 */
public final class ModuleTreeGetAction
        implements GetAction<RoutingContext, ModuleTreeGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    /** @param id reserved for future package re-rooting; ignored today. */
    public record Query(String id) implements Param._QueryString {}

    private final ModuleRegistry registry;
    private final TreeNodeJsonWriter writer = new TreeNodeJsonWriter();

    public ModuleTreeGetAction(ModuleRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
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
            String body = writer.write(rootNode());
            return CompletableFuture.completedFuture(
                    new DocContent(body, "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(
                    "module-tree", "Failed to serialise module tree: " + e.getMessage()));
        }
    }

    private NormalizedNode rootNode() {
        // Build the package trie, then root it at the deepest node that has no
        // siblings (and no ancestor with siblings) — i.e. collapse the common
        // leading package prefix (hue.captains.singapura.js.homing) down to the
        // first branch point, so the tree opens at the level that actually forks
        // rather than a five-deep single-child spine.
        PkgNode virtual = new PkgNode("", "");
        for (ModuleRef m : registry.modules()) {
            PkgNode cur = virtual;
            StringBuilder path = new StringBuilder();
            if (!m.packageName().isEmpty()) {
                for (String seg : m.packageName().split("\\.")) {
                    if (path.length() > 0) path.append('.');
                    path.append(seg);
                    String fp = path.toString();
                    cur = cur.children.computeIfAbsent(seg, s -> new PkgNode(s, fp));
                }
            }
            cur.modules.add(m);
        }

        PkgNode root = virtual;
        while (root.children.size() == 1 && root.modules.isEmpty()) {
            root = root.children.firstEntry().getValue();
        }
        return toNode(root, true, 0);
    }

    /** A package node in the trie: child packages (sorted) + modules directly in it. */
    private static final class PkgNode {
        final String segment;
        final String fullPath;
        final java.util.TreeMap<String, PkgNode> children = new java.util.TreeMap<>();
        final List<ModuleRef> modules = new java.util.ArrayList<>();
        PkgNode(String segment, String fullPath) { this.segment = segment; this.fullPath = fullPath; }
        int subtreeCount() {
            int n = modules.size();
            for (PkgNode c : children.values()) n += c.subtreeCount();
            return n;
        }
    }

    private NormalizedNode toNode(PkgNode node, boolean isRoot, int depth) {
        // Child packages first (folders), then this package's own module leaves.
        List<NormalizedNode> kids = new java.util.ArrayList<>();
        for (PkgNode child : node.children.values()) kids.add(toNode(child, false, depth + 1));
        for (ModuleRef m : node.modules) kids.add(moduleLeaf(m, depth + 1));

        String label   = isRoot ? node.fullPath : node.segment;
        String kind    = isRoot ? "workspace" : "package";
        int    count   = node.subtreeCount();
        String summary = isRoot
                ? count + " modules across " + registry.byPackage().size() + " packages"
                : count + (count == 1 ? " module" : " modules");
        return new NormalizedNode(TreeLevel.atDepth(depth), dims(label, summary, "", kind), kids);
    }

    private NormalizedNode moduleLeaf(ModuleRef m, int depth) {
        // FQCN rides on summary — the field a detail widget reads off the selection.
        return NormalizedNode.leaf(TreeLevel.atDepth(depth),
                dims(m.simpleName(), m.moduleClass(), m.packageName(), "module"));
    }

    private static Map<DimensionKey, DimensionValue> dims(
            String label, String summary, String category, String kind) {
        var m = new LinkedHashMap<DimensionKey, DimensionValue>();
        m.put(DisplayLabel.INSTANCE, new NameValue(label));
        m.put(Summary.INSTANCE,      new NameValue(summary));
        m.put(Category.INSTANCE,     new CategoryValue(category));
        m.put(Kind.INSTANCE,         new KindValue(kind));
        return m;
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason));
    }
}
