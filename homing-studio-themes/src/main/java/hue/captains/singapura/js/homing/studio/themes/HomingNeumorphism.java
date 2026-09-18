package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.theme.type.HomingFonts;

import java.util.Map;

/**
 * Neumorphism — one material, moulded. A raised thing is pressed out of the
 * page, a selected one pressed into it: two shadows, the surface's own tone
 * turned toward the light and away from it, and no line drawn anywhere.
 * Every corner is rounded; the type is round and bold.
 *
 * <p>The fourth design, and the first written as a physique and a palette
 * from the start: {@link NeumorphismDesign} moulds whatever surface a palette
 * provides, and {@link SeedPalette#CLAY} is the classic grey-blue clay it is
 * worn in by default.</p>
 */
public record HomingNeumorphism() implements Design {

    public static final HomingNeumorphism INSTANCE = new HomingNeumorphism();

    /** Over Default: the mould where it has one, Default's for the rest; the clay on the colour plane. */
    @Override public Impl impl(DesignClass<?> pair) {
        if (pair.onColourPlane()) return SeedPalette.CLAY.impl(pair);
        Impl own = NeumorphismDesign.WORDS.get(pair);
        return own != null ? own : HomingDefault.INSTANCE.impl(pair);
    }

    /** Worn in the clay by default; the clay is a palette in its own name, so any physique may wear it. */
    @Override public hue.captains.singapura.js.homing.design.Palette palette() { return SeedPalette.CLAY; }

    @Override public String slug()  { return "neumorphism"; }
    @Override public String label() { return "Neumorphism"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "One material, moulded — pressed out of the page or into it, never drawn on."; }

    /** The legacy palette, for the groups downstream that still read tokens; the clay's own seeds. */
    public record Palette() implements GlobalColorPalette.Provision<HomingNeumorphism> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingNeumorphism theme() { return HomingNeumorphism.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
        @Override public Map<CssVar, String> darkValues() { return DARK; }

        private static final SeedPalette.Seeds L = SeedPalette.CLAY.light(), D = SeedPalette.CLAY.dark();

        private static final Map<CssVar, String> DARK = Map.ofEntries(
                Map.entry(HomingVars.COLOR_SURFACE,                D.surface()),
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,         D.raised()),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED,       D.recessed()),
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED,       D.inverted()),
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           D.text()),
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             D.muted()),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       D.onInverted()),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, D.onInvertedMuted()),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,             D.title()),
                Map.entry(HomingVars.COLOR_TEXT_LINK,              D.accentEmphasis()),
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        D.accent()),
                Map.entry(HomingVars.COLOR_BORDER,                 D.border()),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,        D.accent()),
                Map.entry(HomingVars.COLOR_ACCENT,                 D.accent()),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,        D.accentEmphasis()),
                Map.entry(HomingVars.COLOR_ACCENT_ON,              D.accentOn())
        );

        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                Map.entry(HomingVars.COLOR_SURFACE,                L.surface()),
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,         L.raised()),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED,       L.recessed()),
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED,       L.inverted()),
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           L.text()),
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             L.muted()),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       L.onInverted()),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, L.onInvertedMuted()),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,             L.title()),
                Map.entry(HomingVars.COLOR_TEXT_LINK,              L.accentEmphasis()),
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        L.accent()),
                Map.entry(HomingVars.COLOR_BORDER,                 L.border()),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,        L.accent()),
                Map.entry(HomingVars.COLOR_ACCENT,                 L.accent()),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,        L.accentEmphasis()),
                Map.entry(HomingVars.COLOR_ACCENT_ON,              L.accentOn()),
                Map.entry(HomingVars.SPACE_1, "4px"),
                Map.entry(HomingVars.SPACE_2, "8px"),
                Map.entry(HomingVars.SPACE_3, "12px"),
                Map.entry(HomingVars.SPACE_4, "16px"),
                Map.entry(HomingVars.SPACE_5, "20px"),
                Map.entry(HomingVars.SPACE_6, "24px"),
                Map.entry(HomingVars.SPACE_7, "32px"),
                Map.entry(HomingVars.SPACE_8, "40px"),
                Map.entry(HomingVars.RADIUS_SM, "8px"),
                Map.entry(HomingVars.RADIUS_MD, "12px"),
                Map.entry(HomingVars.RADIUS_LG, "16px")
        );
    }

    /** A round humanist sans throughout; code keeps the house mono. */
    public record Fonts() implements GlobalTypePalette.Provision<HomingNeumorphism> {
        public static final Fonts INSTANCE = new Fonts();
        @Override public HomingNeumorphism theme() { return HomingNeumorphism.INSTANCE; }
        @Override public Map<CssVar, String> values() {
            return Map.of(HomingFonts.FONT_BODY,    NeumorphismDesign.BODY_FACE,
                          HomingFonts.FONT_DISPLAY, NeumorphismDesign.DISPLAY_FACE,
                          HomingFonts.FONT_MONO,    StudioFonts.MONO);
        }
    }
}
