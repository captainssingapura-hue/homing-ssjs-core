package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;

/**
 * The polymorphism, as a value: which {@link JsRuleSet} applies to each {@link
 * JsModuleType}. This is <b>the</b> framework opinion — a fixed, exhaustive
 * policy, not a downstream configuration surface (RFC 0044). The studio renders
 * it by iterating {@link JsModuleType#values()} and calling {@link #rulesFor}.
 *
 * <p>Implementations are Functional Objects (a stateless singleton). A downstream
 * with genuinely different needs authors its <i>own</i> policy from the same
 * constructs and runs it alongside — never a patch to the core one.</p>
 */
public interface JsRulePolicy {

    /**
     * The rule set a module of {@code type} is held to. Must be total — return a
     * (possibly {@link JsRuleSet#empty empty}) set for every type, never null.
     */
    JsRuleSet rulesFor(JsModuleType type);
}
