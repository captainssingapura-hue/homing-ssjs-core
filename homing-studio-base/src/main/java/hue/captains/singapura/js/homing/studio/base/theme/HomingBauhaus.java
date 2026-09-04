package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;

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

    public record Vars() implements ThemeVariables<HomingBauhaus> {
        public static final Vars INSTANCE = new Vars();
        @Override public HomingBauhaus theme() { return HomingBauhaus.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        // Bauhaus palette — black, white, primary yellow / blue / red.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — paper-white page, ink-black header.
                Map.entry(StudioVars.COLOR_SURFACE,          "#FFFFFF"),
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,   "#FFFFFF"),
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED, "#F2F2EC"),  // pale cream
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED, "#0A0A0A"),  // ink black

                // Text — high contrast, no warm-grays.
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#0A0A0A"),
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#666666"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#FFFFFF"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#CCCCCC"),
                Map.entry(StudioVars.COLOR_TEXT_TITLE,              "#1F2D85"),   // title := link, unchanged
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#1F2D85"),  // Itten blue
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#C9252D"),  // Bauhaus red

                // Borders — thin neutral, with yellow emphasis.
                Map.entry(StudioVars.COLOR_BORDER,          "#D8D8D2"),
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS, "#FFD500"),  // Bauhaus yellow

                // Accent — yellow on black.
                Map.entry(StudioVars.COLOR_ACCENT,          "#FFD500"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS, "#C9252D"),  // red for emphasis
                Map.entry(StudioVars.COLOR_ACCENT_ON,       "#0A0A0A"),

                // Spacing / radius — same scale as default.
                Map.entry(StudioVars.SPACE_1, "4px"),
                Map.entry(StudioVars.SPACE_2, "8px"),
                Map.entry(StudioVars.SPACE_3, "12px"),
                Map.entry(StudioVars.SPACE_4, "16px"),
                Map.entry(StudioVars.SPACE_5, "20px"),
                Map.entry(StudioVars.SPACE_6, "24px"),
                Map.entry(StudioVars.SPACE_7, "32px"),
                Map.entry(StudioVars.SPACE_8, "40px"),
                Map.entry(StudioVars.RADIUS_SM, "0px"),   // Bauhaus: no rounded corners
                Map.entry(StudioVars.RADIUS_MD, "0px"),
                Map.entry(StudioVars.RADIUS_LG, "0px")
        );
    }

    public record Globals() implements ThemeGlobals<HomingBauhaus> {
        public static final Globals INSTANCE = new Globals();
        @Override public HomingBauhaus theme() { return HomingBauhaus.INSTANCE; }
        @Override public String css() { return DARK_OVERRIDE + HomingDefault.STRUCTURAL_CSS; }

        /** Bauhaus's dark-mode adaptation — flip page to ink, keep primary triad. */
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: light dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        /* Surfaces — ink at night, primary triad unchanged. */
                        --color-surface:           #0A0A0A;
                        --color-surface-raised:    #1A1A1A;
                        --color-surface-recessed:  #242424;
                        --color-surface-inverted:  #1F2D85;   /* Itten-blue header in dark */

                        /* Text */
                        --color-text-primary:            #F5F5F0;
                        --color-text-muted:              #999999;
                        --color-text-on-inverted:        #FFFFFF;
                        --color-text-on-inverted-muted:  #C7CDEB;
                        --color-text-title:               #FFD500;
                        --color-text-link:               #FFD500;   /* yellow links pop on ink */
                        --color-text-link-hover:         #C9252D;

                        /* Borders */
                        --color-border:           #303030;
                        --color-border-emphasis:  #FFD500;

                        /* Accent */
                        --color-accent:           #FFD500;
                        --color-accent-emphasis:  #C9252D;
                        --color-accent-on:        #0A0A0A;
                    }
                }
                """;
    }
}
