package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** RFC 0044 — Crate conformance for {@code homing-server}. */
class ServerCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(ServerCrate.INSTANCE),
                "every served JS module in homing-server must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(ServerCrate.INSTANCE),
                "every JS import must resolve to the importer's own crate or one it directly requires");
    }
}
