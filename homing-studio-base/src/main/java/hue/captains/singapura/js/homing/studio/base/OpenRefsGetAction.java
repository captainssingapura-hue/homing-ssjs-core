package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.tree.ForestPathResolver;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /open-refs?treeId=<id>&l0=<i>&l1=<i>&…} — the breadcrumb chain
 * for a leveled-Open page, keyed by the same tree path as
 * {@link OpenContentGetAction}. Returns the {@code /doc-refs}-shaped JSON
 * (<code>{title, url, breadcrumbs, references}</code>) the {@code StandardMPA}
 * chrome already consumes (plus the doc's own {@code url} for the redirect
 * widget), so the opened-doc shell renders its breadcrumb
 * directly from the path — URL and breadcrumb finally share one source of
 * truth (RFC 0040). No uuid round-trip: the resolved node hands back its
 * ancestor catalogue labels in one walk.
 *
 * <p>{@code title} is the resolved doc's title (the chrome appends it as the
 * final, no-link crumb); {@code breadcrumbs} are the ancestor catalogue
 * labels along the path. An unresolvable path returns the empty shape so the
 * chrome degrades to a bare title rather than erroring.</p>
 *
 * @since homing-studio-base — RFC 0040 leveled Open
 */
public final class OpenRefsGetAction
        implements GetAction<RoutingContext, OpenRefsGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    private static final int MAX_LEVELS = 32;

    private static final String EMPTY =
            "{\"title\":null,\"url\":null,\"breadcrumbs\":[],\"references\":[]}";

    /** @param path the leveled child-index path ({@code [l0, l1, …]}). */
    public record Query(List<Integer> path) implements Param._QueryString {}

    private final Catalogue<?> root;

    public OpenRefsGetAction(Catalogue<?> root) {
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
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        List<Integer> path = (query == null) ? List.of() : query.path();
        Optional<ForestPathResolver.Resolved> resolved =
                ForestPathResolver.INSTANCE.resolve(root, path);
        if (resolved.isEmpty()) {
            return CompletableFuture.completedFuture(json(EMPTY));
        }
        Doc doc = resolved.get().doc();
        var sb = new StringBuilder("{\"title\":");
        sb.append(jsonString(doc.title()));
        // The doc's own canonical url() — used by the SingleWidgetWorkspace's
        // redirect widget to client-redirect kinds it doesn't render itself
        // (markdown → DocReader, plan → plan viewer, app → app launch), so the
        // legacy server-side /open redirect can be retired entirely.
        sb.append(",\"url\":");
        sb.append(jsonString(doc.url()));
        sb.append(",\"breadcrumbs\":[");
        boolean first = true;
        for (var crumb : resolved.get().trail()) {
            if (!first) sb.append(',');
            first = false;
            // Each ancestor crumb carries its catalogue browse URL, so the chrome
            // renders it as a clickable link (the leaf title is appended no-link).
            sb.append("{\"text\":").append(jsonString(crumb.text()))
              .append(",\"href\":").append(jsonString(crumb.href())).append('}');
        }
        sb.append("],\"references\":[]}");
        return CompletableFuture.completedFuture(json(sb.toString()));
    }

    private static DocContent json(String body) {
        return new DocContent(body, "application/json; charset=utf-8");
    }

    private static String jsonString(String s) {
        if (s == null) return "null";
        var sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }
}
