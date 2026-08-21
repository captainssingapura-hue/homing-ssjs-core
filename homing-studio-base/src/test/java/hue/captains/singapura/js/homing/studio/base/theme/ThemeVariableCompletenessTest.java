package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.ThemeVariables;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue 0001 — theme-variable completeness, enforced. Components reference
 * semantic tokens via {@code var(--token)}; a theme that omits one silently
 * degrades to the CSS fallback. This test turns "should work for all themes"
 * into "cannot ship a theme that doesn't": every registered theme's
 * {@code values()} must cover the whole {@link StudioVars#ALL} vocabulary.
 *
 * <p>Also gates the reverse drift: a theme defining a var that is not in the
 * vocabulary is a token that escaped {@link StudioVars} (or a typo), and a new
 * {@code StudioVars} token added without updating every theme fails the first
 * assertion for all of them at once — exactly when it should.</p>
 */
class ThemeVariableCompletenessTest {

    @Test
    void everyThemeDefinesEveryStudioVar() {
        List<ThemeVariables<?>> all = StudioThemeRegistry.INSTANCE.variables();
        assertTrue(all.size() >= 9, "registry lost themes? found " + all.size());
        for (ThemeVariables<?> vars : all) {
            var missing = new TreeSet<String>();
            for (CssVar v : StudioVars.ALL) {
                if (!vars.values().containsKey(v)) missing.add(v.name());
            }
            assertTrue(missing.isEmpty(),
                    vars.getClass().getEnclosingClass().getSimpleName()
                    + " is missing StudioVars: " + missing);
        }
    }

    @Test
    void noThemeDefinesVarsOutsideTheVocabulary() {
        for (ThemeVariables<?> vars : StudioThemeRegistry.INSTANCE.variables()) {
            var stray = new TreeSet<String>();
            for (CssVar v : vars.values().keySet()) {
                if (!StudioVars.ALL.contains(v)) stray.add(v.name());
            }
            assertTrue(stray.isEmpty(),
                    vars.getClass().getEnclosingClass().getSimpleName()
                    + " defines vars outside StudioVars.ALL (escaped token or typo): " + stray);
        }
    }

    @Test
    void themesAndVariablesStayPaired() {
        // themes() and variables() are parallel lists — a theme added to one
        // but not the other would ship without a palette (or vice versa).
        assertEquals(StudioThemeRegistry.INSTANCE.themes().size(),
                     StudioThemeRegistry.INSTANCE.variables().size(),
                     "themes() and variables() must list the same themes");
    }
}
