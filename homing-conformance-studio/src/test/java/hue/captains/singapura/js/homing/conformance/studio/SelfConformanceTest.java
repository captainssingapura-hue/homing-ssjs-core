package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RFC 0044 Phase 6 — the build-fail gate: runs the conformance engine over every
 * served JS module in homing-ssjs-core's own crate closure (rendered through the
 * real server path) and fails the build on ANY finding. This is homing verifying
 * itself; a downstream writes the same test over its own top-level crates.
 */
class SelfConformanceTest {

    @Test
    void everyServedModuleIsConformant() {
        List<Finding> findings = new ConformanceEngine()
                .checkCrates(CrateClosure.of(TopLevelCrates.ALL));

        assertEquals(List.of(), findings, () -> "conformance violations ("
                + findings.size() + "):\n"
                + findings.stream()
                          .map(f -> "  " + f.moduleClass() + " [" + f.rule().value() + "] " + f.message())
                          .collect(Collectors.joining("\n")));
    }
}
