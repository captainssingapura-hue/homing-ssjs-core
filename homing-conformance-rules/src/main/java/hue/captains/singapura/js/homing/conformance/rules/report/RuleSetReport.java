package hue.captains.singapura.js.homing.conformance.rules.report;

import java.util.List;

/**
 * RFC 0044 — a rule set in exportable form: its {@code id} + {@code title} and
 * the {@link RuleReport}s it applies. Carried once per distinct rule set in the
 * {@link ConformanceReport} summary (shared reference data, not repeated on
 * every module); a {@link ModuleResult} names its set via {@code ruleSet}, and
 * the studio joins the two to show — foldably — the rules a module is held to.
 *
 * @param id    the rule-set id (RuleSetId value; matches {@link ModuleResult#ruleSet()})
 * @param title the human title (e.g. "Consumer", "Game loop")
 * @param rules the rules the set applies, in order
 */
public record RuleSetReport(String id, String title, List<RuleReport> rules) {

    public RuleSetReport {
        rules = List.copyOf(rules);
    }
}
