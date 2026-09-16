package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.core.Theme;

import java.util.Map;

/**
 * Bauhaus theme — austere modernist primary-colour palette: black, white,
 * Bauhaus yellow ({@code #FFD500}), Itten blue ({@code #1F2D85}),
 * Bauhaus red ({@code #C9252D}). Same StudioStyles layout and semantic
 * vocabulary as {@link HomingDefault}; only the primitive palette differs.
 *
 * <p>Geometric, high-contrast, ink-on-paper feel. Black inverted header with
 * white text; pure-white page surface with deep blue links and a yellow
 * emphasis border for accent. Dark mode flips to ink-black surface with
 * white text — the primary triad stays vivid against either background.</p>
 *
 * <p>Activate via {@code ?theme=bauhaus} on any studio URL.</p>
 */
public record HomingBauhaus() implements Theme {

    public static final HomingBauhaus INSTANCE = new HomingBauhaus();

    @Override public String slug()  { return "bauhaus"; }
    @Override public String label() { return "Bauhaus"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "Austere modernist primaries — Bauhaus yellow, Itten blue, black."; }

    public record Palette() implements GlobalColorPalette.Provision<HomingBauhaus> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingBauhaus theme() { return HomingBauhaus.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
            @Override public Map<CssVar, String> darkValues() { return DARK; }

            /** The re-binding under prefers-color-scheme: dark — the colour roles only;
             *  the scales are the same job in both modes. */
            private static final Map<CssVar, String> DARK = Map.ofEntries(
                    Map.entry(HomingVars.COLOR_SURFACE,               "#0A0A0A"),
                    Map.entry(HomingVars.COLOR_SURFACE_RAISED,        "#1A1A1A"),
                    Map.entry(HomingVars.COLOR_SURFACE_RECESSED,      "#242424"),
                    Map.entry(HomingVars.COLOR_SURFACE_INVERTED,      "#1F2D85"),   // Itten-blue header in dark
                    Map.entry(HomingVars.COLOR_TEXT_PRIMARY,          "#F5F5F0"),
                    Map.entry(HomingVars.COLOR_TEXT_MUTED,            "#999999"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,      "#FFFFFF"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#C7CDEB"),
                    Map.entry(HomingVars.COLOR_TEXT_TITLE,            "#FFD500"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK,             "#FFD500"),   // yellow links pop on ink
                    Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,       "#C9252D"),   // Borders
                    Map.entry(HomingVars.COLOR_BORDER,                "#303030"),
                    Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,       "#FFD500"),   // Accent
                    Map.entry(HomingVars.COLOR_ACCENT,                "#FFD500"),
                    Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,       "#C9252D"),
                    Map.entry(HomingVars.COLOR_ACCENT_ON,             "#0A0A0A")
            );

        // Bauhaus palette — black, white, primary yellow / blue / red.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — paper-white page, ink-black header.
                Map.entry(HomingVars.COLOR_SURFACE,          "#FFFFFF"),
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,   "#FFFFFF"),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED, "#F2F2EC"),  // pale cream
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED, "#0A0A0A"),  // ink black

                // Text — high contrast, no warm-grays.
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           "#0A0A0A"),
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             "#666666"),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       "#FFFFFF"),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#CCCCCC"),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,              "#1F2D85"),   // title := link, unchanged
                Map.entry(HomingVars.COLOR_TEXT_LINK,              "#1F2D85"),  // Itten blue
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        "#C9252D"),  // Bauhaus red

                // Borders — thin neutral, with yellow emphasis.
                Map.entry(HomingVars.COLOR_BORDER,          "#D8D8D2"),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS, "#FFD500"),  // Bauhaus yellow

                // Accent — yellow on black.
                Map.entry(HomingVars.COLOR_ACCENT,          "#FFD500"),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS, "#C9252D"),  // red for emphasis
                Map.entry(HomingVars.COLOR_ACCENT_ON,       "#0A0A0A"),

                // Spacing / radius — same scale as default.
                Map.entry(HomingVars.SPACE_1, "4px"),
                Map.entry(HomingVars.SPACE_2, "8px"),
                Map.entry(HomingVars.SPACE_3, "12px"),
                Map.entry(HomingVars.SPACE_4, "16px"),
                Map.entry(HomingVars.SPACE_5, "20px"),
                Map.entry(HomingVars.SPACE_6, "24px"),
                Map.entry(HomingVars.SPACE_7, "32px"),
                Map.entry(HomingVars.SPACE_8, "40px"),
                Map.entry(HomingVars.RADIUS_SM, "0px"),   // Bauhaus: no rounded corners
                Map.entry(HomingVars.RADIUS_MD, "0px"),
                Map.entry(HomingVars.RADIUS_LG, "0px")
        );
    }

}
