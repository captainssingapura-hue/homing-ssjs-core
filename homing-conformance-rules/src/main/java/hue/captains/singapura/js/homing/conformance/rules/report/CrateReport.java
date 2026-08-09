package hue.captains.singapura.js.homing.conformance.rules.report;

import java.util.List;

/**
 * RFC 0044 Phase 8 — one crate's entry in the consolidated summary: the crate's
 * metadata (name, the crates it requires, its member modules by class id) plus
 * roll-up counts. The member ids index into the per-module {@link ModuleResult}
 * files.
 *
 * @param crate        the crate name
 * @param requires     names of the crates this one directly requires
 * @param modules      member module class ids (keys to the per-module files)
 * @param moduleCount  number of member modules
 * @param errorCount   ERROR-severity findings across the crate's modules
 * @param warningCount WARNING-severity findings across the crate's modules
 * @param pass         true iff no member module has an ERROR finding
 */
public record CrateReport(String crate, List<String> requires, List<String> modules,
                          int moduleCount, int errorCount, int warningCount, boolean pass) {

    public CrateReport {
        requires = List.copyOf(requires);
        modules  = List.copyOf(modules);
    }
}
