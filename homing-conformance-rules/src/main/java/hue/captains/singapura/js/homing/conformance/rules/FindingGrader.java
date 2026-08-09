package hue.captains.singapura.js.homing.conformance.rules;

import java.util.List;

/**
 * RFC 0044 Phase 7 — grades raw {@link Finding}s into {@link GradedFinding}s,
 * deciding what fails the build.
 *
 * <p><b>Strict-by-default, with a grandfathered baseline.</b> Every rule is
 * build-failing — a finding is an {@link Severity#ERROR} unless something
 * downgrades it. Two things do:</p>
 * <ol>
 *   <li>an {@link Allowance} — a <em>documented, intentional</em> exception
 *       (infrastructure that is the exception, a sanctioned pattern): always a
 *       WARNING, whatever the switch;</li>
 *   <li>the {@link Baseline} — a <em>pre-existing</em> violation, legacy debt
 *       recorded when the rule was introduced: a WARNING while pre-existing
 *       violations are allowed, an ERROR when they are not.</li>
 * </ol>
 * <p>Anything neither allowed nor baselined is a NEW violation and always an
 * ERROR. This lets the rules be as strict as possible from the start: existing
 * debt is grandfathered, but no new debt can be added. The {@code
 * allowPreExisting} switch is the global knob — flip it to {@code false} (e.g.
 * from CI) to make the whole accumulated baseline build-failing too.</p>
 *
 * @param allowlist        documented exceptions (matching findings → WARNING + reason)
 * @param baseline         recorded pre-existing violations
 * @param allowPreExisting whether a baselined finding is a WARNING (true) or an ERROR (false)
 */
public record FindingGrader(List<Allowance> allowlist, Baseline baseline, boolean allowPreExisting) {

    /** The strictest possible grader: no allowances, no baseline — every finding fails. */
    public static final FindingGrader STRICT = new FindingGrader(List.of(), Baseline.EMPTY, false);

    public FindingGrader {
        allowlist = List.copyOf(allowlist);
    }

    /** This grader plus extra allowances (e.g. an app's own accepted exceptions). */
    public FindingGrader withAllowlist(List<Allowance> more) {
        var combined = new java.util.ArrayList<>(allowlist);
        combined.addAll(more);
        return new FindingGrader(combined, baseline, allowPreExisting);
    }

    /** This grader with its pre-existing-violation baseline set. */
    public FindingGrader withBaseline(Baseline b) {
        return new FindingGrader(allowlist, b, allowPreExisting);
    }

    /** This grader with the global allow-pre-existing switch set (false ⇒ the baseline fails too). */
    public FindingGrader allowingPreExisting(boolean allow) {
        return new FindingGrader(allowlist, baseline, allow);
    }

    public GradedFinding grade(Finding finding) {
        for (Allowance a : allowlist) {
            if (a.matches(finding)) return new GradedFinding(finding, Severity.WARNING, Disposition.ALLOWED, a.reason());
        }
        if (baseline.contains(finding)) {
            return allowPreExisting
                    ? new GradedFinding(finding, Severity.WARNING, Disposition.PRE_EXISTING, "pre-existing (baselined)")
                    : new GradedFinding(finding, Severity.ERROR, Disposition.PRE_EXISTING, "pre-existing violation (disallowed)");
        }
        return new GradedFinding(finding, Severity.ERROR, Disposition.NEW, "new violation");
    }

    public List<GradedFinding> grade(List<Finding> findings) {
        return findings.stream().map(this::grade).toList();
    }
}
