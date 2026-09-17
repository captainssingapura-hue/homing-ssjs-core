package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.PaletteClass;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0066 (from Issue 0001) — palette completeness, enforced over every
 * palette the studio's registry declares as a prior: colour and type. Every
 * registered theme has exactly one provision per prior; each binds everything
 * its palette declares in the light binding, and re-binds nothing outside that
 * set in the dark one — the dark half was invisible to every test until it
 * became a map (RFC 0056).
 *
 * <p><b>Law 2 — extension without shadowing.</b> A theme may bind more than a
 * palette declares (Brutalist's {@code --bru-*} vocabulary), but an extra may
 * not sit in a family a palette declares ({@code --color-}, {@code --space-},
 * {@code --radius-}, {@code --font-}): that is a token that escaped the
 * vocabulary, or a typo, and it would shadow the vocabulary for every
 * downstream of the palette. Extras carry the theme's own prefix.</p>
 */
class PaletteCompletenessTest {

    private static final List<String> FAMILIES = List.of("--color-", "--space-", "--radius-", "--font-");

    /** The palette classes each prior declares, by group class. */
    private static Set<CssVar> declaredBy(CssGroup<?> palette) {
        var out = new HashSet<CssVar>();
        for (CssClass<?> c : palette.cssClasses()) if (c instanceof PaletteClass<?> p) out.addAll(p.declares());
        return out;
    }

    @Test
    void thePriorsAreColourAndType() {
        var priors = StudioThemeRegistry.INSTANCE.priors();
        assertEquals(List.of(GlobalColorPalette.INSTANCE, GlobalTypePalette.INSTANCE), priors,
                "the studio's priors are exactly its palettes, colour first");
    }

    @Test
    void everyThemeHasExactlyOneProvisionPerPrior() {
        for (Theme t : StudioThemeRegistry.INSTANCE.themes()) {
            for (CssGroup<?> prior : StudioThemeRegistry.INSTANCE.priors()) {
                long n = StudioThemeRegistry.INSTANCE.palettes().stream()
                        .filter(p -> p.theme().slug().equals(t.slug()) && p.group().getClass() == prior.getClass())
                        .count();
                assertEquals(1, n, t.slug() + " must provide " + prior.getClass().getSimpleName() + " exactly once");
            }
        }
    }

    @Test
    void everyProvisionBindsEveryDeclaredToken() {
        for (PaletteProvision<?, ?> p : StudioThemeRegistry.INSTANCE.palettes()) {
            var missing = new TreeSet<String>();
            for (CssVar v : declaredBy(p.group())) if (!p.values().containsKey(v)) missing.add(v.name());
            assertTrue(missing.isEmpty(), name(p) + " does not bind: " + missing);
        }
    }

    @Test
    void noProvisionBindsInADeclaredFamilyOutsideItsPalette() {
        for (PaletteProvision<?, ?> p : StudioThemeRegistry.INSTANCE.palettes()) {
            var declared = declaredBy(p.group());
            var stray = new TreeSet<String>();
            var keys = new HashSet<>(p.values().keySet());
            keys.addAll(p.darkValues().keySet());
            for (CssVar v : keys) {
                if (declared.contains(v)) continue;
                if (FAMILIES.stream().anyMatch(f -> v.name().startsWith(f))) stray.add(v.name());
            }
            assertTrue(stray.isEmpty(),
                    name(p) + " binds tokens in a declared family its palette does not declare"
                    + " (escaped token or typo): " + stray);
        }
    }

    @Test
    void aDarkBinding_reBindsOnlyWhatTheLightOneBound() {
        for (PaletteProvision<?, ?> p : StudioThemeRegistry.INSTANCE.palettes()) {
            var stray = new TreeSet<String>();
            for (CssVar v : p.darkValues().keySet()) if (!p.values().containsKey(v)) stray.add(v.name());
            assertTrue(stray.isEmpty(), name(p) + " re-binds in dark what it never bound in light: " + stray);
        }
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
        return p.getClass().getEnclosingClass().getSimpleName() + "." + p.getClass().getSimpleName();
    }
}
