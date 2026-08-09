package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.export.ConformanceReportWriter;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * RFC 0044 Phase 8 — build-time entry point: assemble homing-ssjs-core's own
 * conformance report (the whole crate closure, graded by the shared {@link
 * HomingConformance} config) and write it to the output directory (arg 0) in the
 * D5 layout. Invoked by the Maven {@code exec} plugin at {@code process-classes}
 * — so a report is produced on every build, whatever the test phase does — and
 * the studio's {@code ConformanceReportSource} reads it back.
 *
 * <p>Exported with {@code allowPreExisting = true}: the report reflects the
 * default warn-mode, but every finding carries its {@code Disposition}
 * (allowed / pre-existing / new), so the studio can still distinguish debt from
 * fresh violations.</p>
 */
public final class SelfConformanceExport {

    private SelfConformanceExport() {}

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: SelfConformanceExport <output-directory>");
        }
        Path dir = Paths.get(args[0]);

        ConformanceRun run = new ConformanceEngine().assemble(
                HomingConformance.closure(), HomingConformance.grader(true));
        new ConformanceReportWriter().write(dir, run);

        System.out.println("[SelfConformanceExport] wrote report to " + dir
                + " (" + run.modules().size() + " modules, "
                + run.summary().errorCount() + " errors, "
                + run.summary().warningCount() + " warnings)");
    }
}
