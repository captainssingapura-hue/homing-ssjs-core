package hue.captains.singapura.js.homing.conformance.rules;

import java.util.Objects;

/**
 * Typed identity of a {@link JsRule} — a stable, URL-safe slug, never a raw
 * {@code String} (Names Are Types). Used to key findings back to the rule that
 * produced them and to address a rule in the studio.
 *
 * @param value a non-blank kebab-case slug, e.g. {@code "no-cdn-import"}
 */
public record RuleId(String value) {
    public RuleId {
        Objects.requireNonNull(value, "RuleId.value");
        if (value.isBlank()) throw new IllegalArgumentException("RuleId.value must be non-blank");
    }
    @Override public String toString() { return value; }
}
