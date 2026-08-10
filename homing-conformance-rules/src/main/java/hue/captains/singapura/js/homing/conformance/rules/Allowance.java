package hue.captains.singapura.js.homing.conformance.rules;

import java.util.Objects;

/**
 * RFC 0044 Phase 7 — a documented exception (the allowlist entry): findings from
 * {@code rule} on {@code moduleClass} are graded {@link Severity#WARNING} with a
 * {@code reason}, rather than failing the build. Deliberately NOT a silent
 * suppression — an accepted exception stays visible (e.g. {@code Modal}'s
 * intentional {@code innerHTML = ""}), and a temporary "pending fix" entry is a
 * warning until the module is cleaned and the entry removed.
 *
 * @param moduleClass the fully-qualified module class the exception applies to
 * @param rule        the rule being excepted
 * @param reason      why it's accepted — required, non-blank
 */
public record Allowance(String moduleClass, RuleId rule, String reason) {

    public Allowance {
        Objects.requireNonNull(moduleClass, "Allowance.moduleClass");
        Objects.requireNonNull(rule,        "Allowance.rule");
        Objects.requireNonNull(reason,      "Allowance.reason");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Allowance.reason must not be blank — say why it's accepted");
        }
    }

    public boolean matches(Finding finding) {
        return finding.moduleClass().equals(moduleClass) && finding.rule().equals(rule);
    }
}
