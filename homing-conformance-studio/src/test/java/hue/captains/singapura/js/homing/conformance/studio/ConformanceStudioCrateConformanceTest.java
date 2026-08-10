package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0044 — the Crate conformance for {@code homing-conformance-studio}, and
 * the teeth-check for {@code CrateDependencyRule}'s <b>non-transitive</b>
 * visibility over a real cross-crate edge (ModuleTreeWidget → TreeRendererModule
 * ∈ CoreJsCrate).
 */
class ConformanceStudioCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(ConformanceStudioCrate.INSTANCE),
                "every served JS module in this module must be declared in its crate");
    }

    @Test
    void legalWhenCoreJsIsDirectlyRequired() {
        // The real crate requires CoreJsCrate directly → the import is authorized.
        assertEquals(List.of(), CrateDependencyRule.check(ConformanceStudioCrate.INSTANCE),
                "importing TreeRendererModule is legal because CoreJsCrate is directly required");
    }

    @Test
    void reachingThroughAnotherCrateIsIllegal() {
        // middle requires CoreJsCrate; the studio requires ONLY middle — so CoreJsCrate
        // is reachable transitively but NOT directly. Non-transitive visibility must reject it.
        Crate middle = crate("middle", List.of(), List.of(CoreJsCrate.INSTANCE));
        Crate studioViaMiddle = crate("homing-conformance-studio",
                ConformanceStudioCrate.INSTANCE.entries(), List.of(middle));

        List<String> findings = CrateDependencyRule.check(studioViaMiddle);
        assertTrue(
                findings.stream().anyMatch(f -> f.contains("illegal import") && f.contains("TreeRendererModule")),
                () -> "reach-through import of TreeRendererModule must be flagged illegal; got: " + findings);
    }

    private static Crate crate(String name, List<CrateEntry> entries, List<Crate> requires) {
        return new Crate() {
            @Override public String name() { return name; }
            @Override public List<CrateEntry> entries() { return entries; }
            @Override public List<Crate> requires() { return requires; }
        };
    }
}
