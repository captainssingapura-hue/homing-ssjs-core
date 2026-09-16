package hue.captains.singapura.js.homing.theme.color;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RFC 0044 — the per-module Crate conformance for {@code homing-theme-color}. */
class ThemeColorCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(ThemeColorCrate.INSTANCE),
                "every served JS module in this Maven module must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(ThemeColorCrate.INSTANCE),
                "every JS import must resolve to the importer's own crate or one it directly requires");
    }
}
