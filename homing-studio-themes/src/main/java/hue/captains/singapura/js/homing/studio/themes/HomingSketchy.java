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
 * Sketchy — drawn by hand, in marker, on paper: a 2px line around every box,
 * corners that wobble, handwriting for the body and a sketched capital for
 * headings, and nothing casting a shadow. {@link SketchyDesign} is the hand,
 * {@link SeedPalette#MARKER} the marker it draws with by default. After
 * Bootswatch's Sketchy.
 */
public record HomingSketchy() implements Design {

    public static final DesignId ID = new DesignId("sketchy");
    public static final HomingSketchy INSTANCE = new HomingSketchy();

    /** Over Default: the hand where it has one, Default's for the rest; the marker on the colour plane. */
    @Override public Impl impl(DesignClass<?> pair) {
        if (pair.onColourPlane()) return SeedPalette.MARKER.impl(pair);
        Impl own = SketchyDesign.WORDS.get(pair);
        return own != null ? own : HomingEditorial.INSTANCE.impl(pair);
    }

    /** Worn in the marker by default; the marker is a palette in its own name, so any physique may wear it. */
    @Override public hue.captains.singapura.js.homing.design.Palette palette() { return SeedPalette.MARKER; }

    @Override public DesignId id() { return ID; }
    @Override public String label() { return "Sketchy"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "Drawn by hand in marker on paper — 2px lines, corners that wobble, nothing casts a shadow."; }

    /** The legacy palette, for the groups downstream that still read tokens; the marker's own seeds. */
    public record Palette() implements GlobalColorPalette.Provision<HomingSketchy> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingSketchy theme() { return HomingSketchy.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
        @Override public Map<CssVar, String> darkValues() { return DARK; }

        private static final SeedPalette.Seeds L = SeedPalette.MARKER.light(), D = SeedPalette.MARKER.dark();

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
                Map.entry(HomingVars.RADIUS_SM, "15px"),
                Map.entry(HomingVars.RADIUS_MD, "25px"),
                Map.entry(HomingVars.RADIUS_LG, "35px")
        );
    }

    /** Handwriting and a sketched capital; code keeps the house mono. */
    public record Fonts() implements GlobalTypePalette.Provision<HomingSketchy> {
        public static final Fonts INSTANCE = new Fonts();
        @Override public HomingSketchy theme() { return HomingSketchy.INSTANCE; }
        @Override public Map<CssVar, String> values() {
            return Map.of(HomingFonts.FONT_BODY,    SketchyDesign.BODY_FACE,
                          HomingFonts.FONT_DISPLAY, SketchyDesign.DISPLAY_FACE,
                          HomingFonts.FONT_MONO,    StudioFonts.MONO);
        }
    }
}
