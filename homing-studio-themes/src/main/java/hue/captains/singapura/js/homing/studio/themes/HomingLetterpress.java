package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssBlock;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.theme.type.HomingFonts;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

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
 *   <li>An override of the page reset ({@link Studio#st_page()}, RFC 0066):
 *       the declared {@code background: var(--color-surface)} shorthand resets
 *       background-image to none; the override, appended inside the same
 *       rule, re-installs an SVG-noise background-image on top of that
 *       surface colour — an inline data:URI, no extra asset, no request —
 *       and the serif body face. Other themes keep the Calibri / system-ui
 *       stack.</li>
 * </ol>
 *
 * <p>Activate via {@code ?theme=letterpress} on any studio URL.</p>
 */
public record HomingLetterpress() implements Theme {

    public static final HomingLetterpress INSTANCE = new HomingLetterpress();

    @Override public String slug()  { return "letterpress"; }
    @Override public String label() { return "Letterpress"; }
    @Override public String group() { return "Neutral"; }
    @Override public String inspiration() { return "Editorial broadsheet — brick-red ink on grained parchment."; }

    public record Palette() implements GlobalColorPalette.Provision<HomingLetterpress> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingLetterpress theme() { return HomingLetterpress.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
            @Override public Map<CssVar, String> darkValues() { return DARK; }

            /** The re-binding under prefers-color-scheme: dark — the colour roles only;
             *  the scales are the same job in both modes. */
            private static final Map<CssVar, String> DARK = Map.ofEntries(
                    Map.entry(HomingVars.COLOR_SURFACE,               "#1A1814"),
                    Map.entry(HomingVars.COLOR_SURFACE_RAISED,        "#252118"),
                    Map.entry(HomingVars.COLOR_SURFACE_RECESSED,      "#2E2A20"),
                    Map.entry(HomingVars.COLOR_SURFACE_INVERTED,      "#0F0E0A"),   // Text
                    Map.entry(HomingVars.COLOR_TEXT_PRIMARY,          "#EFE7D6"),
                    Map.entry(HomingVars.COLOR_TEXT_MUTED,            "#A89F8B"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,      "#EFE7D6"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#A89F8B"),
                    Map.entry(HomingVars.COLOR_TEXT_TITLE,            "#D85A3E"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK,             "#D85A3E"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,       "#EFE7D6"),   // Borders
                    Map.entry(HomingVars.COLOR_BORDER,                "#3A3428"),
                    Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,       "#D85A3E"),   // Accent
                    Map.entry(HomingVars.COLOR_ACCENT,                "#D85A3E"),
                    Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,       "#EFE7D6"),
                    Map.entry(HomingVars.COLOR_ACCENT_ON,             "#1A1814")
            );

        // Cream parchment + ink + brick-red. Two-tone editorial palette.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — warm parchment for page, deep ink for header band.
                Map.entry(HomingVars.COLOR_SURFACE,          "#EFE7D6"),  // parchment
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,   "#F7F1E0"),  // raised paper
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED, "#E2D8C2"),  // aged paper
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED, "#1A1814"),  // deep ink

                // Text — ink black on parchment, cream on ink.
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           "#2A2620"),  // ink
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             "#7A6F60"),  // warm grey
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       "#EFE7D6"),  // cream
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#A89F8B"),  // muted cream
                Map.entry(HomingVars.COLOR_TEXT_TITLE,              "#B33A20"),   // title := link, unchanged
                Map.entry(HomingVars.COLOR_TEXT_LINK,              "#B33A20"),  // brick red
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        "#8C2814"),  // darker brick

                // Borders — aged-paper neutral with brick-red emphasis.
                Map.entry(HomingVars.COLOR_BORDER,          "#C7BCA3"),  // aged paper
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS, "#B33A20"),  // brick red

                // Accent — brick red.
                Map.entry(HomingVars.COLOR_ACCENT,          "#B33A20"),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS, "#8C2814"),
                Map.entry(HomingVars.COLOR_ACCENT_ON,       "#EFE7D6"),

                // Spacing / radius — same scale as default.
                Map.entry(HomingVars.SPACE_1, "4px"),
                Map.entry(HomingVars.SPACE_2, "8px"),
                Map.entry(HomingVars.SPACE_3, "12px"),
                Map.entry(HomingVars.SPACE_4, "16px"),
                Map.entry(HomingVars.SPACE_5, "20px"),
                Map.entry(HomingVars.SPACE_6, "24px"),
                Map.entry(HomingVars.SPACE_7, "32px"),
                Map.entry(HomingVars.SPACE_8, "40px"),
                Map.entry(HomingVars.RADIUS_SM, "2px"),  // tighter radius — paper feels less plasticy
                Map.entry(HomingVars.RADIUS_MD, "4px"),
                Map.entry(HomingVars.RADIUS_LG, "6px")
        );
    }

    /** A serif body — the editorial broadsheet; display and code stay the house faces. */
    public record Fonts() implements GlobalTypePalette.Provision<HomingLetterpress> {
        public static final Fonts INSTANCE = new Fonts();
        @Override public HomingLetterpress theme() { return HomingLetterpress.INSTANCE; }
        @Override public Map<CssVar, String> values() {
            return Map.of(HomingFonts.FONT_BODY,    "\"Iowan Old Style\", \"Charter\", \"Georgia\", \"Cambria\", \"Times New Roman\", serif",
                          HomingFonts.FONT_DISPLAY, StudioFonts.DISPLAY,
                          HomingFonts.FONT_MONO,    StudioFonts.MONO);
        }
    }

    /** RFC 0066 — the theme's word on the studio's classes: the grain and the divider. */
    public record Studio() implements StudioStyles.Overrides<HomingLetterpress> {
        public static final Studio INSTANCE = new Studio();
        @Override public HomingLetterpress theme() { return HomingLetterpress.INSTANCE; }

        /** The paper grain multiplied into the parchment
         *  — inverted to light specks on ink under a dark scheme. */
        public CssBlock<StudioStyles.st_page> st_page() { return CssBlock.of("""
                background-image:
                    url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='240' height='240'><filter id='n' x='0' y='0'><feTurbulence type='fractalNoise' baseFrequency='0.92' numOctaves='2' stitchTiles='stitch'/><feColorMatrix values='0 0 0 0 0.18  0 0 0 0 0.15  0 0 0 0 0.12  0 0 0 0.10 0'/></filter><rect width='100%' height='100%' filter='url(%23n)'/></svg>");
                background-repeat: repeat;
                @media (prefers-color-scheme: dark) {
                    & {
                        background-image:
                            url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='240' height='240'><filter id='n' x='0' y='0'><feTurbulence type='fractalNoise' baseFrequency='0.92' numOctaves='2' stitchTiles='stitch'/><feColorMatrix values='0 0 0 0 0.94  0 0 0 0 0.91  0 0 0 0 0.84  0 0 0 0.06 0'/></filter><rect width='100%' height='100%' filter='url(%23n)'/></svg>");
                    }
                }
                """); }

        /** Double rule beneath section titles — the editorial divider. */
        public CssBlock<StudioStyles.st_section_title> st_section_title() { return CssBlock.of("""
                border-bottom-width: 3px;
                border-bottom-style: double;
                """); }
    }
}
