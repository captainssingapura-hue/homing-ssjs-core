package hue.captains.singapura.js.homing.conformance.rules.report;

import hue.captains.singapura.js.homing.conformance.rules.Disposition;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.conformance.rules.Severity;

/**
 * RFC 0044 Phase 8 — a graded finding in its exportable form: a flat, typed
 * record (the {@code rule} as its slug, the offending {@code message} + line,
 * the {@link Severity} and {@link Disposition}, and the note). Serialized by the
 * polyglot codec; rendered by the studio widgets.
 *
 * @param rule        the rule's slug (RuleId value)
 * @param message     the human message (embeds the offending code)
 * @param line        0-based served-text line, or {@code -1}
 * @param severity    WARNING or ERROR
 * @param disposition allowed / pre-existing / new
 * @param note        the allowlist reason or grading note
 */
public record FindingReport(String rule, String message, int line, Severity severity,
                            Disposition disposition, String note) {

    /** Flatten a {@link GradedFinding} into its report form. */
    public static FindingReport of(GradedFinding g) {
        return new FindingReport(
                g.finding().rule().value(),
                g.finding().message(),
                g.finding().line(),
                g.severity(),
                g.disposition(),
                g.note());
    }
}
