package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * RFC 0044 — the Crate-Studio dependency-graph feed: serves the crate graph as
 * Mermaid {@code flowchart} text at {@code GET /crate-graph}. The graph is the
 * full {@code requires} closure of the top-level crates (so external
 * dependency crates appear too); {@link CrateGraphWidget} fetches this text and
 * renders it via the Mermaid proxy.
 */
public final class CrateGraphGetAction
        implements GetAction<RoutingContext, CrateGraphGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String id) implements Param._QueryString {}

    private final List<Crate> topLevel;

    public CrateGraphGetAction(List<Crate> topLevel) {
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
            List<Crate> closure = CrateClosure.of(topLevel);
            Set<String> owned = topLevel.stream().map(Crate::name).collect(Collectors.toSet());
            String mermaid = CrateGraphMermaid.render(closure, owned);
            return CompletableFuture.completedFuture(
                    new DocContent(mermaid, "text/plain; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(
                    "crate-graph", "Failed to render crate graph: " + e.getMessage()));
        }
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason));
    }
}
