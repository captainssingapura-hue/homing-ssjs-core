package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.component.ComponentDetails;
import hue.captains.singapura.js.homing.component.ComponentTrees;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.StampedParams;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.RowDisplaySource;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The components workbench's feed: the catalogue of components the studio's
 * crates deliver, composed by derivation from the crate closure — every
 * vehicle's catalogue grafted under one root — as canonical {@code TreeNode}
 * JSON at {@code GET /component-tree}; with {@code ?path=} the one node's
 * details, for the summary pane: its shape, its tag, its module, the crate
 * that ships it, the words it is reached by.
 */
public final class ComponentTreeGetAction
        implements GetAction<RoutingContext, ComponentTreeGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String path) implements Param._QueryString {}

    private final List<Crate> topLevel;
    private final TreeNodeJsonWriter writer = new TreeNodeJsonWriter();

    public ComponentTreeGetAction(List<Crate> topLevel) {
        this.topLevel = List.copyOf(Objects.requireNonNull(topLevel, "topLevel"));
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("path"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        try {
            var composed = ComponentTrees.compose("components", topLevel);
            if (query.path() != null && !query.path().isBlank()) {
                NormalizedNode node = find(composed.root(), query.path().split("/"), 0);
                if (node == null) return CompletableFuture.failedFuture(notFound("component-tree", "no node at " + query.path()));
                return CompletableFuture.completedFuture(new DocContent(details(node, composed.detailsOf(node.identity())), "application/json; charset=utf-8"));
            }
            RowDisplaySource rows = node -> node instanceof NormalizedNode n && composed.detailsOf(n.identity()) != null
                    ? composed.detailsOf(n.identity()).row() : null;
            return CompletableFuture.completedFuture(new DocContent(writer.write(composed.root(), rows), "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound("component-tree", "Failed to serialise the component tree: " + e.getMessage()));
        }
    }

    /** The node at a name-path below the root — the root's own segment is not part of it, as the renderer addresses. */
    private static NormalizedNode find(NormalizedNode node, String[] segments, int i) {
        if (i == segments.length) return node;
        for (NormalizedNode k : node.children()) if (k.segment().value().equals(segments[i])) return find(k, segments, i + 1);
        return null;
    }

    /** One node's details as JSON, typed by what it is. */
    static String details(NormalizedNode node, ComponentDetails d) {
        var sb = new StringBuilder("{\"segment\":").append(StampedParams.jsString(node.segment().value()))
                .append(",\"level\":").append(StampedParams.jsString(node.level().tag()));
        switch (d) {
            case ComponentDetails.OfComponent c -> sb.append(",\"kind\":\"component\",\"label\":").append(StampedParams.jsString(c.label()))
                    .append(",\"shape\":").append(StampedParams.jsString(c.shape().tag()))
                    .append(",\"tag\":").append(StampedParams.jsString(c.tag()))
                    .append(",\"summary\":").append(StampedParams.jsString(c.summary()))
                    .append(",\"module\":").append(StampedParams.jsString(c.module()))
                    .append(",\"crate\":").append(StampedParams.jsString(c.crate()))
                    .append(",\"path\":").append(StampedParams.jsString(String.join("/", c.path())));
            case ComponentDetails.OfVehicle v -> sb.append(",\"kind\":\"vehicle\",\"label\":").append(StampedParams.jsString(v.name()))
                    .append(",\"summary\":").append(StampedParams.jsString(v.summary()))
                    .append(",\"crate\":").append(StampedParams.jsString(v.crate()))
                    .append(",\"components\":").append(v.componentCount());
            case ComponentDetails.OfCatalogue c -> sb.append(",\"kind\":\"catalogue\",\"label\":").append(StampedParams.jsString(c.name()))
                    .append(",\"summary\":").append(StampedParams.jsString(c.summary()))
                    .append(",\"components\":").append(c.componentCount());
            case ComponentDetails.OfComposition r -> sb.append(",\"kind\":\"composition\",\"label\":").append(StampedParams.jsString(r.name()))
                    .append(",\"vehicles\":").append(r.vehicleCount())
                    .append(",\"components\":").append(r.componentCount());
            case null -> sb.append(",\"kind\":\"unknown\"");
        }
        return sb.append('}').toString();
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason));
    }
}
