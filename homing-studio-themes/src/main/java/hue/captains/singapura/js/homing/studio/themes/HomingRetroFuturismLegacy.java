package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.theme.type.HomingFonts;
import java.util.Map;
import hue.captains.singapura.js.homing.designs.RetroFuturismDesign;
import hue.captains.singapura.js.homing.designs.SynthwavePalette;
import hue.captains.singapura.js.homing.designs.HomingRetroFuturism;

/**
 * The LEGACY palettes {@link HomingRetroFuturism} provides: its values for the global
 * colour palette ({@code --color-*}, spacing, radius) and the global type
 * palette. They are the studio's to keep - they pass through beside the design
 * until the last studio group wears design words - and the design itself lives
 * in {@code homing-designs}, which knows nothing of them.
 */
public final class HomingRetroFuturismLegacy {

    private HomingRetroFuturismLegacy() {}


    /** The legacy palette, for the groups downstream that still read tokens; Synthwave's roles. */
    public record Palette() implements GlobalColorPalette.Provision<HomingRetroFuturism> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingRetroFuturism theme() { return HomingRetroFuturism.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
        @Override public Map<CssVar, String> darkValues() { return DARK; }

        private static final Map<CssVar, String> DARK = Map.ofEntries(
                Map.entry(HomingVars.COLOR_SURFACE,                SynthwavePalette.SKY_D),
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,         SynthwavePalette.PLATE_D),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED,       SynthwavePalette.WELL_D),
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED,       SynthwavePalette.VOID_D),
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           SynthwavePalette.TEXT_D),
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             SynthwavePalette.MUTED_D),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       SynthwavePalette.ON_VOID),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, SynthwavePalette.ON_VOID_MUTED),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,             SynthwavePalette.MAGENTA_D),
                Map.entry(HomingVars.COLOR_TEXT_LINK,              SynthwavePalette.CYAN_D),
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        SynthwavePalette.MAGENTA_D),
                Map.entry(HomingVars.COLOR_BORDER,                 SynthwavePalette.HAIR_D),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,        SynthwavePalette.MAGENTA_D),
                Map.entry(HomingVars.COLOR_ACCENT,                 SynthwavePalette.MAGENTA_D),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,        SynthwavePalette.MAGENTA_D),
                Map.entry(HomingVars.COLOR_ACCENT_ON,              SynthwavePalette.ON_MAGENTA)
        );

        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                Map.entry(HomingVars.COLOR_SURFACE,                SynthwavePalette.SKY),
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,         SynthwavePalette.PLATE),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED,       SynthwavePalette.WELL),
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED,       SynthwavePalette.VOID),
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           SynthwavePalette.TEXT),
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             SynthwavePalette.MUTED),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       SynthwavePalette.ON_VOID),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, SynthwavePalette.ON_VOID_MUTED),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,             SynthwavePalette.TEXT),
                Map.entry(HomingVars.COLOR_TEXT_LINK,              SynthwavePalette.CYAN),
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        SynthwavePalette.MAGENTA),
                Map.entry(HomingVars.COLOR_BORDER,                 SynthwavePalette.HAIR),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,        SynthwavePalette.MAGENTA),
                Map.entry(HomingVars.COLOR_ACCENT,                 SynthwavePalette.MAGENTA),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,        SynthwavePalette.MAGENTA),
                Map.entry(HomingVars.COLOR_ACCENT_ON,              SynthwavePalette.ON_MAGENTA),
                Map.entry(HomingVars.SPACE_1, "4px"),
                Map.entry(HomingVars.SPACE_2, "8px"),
                Map.entry(HomingVars.SPACE_3, "12px"),
                Map.entry(HomingVars.SPACE_4, "16px"),
                Map.entry(HomingVars.SPACE_5, "20px"),
                Map.entry(HomingVars.SPACE_6, "24px"),
                Map.entry(HomingVars.SPACE_7, "32px"),
                Map.entry(HomingVars.SPACE_8, "40px"),
                Map.entry(HomingVars.RADIUS_SM, "2px"),
                Map.entry(HomingVars.RADIUS_MD, "3px"),
                Map.entry(HomingVars.RADIUS_LG, "4px")
        );
    }

    /** A wide geometric display over a condensed body; code keeps the house mono. */
    public record Fonts() implements GlobalTypePalette.Provision<HomingRetroFuturism> {
        public static final Fonts INSTANCE = new Fonts();
        @Override public HomingRetroFuturism theme() { return HomingRetroFuturism.INSTANCE; }
        @Override public Map<CssVar, String> values() {
            return Map.of(HomingFonts.FONT_BODY,    RetroFuturismDesign.BODY_FACE,
                          HomingFonts.FONT_DISPLAY, RetroFuturismDesign.DISPLAY_FACE,
                          HomingFonts.FONT_MONO,    StudioFonts.MONO);
        }
    }
}
