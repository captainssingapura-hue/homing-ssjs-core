package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0044 Phase 8 — the served ES module holding the polyglot codec's <b>JS
 * target</b>. Its body is generated at build by {@link ReportJsCodecGen} (the
 * ECMA generators over the shared {@code ReportCodecManifest}) into {@code
 * target/classes/homing/js/…/ReportCodecsModule.js} — one JS class + codec per
 * report record. The conformance report widget dynamic-imports this module to
 * decode the exported report client-side into typed instances.
 *
 * <p>Exports mirror the generated JS names (record class + {@code <name>Codec}),
 * exactly as {@code WorkspaceStateCodecsModule} does for the workspace codecs —
 * the framework appends the {@code export} statement from this list.</p>
 */
public record ReportCodecsModule() implements DomModule<ReportCodecsModule> {

    public static final ReportCodecsModule INSTANCE = new ReportCodecsModule();

    // Typed classes.
    public record ConformanceReport() implements Exportable._Class<ReportCodecsModule> {}
    public record CrateReport()       implements Exportable._Class<ReportCodecsModule> {}
    public record ModuleResult()      implements Exportable._Class<ReportCodecsModule> {}
    public record FindingReport()     implements Exportable._Class<ReportCodecsModule> {}
    public record RuleSetReport()     implements Exportable._Class<ReportCodecsModule> {}
    public record RuleReport()        implements Exportable._Class<ReportCodecsModule> {}
    // Codecs — one per class.
    public record ConformanceReportCodec() implements Exportable._Class<ReportCodecsModule> {}
    public record CrateReportCodec()       implements Exportable._Class<ReportCodecsModule> {}
    public record ModuleResultCodec()      implements Exportable._Class<ReportCodecsModule> {}
    public record FindingReportCodec()     implements Exportable._Class<ReportCodecsModule> {}
    public record RuleSetReportCodec()     implements Exportable._Class<ReportCodecsModule> {}
    public record RuleReportCodec()        implements Exportable._Class<ReportCodecsModule> {}

    @Override
    public ImportsFor<ReportCodecsModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<ReportCodecsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new ConformanceReport(), new ConformanceReportCodec(),
                new CrateReport(),       new CrateReportCodec(),
                new ModuleResult(),      new ModuleResultCodec(),
                new FindingReport(),     new FindingReportCodec(),
                new RuleSetReport(),     new RuleSetReportCodec(),
                new RuleReport(),        new RuleReportCodec()));
    }
}
