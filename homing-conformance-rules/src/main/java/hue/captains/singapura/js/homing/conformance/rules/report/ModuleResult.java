package hue.captains.singapura.js.homing.conformance.rules.report;

import java.util.List;

/**
 * RFC 0044 Phase 8 — the per-module result: the file written for one served
 * module (named by its class id). A report is <b>data</b>, so the module's
 * classification is captured as its {@code type} <b>slug</b> (a stable string),
 * not the live {@code JsModuleType} — that keeps it serializable across an open,
 * downstream-extensible type set, and is exactly what the studio groups by.
 *
 * @param moduleClass the module's fully-qualified class name (the file key)
 * @param type        the classification's slug (e.g. "consumer", "pure-logic", or a downstream type's slug)
 * @param ruleSet     the rule-set id applied (e.g. "consumer", "pure-logic")
 * @param pass        true iff no ERROR-severity findings
 * @param findings    every graded finding on this module (warnings included)
 */
public record ModuleResult(String moduleClass, String type, String ruleSet,
                           boolean pass, List<FindingReport> findings) {

    public ModuleResult {
        findings = List.copyOf(findings);
    }
}
