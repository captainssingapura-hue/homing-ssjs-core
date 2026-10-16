package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.semantic.ResolvingColours;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Neon;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Night;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Paper;
import hue.captains.singapura.js.homing.design.styles.ink.InkStyles;
import hue.captains.singapura.js.homing.design.styles.loudly.LoudlyStyles;
import hue.captains.singapura.js.homing.design.styles.plain.PlainStyles;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Ink;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Loudly;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An instance's styling (RFC 0067, appendix The Style Jars): the second stage of its registration - the styles it
 * serves over the manager's registrations, its palettes, each style's own - serving each sheet by its own slug, and the
 * themes by theirs.
 */
class StylingTest {

    private static final StyleManager MANAGER = StyleManager.of(StyledTaxonomy.INSTANCE.read(), List.of(ResolvingColours.INSTANCE, ResolvingColours.LoudColours.INSTANCE), List.of(
            StyleRegistration.of(Plain.INSTANCE, PlainStyles.INSTANCE), StyleRegistration.of(Ink.INSTANCE, InkStyles.INSTANCE),
            StyleRegistration.of(Loudly.INSTANCE, LoudlyStyles.INSTANCE)));

    /** Ink and Loudly served, Plain only their base layer. */
    private static final Styling STYLING = new Styling(MANAGER, List.of(Ink.INSTANCE, Loudly.INSTANCE),
            List.of(Night.INSTANCE, Paper.INSTANCE, Neon.INSTANCE), Map.of(Ink.INSTANCE, Night.INSTANCE, Loudly.INSTANCE, Neon.INSTANCE));

    @Test
    void aStyling_servesEachSheetByItsOwnSlug_theFirstOfEachTheDefault() {
        assertEquals(Ink.INSTANCE, STYLING.defaultStyle());
        assertEquals(Night.INSTANCE, STYLING.defaultPalette());
        assertEquals(MANAGER.sheet(Ink.INSTANCE), STYLING.styleSheet("ink"), "the manager's aggregation");
        assertTrue(STYLING.paletteSheet("night").contains("    --fp-copy: oklch(0.93 0.01 260);\n"));
        assertEquals("no style served is plain", assertThrows(IllegalArgumentException.class, () -> STYLING.styleSheet("plain")).getMessage(),
                "a base layer registered is not served for it");
        assertEquals("no palette served is dusk", assertThrows(IllegalArgumentException.class, () -> STYLING.paletteSheet("dusk")).getMessage());
    }

    @Test
    void theThemes_eachStyleInItsOwnFirst_thenInEveryPaletteThatFitsIt() {
        var themes = STYLING.themes();
        assertEquals(List.of("ink", "loudly"), themes.keySet().stream().limit(2).toList(), "each style in its own palette first");
        assertEquals(Paper.INSTANCE, themes.get("ink_paper").palette());
        assertEquals("ink_paper", STYLING.slug(Ink.INSTANCE, Paper.INSTANCE));
        assertEquals("ink", STYLING.slug(themes.get("ink")));
        assertFalse(themes.containsKey("loudly_paper"), "a palette of the base scheme cannot dress a loud style");
        assertFalse(STYLING.fits(Loudly.INSTANCE, Paper.INSTANCE));
        assertTrue(STYLING.fits(Ink.INSTANCE, Paper.INSTANCE));
    }

    @Test
    void aSlug_isWornAsItsStyleInItsPalette_elseTheStylesOwn_elseTheDefault() {
        assertEquals(Paper.INSTANCE, STYLING.dressed("ink_paper").palette());
        assertEquals(Night.INSTANCE, STYLING.dressed("ink_dusk").palette(), "a palette not served: the style's own");
        assertEquals(Neon.INSTANCE, STYLING.dressed("loudly_paper").palette(), "one that does not fit: the style's own");
        assertEquals(Ink.INSTANCE, STYLING.dressed("baroque").style(), "a style not served: the default");
        assertEquals(Night.INSTANCE, STYLING.dressed(null).palette());
    }

    @Test
    void refused_nothing_twoOfOneToken_aStyleNotRegistered_noOwnPalette_orOneNotServed() {
        String nothing = assertThrows(IllegalArgumentException.class, () -> new Styling(MANAGER, List.of(), List.of(), Map.of())).getMessage();
        assertEquals("a styling cannot be made: it serves no style; it serves no palette", nothing);
        String twice = assertThrows(IllegalArgumentException.class, () -> new Styling(MANAGER, List.of(Ink.INSTANCE, Ink.INSTANCE),
                List.of(Night.INSTANCE), Map.of(Ink.INSTANCE, Night.INSTANCE))).getMessage();
        assertTrue(twice.contains("two styles are ink"), twice);
        StyleManager plainOnly = StyleManager.of(StyledTaxonomy.INSTANCE.read(), List.of(ResolvingColours.INSTANCE),
                List.of(StyleRegistration.of(Plain.INSTANCE, PlainStyles.INSTANCE)));
        String unregistered = assertThrows(IllegalArgumentException.class, () -> new Styling(plainOnly, List.of(Ink.INSTANCE),
                List.of(Night.INSTANCE), Map.of(Ink.INSTANCE, Night.INSTANCE))).getMessage();
        assertTrue(unregistered.contains("ink is served, and the manager does not register it"), unregistered);
        String noOwn = assertThrows(IllegalArgumentException.class, () -> new Styling(MANAGER, List.of(Ink.INSTANCE, Loudly.INSTANCE),
                List.of(Night.INSTANCE), Map.of(Ink.INSTANCE, Paper.INSTANCE))).getMessage();
        assertTrue(noOwn.contains("ink's own palette, paper, is not served") && noOwn.contains("loudly has no palette of its own"), noOwn);
        String misfit = assertThrows(IllegalArgumentException.class, () -> new Styling(MANAGER, List.of(Loudly.INSTANCE),
                List.of(Paper.INSTANCE), Map.of(Loudly.INSTANCE, Paper.INSTANCE))).getMessage();
        assertTrue(misfit.contains("loudly's own palette, paper, does not fit it"), misfit);
    }
}
