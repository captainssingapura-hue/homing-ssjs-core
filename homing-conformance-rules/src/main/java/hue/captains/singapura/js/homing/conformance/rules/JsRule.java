package hue.captains.singapura.js.homing.conformance.rules;

import java.util.List;

/**
 * One conformance rule — a <b>pure</b> check over a single {@link ServedModule},
 * returning the violations it found (empty means compliant). Rules are typed
 * data (RFC 0044): composable into a {@link JsRuleSet}, unit-testable in
 * isolation against a synthesized module, and self-describing for the studio via
 * {@link #intent()} + {@link #basis()}.
 *
 * <p>Implementations are Functional Objects — a stateless singleton (a {@code
 * record} with an {@code INSTANCE}), never holding per-run state.</p>
 */
public interface JsRule {

    /** Stable typed identity — keys findings back to this rule; addresses it in the studio. */
    RuleId id();

    /** One line stating the invariant this rule defends — the studio shows it verbatim. */
    String intent();

    /** The doctrine this rule enforces — its <i>why</i>. */
    DoctrineRef basis();

    /** Check the module; return every violation ({@code List.of()} when compliant). Must be pure. */
    List<Finding> check(ServedModule module);
}
