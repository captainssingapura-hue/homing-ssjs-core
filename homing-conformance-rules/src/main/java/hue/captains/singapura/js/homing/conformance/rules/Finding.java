package hue.captains.singapura.js.homing.conformance.rules;

import java.util.Objects;

/**
 * One violation — the actionable unit of a conformance failure. Names the
 * module, the rule that fired, a human message, and the region + line it sits
 * in. Aggregated into the build report and mapped onto modules in the studio.
 *
 * @param moduleClass the fully-qualified class name of the offending module
 * @param rule        the rule that produced this finding
 * @param message     a human-readable description of the violation
 * @param region      which served segment the violation is in
 * @param line        0-based line index within that region, or {@code -1} if not line-anchored
 */
public record Finding(String moduleClass, RuleId rule, String message, JsRegion region, int line) {

    public Finding {
        Objects.requireNonNull(moduleClass, "Finding.moduleClass");
        Objects.requireNonNull(rule,        "Finding.rule");
        Objects.requireNonNull(message,     "Finding.message");
        Objects.requireNonNull(region,      "Finding.region");
    }

    /** A finding not anchored to a specific line. */
    public Finding(String moduleClass, RuleId rule, String message, JsRegion region) {
        this(moduleClass, rule, message, region, -1);
    }
}
