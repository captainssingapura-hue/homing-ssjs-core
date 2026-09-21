package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.conformance.rules.ServedModule;
import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The crate holds every served module, imports only what it requires, keeps its lanes, and declares no class it does not import. */
class UiMenuCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(UiMenuCrate.INSTANCE));
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(UiMenuCrate.INSTANCE));
    }

    @Test
    void everyServedModuleKeepsItsLanesDiscipline() {
        int checked = 0;
        for (var entry : UiMenuCrate.INSTANCE.entries()) {
            if (!(entry.declaredType() instanceof StandardJsModuleType type) || type == StandardJsModuleType.GENERATED_CSS) continue;
            String m = entry.moduleClass();
            String src = String.join("\n", ResourceReader.INSTANCE.getStringsFromResource("homing/js/" + m.replace('.', '/') + ".js"));
            var findings = DefaultJsRulePolicy.INSTANCE.rulesFor(type).checkAll(ServedModule.of(m, type, src));
            assertEquals(List.of(), findings, () -> m + ": "
                    + findings.stream().map(f -> f.rule().value() + "@" + f.line() + ": " + f.message()).toList());
            checked++;
        }
        assertEquals(4, checked, "the steward, the menu, the events and the geometry");
    }

    @Test
    void theStewardAndTheMenuImportEveryClassDeclared() {
        var imported = java.util.stream.Stream.of(ContextMenuStewardModule.INSTANCE.imports(), ContextMenuModule.INSTANCE.imports())
                .flatMap(im -> im.getAllImports().values().stream())
                .flatMap(mi -> mi.allImports().stream()).map(e -> e.getClass().getSimpleName()).toList();
        for (CssClass<MenuStyles> c : MenuStyles.INSTANCE.cssClasses())
            assertTrue(imported.contains(c.getClass().getSimpleName()), c.getClass().getSimpleName() + " is declared but not imported");
    }
}
