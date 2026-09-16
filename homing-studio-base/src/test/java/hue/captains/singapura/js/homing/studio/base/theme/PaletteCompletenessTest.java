package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0066 (from Issue 0001) — palette completeness, enforced. Components read
 * tokens via {@code var(--token)}; a theme that omits one silently degrades to
 * {@code unset}. Every registered theme's {@link GlobalColorPalette.Provision}
 * must bind exactly what {@link GlobalColorPalette.global_color_palette#declares()}
 * names — the palette declares, the provision binds, and the two agree.
 *
 * <p>Also gates the reverse drift: a theme binding a token the palette does
 * not declare is a token that escaped {@link StudioVars} (or a typo), and a new
 * token declared without updating every theme fails the first assertion for
 * all of them at once — exactly when it should. Episode 1 of the RFC keeps the
 * declared set flat; the law here is the one the conformance rules take over
 * when it moves to the graph.</p>
 */
class PaletteCompletenessTest {

    private static final Set<CssVar> DECLARED = new GlobalColorPalette.global_color_palette().declares();

    @Test
    void everyThemeBindsEveryDeclaredToken() {
        List<PaletteProvision<?, ?>> all = StudioThemeRegistry.INSTANCE.palettes();
        assertTrue(all.size() >= 9, "registry lost themes? found " + all.size());
        for (PaletteProvision<?, ?> palette : all) {
            var missing = new TreeSet<String>();
            for (CssVar v : DECLARED) {
                if (!palette.values().containsKey(v)) missing.add(v.name());
            }
            assertTrue(missing.isEmpty(),
                    palette.getClass().getEnclosingClass().getSimpleName()
                    + " does not bind: " + missing);
        }
    }

    @Test
    void noThemeBindsOutsideTheDeclaredSet() {
        for (PaletteProvision<?, ?> palette : StudioThemeRegistry.INSTANCE.palettes()) {
            var stray = new TreeSet<String>();
            for (CssVar v : palette.values().keySet()) {
                if (!DECLARED.contains(v)) stray.add(v.name());
            }
            assertTrue(stray.isEmpty(),
                    palette.getClass().getEnclosingClass().getSimpleName()
                    + " binds tokens the palette does not declare (escaped token or typo): " + stray);
        }
    }

    @Test
    void everyProvisionFillsTheGlobalPalette() {
        for (PaletteProvision<?, ?> palette : StudioThemeRegistry.INSTANCE.palettes()) {
            assertEquals(GlobalColorPalette.INSTANCE, palette.group(),
                    palette.getClass().getName() + " is registered as a palette but fills another group");
        }
    }

    @Test
    void themesAndPalettesStayPaired() {
        // themes() and palettes() are parallel lists — a theme added to one
        // but not the other would ship without a palette (or vice versa).
        assertEquals(StudioThemeRegistry.INSTANCE.themes().size(),
                     StudioThemeRegistry.INSTANCE.palettes().size(),
                     "themes() and palettes() must list the same themes");
    }
}
