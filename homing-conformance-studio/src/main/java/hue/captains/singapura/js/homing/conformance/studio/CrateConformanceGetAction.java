package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.CrateConformance;
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
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0044 — the Crate-Studio conformance feed: runs {@link CrateConformance}
 * over the closure and serves the structured result as JSON at
 * {@code GET /crate-conformance}. Both Conformance panes fetch this once; the
 * crate pane reads {@code crates[...]} (aggregate) and the module pane reads
 * {@code modules[...]} (per-module, which also resolves module → crate). The
 * evaluation is memoised — the checks scan the classpath, so there is no reason
 * to redo them per request.
 */
public final class CrateConformanceGetAction
        implements GetAction<RoutingContext, CrateConformanceGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String id) implements Param._QueryString {}

    private final List<Crate> topLevel;
    private volatile String cached;

    public CrateConformanceGetAction(List<Crate> topLevel) {
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
                    new DocContent(json(), "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(
                    "crate-conformance", "Failed to evaluate crate conformance: " + e.getMessage()));
        }
    }

    private String json() {
        String local = cached;
        if (local != null) return local;
        synchronized (this) {
            if (cached == null) cached = serialize(CrateConformance.evaluate(CrateClosure.of(topLevel)));
            return cached;
        }
    }

    private static String serialize(CrateConformance.Result r) {
        var sb = new StringBuilder();
        sb.append("{\"ok\":").append(r.ok());
        sb.append(",\"crates\":{");
        boolean first = true;
        for (var c : r.crates().values()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(jstr(c.name())).append(":{")
              .append("\"name\":").append(jstr(c.name()))
              .append(",\"modules\":").append(c.modules())
              .append(",\"ok\":").append(c.ok())
              .append(",\"orphans\":").append(jarr(c.orphans()))
              .append(",\"illegalImports\":").append(jarr(c.illegalImports()))
              .append('}');
        }
        sb.append("},\"modules\":{");
        first = true;
        for (var m : r.modules().values()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(jstr(m.moduleClass())).append(":{")
              .append("\"moduleClass\":").append(jstr(m.moduleClass()))
              .append(",\"crate\":").append(jstr(m.crate()))
              .append(",\"form\":").append(jstr(m.form()))
              .append(",\"ok\":").append(m.ok())
              .append(",\"findings\":").append(jarr(m.findings()))
              .append('}');
        }
        sb.append("}}");
        return sb.toString();
    }

    private static String jarr(List<String> items) {
        var sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(jstr(items.get(i)));
        }
        return sb.append(']').toString();
    }

    private static String jstr(String s) {
        var sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> { if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch)); else sb.append(ch); }
            }
        }
        return sb.append('"').toString();
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason));
    }
}
