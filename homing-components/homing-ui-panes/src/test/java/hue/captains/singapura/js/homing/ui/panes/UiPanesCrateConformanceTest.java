package hue.captains.singapura.js.homing.ui.panes;

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
class UiPanesCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(UiPanesCrate.INSTANCE));
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(UiPanesCrate.INSTANCE));
    }

    @Test
    void thePanePaintsNothingOfItsOwn() {
        var worn = Deployment.wornBy(List.of(PaneStyles.INSTANCE));
        assertTrue(worn.size() >= 25, "fifteen classes wear over twenty-five distinct pairs; found " + worn.size());
        for (CssClass<PaneStyles> c : PaneStyles.INSTANCE.cssClasses()) {
            String body = c.body();
            assertFalse(body.contains("#") || body.contains("rgb") || body.contains("px solid") || body.contains("var(--color"),
                    c.getClass().getSimpleName() + " holds a value the substrate should bind: " + body);
        }
    }

    /** Each served JS module under the lane its crate entry declares: the primitives' DOM-owner discipline, the events' no-DOM one, the line limit for all. */
    @Test
    void everyServedModuleKeepsItsLanesDiscipline() {
        int checked = 0;
        for (var entry : UiPanesCrate.INSTANCE.entries()) {
            if (!(entry.declaredType() instanceof StandardJsModuleType type) || type == StandardJsModuleType.GENERATED_CSS) continue;
            String m = entry.moduleClass();
            String src = String.join("\n", ResourceReader.INSTANCE.getStringsFromResource("homing/js/" + m.replace('.', '/') + ".js"));
            var findings = DefaultJsRulePolicy.INSTANCE.rulesFor(type).checkAll(ServedModule.of(m, type, src));
            assertEquals(List.of(), findings, () -> m + ": "
                    + findings.stream().map(f -> f.rule().value() + "@" + f.line() + ": " + f.message()).toList());
            checked++;
        }
        assertEquals(5, checked, "the pane, the strip, the hand, the events and the drag");
    }

    @Test
    void thePaneAndTheStripImportEveryClassDeclared() {
        var imported = java.util.stream.Stream.of(MultiTabPaneModule.INSTANCE.imports(), TabStripModule.INSTANCE.imports())
                .flatMap(im -> im.getAllImports().values().stream())
                .flatMap(mi -> mi.allImports().stream()).map(e -> e.getClass().getSimpleName()).toList();
        for (CssClass<PaneStyles> c : PaneStyles.INSTANCE.cssClasses())
            assertTrue(imported.contains(c.getClass().getSimpleName()), c.getClass().getSimpleName() + " is declared but not imported");
    }
}
