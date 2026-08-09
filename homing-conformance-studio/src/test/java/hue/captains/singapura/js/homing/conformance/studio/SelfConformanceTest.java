package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.rules.Allowance;
import hue.captains.singapura.js.homing.conformance.rules.Baseline;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.conformance.rules.RuleId;
import hue.captains.singapura.js.homing.conformance.rules.Severity;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RFC 0044 Phase 6/7 — the build-fail gate. Runs the conformance engine over
 * every served JS module in homing-ssjs-core's crate closure (rendered through
 * the real server path), grades the findings, and fails the build on any
 * {@link Severity#ERROR}.
 *
 * <p>The rules are <b>strict</b>: any finding is build-failing unless it is a
 * documented {@link Allowance} or a grandfathered pre-existing violation in the
 * committed {@link Baseline}. Pre-existing debt is warned (not failed) while the
 * global {@code -Dconformance.allowPreExisting} switch is {@code true} (the
 * default); a NEW violation — anything not allowed and not baselined — fails the
 * build immediately, so debt can only shrink. Set the switch to {@code false} to
 * make the whole baseline build-failing too.</p>
 */
class SelfConformanceTest {

    /** homing-ssjs-core's documented, intentional exceptions (not debt). */
    private static final List<Allowance> HOMING_ALLOWANCES = List.of(
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
                            + "infrastructure that emits the served CSS, not a consumer view."));

    /**
     * The global switch: pre-existing (baselined) violations are allowed (warned)
     * by default; {@code -Dconformance.allowPreExisting=false} makes them fail.
     */
    private static final boolean ALLOW_PRE_EXISTING =
            Boolean.parseBoolean(System.getProperty("conformance.allowPreExisting", "true"));

    private static final FindingGrader HOMING_GRADER = FindingGrader.STRICT
            .withAllowlist(HOMING_ALLOWANCES)
            .withBaseline(loadBaseline())
            .allowingPreExisting(ALLOW_PRE_EXISTING);

    @Test
    void everyServedModuleIsConformant() {
        List<Finding> raw = new ConformanceEngine().checkCrates(CrateClosure.of(TopLevelCrates.ALL));
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

    private static Baseline loadBaseline() {
        try (InputStream in = SelfConformanceTest.class.getResourceAsStream("/conformance-baseline.txt")) {
            if (in == null) return Baseline.EMPTY;
            var lines = new ArrayList<String>();
            try (var r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                for (String line; (line = r.readLine()) != null; ) lines.add(line);
            }
            return Baseline.of(lines);
        } catch (java.io.IOException e) {
            throw new RuntimeException("failed to load conformance baseline", e);
        }
    }

    private static String describe(GradedFinding g) {
        Finding f = g.finding();
        return f.moduleClass() + " [" + f.rule().value() + "] " + f.message()
                + (g.note().isBlank() ? "" : "  (" + g.note() + ")");
    }
}
