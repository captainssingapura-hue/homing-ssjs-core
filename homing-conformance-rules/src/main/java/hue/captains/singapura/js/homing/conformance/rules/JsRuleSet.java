package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.tao.ontology.Immutable;

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
public record JsRuleSet(RuleSetId id, String title, List<JsRule> rules) implements Immutable {

    /** The empty rule set — nothing to check (e.g. a bundled external). */
    public JsRuleSet(RuleSetId id, String title) {
        this(id, title, List.of());
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

    /** True iff {@code set} sits in a lane: global rules + (DOM-owner discipline | no-DOM). */
    public boolean inLane() {
                if (!rules.containsAll(DefaultJsRulePolicy.INSTANCE.global())) return false;
        return rules.containsAll(DefaultJsRulePolicy.INSTANCE.domOwnerDiscipline())
                || rules.contains(NoDomAccessRule.INSTANCE);
    }

    /** Refuses a set that sits in no lane, as an extension for {@code type}. */
    void requireLane(JsModuleType type) {
        if (inLane()) return;
        throw new IllegalArgumentException(
                "extension rule set '" + id.value() + "' for type '" + type.slug()
                        + "' sits in no lane. An extension type extends the framework's discipline, it does"
                        + " not opt out of it: carry the global rules plus either the full DOM-owner discipline"
                        + " (DefaultJsRulePolicy.INSTANCE.domOwnerDiscipline() — the module builds or styles DOM) or"
                        + " NoDomAccessRule (the module is headless). DefaultJsRulePolicy.domOwnerLane(...) /"
                        + " headlessLane(...) build one.");
    }
}
