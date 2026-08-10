package hue.captains.singapura.js.homing.conformance.report.codec;

import hue.captains.singapura.js.homing.conformance.rules.Disposition;
import hue.captains.singapura.js.homing.conformance.rules.Severity;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceReport;
import hue.captains.singapura.js.homing.conformance.rules.report.CrateReport;
import hue.captains.singapura.js.homing.conformance.rules.report.FindingReport;
import hue.captains.singapura.js.homing.conformance.rules.report.ModuleResult;
import hue.captains.singapura.js.homing.conformance.rules.report.RuleReport;
import hue.captains.singapura.js.homing.conformance.rules.report.RuleSetReport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Round-trip parity on the GENERATED Java codec — {@code transformFrom(transformTo(x)) == x}
 * for the report model: enums, nested records, and {@code List<record>} /
 * {@code List<String>} all survive a JSON round-trip. This is the correctness
 * discipline the ontology prescribes (in place of drift-guarding committed output).
 */
class ReportCodecRoundTripTest {

    @Test
    void moduleResultWithFindingsRoundTrips() {
        var m = new ModuleResult(
                "hue.demo.Widget", "consumer", "consumer", false,
                List.of(
                        new FindingReport("no-raw-href", "raw href: a.href = x", 42,
                                Severity.WARNING, Disposition.PRE_EXISTING, "pre-existing (baselined)"),
                        new FindingReport("no-cdn-import", "CDN import: https://x", 1,
                                Severity.ERROR, Disposition.NEW, "new violation")));

        String wire = ModuleResultCodec.INSTANCE.transformTo(m);
        ModuleResult back = ModuleResultCodec.INSTANCE.transformFrom(wire);
        assertEquals(m, back, () -> "wire=" + wire);
    }

    @Test
    void conformanceReportWithCratesRoundTrips() {
        var r = new ConformanceReport(
                "1", true, 69, 5, 0, 7,
                List.of(
                        new CrateReport("homing-core", List.of(), List.of("a.B", "a.C"), 2, 0, 1, true),
                        new CrateReport("homing-studio-base", List.of("homing-core"),
                                List.of("s.X"), 1, 0, 6, true)),
                List.of(
                        new RuleSetReport("consumer", "Consumer", List.of(
                                new RuleReport("no-cdn-import", "No CDN imports."),
                                new RuleReport("no-raw-href", "Use the href.* API."))),
                        new RuleSetReport("pure-logic", "Pure logic", List.of(
                                new RuleReport("no-dom-access", "A headless module must not touch the DOM.")))));

        String wire = ConformanceReportCodec.INSTANCE.transformTo(r);
        ConformanceReport back = ConformanceReportCodec.INSTANCE.transformFrom(wire);
        assertEquals(r, back, () -> "wire=" + wire);
    }
}
