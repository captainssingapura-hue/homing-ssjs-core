package hue.captains.singapura.js.homing.conformance.rules;

import java.util.Objects;

/**
 * RFC 0044 Phase 7 — a {@link Finding} paired with the {@link Severity} a
 * {@link FindingGrader} assigned it, plus an optional {@code note} (the
 * allowlist reason, when downgraded). Only {@link Severity#ERROR} graded
 * findings fail the build.
 */
public record GradedFinding(Finding finding, Severity severity, String note) {

    public GradedFinding {
        Objects.requireNonNull(finding,  "GradedFinding.finding");
        Objects.requireNonNull(severity, "GradedFinding.severity");
        note = (note == null) ? "" : note;
    }

    public boolean isError() { return severity == Severity.ERROR; }
}
