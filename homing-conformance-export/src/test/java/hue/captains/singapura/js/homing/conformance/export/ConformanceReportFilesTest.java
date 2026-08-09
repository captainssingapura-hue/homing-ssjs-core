package hue.captains.singapura.js.homing.conformance.export;

import hue.captains.singapura.js.homing.conformance.rules.Disposition;
import hue.captains.singapura.js.homing.conformance.rules.Severity;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceReport;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;
import hue.captains.singapura.js.homing.conformance.rules.report.CrateReport;
import hue.captains.singapura.js.homing.conformance.rules.report.FindingReport;
import hue.captains.singapura.js.homing.conformance.rules.report.ModuleResult;
import hue.captains.singapura.js.homing.core.JsModuleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 8 proof — the export layout (D5) round-trips through files: an assembled
 * {@link ConformanceRun} written by {@link ConformanceReportWriter} reads back
 * identically through {@link ConformanceReportSource}, summary and per-module
 * files alike, via the generated codec.
 */
class ConformanceReportFilesTest {

    private static ConformanceRun sampleRun() {
        var module = new ModuleResult(
                "hue.demo.Widget", JsModuleType.CONSUMER, "consumer", false,
                List.of(new FindingReport("no-raw-href", "raw href: a.href = x", 42,
                        Severity.WARNING, Disposition.PRE_EXISTING, "pre-existing (baselined)")));
        var summary = new ConformanceReport("1", true, 69, 1, 0, 1, List.of(
                new CrateReport("homing-demo", List.of("homing-core"),
                        List.of("hue.demo.Widget"), 1, 0, 1, true)));
        return new ConformanceRun(summary, List.of(module));
    }

    @Test
    void summaryAndModulesRoundTripThroughFiles(@TempDir Path dir) throws IOException {
        ConformanceRun run = sampleRun();

        new ConformanceReportWriter().write(dir, run);
        var source = new ConformanceReportSource(dir);

        assertTrue(source.exists());
        assertEquals(run.summary(), source.summary());
        assertEquals(run.modules().get(0), source.module("hue.demo.Widget").orElseThrow());
        assertTrue(source.module("does.not.Exist").isEmpty());
    }
}
