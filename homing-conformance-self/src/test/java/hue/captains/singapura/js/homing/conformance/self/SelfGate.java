package hue.captains.singapura.js.homing.conformance.self;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.rules.CssConformance;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.conformance.rules.Severity;
import hue.captains.singapura.js.homing.core.Crate;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A gate over a set of crates: every served module conformant, the baseline naming only live
 * debt, the CSS graph keeping its laws - graded through the set's own allowances and baseline,
 * over the findings of the modules that are the set's own (the closure carries what they require,
 * gated where it is owned).
 */
abstract class SelfGate {

    /** Pre-existing violations are warnings unless {@code -Dconformance.allowPreExisting=false}. */
    static final boolean ALLOW_PRE_EXISTING =
            Boolean.parseBoolean(System.getProperty("conformance.allowPreExisting", "true"));

    abstract Collection<Crate> closure();
    abstract FindingGrader grader();
    /** The modules whose findings this gate grades. */
    abstract Predicate<String> owns();
    abstract String baselineFile();

    private List<Finding> own(List<Finding> raw) {
        Predicate<String> owns = owns();
        return raw.stream().filter(f -> owns.test(f.moduleClass())).toList();
    }

    @Test
    void everyServedModuleIsConformant() {
        FindingGrader grader = grader();
        List<GradedFinding> graded = grader.grade(own(new ConformanceEngine().checkCrates(closure())));
        List<GradedFinding> warnings = graded.stream().filter(g -> g.severity() == Severity.WARNING).toList();
        List<GradedFinding> errors = graded.stream().filter(GradedFinding::isError).toList();
        if (!warnings.isEmpty()) {
            System.out.println("[conformance] " + getClass().getSimpleName() + ": " + warnings.size() + " warning(s) ("
                    + grader.baseline().size() + " baselined, allowPreExisting=" + ALLOW_PRE_EXISTING + ")");
        }
        assertEquals(List.of(), errors, () -> "conformance ERRORS (" + errors.size() + "):\n"
                + errors.stream().map(SelfGate::describe).collect(Collectors.joining("\n")));
    }

    /**
     * The baseline is a ledger of debt that exists: every fingerprint in it must match a finding
     * the engine still produces. A line whose module was deleted or whose violation was fixed is
     * stale, and fails here, so the file never carries grants nothing claims.
     */
    @Test
    void everyBaselineFingerprintNamesLiveDebt() {
        var live = new ConformanceEngine().checkCrates(closure()).stream().map(Finding::fingerprint).collect(Collectors.toSet());
        live.addAll(CssConformance.check(closure()).stream().map(Finding::fingerprint).toList());
        List<String> stale = grader().baseline().fingerprints().stream().filter(fp -> !live.contains(fp)).sorted().toList();
        assertEquals(List.of(), stale, () -> "stale baseline fingerprints (" + stale.size()
                + ") - no current finding matches; remove them from " + baselineFile() + ":\n" + String.join("\n", stale));
    }

    /**
     * RFC 0066 - the laws over the CSS graph: every variable a body reads is a design word or its
     * own runtime's, nested names declared, crates requiring what their groups lean on. They fail
     * the build like any JS rule, graded through the same allowances and baseline.
     */
    @Test
    void theCssGraphKeepsItsLaws() {
        List<GradedFinding> graded = grader().grade(own(CssConformance.check(closure())));
        List<GradedFinding> errors = graded.stream().filter(GradedFinding::isError).toList();
        assertEquals(List.of(), errors, () -> "css graph errors (" + errors.size() + "):\n"
                + errors.stream().map(SelfGate::describe).collect(Collectors.joining("\n")));
    }

    static String describe(GradedFinding g) {
        Finding f = g.finding();
        return f.moduleClass() + " [" + f.rule().value() + "] " + f.message() + (g.note().isBlank() ? "" : "  (" + g.note() + ")");
    }
}
