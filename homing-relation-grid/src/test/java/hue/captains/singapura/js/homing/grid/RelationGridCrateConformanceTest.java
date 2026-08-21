package hue.captains.singapura.js.homing.grid;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RFC 0044 — Crate conformance for {@code homing-relation-grid}: every served
 * JS module is declared (with its type) in {@link RelationGridCrate}, and every
 * import resolves within the crate or its requires. This is the RFC 0050
 * journey's Phase-0 standing constraint made mechanical: conformance is
 * ambient from the first module.
 */
class RelationGridCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(RelationGridCrate.INSTANCE),
                "every served JS module in homing-relation-grid must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(RelationGridCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }
}
