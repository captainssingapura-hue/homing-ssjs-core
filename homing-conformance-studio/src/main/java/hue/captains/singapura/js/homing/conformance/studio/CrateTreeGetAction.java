package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
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
 * RFC 0044 — the Crate-Studio Navigator feed: the top-level (owned) crates and
 * their modules as canonical {@code TreeNode} JSON at {@code GET /crate-tree}.
 * Root → crate (L1) → <b>package hierarchy</b> → module leaf. Within a crate the
 * modules are nested by their Java package so a large module set stays navigable
 * (rather than one flat list): the crate's common package prefix is stripped,
 * and single-child package chains are collapsed into one node (e.g. {@code
 * demo.es}). A package node's {@code kind} is {@code "package"}; a module leaf's
 * {@code kind} is its {@link hue.captains.singapura.js.homing.core.ModuleForm}
 * (so the tree shows the real mechanical type) and its {@code summary} is the
 * FQCN (which a detail widget reads to fetch the served artifact). Only
 * top-level crates appear — external dependency crates live in the graph feed,
 * not here.
 */
public final class CrateTreeGetAction
        implements GetAction<RoutingContext, CrateTreeGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String id) implements Param._QueryString {}

    private final List<Crate> topLevel;
    private final TreeNodeJsonWriter writer = new TreeNodeJsonWriter();

    public CrateTreeGetAction(List<Crate> topLevel) {
        this.topLevel = List.copyOf(Objects.requireNonNull(topLevel, "topLevel"));
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
            return CompletableFuture.completedFuture(
                    new DocContent(writer.write(rootNode()), "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(
                    "crate-tree", "Failed to serialise crate tree: " + e.getMessage()));
        }
    }

    private NormalizedNode rootNode() {
        List<NormalizedNode> crates = topLevel.stream().map(this::crateNode).toList();
        int modules = topLevel.stream().mapToInt(c -> c.entries().size()).sum();
        String summary = topLevel.size() + " crates · " + modules + " modules";
        return new NormalizedNode(TreeLevel.L0.INSTANCE, dims("Crates", summary, "", "workspace"), crates);
    }

    private NormalizedNode crateNode(Crate crate) {
        // Build a package trie from the crate's modules, then strip the common
        // package prefix so the tree starts where the packages actually branch.
        PackageTrie trie = new PackageTrie();
        for (CrateEntry e : crate.entries()) trie.insert(segments(packageOf(e.moduleClass())), e);
        StringBuilder prefix = new StringBuilder();
        while (trie.entries.isEmpty() && trie.children.size() == 1) {
            var only = trie.children.entrySet().iterator().next();
            if (prefix.length() > 0) prefix.append('.');
            prefix.append(only.getKey());
            trie = only.getValue();
        }

        List<NormalizedNode> children = trie.toNodes(TreeLevel.L2.INSTANCE);
        String reqs = crate.requires().isEmpty() ? "no requires"
                : "requires " + String.join(", ", crate.requires().stream().map(Crate::name).toList());
        String pkg = prefix.length() == 0 ? "" : prefix + ".* · ";
        String summary = crate.entries().size() + " modules · " + pkg + reqs;
        return new NormalizedNode(TreeLevel.L1.INSTANCE, dims(crate.name(), summary, "", "crate"), children);
    }

    private NormalizedNode moduleLeaf(CrateEntry e, TreeLevel level) {
        // kind = the mechanical ModuleForm; summary = FQCN (a detail widget reads it).
        String form = e.form().name().toLowerCase().replace('_', '-');
        return NormalizedNode.leaf(level,
                dims(e.module().getClass().getSimpleName(), e.moduleClass(), "", form));
    }

    private static String packageOf(String fqcn) {
        int dot = fqcn.lastIndexOf('.');
        return dot < 0 ? "" : fqcn.substring(0, dot);
    }

    private static List<String> segments(String pkg) {
        return pkg.isEmpty() ? List.of() : List.of(pkg.split("\\."));
    }

    /**
     * A package trie of a crate's module entries: each edge is one package
     * segment; {@code entries} holds the modules that live directly in this
     * node's package. {@link #toNodes} renders it to {@code NormalizedNode}s,
     * collapsing single-child chains so a deep package path shows as one node.
     */
    private final class PackageTrie {
        final Map<String, PackageTrie> children = new LinkedHashMap<>();
        final List<CrateEntry> entries = new java.util.ArrayList<>();

        void insert(List<String> segs, CrateEntry e) {
            if (segs.isEmpty()) { entries.add(e); return; }
            children.computeIfAbsent(segs.get(0), k -> new PackageTrie())
                    .insert(segs.subList(1, segs.size()), e);
        }

        int moduleCount() {
            int n = entries.size();
            for (PackageTrie c : children.values()) n += c.moduleCount();
            return n;
        }

        /** This trie's children (package nodes, then module leaves) at {@code level}. */
        List<NormalizedNode> toNodes(TreeLevel level) {
            var out = new java.util.ArrayList<NormalizedNode>();
            TreeLevel deeper = level.below().orElse(level);
            for (var ch : children.entrySet()) {
                String label = ch.getKey();
                PackageTrie t = ch.getValue();
                while (t.entries.isEmpty() && t.children.size() == 1) { // collapse the chain
                    var only = t.children.entrySet().iterator().next();
                    label = label + "." + only.getKey();
                    t = only.getValue();
                }
                int count = t.moduleCount();
                out.add(new NormalizedNode(level,
                        dims(label, count + (count == 1 ? " module" : " modules"), "", "package"),
                        t.toNodes(deeper)));
            }
            for (CrateEntry e : entries) out.add(moduleLeaf(e, level));
            return out;
        }
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
