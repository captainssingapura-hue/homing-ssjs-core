package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.core.Theme;

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
    @Override public String inspiration() { return "Imperial palace — vermilion walls, gold roofs, parchment."; }

    public record Palette() implements GlobalColorPalette.Provision<HomingForbiddenCity> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingForbiddenCity theme() { return HomingForbiddenCity.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
            @Override public Map<CssVar, String> darkValues() { return DARK; }

            /** The re-binding under prefers-color-scheme: dark — the colour roles only;
             *  the scales are the same job in both modes. */
            private static final Map<CssVar, String> DARK = Map.ofEntries(
                    Map.entry(HomingVars.COLOR_SURFACE,               "#1A0E0A"),
                    Map.entry(HomingVars.COLOR_SURFACE_RAISED,        "#2A1810"),
                    Map.entry(HomingVars.COLOR_SURFACE_RECESSED,      "#3A2418"),
                    Map.entry(HomingVars.COLOR_SURFACE_INVERTED,      "#5C140F"),   // deeper vermilion
                    Map.entry(HomingVars.COLOR_TEXT_PRIMARY,          "#F5E8D3"),
                    Map.entry(HomingVars.COLOR_TEXT_MUTED,            "#B89878"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,      "#F5E8D3"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#D4B896"),
                    Map.entry(HomingVars.COLOR_TEXT_TITLE,            "#E8B85C"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK,             "#E8B85C"),   // lifted gold
                    Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,       "#FFD700"),   // bright gold
                    Map.entry(HomingVars.COLOR_BORDER,                "#4A3424"),
                    Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,       "#C8911C"),   // Accent — gold reads strongly against the dark ground.
                    Map.entry(HomingVars.COLOR_ACCENT,                "#C8911C"),
                    Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,       "#E8B85C"),
                    Map.entry(HomingVars.COLOR_ACCENT_ON,             "#1A0E0A")
            );

        // Vermilion + imperial gold + parchment + ink. Warm, saturated, historical.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — parchment page, vermilion header band.
                Map.entry(HomingVars.COLOR_SURFACE,          "#F5E8D3"),  // warm parchment
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,   "#FBF5E6"),  // raised paper
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED, "#EAD9B8"),  // aged paper
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED, "#7A1F1A"),  // imperial vermilion

                // Text — dark ink on parchment, cream on vermilion.
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           "#2A1810"),  // dark ink
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             "#7A5A3E"),  // tea brown
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       "#F5E8D3"),  // cream
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#D4B896"),  // muted cream
                Map.entry(HomingVars.COLOR_TEXT_TITLE,              "#7A1F1A"),   // title := link, unchanged
                Map.entry(HomingVars.COLOR_TEXT_LINK,              "#7A1F1A"),  // vermilion
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        "#A03028"),  // brighter red

                // Borders — tan with imperial-gold emphasis.
                Map.entry(HomingVars.COLOR_BORDER,          "#D4B896"),  // tan
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS, "#C8911C"),  // imperial gold

                // Accent — imperial gold; emphasis flips to vermilion.
                Map.entry(HomingVars.COLOR_ACCENT,          "#C8911C"),  // imperial gold
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS, "#A03028"),  // vermilion
                Map.entry(HomingVars.COLOR_ACCENT_ON,       "#2A1810"),  // dark ink on gold

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

    /** The house faces — this theme has no typographic identity of its own. */
    public record Fonts() implements GlobalTypePalette.Provision<HomingForbiddenCity> {
        public static final Fonts INSTANCE = new Fonts();
        @Override public HomingForbiddenCity theme() { return HomingForbiddenCity.INSTANCE; }
        @Override public Map<CssVar, String> values() { return StudioFonts.HOUSE; }
    }
}
