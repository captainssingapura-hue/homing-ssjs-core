package hue.captains.singapura.js.homing.ui.focus;

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
class UiFocusCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(UiFocusCrate.INSTANCE));
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(UiFocusCrate.INSTANCE));
    }

    @Test
    void everyServedModuleKeepsItsLanesDiscipline() {
        int checked = 0;
        for (var entry : UiFocusCrate.INSTANCE.entries()) {
            if (!(entry.declaredType() instanceof StandardJsModuleType type) || type == StandardJsModuleType.GENERATED_CSS) continue;
            String m = entry.moduleClass();
            String src = String.join("\n", ResourceReader.INSTANCE.getStringsFromResource("homing/js/" + m.replace('.', '/') + ".js"));
            var findings = DefaultJsRulePolicy.INSTANCE.rulesFor(type).checkAll(ServedModule.of(m, type, src));
            assertEquals(List.of(), findings, () -> m + ": "
                    + findings.stream().map(f -> f.rule().value() + "@" + f.line() + ": " + f.message()).toList());
            checked++;
        }
        assertEquals(2, checked, "the two monitors; the styles are a sheet");
    }

    /** Every class the sheet declares is imported by one of the two monitors — the fm_ ones by the focus monitor, the sm_ ones by the steward monitor. */
    @Test
    void theMonitorsImportEveryClassDeclared() {
        var byFocus = FocusMonitorModule.INSTANCE.imports().getAllImports().values().stream()
                .flatMap(mi -> mi.allImports().stream()).map(e -> e.getClass().getSimpleName()).toList();
        var bySteward = StewardMonitorModule.INSTANCE.imports().getAllImports().values().stream()
                .flatMap(mi -> mi.allImports().stream()).map(e -> e.getClass().getSimpleName()).toList();
        for (CssClass<FocusStyles> c : FocusStyles.INSTANCE.cssClasses()) {
            String n = c.getClass().getSimpleName();
            assertTrue((n.startsWith("fm_") ? byFocus : bySteward).contains(n), n + " is declared but not imported by its monitor");
        }
    }
}
