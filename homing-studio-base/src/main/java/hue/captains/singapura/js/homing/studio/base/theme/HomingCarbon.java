package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;

import java.util.Map;

/**
 * Carbon theme — a neutral slate dark theme, in the register of a professional
 * tool rather than a decorated one. The reference points are LSEG Refinitiv
 * Workspace and IntelliJ's dark theme: near-black slate surfaces, cool grey
 * text, one saturated blue carrying every accent.
 *
 * <p><b>Dark by identity, not by preference.</b> Every other theme here is
 * light-primary with a {@code prefers-color-scheme: dark} override — the
 * palette in {@link Vars} is its light face. Carbon inverts that: {@code Vars}
 * <i>is</i> the dark palette, and there is no light variant, because a light
 * Carbon would not be Carbon. {@code color-scheme: dark} is declared so the
 * browser renders scrollbars, form controls and the canvas to match; without it
 * a native scrollbar arrives as a bright stripe down a black page.</p>
 *
 * <p>The palette is deliberately narrow. Four slate steps carry the whole
 * surface hierarchy, and a single blue is accent, link and emphasis border.
 * Long sessions are the use case, so contrast is high on text and low
 * everywhere else — borders separate by a shade rather than a line you notice.</p>
 *
 * <p>Activate via {@code ?theme=carbon} on any studio URL.</p>
 */
public record HomingCarbon() implements Theme {

    public static final HomingCarbon INSTANCE = new HomingCarbon();

    @Override public String slug()  { return "carbon"; }
    @Override public String label() { return "Carbon"; }
    @Override public String group() { return "Neutral"; }

    public record Vars() implements ThemeVariables<HomingCarbon> {
        public static final Vars INSTANCE = new Vars();
        @Override public HomingCarbon theme() { return HomingCarbon.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        // Carbon palette — DARK values here, unlike every other theme in this
        // package. There is no @media light override; see the class javadoc.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — four slate steps, darkest for the chrome band so the
                // header reads as a frame rather than as another panel.
                Map.entry(StudioVars.COLOR_SURFACE,          "#16181D"),  // page
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,   "#1E212A"),  // cards, panels
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED, "#101216"),  // wells, inputs
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED, "#0C0E12"),  // header band

                // Text — cool grey rather than white. Pure #FFF on near-black
                // vibrates over long reads; #CDD6E3 still clears 11:1 on the page.
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#CDD6E3"),
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#8792A6"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#E4E9F0"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#9AA5B8"),
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#6BA5F7"),
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#9CC5FF"),

                // Borders — a shade, not a line. Emphasis borrows the accent.
                Map.entry(StudioVars.COLOR_BORDER,          "#2A2F3A"),
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS, "#4C8DF6"),

                // Accent — one blue does accent, link and emphasis.
                Map.entry(StudioVars.COLOR_ACCENT,          "#4C8DF6"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS, "#3574F0"),
                Map.entry(StudioVars.COLOR_ACCENT_ON,       "#0B0D11"),

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

    public record Globals() implements ThemeGlobals<HomingCarbon> {
        public static final Globals INSTANCE = new Globals();
        @Override public HomingCarbon theme() { return HomingCarbon.INSTANCE; }
        @Override public String css() { return SCHEME + HomingDefault.STRUCTURAL_CSS; }

        /**
         * No @media block. Carbon is dark in both OS modes on purpose, and
         * color-scheme:dark is what makes the browser's own chrome — scrollbars,
         * select popups, the canvas behind an overscroll — follow the page.
         */
        private static final String SCHEME = """
                :root { color-scheme: dark; }
                """;
    }
}
