package hue.captains.singapura.js.homing.color.studio;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /color-tree} — serialises the ColorGroup taxonomy ({@link ColorTree})
 * as the canonical {@code TreeNode} JSON the generic {@code TreeRenderer}
 * consumes. The Color Studio's {@code ColorTreeWidget} fetches this to draw the
 * colour-group tree; the {@code ColorListWidget} fetches it once and derives the
 * colours of a selected group from the same payload.
 *
 * <p>Stateless: the whole tree is served (no re-rooting). Mirrors
 * {@code CatalogueTreeGetAction}'s response conventions.</p>
 */
public final class ColorTreeGetAction
        implements GetAction<RoutingContext, ColorTreeGetAction.NoQuery, EmptyParam.NoHeaders, DocContent> {

    /** No query parameters — the whole colour tree is always served. */
    public record NoQuery() implements Param._QueryString {}

    private final TreeNodeJsonWriter writer = new TreeNodeJsonWriter();

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
        String body = writer.write(ColorTree.build());
        return CompletableFuture.completedFuture(new DocContent(body, "application/json; charset=utf-8"));
    }
}
