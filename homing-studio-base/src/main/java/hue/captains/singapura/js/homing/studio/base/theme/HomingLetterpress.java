package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;

import java.util.Map;

/**
 * Letterpress theme — editorial broadsheet aesthetic: warm parchment paper with
 * a subtle SVG-noise grain, brick-red ({@code #B33A20}) ink-on-cream typography,
 * deep-ink ({@code #1A1814}) header band. The first theme to override the
 * default body font (serif: Iowan / Charter / Georgia) and the first to layer
 * a textured background on top of {@code --color-surface}.
 *
 * <p>Two things make this theme more elaborate than the other four:</p>
 *
 * <ol>
 *   <li>A theme-specific {@code TEXTURE_OVERRIDE} CSS block appended after the
 *       shared {@link HomingDefault#STRUCTURAL_CSS} block. The shared block
 *       sets {@code background: var(--color-surface)} (the shorthand resets
 *       background-image to none); the override re-installs an SVG-noise
 *       background-image on top of that surface color. The grain is an
 *       inline data:URI — no extra asset, no extra HTTP request.</li>
 *   <li>A serif body-font override applied at the same point. Affects only
 *       this theme — other themes keep the Calibri / system-ui stack.</li>
 * </ol>
 *
 * <p>Activate via {@code ?theme=letterpress} on any studio URL.</p>
 */
public record HomingLetterpress() implements Theme {

    public static final HomingLetterpress INSTANCE = new HomingLetterpress();

    @Override public String slug()  { return "letterpress"; }
    @Override public String label() { return "Letterpress"; }
    @Override public String group() { return "Neutral"; }

    public record Vars() implements ThemeVariables<HomingLetterpress> {
        public static final Vars INSTANCE = new Vars();
        @Override public HomingLetterpress theme() { return HomingLetterpress.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        // Cream parchment + ink + brick-red. Two-tone editorial palette.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — warm parchment for page, deep ink for header band.
                Map.entry(StudioVars.COLOR_SURFACE,          "#EFE7D6"),  // parchment
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,   "#F7F1E0"),  // raised paper
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED, "#E2D8C2"),  // aged paper
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED, "#1A1814"),  // deep ink

                // Text — ink black on parchment, cream on ink.
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#2A2620"),  // ink
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#7A6F60"),  // warm grey
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#EFE7D6"),  // cream
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#A89F8B"),  // muted cream
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#B33A20"),  // brick red
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#8C2814"),  // darker brick

                // Borders — aged-paper neutral with brick-red emphasis.
                Map.entry(StudioVars.COLOR_BORDER,          "#C7BCA3"),  // aged paper
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS, "#B33A20"),  // brick red

                // Accent — brick red.
                Map.entry(StudioVars.COLOR_ACCENT,          "#B33A20"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS, "#8C2814"),
                Map.entry(StudioVars.COLOR_ACCENT_ON,       "#EFE7D6"),

                // Spacing / radius — same scale as default.
                Map.entry(StudioVars.SPACE_1, "4px"),
                Map.entry(StudioVars.SPACE_2, "8px"),
                Map.entry(StudioVars.SPACE_3, "12px"),
                Map.entry(StudioVars.SPACE_4, "16px"),
                Map.entry(StudioVars.SPACE_5, "20px"),
                Map.entry(StudioVars.SPACE_6, "24px"),
                Map.entry(StudioVars.SPACE_7, "32px"),
                Map.entry(StudioVars.SPACE_8, "40px"),
                Map.entry(StudioVars.RADIUS_SM, "2px"),  // tighter radius — paper feels less plasticy
                Map.entry(StudioVars.RADIUS_MD, "4px"),
                Map.entry(StudioVars.RADIUS_LG, "6px")
        );
    }

    public record Globals() implements ThemeGlobals<HomingLetterpress> {
        public static final Globals INSTANCE = new Globals();
        @Override public HomingLetterpress theme() { return HomingLetterpress.INSTANCE; }
        @Override public String css() {
            // Order matters: structural CSS sets `background: var(--color-surface)`
            // (shorthand → clears background-image). TEXTURE_OVERRIDE comes AFTER
            // so the background-image longhand lands on top, preserving the color.
            return DARK_OVERRIDE + HomingDefault.STRUCTURAL_CSS + TEXTURE_OVERRIDE;
        }

        /** Dark-mode adaptation — night reading on inked paper.
         *  Surface flips to deep ink; link tone lifts to a softer brick. */
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: light dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        /* Surfaces — ink at night. */
                        --color-surface:           #1A1814;
                        --color-surface-raised:    #252118;
                        --color-surface-recessed:  #2E2A20;
                        --color-surface-inverted:  #0F0E0A;

                        /* Text */
                        --color-text-primary:            #EFE7D6;
                        --color-text-muted:              #A89F8B;
                        --color-text-on-inverted:        #EFE7D6;
                        --color-text-on-inverted-muted:  #A89F8B;
                        --color-text-link:               #D85A3E;
                        --color-text-link-hover:         #EFE7D6;

                        /* Borders */
                        --color-border:           #3A3428;
                        --color-border-emphasis:  #D85A3E;

                        /* Accent */
                        --color-accent:           #D85A3E;
                        --color-accent-emphasis:  #EFE7D6;
                        --color-accent-on:        #1A1814;
                    }
                }
                """;

        /**
         * Texture + typography overrides — distinguishing features of this
         * theme. The SVG noise is an inline data:URI (no extra HTTP, no extra
         * asset) generated by feTurbulence + feColorMatrix:
         * <ul>
         *   <li>{@code feTurbulence baseFrequency="0.92"} → fine paper grain (large value = small specks)</li>
         *   <li>{@code feColorMatrix} → tint the noise: dark warm grain for light mode (ink dust on paper); soft cream grain for dark mode (paper specks lifted on ink)</li>
         * </ul>
         * The {@code %23} encodes {@code #} for the filter reference — required
         * inside a {@code data:} URI. {@code stitchTiles='stitch'} keeps the
         * pattern seamless across the repeat.
         */
        private static final String TEXTURE_OVERRIDE = """
                html, body {
                    /* Serif body type — editorial broadsheet feel. Falls back gracefully. */
                    font-family: "Iowan Old Style", "Charter", "Georgia", "Cambria", "Times New Roman", serif;
                    /* Paper grain — fine dark specks, multiplied into the parchment. */
                    background-image:
                        url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='240' height='240'><filter id='n' x='0' y='0'><feTurbulence type='fractalNoise' baseFrequency='0.92' numOctaves='2' stitchTiles='stitch'/><feColorMatrix values='0 0 0 0 0.18  0 0 0 0 0.15  0 0 0 0 0.12  0 0 0 0.10 0'/></filter><rect width='100%' height='100%' filter='url(%23n)'/></svg>");
                    background-repeat: repeat;
                }
                @media (prefers-color-scheme: dark) {
                    html, body {
                        /* Inverted grain — light specks of paper dust on ink. */
                        background-image:
                            url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='240' height='240'><filter id='n' x='0' y='0'><feTurbulence type='fractalNoise' baseFrequency='0.92' numOctaves='2' stitchTiles='stitch'/><feColorMatrix values='0 0 0 0 0.94  0 0 0 0 0.91  0 0 0 0 0.84  0 0 0 0.06 0'/></filter><rect width='100%' height='100%' filter='url(%23n)'/></svg>");
                    }
                }
                /* Drop double-rule beneath section titles — editorial divider. */
                .st-section-title {
                    border-bottom-width: 3px;
                    border-bottom-style: double;
                }
                """;
    }
}
