package hue.captains.singapura.js.homing.conformance.rules;

import java.util.List;
import java.util.Set;

/**
 * RFC 0044 Phase 7 — grades raw {@link Finding}s into {@link GradedFinding}s,
 * deciding what fails the build.
 *
 * <p>Phased-tightening model: a finding is a {@link Severity#WARNING} by default
 * — so porting a scanner into a rule surfaces violations without breaking the
 * build — and only becomes an {@link Severity#ERROR} once its rule is in
 * {@code errorRules} (promoted after the codebase is clean for it). An
 * {@link Allowance} always wins, grading a matching finding a documented
 * WARNING regardless of promotion.</p>
 *
 * @param errorRules rules promoted to build-failing; everything else warns
 * @param allowlist  documented exceptions (matching findings → WARNING + reason)
 */
public record FindingGrader(Set<RuleId> errorRules, List<Allowance> allowlist) {

    /**
     * The framework default: only the proven {@code no-cdn-import} rule fails the
     * build; every other (newly-ported) rule warns until promoted. No exceptions
     * yet. Grows as rules are cleaned + promoted and justified exceptions added.
     */
    public static final FindingGrader DEFAULT = new FindingGrader(
            Set.of(new RuleId("no-cdn-import")),
            List.of());

    public FindingGrader {
        errorRules = Set.copyOf(errorRules);
        allowlist  = List.copyOf(allowlist);
    }

    public GradedFinding grade(Finding finding) {
        for (Allowance a : allowlist) {
            if (a.matches(finding)) return new GradedFinding(finding, Severity.WARNING, a.reason());
        }
        Severity severity = errorRules.contains(finding.rule()) ? Severity.ERROR : Severity.WARNING;
        return new GradedFinding(finding, severity, "");
    }

    public List<GradedFinding> grade(List<Finding> findings) {
        return findings.stream().map(this::grade).toList();
    }
}
