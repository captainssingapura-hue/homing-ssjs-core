package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.theme.type.HomingFonts;

import java.util.Map;

/**
 * Glassmorphism — plates of frosted glass over an aurora. Every raised plate
 * and the masthead blur what is behind them, rimmed by a hairline of light,
 * on a soft wide shadow, corners well rounded; a selected thing is a clearer
 * pane lit along its top edge. {@link GlassmorphismDesign} is the frosting,
 * {@link FrostPalette} what the glass is made of and what shows through it.
 */
public record HomingGlassmorphism() implements Design {

    public static final DesignId ID = new DesignId("glassmorphism");
    public static final HomingGlassmorphism INSTANCE = new HomingGlassmorphism();

    /** Over Default: the frosting where it has one, Default's for the rest; Frost on the colour plane. */
    @Override public Impl impl(DesignClass<?> pair) {
        if (pair.onColourPlane()) return FrostPalette.INSTANCE.impl(pair);
        Impl own = GlassmorphismDesign.WORDS.get(pair);
        return own != null ? own : HomingEditorial.INSTANCE.impl(pair);
    }

    /** Worn in Frost by default; Frost is a palette in its own name, so any physique may wear it. */
    @Override public hue.captains.singapura.js.homing.design.Palette palette() { return FrostPalette.INSTANCE; }

    @Override public DesignId id() { return ID; }
    @Override public String label() { return "Glassmorphism"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "Plates of frosted glass over an aurora — blurred, rimmed in light, lifted on soft shadow."; }

    /** The legacy palette, for the groups downstream that still read tokens; Frost's opaque roles. */
    public record Palette() implements GlobalColorPalette.Provision<HomingGlassmorphism> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingGlassmorphism theme() { return HomingGlassmorphism.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
        @Override public Map<CssVar, String> darkValues() { return DARK; }

        private static final Map<CssVar, String> DARK = Map.ofEntries(
                Map.entry(HomingVars.COLOR_SURFACE,                FrostPalette.PAGE_D),
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,         FrostPalette.GLASS_D),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED,       FrostPalette.THIN_D),
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED,       FrostPalette.SMOKE_D),
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           FrostPalette.TEXT_D),
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             FrostPalette.MUTED_D),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       FrostPalette.ON_SMOKE),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, FrostPalette.ON_SMOKE_MUTED),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,             "#FFFFFF"),
                Map.entry(HomingVars.COLOR_TEXT_LINK,              FrostPalette.INK_BLUE_D),
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        FrostPalette.VIOLET_D),
                Map.entry(HomingVars.COLOR_BORDER,                 FrostPalette.HAIR_D),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,        FrostPalette.BLUE_D),
                Map.entry(HomingVars.COLOR_ACCENT,                 FrostPalette.BLUE_D),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,        FrostPalette.VIOLET_D),
                Map.entry(HomingVars.COLOR_ACCENT_ON,              FrostPalette.ON_BLUE)
        );

        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                Map.entry(HomingVars.COLOR_SURFACE,                FrostPalette.PAGE),
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,         FrostPalette.GLASS),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED,       FrostPalette.THIN),
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED,       FrostPalette.SMOKE),
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           FrostPalette.TEXT),
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             FrostPalette.MUTED),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       FrostPalette.ON_SMOKE),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, FrostPalette.ON_SMOKE_MUTED),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,             FrostPalette.TEXT),
                Map.entry(HomingVars.COLOR_TEXT_LINK,              FrostPalette.INK_BLUE),
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        FrostPalette.VIOLET),
                Map.entry(HomingVars.COLOR_BORDER,                 FrostPalette.HAIR),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,        FrostPalette.BLUE),
                Map.entry(HomingVars.COLOR_ACCENT,                 FrostPalette.BLUE),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,        FrostPalette.VIOLET),
                Map.entry(HomingVars.COLOR_ACCENT_ON,              FrostPalette.ON_BLUE),
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
                Map.entry(HomingVars.RADIUS_LG, "18px")
        );
    }

    /** A clean grotesque throughout; code keeps the house mono. */
    public record Fonts() implements GlobalTypePalette.Provision<HomingGlassmorphism> {
        public static final Fonts INSTANCE = new Fonts();
        @Override public HomingGlassmorphism theme() { return HomingGlassmorphism.INSTANCE; }
        @Override public Map<CssVar, String> values() {
            return Map.of(HomingFonts.FONT_BODY,    GlassmorphismDesign.BODY_FACE,
                          HomingFonts.FONT_DISPLAY, GlassmorphismDesign.DISPLAY_FACE,
                          HomingFonts.FONT_MONO,    StudioFonts.MONO);
        }
    }
}
