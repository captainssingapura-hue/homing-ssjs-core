package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.export.ConformanceReportSource;
import hue.captains.singapura.js.homing.conformance.report.codec.ConformanceReportCodec;
import hue.captains.singapura.js.homing.conformance.report.codec.ModuleResultCodec;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceReport;
import hue.captains.singapura.js.homing.conformance.rules.report.CrateReport;
import hue.captains.singapura.js.homing.conformance.rules.report.ModuleResult;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0044 Phase 8 — serves the whole BUILD-EXPORTED report as one JSON payload
 * ({@code {summary, modules:[…]}}), re-encoded through the Java codec so the wire
 * form is exactly what the polyglot JS codec decodes. The {@link
 * ConformanceReportWidget} fetches this, decodes it client-side via the served
 * {@code ReportCodecsModule}, and renders it polymorphically by module type.
 */
public final class ConformanceReportGetAction
        implements GetAction<RoutingContext, ConformanceReportGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query() implements Param._QueryString {}

    private volatile String cached;

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query();
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
                    "conformance-report", "Failed to read the exported report: " + e.getMessage()));
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
        ConformanceReportSource source = exportedReport();
        ConformanceReport summary = source.summary();

        var modules = new StringBuilder("[");
        boolean first = true;
        for (CrateReport crate : summary.crates()) {
            for (String moduleClass : crate.modules()) {
                ModuleResult m = source.module(moduleClass).orElse(null);
                if (m == null) continue;
                if (!first) modules.append(',');
                first = false;
                modules.append(ModuleResultCodec.INSTANCE.transformTo(m));
            }
        }
        modules.append(']');

        return "{\"summary\":" + ConformanceReportCodec.INSTANCE.transformTo(summary)
                + ",\"modules\":" + modules + "}";
    }

    /** Locate the build-exported report on the classpath (target/classes/conformance-report). */
    private static ConformanceReportSource exportedReport() {
        URL url = ConformanceReportGetAction.class.getResource("/conformance-report/report.json");
        if (url == null) {
            throw new IllegalStateException(
                    "no exported conformance report on the classpath — the build export must run first");
        }
        try {
            return new ConformanceReportSource(Path.of(url.toURI()).getParent());
        } catch (java.net.URISyntaxException e) {
            throw new IllegalStateException("bad conformance-report resource URL: " + url, e);
        }
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason));
    }
}
