package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.CrateConformance;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0044 — the Crate-Studio conformance feed. Combines the two conformance
 * dimensions for the studio panes: <b>crate integrity</b> ({@link CrateConformance}
 * — orphans + layering) and <b>rule conformance</b> ({@link ConformanceEngine}
 * — the served-artifact rule findings). Served as JSON at
 * {@code GET /crate-conformance}, memoised (both scan/render the whole set).
 * Per-module {@code findings} merge layering + rule findings; the crate is the
 * aggregation point.
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
            if (cached == null) cached = compute();
            return cached;
        }
    }

    private String compute() {
        List<Crate> closure = CrateClosure.of(topLevel);
        CrateConformance.Result base = CrateConformance.evaluate(closure);

        // Engine rule findings, grouped by module class.
        Map<String, List<String>> ruleByModule = new LinkedHashMap<>();
        for (Finding f : new ConformanceEngine().checkCrates(closure)) {
            ruleByModule.computeIfAbsent(f.moduleClass(), k -> new ArrayList<>())
                    .add(f.rule().value() + ": " + f.message());
        }

        var crates = new StringBuilder("{");
        var modules = new StringBuilder("{");
        boolean firstC = true, firstM = true, allOk = true;

        for (Crate c : closure) {
            CrateConformance.CrateResult cr = base.crates().get(c.name());
            var crateRuleFindings = new ArrayList<String>();

            for (CrateEntry entry : c.entries()) {
                String fqcn = entry.moduleClass();
                CrateConformance.ModuleResult mr = base.modules().get(fqcn);
                var findings = new ArrayList<>(mr == null ? List.<String>of() : mr.findings());
                List<String> rules = ruleByModule.getOrDefault(fqcn, List.of());
                findings.addAll(rules);
                crateRuleFindings.addAll(rules);
                boolean mok = findings.isEmpty();

                if (!firstM) modules.append(',');
                firstM = false;
                modules.append(jstr(fqcn)).append(":{")
                       .append("\"moduleClass\":").append(jstr(fqcn))
                       .append(",\"crate\":").append(jstr(c.name()))
                       .append(",\"form\":").append(jstr(mr == null ? "" : mr.form()))
                       .append(",\"ok\":").append(mok)
                       .append(",\"findings\":").append(jarr(findings))
                       .append('}');
            }

            boolean cok = cr != null && cr.ok() && crateRuleFindings.isEmpty();
            allOk &= cok;
            if (!firstC) crates.append(',');
            firstC = false;
            crates.append(jstr(c.name())).append(":{")
                  .append("\"name\":").append(jstr(c.name()))
                  .append(",\"modules\":").append(c.entries().size())
                  .append(",\"ok\":").append(cok)
                  .append(",\"orphans\":").append(jarr(cr == null ? List.of() : cr.orphans()))
                  .append(",\"illegalImports\":").append(jarr(cr == null ? List.of() : cr.illegalImports()))
                  .append(",\"ruleFindings\":").append(jarr(crateRuleFindings))
                  .append('}');
        }
        crates.append('}');
        modules.append('}');
        return "{\"ok\":" + allOk + ",\"crates\":" + crates + ",\"modules\":" + modules + "}";
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
