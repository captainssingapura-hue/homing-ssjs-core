package hue.captains.singapura.js.homing.workspace.persistence;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RFC 0044 — Crate conformance for {@code homing-workspace-persistence}. */
class WorkspacePersistenceCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(WorkspacePersistenceCrate.INSTANCE),
                "every served JS module in homing-workspace-persistence must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(WorkspacePersistenceCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }
}
