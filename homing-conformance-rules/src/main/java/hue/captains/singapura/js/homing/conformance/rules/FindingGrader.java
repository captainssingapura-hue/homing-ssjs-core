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
     * The framework default — the rules proven clean across the codebase and so
     * promoted to build-failing: {@code no-cdn-import} and {@code no-dom-access}
     * (a module declared no-DOM touching the DOM is a broken promise, not a
     * warning). Newly-ported rules still under triage — e.g. {@code
     * no-dom-destruction} — warn until they too are clean and promoted. No
     * exceptions yet; grows as rules are cleaned + promoted and justified
     * exceptions added.
     */
    public static final FindingGrader DEFAULT = new FindingGrader(
            Set.of(new RuleId("no-cdn-import"), new RuleId("no-dom-access")),
            List.of());

    public FindingGrader {
        errorRules = Set.copyOf(errorRules);
        allowlist  = List.copyOf(allowlist);
    }

    /** This grader plus extra allowances (e.g. an app's own accepted exceptions). */
    public FindingGrader withAllowlist(List<Allowance> more) {
        var combined = new java.util.ArrayList<>(allowlist);
        combined.addAll(more);
        return new FindingGrader(errorRules, combined);
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
