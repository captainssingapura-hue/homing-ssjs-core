package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0066 (from Issue 0001) — palette completeness, enforced. Components read
 * tokens via {@code var(--token)}; a theme that omits one silently degrades to
 * {@code unset}. Every registered theme's {@link GlobalColorPalette.Provision}
 * must bind everything {@link GlobalColorPalette.global_color_palette#declares()}
 * names in its light binding, and re-bind nothing outside that set in its dark
 * one — the dark half was invisible to every test until it became a map
 * (RFC 0056).
 *
 * <p><b>Law 2 — extension without shadowing.</b> A theme may bind more than the
 * palette declares (Brutalist's {@code --bru-*} vocabulary), but an extra may
 * not sit in a family the palette declares ({@code --color-}, {@code --space-},
 * {@code --radius-}): that is a token that escaped {@link HomingVars}, or a
 * typo, and it would shadow the vocabulary for every downstream of the
 * palette. Extras carry the theme's own prefix.</p>
 */
class PaletteCompletenessTest {

    private static final Set<CssVar> DECLARED = new GlobalColorPalette.global_color_palette().declares();
    private static final List<String> FAMILIES = List.of("--color-", "--space-", "--radius-");

    @Test
    void everyThemeBindsEveryDeclaredToken() {
        List<PaletteProvision<?, ?>> all = StudioThemeRegistry.INSTANCE.palettes();
        assertTrue(all.size() >= 9, "registry lost themes? found " + all.size());
        for (PaletteProvision<?, ?> palette : all) {
            var missing = new TreeSet<String>();
            for (CssVar v : DECLARED) {
                if (!palette.values().containsKey(v)) missing.add(v.name());
            }
            assertTrue(missing.isEmpty(), name(palette) + " does not bind: " + missing);
        }
    }

    @Test
    void noThemeBindsInADeclaredFamilyOutsideTheDeclaredSet() {
        for (PaletteProvision<?, ?> palette : StudioThemeRegistry.INSTANCE.palettes()) {
            var stray = new TreeSet<String>();
            var keys = new HashSet<>(palette.values().keySet());
            keys.addAll(palette.darkValues().keySet());
            for (CssVar v : keys) {
                if (DECLARED.contains(v)) continue;
                if (FAMILIES.stream().anyMatch(f -> v.name().startsWith(f))) stray.add(v.name());
            }
            assertTrue(stray.isEmpty(),
                    name(palette) + " binds tokens in a declared family the palette does not declare"
                    + " (escaped token or typo): " + stray);
        }
    }

    @Test
    void aDarkBinding_reBindsOnlyDeclaredTokens_orTheThemesOwnExtras() {
        for (PaletteProvision<?, ?> palette : StudioThemeRegistry.INSTANCE.palettes()) {
            var stray = new TreeSet<String>();
            for (CssVar v : palette.darkValues().keySet()) {
                if (!DECLARED.contains(v) && !palette.values().containsKey(v)) stray.add(v.name());
            }
            assertTrue(stray.isEmpty(), name(palette) + " re-binds in dark what it never bound in light: " + stray);
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

    @Test
    void everyOverride_speaksForARegisteredTheme() {
        var slugs = new HashSet<String>();
        StudioThemeRegistry.INSTANCE.themes().forEach(t -> slugs.add(t.slug()));
        for (CssGroupImpl<?, ?> impl : StudioThemeRegistry.INSTANCE.overrides()) {
            assertTrue(slugs.contains(impl.theme().slug()),
                    impl.getClass().getName() + " overrides for a theme the registry does not list: " + impl.theme().slug());
        }
    }

    private static String name(PaletteProvision<?, ?> p) {
        return p.getClass().getEnclosingClass().getSimpleName();
    }
}
