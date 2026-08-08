package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RFC 0044 — the per-module Crate conformance for {@code homing-core-js}: proves
 * the completeness guard (no orphan served modules) and the layering rule (every
 * JS import lands in a directly-required crate). This is the reusable shape every
 * Maven module's crate test will take.
 */
class CoreJsCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(CoreJsCrate.INSTANCE),
                "every served JS module in this Maven module must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(CoreJsCrate.INSTANCE),
                "every JS import must resolve to the importer's own crate or one it directly requires");
    }
}
