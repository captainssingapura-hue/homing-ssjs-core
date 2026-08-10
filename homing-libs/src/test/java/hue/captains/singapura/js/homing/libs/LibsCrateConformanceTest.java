package hue.captains.singapura.js.homing.libs;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RFC 0044 — Crate conformance for {@code homing-libs}. */
class LibsCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(LibsCrate.INSTANCE),
                "every bundled module must be declared (via HomingLibsRegistry.ALL)");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(LibsCrate.INSTANCE),
                "bundled externals import nothing — no cross-crate edges");
    }
}
