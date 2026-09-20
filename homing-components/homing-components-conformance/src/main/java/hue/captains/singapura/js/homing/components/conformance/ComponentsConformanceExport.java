package hue.captains.singapura.js.homing.components.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.export.ConformanceReportWriter;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Build-time entry point: assemble the components' conformance report and
 * write it (arg 0) in the studio's layout, read back at runtime by the
 * served studio. Run by the Maven {@code exec} plugin at
 * {@code process-classes}, so a report is produced on every build. Exported
 * warn-mode ({@code allowPreExisting = true}) so every finding keeps its
 * disposition for the studio to show; the gate is the test.
 */
public final class ComponentsConformanceExport {

    private ComponentsConformanceExport() {}

    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Usage: ComponentsConformanceExport <output-directory>");
        Path dir = Paths.get(args[0]);
        ConformanceRun run = new ConformanceEngine(ComponentsConformance.POLICY, new ServedModuleRenderer())
                .assemble(ComponentsConformance.TOP_LEVEL, ComponentsConformance.grader(true));
        new ConformanceReportWriter().write(dir, run);
        System.out.println("[ComponentsConformanceExport] wrote report to " + dir
                + " (" + run.modules().size() + " modules, "
                + run.summary().errorCount() + " errors, "
                + run.summary().warningCount() + " warnings)");
    }
}
