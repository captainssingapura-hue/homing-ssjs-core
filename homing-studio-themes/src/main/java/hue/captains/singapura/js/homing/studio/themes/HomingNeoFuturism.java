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
 * Neo-Futurism — cool glass over deep space: near-white plates in light,
 * near-black in dark, every edge a filament of one electric cyan, the
 * primary action a solid bar of it. Violet is the second voice, magenta the
 * alarm. A wide geometric display set light and tracked; a slow decelerating
 * ease that glows rather than snaps.
 *
 * <p>The third design, and the first written on the design classes from the
 * start: no override ever existed for it, so it is exactly the sum of its
 * words in {@link NeoFuturismDesign} over {@link HomingDefault}'s.</p>
 *
 * <p>Dark mode is the native register — the light mode is the same design
 * seen in daylight, the glow kept but dimmed to what a white page can carry.
 * The masthead stays space in both; only the filament beneath it divides it
 * from the page.</p>
 */
public record HomingNeoFuturism() implements Design {

    public static final HomingNeoFuturism INSTANCE = new HomingNeoFuturism();

    /** Over Default: its own word where it has one, Default's for the rest — the base is a plain call. */
    @Override public Impl impl(DesignClass<?> pair) {
        Impl own = NeoFuturismDesign.WORDS.get(pair);
        return own != null ? own : HomingDefault.INSTANCE.impl(pair);
    }

    @Override public String slug()  { return "neo-futurism"; }
    @Override public String label() { return "Neo-Futurism"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "Cool glass over deep space — one electric cyan, every edge a filament."; }

    /** The legacy palette, for the groups downstream that still read tokens; the same colours as the words. */
    public record Palette() implements GlobalColorPalette.Provision<HomingNeoFuturism> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingNeoFuturism theme() { return HomingNeoFuturism.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
        @Override public Map<CssVar, String> darkValues() { return DARK; }

        private static final Map<CssVar, String> DARK = Map.ofEntries(
                Map.entry(HomingVars.COLOR_SURFACE,                NeoFuturismDesign.SURFACE_D),
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,         NeoFuturismDesign.RAISED_D),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED,       NeoFuturismDesign.RECESSED_D),
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED,       NeoFuturismDesign.INVERTED_D),
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           NeoFuturismDesign.TEXT_D),
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             NeoFuturismDesign.MUTED_D),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       NeoFuturismDesign.ON_INVERTED_D),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, NeoFuturismDesign.ON_INVERTED_MUTED_D),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,             NeoFuturismDesign.TEXT_D),
                Map.entry(HomingVars.COLOR_TEXT_LINK,              NeoFuturismDesign.CYAN_INK_D),
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        NeoFuturismDesign.VIOLET_D),
                Map.entry(HomingVars.COLOR_BORDER,                 NeoFuturismDesign.BORDER_D),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,        NeoFuturismDesign.CYAN_D),
                Map.entry(HomingVars.COLOR_ACCENT,                 NeoFuturismDesign.CYAN_D),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,        NeoFuturismDesign.VIOLET_D),
                Map.entry(HomingVars.COLOR_ACCENT_ON,              NeoFuturismDesign.ON_CYAN)
        );

        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — glass in daylight, an ink masthead.
                Map.entry(HomingVars.COLOR_SURFACE,          NeoFuturismDesign.SURFACE),
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,   NeoFuturismDesign.RAISED),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED, NeoFuturismDesign.RECESSED),
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED, NeoFuturismDesign.INVERTED),

                // Text
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           NeoFuturismDesign.TEXT),
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             NeoFuturismDesign.MUTED),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       NeoFuturismDesign.ON_INVERTED),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, NeoFuturismDesign.ON_INVERTED_MUTED),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,             NeoFuturismDesign.TEXT),
                Map.entry(HomingVars.COLOR_TEXT_LINK,              NeoFuturismDesign.CYAN_INK),
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        NeoFuturismDesign.VIOLET),

                // Borders — quiet hairlines; the light for emphasis.
                Map.entry(HomingVars.COLOR_BORDER,          NeoFuturismDesign.BORDER),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS, NeoFuturismDesign.CYAN),

                // Accent — the light; violet when pressed; ink on it.
                Map.entry(HomingVars.COLOR_ACCENT,          NeoFuturismDesign.CYAN),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS, NeoFuturismDesign.VIOLET),
                Map.entry(HomingVars.COLOR_ACCENT_ON,       NeoFuturismDesign.ON_CYAN),

                // Spacing — the default scale.
                Map.entry(HomingVars.SPACE_1, "4px"),
                Map.entry(HomingVars.SPACE_2, "8px"),
                Map.entry(HomingVars.SPACE_3, "12px"),
                Map.entry(HomingVars.SPACE_4, "16px"),
                Map.entry(HomingVars.SPACE_5, "20px"),
                Map.entry(HomingVars.SPACE_6, "24px"),
                Map.entry(HomingVars.SPACE_7, "32px"),
                Map.entry(HomingVars.SPACE_8, "40px"),

                // Radius — a bevel's worth.
                Map.entry(HomingVars.RADIUS_SM, "2px"),
                Map.entry(HomingVars.RADIUS_MD, "2px"),
                Map.entry(HomingVars.RADIUS_LG, "4px")
        );
    }

    /** A wide geometric display over a humanist body; code keeps the house mono. */
    public record Fonts() implements GlobalTypePalette.Provision<HomingNeoFuturism> {
        public static final Fonts INSTANCE = new Fonts();
        @Override public HomingNeoFuturism theme() { return HomingNeoFuturism.INSTANCE; }
        @Override public Map<CssVar, String> values() {
            return Map.of(HomingFonts.FONT_BODY,    NeoFuturismDesign.BODY_FACE,
                          HomingFonts.FONT_DISPLAY, NeoFuturismDesign.DISPLAY_FACE,
                          HomingFonts.FONT_MONO,    StudioFonts.MONO);
        }
    }
}
