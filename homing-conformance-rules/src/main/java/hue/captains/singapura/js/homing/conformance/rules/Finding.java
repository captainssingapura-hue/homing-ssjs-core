package hue.captains.singapura.js.homing.conformance.rules;

import java.util.Objects;

/**
 * RFC 0044 — one rule violation on a served module: which module, which rule,
 * a human message, and the 0-based line in the served text ({@code -1} if not
 * line-specific). Any Finding fails the build.
 */
public record Finding(String moduleClass, RuleId rule, String message, int line) {

    public Finding {
        Objects.requireNonNull(moduleClass, "Finding.moduleClass");
        Objects.requireNonNull(rule,        "Finding.rule");
        Objects.requireNonNull(message,     "Finding.message");
    }

    /** A finding that is not tied to a specific line. */
    public Finding(String moduleClass, RuleId rule, String message) {
        this(moduleClass, rule, message, -1);
    }
}
