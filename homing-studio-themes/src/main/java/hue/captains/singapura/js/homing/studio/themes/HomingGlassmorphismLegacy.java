package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.theme.type.HomingFonts;
import java.util.Map;
import hue.captains.singapura.js.homing.designs.FrostPalette;
import hue.captains.singapura.js.homing.designs.GlassmorphismDesign;
import hue.captains.singapura.js.homing.designs.HomingGlassmorphism;

/**
 * The LEGACY palettes {@link HomingGlassmorphism} provides: its values for the global
 * colour palette ({@code --color-*}, spacing, radius) and the global type
 * palette. They are the studio's to keep - they pass through beside the design
 * until the last studio group wears design words - and the design itself lives
 * in {@code homing-designs}, which knows nothing of them.
 */
public final class HomingGlassmorphismLegacy {

    private HomingGlassmorphismLegacy() {}


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
