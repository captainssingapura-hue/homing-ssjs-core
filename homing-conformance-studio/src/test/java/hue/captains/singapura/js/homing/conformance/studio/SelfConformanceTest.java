package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.rules.CssConformance;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.conformance.rules.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RFC 0044 Phase 6/7 — the build-fail gate. Runs the conformance engine over
 * every served JS module in homing-ssjs-core's crate closure (rendered through
 * the real server path), grades the findings via the shared {@link
 * HomingConformance} config, and fails the build on any {@link Severity#ERROR}.
 *
 * <p>The rules are <b>strict</b>: any finding is build-failing unless it is a
 * documented {@code Allowance} or a grandfathered pre-existing violation in the
 * committed baseline. Pre-existing debt is warned (not failed) while the global
 * {@code -Dconformance.allowPreExisting} switch is {@code true} (the default); a
 * NEW violation fails the build immediately, so debt can only shrink.</p>
 */
class SelfConformanceTest {

    private static final boolean ALLOW_PRE_EXISTING =
            Boolean.parseBoolean(System.getProperty("conformance.allowPreExisting", "true"));

    private static final FindingGrader HOMING_GRADER = HomingConformance.grader(ALLOW_PRE_EXISTING);

    @Test
    void everyServedModuleIsConformant() {
        List<Finding> raw = new ConformanceEngine().checkCrates(HomingConformance.closure());
        List<GradedFinding> graded = HOMING_GRADER.grade(raw);

        List<GradedFinding> warnings = graded.stream()
                .filter(g -> g.severity() == Severity.WARNING).toList();
        List<GradedFinding> errors = graded.stream()
                .filter(GradedFinding::isError).toList();

        if (!warnings.isEmpty()) {
            System.out.println("[conformance] " + warnings.size() + " warning(s) "
                    + "(" + HOMING_GRADER.baseline().size() + " baselined pre-existing violations, "
                    + "allowPreExisting=" + ALLOW_PRE_EXISTING + "):");
            warnings.forEach(g -> System.out.println("  WARN " + describe(g)));
        }

        assertEquals(List.of(), errors, () -> "conformance ERRORS (" + errors.size() + "):\n"
                + errors.stream().map(SelfConformanceTest::describe).collect(Collectors.joining("\n")));
    }

    /**
     * The baseline is a ledger of debt that <em>exists</em>: every fingerprint in
     * it must match a finding the engine still produces. A line whose module was
     * deleted or whose violation was fixed is stale — the ratchet turned and the
     * ledger did not — and it fails here so the file never silently carries
     * grants nothing claims. Fixing debt means removing its line.
     */
    @Test
    void everyBaselineFingerprintNamesLiveDebt() {
        List<Finding> raw = new ConformanceEngine().checkCrates(HomingConformance.closure());
        var live = raw.stream().map(Finding::fingerprint).collect(Collectors.toSet());
        List<String> stale = HOMING_GRADER.baseline().fingerprints().stream()
                .filter(fp -> !live.contains(fp)).sorted().toList();
        assertEquals(List.of(), stale, () -> "stale baseline fingerprints (" + stale.size()
                + ") — no current finding matches; remove them from conformance-baseline.txt:\n"
                + String.join("\n", stale));
    }

    /**
     * RFC 0066 — the laws over the CSS graph: palettes complete, tokens declared
     * by a palette the class reaches, priors palettes, nested names declared,
     * crates requiring what their groups lean on. These FAIL the build like any
     * JS rule, graded through the same allowances and baseline. Law 6 — no
     * literal where a token family is declared — is reported and not failed
     * until the shape and space palettes exist to name what the literals say
     * (29 radii, 11 fallbacks, measured); {@code -Dconformance.css.strict=true}
     * fails on it too.
     */
    private static final boolean CSS_STRICT =
            Boolean.parseBoolean(System.getProperty("conformance.css.strict", "false"));

    @Test
    void theCssGraphKeepsItsLaws() {
        List<GradedFinding> graded = HOMING_GRADER.grade(
                CssConformance.check(HomingConformance.closure(), HomingConformance.provisions()));
        List<GradedFinding> reported = graded.stream()
                .filter(g -> !CSS_STRICT && g.finding().rule().equals(CssConformance.NO_LITERAL_FAMILY)).toList();
        List<GradedFinding> errors = graded.stream()
                .filter(GradedFinding::isError).filter(g -> !reported.contains(g)).toList();
        if (!graded.isEmpty()) {
            System.out.println("[conformance] css graph: " + graded.size() + " finding(s), "
                    + errors.size() + " error(s), " + reported.size() + " reported only (" + CssConformance.NO_LITERAL_FAMILY.value()
                    + "; -Dconformance.css.strict=true to fail on them):");
            graded.forEach(g -> System.out.println("  " + (errors.contains(g) ? "ERROR " : "WARN  ") + describe(g)));
        }
        assertEquals(List.of(), errors, () -> "css graph errors (" + errors.size() + "):\n"
                + errors.stream().map(SelfConformanceTest::describe).collect(Collectors.joining("\n")));
    }

    private static String describe(GradedFinding g) {
        Finding f = g.finding();
        return f.moduleClass() + " [" + f.rule().value() + "] " + f.message()
                + (g.note().isBlank() ? "" : "  (" + g.note() + ")");
    }
}
