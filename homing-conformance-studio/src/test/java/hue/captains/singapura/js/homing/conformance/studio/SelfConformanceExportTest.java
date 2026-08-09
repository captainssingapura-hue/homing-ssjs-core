package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.export.ConformanceReportSource;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;
import hue.captains.singapura.js.homing.conformance.rules.report.ModuleResult;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0044 Phase 8 — proves the <b>build-exported</b> report (written by {@link
 * SelfConformanceExport} at process-classes into {@code target/classes/
 * conformance-report}) reads back through {@link ConformanceReportSource} and is
 * identical to a fresh in-process assemble. The decoupled read path (studio reads
 * files) agrees with the enforce path (engine assembles) — the whole point of the
 * export.
 */
class SelfConformanceExportTest {

    @Test
    void exportedReportReadsBackAndMatchesInProcessAssemble() throws Exception {
        URL url = getClass().getResource("/conformance-report/report.json");
        assertNotNull(url, "the export must run at process-classes before tests");
        Path dir = Path.of(url.toURI()).getParent();

        var source = new ConformanceReportSource(dir);
        assertTrue(source.exists());

        ConformanceRun fresh = new ConformanceEngine()
                .assemble(HomingConformance.closure(), HomingConformance.grader(true));

        assertEquals(fresh.summary(), source.summary(),
                "the exported summary must equal a fresh in-process assemble");

        ModuleResult freshModule = fresh.modules().stream()
                .filter(m -> m.moduleClass().endsWith("LayoutSecretaryModule"))
                .findFirst().orElseThrow();
        assertEquals(freshModule, source.module(freshModule.moduleClass()).orElseThrow(),
                "a per-module file must read back identically from disk");
    }
}
