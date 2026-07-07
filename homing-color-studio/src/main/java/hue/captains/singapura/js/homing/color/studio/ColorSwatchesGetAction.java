package hue.captains.singapura.js.homing.color.studio;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /color-swatches} — the colour vocabulary's physical realisation as
 * a flat {@code name → CSS colour} JSON object, e.g.
 * {@code {"surface":"var(--color-surface)","danger":"#D64545", …}}, produced by
 * {@link ColorSwatchResolver}.
 *
 * <p>Served as a side table rather than baked into {@code /color-tree}: the tree
 * substrate's {@code DimensionKey} vocabulary is deliberately closed (colour is
 * node decoration, not a grouping axis), so the {@code ColorListWidget} fetches
 * this once and joins swatches to tree nodes by name. Structural/identity values
 * are CSS var references, so painted swatches follow the active theme with no
 * per-theme plumbing; affective/symbolic values are anchor hexes.</p>
 */
public final class ColorSwatchesGetAction
        implements GetAction<RoutingContext, ColorSwatchesGetAction.NoQuery, EmptyParam.NoHeaders, DocContent> {

    /** No query parameters — the whole swatch table is always served. */
    public record NoQuery() implements Param._QueryString {}

    @Override
    public ParamMarshaller._QueryString<RoutingContext, NoQuery> queryStrMarshaller() {
        return ctx -> new NoQuery();
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(NoQuery query, EmptyParam.NoHeaders headers) {
        Map<String, String> swatches = ColorSwatchResolver.INSTANCE.swatchesByName();
        var sb = new StringBuilder("{");
        boolean first = true;
        for (var e : swatches.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escape(e.getKey())).append("\":\"")
              .append(escape(e.getValue())).append('"');
        }
        sb.append('}');
        return CompletableFuture.completedFuture(
                new DocContent(sb.toString(), "application/json; charset=utf-8"));
    }

    /** Minimal JSON string escaping — names and CSS colour strings only. */
    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
