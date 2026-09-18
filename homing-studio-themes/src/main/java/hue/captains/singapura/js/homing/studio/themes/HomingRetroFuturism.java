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
 * Retro-futurism — the future as 1984 drew it. Everything is outlined in neon
 * and glows; a title is wide, upright, capitalised and haloed; a selected
 * thing burns brighter. {@link RetroFuturismDesign} is the neon,
 * {@link SynthwavePalette} which neon, and the sun and the grid behind it.
 */
public record HomingRetroFuturism() implements Design {

    public static final HomingRetroFuturism INSTANCE = new HomingRetroFuturism();

    /** Over Default: the neon where it has one, Default's for the rest; Synthwave on the colour plane. */
    @Override public Impl impl(DesignClass<?> pair) {
        if (pair.onColourPlane()) return SynthwavePalette.INSTANCE.impl(pair);
        Impl own = RetroFuturismDesign.WORDS.get(pair);
        return own != null ? own : HomingDefault.INSTANCE.impl(pair);
    }

    /** Worn in Synthwave by default; a palette in its own name, so any physique may wear it. */
    @Override public hue.captains.singapura.js.homing.design.Palette palette() { return SynthwavePalette.INSTANCE; }

    @Override public String slug()  { return "retro-futurism"; }
    @Override public String label() { return "Retro-Futurism"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "A magenta sun over a cyan grid — everything outlined in neon and glowing."; }

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
