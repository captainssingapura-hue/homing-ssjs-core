package hue.captains.singapura.js.homing.conformance.rules;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 7 proof — the strict-with-baseline grading: a finding is an ERROR unless
 * it is a documented {@link Allowance} (always a warning) or a grandfathered
 * {@link Baseline} entry (a warning only while pre-existing violations are
 * allowed). Anything new is always an ERROR.
 */
class FindingGraderTest {

    private static final Finding ALLOWED =
            new Finding("demo.Infra",  new RuleId("no-raw-href"), "raw href: it IS the manager", 3);
    private static final Finding PRE_EXISTING =
            new Finding("demo.Legacy", new RuleId("view-doctrine"), "DOM lookup: host.querySelector('x')", 9);
    private static final Finding NEW =
            new Finding("demo.Fresh",  new RuleId("no-cdn-import"), "CDN import: https://evil/x.mjs", 1);

    private static FindingGrader grader(boolean allowPreExisting) {
        return FindingGrader.STRICT
                .withAllowlist(List.of(new Allowance("demo.Infra", new RuleId("no-raw-href"), "it is the manager")))
                .withBaseline(new Baseline(java.util.Set.of(PRE_EXISTING.fingerprint())))
                .allowingPreExisting(allowPreExisting);
    }

    @Test
    void allowlistedIsAlwaysAWarning() {
        assertEquals(Severity.WARNING, grader(true).grade(ALLOWED).severity());
        assertEquals(Severity.WARNING, grader(false).grade(ALLOWED).severity(),
                "an intentional allowance stays a warning even in strict mode");
        assertEquals(Disposition.ALLOWED, grader(false).grade(ALLOWED).disposition());
    }

    @Test
    void baselinedFollowsTheGlobalSwitch() {
        assertEquals(Severity.WARNING, grader(true).grade(PRE_EXISTING).severity());
        assertEquals(Severity.ERROR,   grader(false).grade(PRE_EXISTING).severity(),
                "disallowing pre-existing makes the baseline build-failing");
        assertEquals(Disposition.PRE_EXISTING, grader(true).grade(PRE_EXISTING).disposition());
    }

    @Test
    void aNewViolationAlwaysFails() {
        assertEquals(Severity.ERROR, grader(true).grade(NEW).severity());
        assertEquals(Severity.ERROR, grader(false).grade(NEW).severity());
        assertEquals(Disposition.NEW, grader(true).grade(NEW).disposition());
    }

    @Test
    void fingerprintIgnoresLineNumber() {
        var a = new Finding("m.M", new RuleId("r"), "same offending code", 10);
        var b = new Finding("m.M", new RuleId("r"), "same offending code", 42);
        assertEquals(a.fingerprint(), b.fingerprint());
        assertTrue(new Baseline(java.util.Set.of(a.fingerprint())).contains(b),
                "a baselined finding still matches after its line number shifts");
    }
}
