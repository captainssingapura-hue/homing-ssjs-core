package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;

import java.util.Map;

/**
 * Forest theme — green/earth brand variant. Same StudioStyles layout and
 * semantic vocabulary as {@link HomingDefault}; only the primitive palette
 * (and the brand role mapping) differs.
 *
 * <p>Self-contained: this single file delivers a complete theme — light
 * mode primitives in {@link Vars}, dark-mode {@code @media} override in
 * {@link Globals}.</p>
 *
 * <p>Activate via {@code ?theme=forest} on any studio URL.</p>
 */
public record HomingForest() implements Theme {

    public static final HomingForest INSTANCE = new HomingForest();

    @Override public String slug()  { return "forest"; }
    @Override public String label() { return "Forest"; }
    @Override public String group() { return "Nature"; }

    public record Vars() implements ThemeVariables<HomingForest> {
        public static final Vars INSTANCE = new Vars();
        @Override public HomingForest theme() { return HomingForest.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        // Forest palette — semantic-only. Greens + earth tones for the brand
        // accents; pale-green/sage surfaces for light mode.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces
                Map.entry(StudioVars.COLOR_SURFACE,          "#F4F8F2"),  // pale green page bg
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,   "#FFFFFF"),
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED, "#E8EFE3"),  // pale sage subtle
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED, "#1A3829"),  // deep evergreen header

                // Text
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#2A3D2E"),  // dark forest text
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#5C7561"),  // muted moss
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#FFFFFF"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#C8E6C9"),  // pale moss
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#2D5F3F"),  // forest green
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#A6781E"),  // dark honey

                // Borders
                Map.entry(StudioVars.COLOR_BORDER,          "#D4DFCC"),  // sage
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS, "#D4A04C"),  // honey

                // Accent — honey
                Map.entry(StudioVars.COLOR_ACCENT,          "#D4A04C"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS, "#A6781E"),
                Map.entry(StudioVars.COLOR_ACCENT_ON,       "#1A3829"),

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

    public record Globals() implements ThemeGlobals<HomingForest> {
        public static final Globals INSTANCE = new Globals();
        @Override public HomingForest theme() { return HomingForest.INSTANCE; }
        @Override public String css() { return DARK_OVERRIDE + HomingDefault.STRUCTURAL_CSS; }

        /** Forest's dark-mode adaptation — deeper greens for night. */
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: light dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        /* Surfaces — deep forest at night. */
                        --color-surface:           #0E1A12;
                        --color-surface-raised:    #1A2A1F;
                        --color-surface-recessed:  #243528;
                        --color-surface-inverted:  #1A3829;   /* kept — header bg */

                        /* Text */
                        --color-text-primary:            #DDEBD8;   /* light moss */
                        --color-text-muted:              #94B59C;
                        --color-text-on-inverted:        #DDEBD8;
                        --color-text-on-inverted-muted:  #A8D5B0;
                        --color-text-link:               #7BAB85;   /* lifted sage */
                        --color-text-link-hover:         #D4A04C;   /* lifted honey */

                        /* Borders */
                        --color-border:           #2E4034;
                        --color-border-emphasis:  #D4A04C;          /* honey */

                        /* Accent */
                        --color-accent:           #D4A04C;
                        --color-accent-emphasis:  #B5873A;
                        --color-accent-on:        #1A3829;
                    }
                }
                """;
    }
}
