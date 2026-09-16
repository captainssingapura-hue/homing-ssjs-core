package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.core.Theme;

import java.util.Map;

/**
 * Forest theme — green/earth brand variant. Same StudioStyles layout and
 * semantic vocabulary as {@link HomingDefault}; only the primitive palette
 * (and the brand role mapping) differs.
 *
 * <p>Self-contained: this single file delivers a complete theme — the light
 * binding and the dark re-binding, both in {@link Palette} (RFC 0066).</p>
 *
 * <p>Activate via {@code ?theme=forest} on any studio URL.</p>
 */
public record HomingForest() implements Theme {

    public static final HomingForest INSTANCE = new HomingForest();

    @Override public String slug()  { return "forest"; }
    @Override public String label() { return "Forest"; }
    @Override public String group() { return "Nature"; }
    @Override public String inspiration() { return "Green and earth tones, with honey for the accents."; }

    public record Palette() implements GlobalColorPalette.Provision<HomingForest> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingForest theme() { return HomingForest.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
            @Override public Map<CssVar, String> darkValues() { return DARK; }

            /** The re-binding under prefers-color-scheme: dark — the colour roles only;
             *  the scales are the same job in both modes. */
            private static final Map<CssVar, String> DARK = Map.ofEntries(
                    Map.entry(HomingVars.COLOR_SURFACE,               "#0E1A12"),
                    Map.entry(HomingVars.COLOR_SURFACE_RAISED,        "#1A2A1F"),
                    Map.entry(HomingVars.COLOR_SURFACE_RECESSED,      "#243528"),
                    Map.entry(HomingVars.COLOR_SURFACE_INVERTED,      "#1A3829"),   // kept — header bg
                    Map.entry(HomingVars.COLOR_TEXT_PRIMARY,          "#DDEBD8"),   // light moss
                    Map.entry(HomingVars.COLOR_TEXT_MUTED,            "#94B59C"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,      "#DDEBD8"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#A8D5B0"),
                    Map.entry(HomingVars.COLOR_TEXT_TITLE,            "#7BAB85"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK,             "#7BAB85"),   // lifted sage
                    Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,       "#D4A04C"),   // lifted honey
                    Map.entry(HomingVars.COLOR_BORDER,                "#2E4034"),
                    Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,       "#D4A04C"),   // honey
                    Map.entry(HomingVars.COLOR_ACCENT,                "#D4A04C"),
                    Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,       "#B5873A"),
                    Map.entry(HomingVars.COLOR_ACCENT_ON,             "#1A3829")
            );

        // Forest palette — semantic-only. Greens + earth tones for the brand
        // accents; pale-green/sage surfaces for light mode.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces
                Map.entry(HomingVars.COLOR_SURFACE,          "#F4F8F2"),  // pale green page bg
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,   "#FFFFFF"),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED, "#E8EFE3"),  // pale sage subtle
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED, "#1A3829"),  // deep evergreen header

                // Text
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           "#2A3D2E"),  // dark forest text
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             "#5C7561"),  // muted moss
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       "#FFFFFF"),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#C8E6C9"),  // pale moss
                Map.entry(HomingVars.COLOR_TEXT_TITLE,              "#2D5F3F"),   // title := link, unchanged
                Map.entry(HomingVars.COLOR_TEXT_LINK,              "#2D5F3F"),  // forest green
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        "#A6781E"),  // dark honey

                // Borders
                Map.entry(HomingVars.COLOR_BORDER,          "#D4DFCC"),  // sage
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS, "#D4A04C"),  // honey

                // Accent — honey
                Map.entry(HomingVars.COLOR_ACCENT,          "#D4A04C"),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS, "#A6781E"),
                Map.entry(HomingVars.COLOR_ACCENT_ON,       "#1A3829"),

                // Spacing / radius — same scale as default.
                Map.entry(HomingVars.SPACE_1, "4px"),
                Map.entry(HomingVars.SPACE_2, "8px"),
                Map.entry(HomingVars.SPACE_3, "12px"),
                Map.entry(HomingVars.SPACE_4, "16px"),
                Map.entry(HomingVars.SPACE_5, "20px"),
                Map.entry(HomingVars.SPACE_6, "24px"),
                Map.entry(HomingVars.SPACE_7, "32px"),
                Map.entry(HomingVars.SPACE_8, "40px"),
                Map.entry(HomingVars.RADIUS_SM, "4px"),
                Map.entry(HomingVars.RADIUS_MD, "8px"),
                Map.entry(HomingVars.RADIUS_LG, "12px")
        );
    }

}
