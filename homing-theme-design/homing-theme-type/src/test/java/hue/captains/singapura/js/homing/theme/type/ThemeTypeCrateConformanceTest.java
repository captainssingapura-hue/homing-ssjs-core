package hue.captains.singapura.js.homing.theme.type;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RFC 0044 — the per-module Crate conformance for {@code homing-theme-type}. */
class ThemeTypeCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(ThemeTypeCrate.INSTANCE),
                "every served JS module in this Maven module must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(ThemeTypeCrate.INSTANCE),
                "every JS import must resolve to the importer's own crate or one it directly requires");
    }
}
