package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.rules.Allowance;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.conformance.rules.RuleId;
import hue.captains.singapura.js.homing.conformance.rules.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RFC 0044 Phase 6/7 — the build-fail gate: runs the conformance engine over
 * every served JS module in homing-ssjs-core's crate closure (rendered through
 * the real server path), grades the findings, and fails the build on any
 * {@link Severity#ERROR}. {@link Severity#WARNING} findings (newly-ported rules,
 * or allowlisted exceptions) are printed but non-fatal — porting a scanner into
 * a rule surfaces violations without breaking the build.
 */
class SelfConformanceTest {

    /** The framework baseline plus homing-ssjs-core's own accepted exceptions. */
    private static final FindingGrader HOMING_GRADER = FindingGrader.DEFAULT.withAllowlist(List.of(
            new Allowance(
                    "hue.captains.singapura.js.homing.studio.base.ui.layout.ModalModule",
                    new RuleId("no-dom-destruction"),
                    "Modal.setContent is a wholesale-body-swap API; the drag-to-modal flow never "
                            + "wipes widget DOM (MultiTabPaneDragModule moves content out first)."),
            new Allowance(
                    "hue.captains.singapura.js.homing.server.HrefManager",
                    new RuleId("no-raw-href"),
                    "HrefManager IS the href-manager implementation — it defines the href.* API "
                            + "the rule redirects consumers to; window.location/setAttribute('href') "
                            + "here are the sanctioned primitives, not a bypass."),
            new Allowance(
                    "hue.captains.singapura.js.homing.server.CssClassManager",
                    new RuleId("no-raw-href"),
                    "CssClassManager builds its own stylesheet <link> href — framework "
                            + "infrastructure that emits the served CSS, not a consumer view.")));

    @Test
    void everyServedModuleIsConformant() {
        List<Finding> raw = new ConformanceEngine().checkCrates(CrateClosure.of(TopLevelCrates.ALL));
        List<GradedFinding> graded = HOMING_GRADER.grade(raw);

        List<GradedFinding> warnings = graded.stream()
                .filter(g -> g.severity() == Severity.WARNING).toList();
        List<GradedFinding> errors = graded.stream()
                .filter(GradedFinding::isError).toList();

        if (!warnings.isEmpty()) {
            System.out.println("[conformance] " + warnings.size() + " warning(s):");
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
