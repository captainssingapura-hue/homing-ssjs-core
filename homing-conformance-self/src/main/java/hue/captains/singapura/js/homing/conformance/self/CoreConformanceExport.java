package hue.captains.singapura.js.homing.conformance.self;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.export.ConformanceReportWriter;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Core's conformance report, exported: run over {@link CoreConformance#closure()} with its grader,
 * written where the studio reads it back - the build's classes, {@code conformance-report/}.
 */
public final class CoreConformanceExport {

    private CoreConformanceExport() {}

    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Usage: CoreConformanceExport <output-directory>");
        Path dir = Paths.get(args[0]);
        ConformanceRun run = new ConformanceEngine().assemble(CoreConformance.closure(), CoreConformance.grader(true));
        new ConformanceReportWriter().write(dir, run);
        System.out.println("[CoreConformanceExport] wrote report to " + dir + " (" + run.modules().size() + " modules, "
                + run.summary().errorCount() + " errors, " + run.summary().warningCount() + " warnings)");
    }
}
