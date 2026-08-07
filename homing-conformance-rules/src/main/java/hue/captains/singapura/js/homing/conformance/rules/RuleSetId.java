package hue.captains.singapura.js.homing.conformance.rules;

import java.util.Objects;

/**
 * Typed identity of a {@link JsRuleSet} — a stable, URL-safe slug (Names Are
 * Types). Names the bundle of rules a {@link JsModuleType} is held to.
 *
 * @param value a non-blank kebab-case slug, e.g. {@code "consumer"}
 */
public record RuleSetId(String value) {
    public RuleSetId {
        Objects.requireNonNull(value, "RuleSetId.value");
        if (value.isBlank()) throw new IllegalArgumentException("RuleSetId.value must be non-blank");
    }
    @Override public String toString() { return value; }
}
