package hue.captains.singapura.js.homing.ui.docking;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.conformance.rules.ServedModule;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The crate holds every served module and imports only what it requires. */
class UiDockingCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(UiDockingCrate.INSTANCE));
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(UiDockingCrate.INSTANCE));
    }

    /** Each served JS module under the lane its crate entry declares: the primitives' DOM-owner discipline, the events' no-DOM one, the line limit for all. */
    @Test
    void everyServedModuleKeepsItsLanesDiscipline() {
        int checked = 0;
        for (var entry : UiDockingCrate.INSTANCE.entries()) {
            if (!(entry.declaredType() instanceof StandardJsModuleType type) || type == StandardJsModuleType.GENERATED_CSS) continue;
            String m = entry.moduleClass();
            String src = String.join("\n", ResourceReader.INSTANCE.getStringsFromResource("homing/js/" + m.replace('.', '/') + ".js"));
            var findings = DefaultJsRulePolicy.INSTANCE.rulesFor(type).checkAll(ServedModule.of(m, type, src));
            assertEquals(List.of(), findings, () -> m + ": "
                    + findings.stream().map(f -> f.rule().value() + "@" + f.line() + ": " + f.message()).toList());
            checked++;
        }
        assertEquals(2, checked, "the docking and the events");
    }

}
