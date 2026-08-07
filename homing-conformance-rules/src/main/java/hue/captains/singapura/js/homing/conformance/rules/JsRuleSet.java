package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A named, composable bundle of {@link JsRule}s — the rules a {@link
 * JsModuleType} is held to. Running the set over a module is just running each
 * rule and concatenating the findings.
 *
 * @param id    stable typed identity, e.g. {@code "consumer"}
 * @param title human-readable title, for the studio
 * @param rules the rules, in order
 */
public record JsRuleSet(RuleSetId id, String title, List<JsRule> rules) {

    /** The empty rule set — nothing to check (e.g. a bundled external). */
    public static JsRuleSet empty(RuleSetId id, String title) {
        return new JsRuleSet(id, title, List.of());
    }

    public JsRuleSet {
        Objects.requireNonNull(id,    "JsRuleSet.id");
        Objects.requireNonNull(title, "JsRuleSet.title");
        Objects.requireNonNull(rules, "JsRuleSet.rules");
        rules = List.copyOf(rules);
    }

    /** Run every rule over {@code module}; return all findings (empty when compliant). */
    public List<Finding> checkAll(ServedModule module) {
        var all = new ArrayList<Finding>();
        for (JsRule rule : rules) {
            all.addAll(rule.check(module));
        }
        return List.copyOf(all);
    }
}
