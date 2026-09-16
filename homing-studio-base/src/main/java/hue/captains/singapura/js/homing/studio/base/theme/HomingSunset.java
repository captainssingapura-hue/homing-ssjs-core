package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;

import java.util.Map;

/**
 * Sunset theme — warm coral/terracotta brand variant. Same StudioStyles
 * layout and semantic vocabulary as {@link HomingDefault}; the primitive
 * palette shifts to warm tones.
 *
 * <p>Self-contained: light mode primitives in {@link Palette}, dark-mode
 * {@code @media} override in {@link Globals}. Structural CSS reused from
 * {@link HomingDefault#STRUCTURAL_CSS}.</p>
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

    public record Globals() implements ThemeGlobals<HomingSunset> {
        public static final Globals INSTANCE = new Globals();
        @Override public HomingSunset theme() { return HomingSunset.INSTANCE; }
        @Override public String css() { return DARK_OVERRIDE + HomingDefault.STRUCTURAL_CSS; }

        /** Sunset's dark-mode adaptation — dusk tones. */
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: light dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        /* Surfaces — deep dusk. */
                        --color-surface:           #1A0F08;
                        --color-surface-raised:    #2A1A10;
                        --color-surface-recessed:  #3A2418;
                        --color-surface-inverted:  #7A2E2E;   /* kept — header bg */

                        /* Text */
                        --color-text-primary:            #FFE4D1;   /* light peach */
                        --color-text-muted:              #C9A78B;
                        --color-text-on-inverted:        #FFE4D1;
                        --color-text-on-inverted-muted:  #FFB67A;
                        --color-text-title:               #E89580;
                        --color-text-link:               #E89580;   /* lifted terracotta */
                        --color-text-link-hover:         #FF8C42;   /* lifted sunset orange */

                        /* Borders */
                        --color-border:           #4A3424;
                        --color-border-emphasis:  #FF8C42;          /* sunset orange */

                        /* Accent */
                        --color-accent:           #FF8C42;
                        --color-accent-emphasis:  #FFA363;
                        --color-accent-on:        #7A2E2E;
                    }
                }
                """;
    }
}
