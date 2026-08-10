package hue.captains.singapura.js.homing.conformance.rules.report;

import java.util.List;

/**
 * RFC 0044 Phase 8 — the assembled report, in memory, before it is written: the
 * consolidated {@link ConformanceReport} summary plus the per-module {@link
 * ModuleResult}s it indexes. The writer serializes the summary to one file and
 * each module to its own; the {@code ResultSource} reads them back.
 *
 * @param summary the consolidated summary (per-crate metadata + roll-ups)
 * @param modules every module's full result (findings included)
 */
public record ConformanceRun(ConformanceReport summary, List<ModuleResult> modules) {

    public ConformanceRun {
        modules = List.copyOf(modules);
    }
}
