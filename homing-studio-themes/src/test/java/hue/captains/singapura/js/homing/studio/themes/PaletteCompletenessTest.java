package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.conformance.rules.CssConformance;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.ThemeColorCrate;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.theme.type.ThemeTypeCrate;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0066 — the completeness law over the studio's eleven, as the conformance
 * rules state it: every theme provides every prior exactly once, binds what the
 * palette declares, re-binds in dark only what it bound in light, and extends
 * only outside the declared families. One call; the rules are the test.
 */
class PaletteCompletenessTest {

    @Test
    void thePriorsAreColourAndType() {
        assertEquals(List.of(GlobalColorPalette.INSTANCE, GlobalTypePalette.INSTANCE),
                StudioThemeRegistry.INSTANCE.priors(), "the studio's priors are exactly its palettes, colour first");
    }

    @Test
    void everyThemeIsComplete_byTheRules() {
        List<Finding> findings = CssConformance.check(
                List.of(ThemeColorCrate.INSTANCE, ThemeTypeCrate.INSTANCE),
                StudioWorkspaceThemes.INSTANCE.palettes());
        assertEquals(List.of(), findings, () -> findings.stream().map(Finding::fingerprint).toList().toString());
    }

    @Test
    void everyOverride_speaksForARegisteredTheme() {
        var slugs = new HashSet<String>();
        StudioWorkspaceThemes.INSTANCE.themes().forEach(t -> slugs.add(t.slug()));
        for (CssGroupImpl<?, ?> impl : StudioWorkspaceThemes.INSTANCE.overrides()) {
            assertTrue(slugs.contains(impl.theme().slug()),
                    impl.getClass().getName() + " overrides for a theme the registry does not list: " + impl.theme().slug());
        }
    }
}
