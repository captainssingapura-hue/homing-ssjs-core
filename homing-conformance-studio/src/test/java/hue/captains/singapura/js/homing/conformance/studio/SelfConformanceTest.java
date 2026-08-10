package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
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

    private static String describe(GradedFinding g) {
        Finding f = g.finding();
        return f.moduleClass() + " [" + f.rule().value() + "] " + f.message()
                + (g.note().isBlank() ? "" : "  (" + g.note() + ")");
    }
}
