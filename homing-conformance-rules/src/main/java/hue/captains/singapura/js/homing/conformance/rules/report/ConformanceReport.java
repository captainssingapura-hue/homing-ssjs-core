package hue.captains.singapura.js.homing.conformance.rules.report;

import java.util.List;

/**
 * RFC 0044 Phase 8 — the consolidated summary file: the top of the exported
 * report. Records the run's grading mode and roll-up totals, and indexes the
 * crates (each {@link CrateReport} carrying its members' class ids, which key
 * into the per-module {@link ModuleResult} files). The studio's ResultSource
 * loads this, then lazy-loads a module's file by class id on demand.
 *
 * @param schemaVersion    the report schema version (so the codec can evolve)
 * @param allowPreExisting the grading switch in force for this run
 * @param baselineSize     number of grandfathered pre-existing violations
 * @param moduleCount      total modules across all crates
 * @param errorCount       total ERROR findings
 * @param warningCount     total WARNING findings
 * @param crates           per-crate summaries + metadata
 * @param ruleSets         the distinct rule sets applied in this run (shared
 *                         reference data; a module names its set via its
 *                         {@code ruleSet} id, the studio joins to show the rules)
 */
public record ConformanceReport(String schemaVersion, boolean allowPreExisting, int baselineSize,
                                int moduleCount, int errorCount, int warningCount,
                                List<CrateReport> crates, List<RuleSetReport> ruleSets) {

    /** The current report schema version. */
    public static final String SCHEMA_VERSION = "1";

    public ConformanceReport {
        crates = List.copyOf(crates);
        ruleSets = List.copyOf(ruleSets);
    }

    /** True iff no module in any crate has an ERROR finding. */
    public boolean pass() {
        return errorCount == 0;
    }
}
