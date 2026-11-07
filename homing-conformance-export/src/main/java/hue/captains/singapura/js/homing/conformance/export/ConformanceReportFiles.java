package hue.captains.singapura.js.homing.conformance.export;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * RFC 0044 Phase 8 — the on-disk layout of an exported report (D5), shared by
 * the {@link ConformanceReportWriter} and {@link ConformanceReportSource} so the
 * two never disagree. One consolidated summary file, and one file per module
 * under {@code modules/}, named by the module's class id.
 */
public record ConformanceReportFiles() implements StatelessFunctionalObject {

    public static final ConformanceReportFiles INSTANCE = new ConformanceReportFiles();

    /** The consolidated summary file (ConformanceReport). */
    public static final String SUMMARY = "report.json";

    /** The directory of per-module result files (ModuleResult, one per module). */
    public static final String MODULES_DIR = "modules";

    /** The per-module file name for a given class id. */
    public String moduleFile(String moduleClass) {
        return moduleClass + ".json";
    }
}
