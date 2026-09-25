package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.theme.type.HomingFonts;
import java.util.Map;
import hue.captains.singapura.js.homing.designs.NeumorphismDesign;
import hue.captains.singapura.js.homing.designs.SeedPalette;
import hue.captains.singapura.js.homing.designs.HomingNeumorphism;

/**
 * The LEGACY palettes {@link HomingNeumorphism} provides: its values for the global
 * colour palette ({@code --color-*}, spacing, radius) and the global type
 * palette. They are the studio's to keep - they pass through beside the design
 * until the last studio group wears design words - and the design itself lives
 * in {@code homing-designs}, which knows nothing of them.
 */
public final class HomingNeumorphismLegacy {

    private HomingNeumorphismLegacy() {}


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
