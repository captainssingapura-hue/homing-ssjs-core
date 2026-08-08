package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RFC 0044 — Crate conformance for {@code homing-workspace-shell}. */
class WorkspaceShellCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(WorkspaceShellCrate.INSTANCE),
                "every served JS module in homing-workspace-shell must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(WorkspaceShellCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }
}
