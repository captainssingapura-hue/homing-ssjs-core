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
 * Three levels: root → crate (L1) → module leaf (L2). A module leaf's
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
        List<NormalizedNode> leaves = crate.entries().stream().map(this::moduleLeaf).toList();
        String reqs = crate.requires().isEmpty() ? "no requires"
                : "requires " + String.join(", ", crate.requires().stream().map(Crate::name).toList());
        String summary = crate.entries().size() + " modules · " + reqs;
        return new NormalizedNode(TreeLevel.L1.INSTANCE, dims(crate.name(), summary, "", "crate"), leaves);
    }

    private NormalizedNode moduleLeaf(CrateEntry e) {
        // kind = the mechanical ModuleForm; summary = FQCN (a detail widget reads it).
        String form = e.form().name().toLowerCase().replace('_', '-');
        return NormalizedNode.leaf(TreeLevel.L2.INSTANCE,
                dims(e.module().getClass().getSimpleName(), e.moduleClass(), "", form));
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
