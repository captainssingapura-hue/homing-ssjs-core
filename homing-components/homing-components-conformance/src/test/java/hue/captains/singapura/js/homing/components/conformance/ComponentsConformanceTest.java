package hue.captains.singapura.js.homing.components.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.CrateConformance;
import hue.captains.singapura.js.homing.conformance.rules.CrateCoverage;
import hue.captains.singapura.js.homing.conformance.rules.CssConformance;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.design.Deployment;
import hue.captains.singapura.js.homing.design.Design;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The components' gate, strict from the first day: the baseline is empty and
 * every finding fails the build. Five checks over the fresh crates —
 * <ol>
 *   <li><b>crate integrity</b>: each declares every served module in its
 *       Maven module and every cross-crate import in {@code requires()};</li>
 *   <li><b>coverage</b>: nothing served from those modules is uncrated;</li>
 *   <li><b>the lanes</b>: the served artifact of every module, graded by the
 *       framework's policy under the role its crate declares;</li>
 *   <li><b>the CSS graph laws</b>: tokens declared, priors are palettes,
 *       nested references declared, crate reach, no literal families;</li>
 *   <li><b>the substrate</b>: every one of the seven designs binds every
 *       pair every class in these crates wears, and anchors what is worn
 *       with an extent.</li>
 * </ol>
 * The export and the served studio grade by the same configuration.
 */
class ComponentsConformanceTest {

    private static final boolean ALLOW_PRE_EXISTING =
            Boolean.parseBoolean(System.getProperty("conformance.allowPreExisting", "true"));

    @Test
    void everyCrateIsStructurallyComplete() {
        CrateConformance.Result result = CrateConformance.evaluate(new ArrayList<>(ComponentsConformance.closure()));
        for (Crate c : ComponentsConformance.TOP_LEVEL) {
            CrateConformance.CrateResult crate = result.crates().get(c.name());
            assertNotNull(crate, c.name() + " must be present in the evaluation");
            assertEquals(List.of(), crate.orphans(), c.name() + ": every served module must be declared");
            assertEquals(List.of(), crate.illegalImports(), c.name() + ": every cross-crate import must be declared in requires()");
        }
    }

    @Test
    void everyServedModuleIsCrated() {
        List<String> gaps = CrateCoverage.check(ComponentsConformance.closure(), ComponentsConformance.ANCHORS);
        assertEquals(List.of(), gaps, () -> "served modules missing from every crate:\n" + String.join("\n", gaps));
    }

    @Test
    void everyServedModuleKeepsItsLane() {
        List<Finding> raw = new ConformanceEngine(ComponentsConformance.POLICY, new ServedModuleRenderer())
                .checkCrates(ComponentsConformance.TOP_LEVEL);
        List<GradedFinding> graded = ComponentsConformance.grader(ALLOW_PRE_EXISTING).grade(raw);
        List<GradedFinding> errors = graded.stream().filter(GradedFinding::isError).toList();
        assertEquals(List.of(), errors, () -> "conformance ERRORS (" + errors.size() + "):\n"
                + errors.stream().map(ComponentsConformanceTest::describe).collect(Collectors.joining("\n")));
        assertEquals(0, ComponentsConformance.baseline().size(), "the baseline is empty on purpose; a finding is fixed, not filed");
    }

    @Test
    void theCssGraphKeepsItsLaws() {
        Set<String> own = ComponentsConformance.ownModules();
        List<Finding> raw = CssConformance.check(ComponentsConformance.closure(), ComponentsConformance.provisions())
                .stream().filter(f -> own.contains(f.moduleClass())).toList();
        List<GradedFinding> errors = ComponentsConformance.grader(ALLOW_PRE_EXISTING).grade(raw)
                .stream().filter(GradedFinding::isError).toList();
        assertEquals(List.of(), errors, () -> "css graph ERRORS (" + errors.size() + "):\n"
                + errors.stream().map(ComponentsConformanceTest::describe).collect(Collectors.joining("\n")));
    }

    @Test
    void everyDesignBindsEveryPairTheComponentsWear() {
        var groups = new ArrayList<CssGroup<?>>();
        for (Crate c : ComponentsConformance.TOP_LEVEL)
            for (var e : c.entries()) if (e.module() instanceof CssGroup<?> g) groups.add(g);
        assertEquals(7, groups.size(), "one style group per crate");
        var worn = Deployment.wornBy(groups);
        var scaled = Deployment.scaledBy(groups);
        var grown = Deployment.grownBy(groups);
        assertTrue(worn.size() > 50, "the six groups wear many distinct pairs; found " + worn.size());
        assertTrue(scaled.size() >= 1, "the splitter's lit handle is worn with an extent");
        assertTrue(grown.get(hue.captains.singapura.js.homing.design.Growth.SIZE).size() >= 1, "the button is worn with a size");
        assertTrue(grown.get(hue.captains.singapura.js.homing.design.Growth.ASPECT).size() >= 1, "the card is worn with an aspect");
        List<Design> designs = ComponentsConformance.designs();
        assertTrue(designs.size() >= 7, "the seven designs, each in the palettes that fit it; found " + designs.size());
        for (Design d : designs) {
            var r = Deployment.of(worn, scaled, grown, d).resolve();
            assertEquals(List.of(), r.findings(), () -> d.slug() + ": " + r.findings());
        }
    }

    private static String describe(GradedFinding g) {
        Finding f = g.finding();
        return f.moduleClass() + " [" + f.rule().value() + "] " + f.message()
                + (g.note().isBlank() ? "" : "  (" + g.note() + ")");
    }
}
