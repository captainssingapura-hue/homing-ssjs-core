package hue.captains.singapura.js.homing.ui.split;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.conformance.rules.ServedModule;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.design.Deployment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The crate holds every served module and imports only what it requires; no class holds a value the substrate should bind. */
class UiSplitCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(UiSplitCrate.INSTANCE));
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(UiSplitCrate.INSTANCE));
    }

    @Test
    void theSplitterPaintsNothingOfItsOwn() {
        var worn = Deployment.wornBy(List.of(SplitStyles.INSTANCE));
        assertTrue(worn.size() >= 5, "the divider classes wear a handful of pairs; found " + worn.size());
        for (CssClass<SplitStyles> c : SplitStyles.INSTANCE.cssClasses()) {
            String body = c.body();
            assertFalse(body.contains("#") || body.contains("rgb") || body.contains("px solid") || body.contains("var(--color"),
                    c.getClass().getSimpleName() + " holds a value the substrate should bind: " + body);
        }
    }

    /** Each served JS module under the lane its crate entry declares: the primitives' DOM-owner discipline, the events' no-DOM one, the line limit for all. */
    @Test
    void everyServedModuleKeepsItsLanesDiscipline() {
        int checked = 0;
        for (var entry : UiSplitCrate.INSTANCE.entries()) {
            if (!(entry.declaredType() instanceof StandardJsModuleType type) || type == StandardJsModuleType.GENERATED_CSS) continue;
            String m = entry.moduleClass();
            String src = String.join("\n", ResourceReader.INSTANCE.getStringsFromResource("homing/js/" + m.replace('.', '/') + ".js"));
            var findings = DefaultJsRulePolicy.INSTANCE.rulesFor(type).checkAll(ServedModule.of(m, type, src));
            assertEquals(List.of(), findings, () -> m + ": "
                    + findings.stream().map(f -> f.rule().value() + "@" + f.line() + ": " + f.message()).toList());
            checked++;
        }
        assertEquals(2, checked, "the pane and the events");
    }

    @Test
    void theSplitterImportsEveryClassDeclared() {
        var imported = java.util.stream.Stream.of(SplitPane.INSTANCE.imports())
                .flatMap(im -> im.getAllImports().values().stream())
                .flatMap(mi -> mi.allImports().stream()).map(e -> e.getClass().getSimpleName()).toList();
        for (CssClass<SplitStyles> c : SplitStyles.INSTANCE.cssClasses())
            assertTrue(imported.contains(c.getClass().getSimpleName()), c.getClass().getSimpleName() + " is declared but not imported");
    }
}
