package hue.captains.singapura.js.homing.conformance.rules;

import java.util.Objects;

/**
 * RFC 0044 Phase 7 — a {@link Finding} paired with the {@link Severity} a
 * {@link FindingGrader} assigned it, the {@link Disposition} that explains why
 * (allowed / pre-existing / new), and an optional {@code note}. Only {@link
 * Severity#ERROR} graded findings fail the build.
 */
public record GradedFinding(Finding finding, Severity severity, Disposition disposition, String note) {

    public GradedFinding {
        Objects.requireNonNull(finding,     "GradedFinding.finding");
        Objects.requireNonNull(severity,    "GradedFinding.severity");
        Objects.requireNonNull(disposition, "GradedFinding.disposition");
        note = (note == null) ? "" : note;
    }

    public boolean isError() { return severity == Severity.ERROR; }
}
