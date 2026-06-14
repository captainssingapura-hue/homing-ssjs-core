package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /open?l0=<i>&l1=<i>&…} — resolves a node's <b>leveled tree path</b>
 * (its structural position, a sequence of child indices from the root) to the
 * target doc's own authoritative viewer URL ({@code Doc.url()}, per-kind) and
 * redirects there in regular MPA mode.
 *
 * <p>RFC 0040: the Navigator carries <i>no stamped node id</i> — the client
 * derives each node's path from the rendered tree's structure and encodes it as
 * {@code l0,l1,…}. This action walks the same forest by that path and returns
 * the doc's url(), so identity is positional and the resolver follows exactly
 * the tree the Navigator shows (including {@code OfStudio} portals, descended
 * here as the forest does). No uuid index to keep in sync.</p>
 *
 * <p>Child ordering matches every tree builder (the legacy
 * {@code CatalogueTreeAdapter} and the {@code CatalogueNormalizer}):
 * sub-catalogues first, then {@code OfDoc} / {@code OfStudio} leaves in order;
 * {@code OfIllustration} is skipped. A {@code treeId} query parameter is
 * reserved for future multi-tree addressing.</p>
 *
 * <p>Redirect mechanism mirrors {@code RootRedirectGetAction}: an HTML
 * meta-refresh + {@code location.replace} page. An unresolvable path → {@code /}.</p>
 *
 * @since homing-studio-workspace — Studio Workspace, leaf "Open" action
 */
public final class OpenDocGetAction
        implements GetAction<RoutingContext, OpenDocGetAction.Query, EmptyParam.NoHeaders, HtmlPageContent> {

    /** Defensive cap on path depth (mirrors {@code DocGetAction}'s MAX_LEVELS). */
    private static final int MAX_LEVELS = 32;

    /** @param path the leveled child-index path ({@code [l0, l1, …]}). */
    public record Query(List<Integer> path) implements Param._QueryString {}

    private final Catalogue<?> root;

    public OpenDocGetAction(Catalogue<?> root) {
        this.root = Objects.requireNonNull(root, "root catalogue");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> {
            var path = new ArrayList<Integer>();
            for (int n = 0; n < MAX_LEVELS; n++) {
                String v = ctx.request().getParam("l" + n);
                if (v == null) break;
                try { path.add(Integer.parseInt(v)); }
                catch (NumberFormatException e) { break; }
            }
            return new Query(path);
        };
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<HtmlPageContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String target = (query == null) ? null : resolve(root, query.path());
        if (target == null) target = "/";   // unresolvable path → home
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta http-equiv="refresh" content="0;url=%s">
                    <title>Opening…</title>
                </head>
                <body>
                    <script>window.location.replace(%s);</script>
                </body>
                </html>
                """.formatted(htmlAttrEscape(target), "\"" + jsStringEscape(target) + "\"");
        return CompletableFuture.completedFuture(new HtmlPageContent(html));
    }

    // ── leveled path resolution ──────────────────────────────────────────────

    /** Walk the forest by the child-index path to a doc's {@code url()}, or
     *  {@code null} if the path is out of range or doesn't land on a doc.
     *  Package-private for testing. */
    static String resolve(Catalogue<?> root, List<Integer> path) {
        if (path == null || path.isEmpty()) return null;
        Catalogue<?> cat = root;
        for (int i = 0; i < path.size(); i++) {
            List<NavChild> children = orderedNavChildren(cat);
            int idx = path.get(i);
            if (idx < 0 || idx >= children.size()) return null;
            NavChild child = children.get(idx);
            boolean last = (i == path.size() - 1);
            switch (child) {
                case NavSub ns    -> cat = ns.catalogue();          // descend a sub-catalogue
                case NavPortal np -> cat = np.source();             // descend an OfStudio portal
                case NavDoc nd    -> { return last ? nd.doc().url() : null; }
            }
        }
        return null;   // path ended on a branch (catalogue/portal) — no doc page
    }

    /** The nav children of a catalogue, in the exact order every tree builder
     *  emits them: sub-catalogues, then OfDoc/OfStudio leaves. */
    private static List<NavChild> orderedNavChildren(Catalogue<?> cat) {
        var out = new ArrayList<NavChild>();
        for (Catalogue<?> sub : cat.subCatalogues()) out.add(new NavSub(sub));
        for (Entry<?> entry : cat.leaves()) {
            if (entry instanceof Entry.OfDoc<?, ?> od) {
                out.add(new NavDoc(od.doc()));
            } else if (entry instanceof Entry.OfStudio<?, ?> os) {
                out.add(new NavPortal(os.proxy().source()));
            }
            // OfIllustration — skipped, matching the tree builders.
        }
        return out;
    }

    private sealed interface NavChild permits NavSub, NavDoc, NavPortal {}
    private record NavSub(Catalogue<?> catalogue) implements NavChild {}
    private record NavDoc(Doc doc) implements NavChild {}
    private record NavPortal(Catalogue<?> source) implements NavChild {}

    private static String htmlAttrEscape(String s) {
        return s.replace("&", "&amp;").replace("\"", "&quot;");
    }

    private static String jsStringEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
