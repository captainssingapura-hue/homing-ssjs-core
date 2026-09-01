package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.tree.NodeIdentity;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import hue.captains.singapura.js.homing.tree.RowDisplaySource;
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
            // RFC 0053 — structure and answers from one walk, as the catalogue does.
            var details = new LinkedHashMap<NodeIdentity, CrateDetails>();
            NormalizedNode root = rootNode(details);
            RowDisplaySource rows = node -> node instanceof NormalizedNode n
                    ? (details.get(n.identity()) == null ? null : details.get(n.identity()).row())
                    : null;
            return CompletableFuture.completedFuture(
                    new DocContent(writer.write(root, rows), "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(
                    "crate-tree", "Failed to serialise crate tree: " + e.getMessage()));
        }
    }

    private NormalizedNode rootNode(Map<NodeIdentity, CrateDetails> details) {
        List<NormalizedNode> crates = topLevel.stream().map(c -> crateNode(c, details)).toList();
        int modules = topLevel.stream().mapToInt(c -> c.entries().size()).sum();
        details.put(CrateNodeIdentity.root(),
                new CrateDetails.OfWorkspace(topLevel.size(), modules));
        return new NormalizedNode(TreeLevel.L0.INSTANCE, new NodeName("crates"),
                CrateNodeIdentity.root(), Map.of(), crates);
    }

    private NormalizedNode crateNode(Crate crate, Map<NodeIdentity, CrateDetails> details) {
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

        List<NormalizedNode> children = trie.toNodes(TreeLevel.L2.INSTANCE, crate.name(), "", details);
        details.put(CrateNodeIdentity.ofCrate(crate.name()), new CrateDetails.OfCrate(
                crate.name(), crate.entries().size(), prefix.toString(),
                crate.requires().stream().map(Crate::name).toList()));
        return new NormalizedNode(TreeLevel.L1.INSTANCE, NodeName.slug(crate.name()),
                CrateNodeIdentity.ofCrate(crate.name()), Map.of(), children);
    }

    private NormalizedNode moduleLeaf(CrateEntry e, TreeLevel level,
                                      Map<NodeIdentity, CrateDetails> details) {
        // The form stays an enum and the FQCN gets its own name. It used to travel
        // as `summary`, which is what four widgets still read it out of.
        details.put(CrateNodeIdentity.ofModule(e.moduleClass()), new CrateDetails.OfModule(
                e.module().getClass().getSimpleName(), e.form(), e.moduleClass()));
        return NormalizedNode.leaf(level, NodeName.slug(e.module().getClass().getSimpleName()),
                CrateNodeIdentity.ofModule(e.moduleClass()), Map.of());
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
        List<NormalizedNode> toNodes(TreeLevel level, String crate, String prefix,
                                     Map<NodeIdentity, CrateDetails> details) {
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
                String pkgPath = prefix.isEmpty() ? label : prefix + "." + label;
                details.put(CrateNodeIdentity.ofPackage(crate, pkgPath),
                        new CrateDetails.OfPackage(label, count));
                out.add(new NormalizedNode(level, NodeName.slug(label),
                        CrateNodeIdentity.ofPackage(crate, pkgPath), Map.of(),
                        t.toNodes(deeper, crate, pkgPath, details)));
            }
            for (CrateEntry e : entries) out.add(moduleLeaf(e, level, details));
            return out;
        }
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason));
    }
}
