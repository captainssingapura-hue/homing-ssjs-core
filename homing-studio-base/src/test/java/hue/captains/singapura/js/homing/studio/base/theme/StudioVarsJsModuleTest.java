package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Exportable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drift detector: every {@link StudioVars} public static {@link CssVar}
 * field must have a matching inner record export in
 * {@link StudioVarsJsModule}. Adding a token without adding the export
 * record (or vice versa) fails this test, surfacing the omission at
 * build time rather than at the next consumer's import.
 *
 * <p>Also verifies the generated {@code selfContent} body contains the
 * expected {@code const NAME = 'var(--…)';} line for every token — the
 * JS module's body and its exports must be consistent.</p>
 */
class StudioVarsJsModuleTest {

    @Test
    void everyStudioVarsFieldHasAMatchingExportRecord() {
        Set<String> javaFields    = new TreeSet<>();
        Set<String> exportRecords = new TreeSet<>();

        for (Field f : StudioVars.class.getDeclaredFields()) {
            int m = f.getModifiers();
            if (Modifier.isPublic(m) && Modifier.isStatic(m)
                    && CssVar.class.equals(f.getType())) {
                javaFields.add(f.getName());
            }
        }
        for (Exportable<?> e : StudioVarsJsModule.INSTANCE.exports().exports()) {
            exportRecords.add(e.getClass().getSimpleName());
        }
        assertEquals(javaFields, exportRecords,
                "StudioVars fields and StudioVarsJsModule export records "
              + "must be in sync — drift breaks consumer imports.");
    }

    @Test
    void selfContentEmitsConstLineForEveryToken() {
        var lines = StudioVarsJsModule.INSTANCE.selfContent(null);
        Set<String> covered = new HashSet<>();
        for (String line : lines) {
            if (!line.startsWith("const ")) continue;
            // const NAME = 'var(--foo)';
            int eq = line.indexOf(" = ");
            if (eq < 0) continue;
            String name = line.substring("const ".length(), eq);
            covered.add(name);
        }
        for (Field f : StudioVars.class.getDeclaredFields()) {
            int m = f.getModifiers();
            if (!Modifier.isPublic(m) || !Modifier.isStatic(m)) continue;
            if (!CssVar.class.equals(f.getType())) continue;
            assertTrue(covered.contains(f.getName()),
                    "selfContent missing const for " + f.getName());
        }
    }

    @Test
    void constLineMatchesCssVarRef() throws Exception {
        var lines = StudioVarsJsModule.INSTANCE.selfContent(null);
        for (Field f : StudioVars.class.getDeclaredFields()) {
            int m = f.getModifiers();
            if (!Modifier.isPublic(m) || !Modifier.isStatic(m)) continue;
            if (!CssVar.class.equals(f.getType())) continue;
            CssVar v = (CssVar) f.get(null);
            String expected = "const " + f.getName() + " = '" + v.ref() + "';";
            boolean found = false;
            for (String line : lines) if (line.equals(expected)) { found = true; break; }
            assertTrue(found, "missing line: " + expected);
        }
    }
}
