package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;

import java.util.Map;

/**
 * Carbon theme — a neutral grey dark theme, in the register of a professional
 * tool rather than a decorated one. Drawn from a screenshot of LSEG Refinitiv
 * Workspace rather than from memory, which corrected two things a guess had got
 * wrong.
 *
 * <p><b>The surfaces are neutral, not slate.</b> The first cut used cool,
 * blue-cast greys. The reference does not: its greys are genuinely neutral, and
 * the difference is visible the moment the two sit side by side — a blue cast
 * reads as a decision, and this palette is meant to disappear.</p>
 *
 * <p><b>Blue and amber do different jobs.</b> The first cut had one blue serving
 * as link, accent and emphasis border. The reference splits them: blue marks
 * <i>links</i> — the identifiers in a data grid — and amber marks
 * <i>attention</i>: an active filter, a live count, a checked box. Collapsing
 * the two loses the distinction between "you can go here" and "look at this",
 * which in a dense tool is the more useful of the pair. The amber also settles
 * the studio's gold brand mark, which under a blue-only Carbon was the single
 * warm thing on the page and read as a leftover.</p>
 *
 * <p><b>Dark by identity, not by preference.</b> Every other theme here is
 * light-primary with a {@code prefers-color-scheme: dark} override — the
 * palette in {@link Vars} is its light face. Carbon inverts that: {@code Vars}
 * <i>is</i> the dark palette, and there is no light variant, because a light
 * Carbon would not be Carbon. {@code color-scheme: dark} is declared so the
 * browser renders scrollbars, form controls and the canvas to match; without it
 * a native scrollbar arrives as a bright stripe down a black page.</p>
 *
 * <p>Four grey steps carry the whole surface hierarchy, darkest for the chrome
 * band. Long sessions are the use case, so contrast is high on text and low
 * everywhere else — borders separate by a shade rather than a line you notice.</p>
 *
 * <p>Activate via {@code ?theme=carbon} on any studio URL.</p>
 */
public record HomingCarbon() implements Theme {

    public static final HomingCarbon INSTANCE = new HomingCarbon();

    @Override public String slug()  { return "carbon"; }
    @Override public String label() { return "Carbon"; }
    @Override public String group() { return "Neutral"; }
    @Override public String inspiration() { return "Neutral slate for long sessions, after LSEG Refinitiv Workspace."; }

    public record Vars() implements ThemeVariables<HomingCarbon> {
        public static final Vars INSTANCE = new Vars();
        @Override public HomingCarbon theme() { return HomingCarbon.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        // Carbon palette — DARK values here, unlike every other theme in this
        // package. There is no @media light override; see the class javadoc.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — four NEUTRAL grey steps, darkest for the chrome band
                // so the header reads as a frame rather than as another panel.
                // Neutral is the correction: the first cut was blue-cast slate,
                // and against the reference the cast was the thing you noticed.
                Map.entry(StudioVars.COLOR_SURFACE,          "#242424"),  // page, grid
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,   "#2C2C2C"),  // panels, cards
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED, "#1C1C1C"),  // wells, inputs
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED, "#161616"),  // chrome band

                // Text — neutral grey rather than white. Pure #FFF on near-black
                // vibrates over long reads; #D6D6D6 still clears ~11:1 on the page.
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#D6D6D6"),
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#8C8C8C"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#E4E4E4"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#9A9A9A"),

                // The title is NOT the link colour. A link blue is lifted so it
                // reads at 13px on a dark ground; the same value at 44px is a
                // shout, and the reference has no large coloured text at all.
                // Near-white, so the heading reads through size and weight.
                Map.entry(StudioVars.COLOR_TEXT_TITLE,             "#EDEDED"),

                // Blue is for LINKS only — "you can go here". In the reference
                // this is the colour of a row identifier and of nothing else.
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#4C9AFF"),
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#7FB8FF"),

                // Borders — a shade, not a line. Emphasis takes the accent.
                Map.entry(StudioVars.COLOR_BORDER,          "#3A3A3A"),
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS, "#E8912D"),

                // Amber is for ATTENTION — "look at this". An active filter, a
                // live count, a checked box. Distinct from blue on purpose, and
                // the reason the studio's gold brand mark stops looking stranded.
                Map.entry(StudioVars.COLOR_ACCENT,          "#E8912D"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS, "#C97518"),
                Map.entry(StudioVars.COLOR_ACCENT_ON,       "#1A1A1A"),

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
