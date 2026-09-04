package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;

import java.util.Map;

/**
 * Forbidden City theme — imperial Chinese palace palette: vermilion walls
 * ({@code #7A1F1A}), imperial gold roofs ({@code #C8911C}), warm parchment
 * surfaces ({@code #F5E8D3}), dark-ink text ({@code #2A1810}). Same
 * StudioStyles layout and semantic vocabulary as {@link HomingDefault};
 * only the primitive palette differs.
 *
 * <p>Identity reads as ink-on-rice-paper with a deep red header band and a
 * gold emphasis line — a different mood from the Bauhaus / Forest / Sunset
 * trio: warmer than Default, more saturated than Forest, more historical
 * than Bauhaus. Dark mode flips to a near-black ground with a gold-lifted
 * link tone.</p>
 *
 * <p>Activate via {@code ?theme=forbidden-city} on any studio URL.</p>
 */
public record HomingForbiddenCity() implements Theme {

    public static final HomingForbiddenCity INSTANCE = new HomingForbiddenCity();

    @Override public String slug()  { return "forbidden-city"; }
    @Override public String label() { return "Forbidden City"; }
    @Override public String group() { return "Expressive"; }

    public record Vars() implements ThemeVariables<HomingForbiddenCity> {
        public static final Vars INSTANCE = new Vars();
        @Override public HomingForbiddenCity theme() { return HomingForbiddenCity.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        // Vermilion + imperial gold + parchment + ink. Warm, saturated, historical.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — parchment page, vermilion header band.
                Map.entry(StudioVars.COLOR_SURFACE,          "#F5E8D3"),  // warm parchment
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,   "#FBF5E6"),  // raised paper
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED, "#EAD9B8"),  // aged paper
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED, "#7A1F1A"),  // imperial vermilion

                // Text — dark ink on parchment, cream on vermilion.
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#2A1810"),  // dark ink
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#7A5A3E"),  // tea brown
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#F5E8D3"),  // cream
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#D4B896"),  // muted cream
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#7A1F1A"),  // vermilion
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#A03028"),  // brighter red

                // Borders — tan with imperial-gold emphasis.
                Map.entry(StudioVars.COLOR_BORDER,          "#D4B896"),  // tan
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS, "#C8911C"),  // imperial gold

                // Accent — imperial gold; emphasis flips to vermilion.
                Map.entry(StudioVars.COLOR_ACCENT,          "#C8911C"),  // imperial gold
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS, "#A03028"),  // vermilion
                Map.entry(StudioVars.COLOR_ACCENT_ON,       "#2A1810"),  // dark ink on gold

                // Spacing / radius — same scale as default.
                Map.entry(StudioVars.SPACE_1, "4px"),
                Map.entry(StudioVars.SPACE_2, "8px"),
                Map.entry(StudioVars.SPACE_3, "12px"),
                Map.entry(StudioVars.SPACE_4, "16px"),
                Map.entry(StudioVars.SPACE_5, "20px"),
                Map.entry(StudioVars.SPACE_6, "24px"),
                Map.entry(StudioVars.SPACE_7, "32px"),
                Map.entry(StudioVars.SPACE_8, "40px"),
                Map.entry(StudioVars.RADIUS_SM, "4px"),
                Map.entry(StudioVars.RADIUS_MD, "8px"),
                Map.entry(StudioVars.RADIUS_LG, "12px")
        );
    }

    public record Globals() implements ThemeGlobals<HomingForbiddenCity> {
        public static final Globals INSTANCE = new Globals();
        @Override public HomingForbiddenCity theme() { return HomingForbiddenCity.INSTANCE; }
        @Override public String css() { return DARK_OVERRIDE + HomingDefault.STRUCTURAL_CSS; }

        /** Dark-mode adaptation — near-black ground with the same vermilion
         *  band and gold-lifted link tones for night reading. */
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: light dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        /* Surfaces — ink-tinted near-black at night. */
                        --color-surface:           #1A0E0A;
                        --color-surface-raised:    #2A1810;
                        --color-surface-recessed:  #3A2418;
                        --color-surface-inverted:  #5C140F;   /* deeper vermilion */

                        /* Text */
                        --color-text-primary:            #F5E8D3;
                        --color-text-muted:              #B89878;
                        --color-text-on-inverted:        #F5E8D3;
                        --color-text-on-inverted-muted:  #D4B896;
                        --color-text-link:               #E8B85C;   /* lifted gold */
                        --color-text-link-hover:         #FFD700;   /* bright gold */

                        /* Borders */
                        --color-border:           #4A3424;
                        --color-border-emphasis:  #C8911C;

                        /* Accent — gold reads strongly against the dark ground. */
                        --color-accent:           #C8911C;
                        --color-accent-emphasis:  #E8B85C;
                        --color-accent-on:        #1A0E0A;
                    }
                }
                """;
    }
}
