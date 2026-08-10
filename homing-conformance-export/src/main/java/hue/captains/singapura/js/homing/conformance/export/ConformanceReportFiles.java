package hue.captains.singapura.js.homing.conformance.export;

/**
 * RFC 0044 Phase 8 — the on-disk layout of an exported report (D5), shared by
 * the {@link ConformanceReportWriter} and {@link ConformanceReportSource} so the
 * two never disagree. One consolidated summary file, and one file per module
 * under {@code modules/}, named by the module's class id.
 */
public final class ConformanceReportFiles {

    private ConformanceReportFiles() {}

    /** The consolidated summary file (ConformanceReport). */
    public static final String SUMMARY = "report.json";

    /** The directory of per-module result files (ModuleResult, one per module). */
    public static final String MODULES_DIR = "modules";

    /** The per-module file name for a given class id. */
    public static String moduleFile(String moduleClass) {
        return moduleClass + ".json";
    }
}
