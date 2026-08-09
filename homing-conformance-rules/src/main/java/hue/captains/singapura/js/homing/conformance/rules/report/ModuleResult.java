package hue.captains.singapura.js.homing.conformance.rules.report;

import hue.captains.singapura.js.homing.core.JsModuleType;

import java.util.List;

/**
 * RFC 0044 Phase 8 — the per-module result: the file written for one served
 * module (named by its class id). Carries the module's declared/derived type,
 * the rule set it was held to, its pass/fail, and its graded findings.
 *
 * @param moduleClass the module's fully-qualified class name (the file key)
 * @param type        the classification the policy dispatched on
 * @param ruleSet     the rule-set id applied (e.g. "consumer", "pure-logic")
 * @param pass        true iff no ERROR-severity findings
 * @param findings    every graded finding on this module (warnings included)
 */
public record ModuleResult(String moduleClass, JsModuleType type, String ruleSet,
                           boolean pass, List<FindingReport> findings) {

    public ModuleResult {
        findings = List.copyOf(findings);
    }
}
