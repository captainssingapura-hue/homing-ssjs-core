package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.core.Theme;

import java.util.Map;

/**
 * Sunset theme — warm coral/terracotta brand variant. Same StudioStyles
 * layout and semantic vocabulary as {@link HomingDefault}; the primitive
 * palette shifts to warm tones.
 *
 * <p>Self-contained: the light binding and the dark re-binding, both in
 * {@link Palette} (RFC 0066). Palette-only — nothing overridden.</p>
 *
 * <p>Activate via {@code ?theme=sunset} on any studio URL.</p>
 */
public record HomingSunset() implements Theme {

    public static final HomingSunset INSTANCE = new HomingSunset();

    @Override public String slug()  { return "sunset"; }
    @Override public String label() { return "Sunset"; }
    @Override public String group() { return "Nature"; }
    @Override public String inspiration() { return "Warm coral and terracotta — a dusk palette."; }

    public record Palette() implements GlobalColorPalette.Provision<HomingSunset> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingSunset theme() { return HomingSunset.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
            @Override public Map<CssVar, String> darkValues() { return DARK; }

            /** The re-binding under prefers-color-scheme: dark — the colour roles only;
             *  the scales are the same job in both modes. */
            private static final Map<CssVar, String> DARK = Map.ofEntries(
                    Map.entry(HomingVars.COLOR_SURFACE,               "#1A0F08"),
                    Map.entry(HomingVars.COLOR_SURFACE_RAISED,        "#2A1A10"),
                    Map.entry(HomingVars.COLOR_SURFACE_RECESSED,      "#3A2418"),
                    Map.entry(HomingVars.COLOR_SURFACE_INVERTED,      "#7A2E2E"),   // kept — header bg
                    Map.entry(HomingVars.COLOR_TEXT_PRIMARY,          "#FFE4D1"),   // light peach
                    Map.entry(HomingVars.COLOR_TEXT_MUTED,            "#C9A78B"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,      "#FFE4D1"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#FFB67A"),
                    Map.entry(HomingVars.COLOR_TEXT_TITLE,            "#E89580"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK,             "#E89580"),   // lifted terracotta
                    Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,       "#FF8C42"),   // lifted sunset orange
                    Map.entry(HomingVars.COLOR_BORDER,                "#4A3424"),
                    Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,       "#FF8C42"),   // sunset orange
                    Map.entry(HomingVars.COLOR_ACCENT,                "#FF8C42"),
                    Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,       "#FFA363"),
                    Map.entry(HomingVars.COLOR_ACCENT_ON,             "#7A2E2E")
            );

        // Sunset palette — semantic-only. Warm coral/terracotta accents on
        // cream surfaces.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces
                Map.entry(HomingVars.COLOR_SURFACE,          "#FFF5EB"),  // cream page bg
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,   "#FFFFFF"),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED, "#F5E8DA"),  // pale sand
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED, "#7A2E2E"),  // deep clay header

                // Text
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           "#4A2D1A"),  // dark cocoa
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             "#8B6F4E"),  // muted sand
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       "#FFFFFF"),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#FFD4A8"),  // peach
                Map.entry(HomingVars.COLOR_TEXT_TITLE,              "#B85450"),   // title := link, unchanged
                Map.entry(HomingVars.COLOR_TEXT_LINK,              "#B85450"),  // terracotta
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        "#D2691E"),  // burnt orange

                // Borders
                Map.entry(HomingVars.COLOR_BORDER,          "#E8D5C0"),  // sand
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS, "#FF8C42"),  // sunset orange

                // Accent — sunset orange
                Map.entry(HomingVars.COLOR_ACCENT,          "#FF8C42"),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS, "#D2691E"),
                Map.entry(HomingVars.COLOR_ACCENT_ON,       "#7A2E2E"),

                // Spacing / radius — same scale.
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
    public record Fonts() implements GlobalTypePalette.Provision<HomingSunset> {
        public static final Fonts INSTANCE = new Fonts();
        @Override public HomingSunset theme() { return HomingSunset.INSTANCE; }
        @Override public Map<CssVar, String> values() { return StudioFonts.HOUSE; }
    }
}
