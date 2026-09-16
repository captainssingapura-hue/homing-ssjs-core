package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssBlock;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.Map;

/**
 * Turbo C — Borland's DOS IDE, in the EGA sixteen.
 *
 * <p>Every value here is a colour an EGA card could actually produce, because
 * that constraint is the theme. The IDE looked the way it did because sixteen
 * colours was all there was, and picking a "nicer" blue would be picking a blue
 * no one ever saw.</p>
 *
 * <p><b>The header is the menu bar.</b> The one mapping that makes this work:
 * {@code COLOR_SURFACE_INVERTED} is EGA light grey, so the studio's chrome band
 * lands as the grey strip across the top of Turbo C with black text on it, over
 * a blue page. That is the silhouette of the IDE, and it falls out of the token
 * set rather than being drawn.</p>
 *
 * <p><b>Dark by identity, like {@link HomingCarbon}.</b> No light variant and no
 * {@code prefers-color-scheme} block — a light Turbo C is a contradiction, since
 * the blue IS the product. {@code color-scheme: dark} is declared so the
 * browser's own scrollbars and form controls do not arrive as a bright stripe
 * down a blue page.</p>
 *
 * <p><b>Yellow is both the title colour and the accent, on purpose.</b> Carbon
 * separates them, because its reference has no large coloured text and a lifted
 * link blue at 44px is a shout. Turbo C's reference is the opposite: it is
 * coloured text nearly all the way down, and Borland yellow on blue is the image
 * people actually remember. Splitting the role would spend the signature colour
 * on one heading and hide it everywhere else.</p>
 *
 * <p><b>Radius is zero.</b> A DOS text mode has no rounded corners because it
 * has no pixels to round — the smallest unit is an 8×16 character cell. Every
 * radius token is {@code 0px}, which is the cheapest and most legible signal
 * that this is a text-mode theme.</p>
 */
public record HomingTurboC() implements Theme {

    public static final HomingTurboC INSTANCE = new HomingTurboC();

    @Override public String slug()  { return "turbo-c"; }
    @Override public String label() { return "Turbo C"; }
    @Override public String group() { return "Retro"; }
    @Override public String inspiration() {
        return "Borland's DOS IDE — EGA blue, a grey menu bar, and yellow where it counts.";
    }

    public record Palette() implements GlobalColorPalette.Provision<HomingTurboC> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingTurboC theme() { return HomingTurboC.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
        /** Dark in every mode: the browser's own chrome follows. */
        @Override public String colorScheme() { return "dark"; }

        // The EGA sixteen, and nothing outside them. Named where a value is one
        // of the canonical entries; the two blues that are not EGA are the two
        // surface steps the IDE got from window borders rather than from colour.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces. The page is the editor: EGA blue, the single colour
                // this whole theme is remembered for. INVERTED is EGA light grey
                // so the chrome band reads as the menu bar — see the javadoc.
                Map.entry(HomingVars.COLOR_SURFACE,          "#0000A8"),  // EGA 1  — the editor
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,   "#0000C8"),  // a window over it
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED, "#000080"),  // a sunken well
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED, "#A8A8A8"),  // EGA 7  — the menu bar

                // Text. White is the active line, EGA light grey is everything
                // quieter — which is also the order the IDE used them in. There
                // is no third step available: EGA dark grey on this blue is
                // 1.8:1 and simply cannot be read.
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           "#FFFFFF"),  // 13.4:1 on the page
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             "#A8A8A8"),  //  5.6:1 on the page

                // On the grey menu bar, black — as the IDE did. The muted step is
                // darkened past EGA dark grey (#555555), which lands at 3.1:1 on
                // this grey and was only ever used for DISABLED items.
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       "#000000"),  //  8.8:1 on grey
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#333333"),  //  5.3:1 on grey

                // Borland yellow. Titles and accent share it deliberately.
                Map.entry(HomingVars.COLOR_TEXT_TITLE,             "#FFFF55"),  // EGA 14 — 12.6:1

                // Cyan for links, and only for links.
                Map.entry(HomingVars.COLOR_TEXT_LINK,              "#55FFFF"),  // EGA 11 — 10.9:1
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        "#FFFFFF"),

                // Borders. The quiet one is a dim blue that separates without
                // drawing a line; emphasis takes EGA light grey, which is what
                // the IDE drew its double-line window frames in.
                Map.entry(HomingVars.COLOR_BORDER,          "#4444A0"),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS, "#A8A8A8"),

                // Yellow again — the brand mark, active states, anything asking
                // to be looked at. Black on yellow is the IDE's highlight bar.
                Map.entry(HomingVars.COLOR_ACCENT,          "#FFFF55"),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS, "#FFFFAA"),
                Map.entry(HomingVars.COLOR_ACCENT_ON,       "#000000"),

                // Spacing — the default scale. Nothing about the IDE's rhythm is
                // carried by margins, and changing it would only make layouts odd.
                Map.entry(HomingVars.SPACE_1, "4px"),
                Map.entry(HomingVars.SPACE_2, "8px"),
                Map.entry(HomingVars.SPACE_3, "12px"),
                Map.entry(HomingVars.SPACE_4, "16px"),
                Map.entry(HomingVars.SPACE_5, "20px"),
                Map.entry(HomingVars.SPACE_6, "24px"),
                Map.entry(HomingVars.SPACE_7, "32px"),
                Map.entry(HomingVars.SPACE_8, "40px"),

                // Zero radius — a character cell has no corners to round.
                Map.entry(HomingVars.RADIUS_SM, "0px"),
                Map.entry(HomingVars.RADIUS_MD, "0px"),
                Map.entry(HomingVars.RADIUS_LG, "0px")
        );
    }

    /**
     * RFC 0066 — the theme's word on the studio's classes: the monospace face,
     * everywhere the studio set a serif. The studio names Georgia on twelve
     * typed classes and on prose headings; a theme cannot inherit its way past
     * a class, so the face is answered where it is set — one override per
     * class, each appended inside that class's rule. (The count is the case
     * for a typography vocabulary in the palette — Episode 2's sub-module.)
     */
    public record Studio() implements StudioStyles.Overrides<HomingTurboC> {
        public static final Studio INSTANCE = new Studio();
        @Override public HomingTurboC theme() { return HomingTurboC.INSTANCE; }

        private static final String MONO = """
                font-family: "Consolas", "DejaVu Sans Mono", "Lucida Console", "Courier New", monospace;
                letter-spacing: 0;
                """;

        public CssBlock<StudioStyles.st_page>            st_page()            { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_title>           st_title()           { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_section_title>   st_section_title()   { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_panel_title>     st_panel_title()     { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_card_title>      st_card_title()      { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_brand_word>      st_brand_word()      { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_list_item_label> st_list_item_label() { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_app_pill_label>  st_app_pill_label()  { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_app_pill_icon>   st_app_pill_icon()   { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_step_id>         st_step_id()         { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_step_label>      st_step_label()      { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_effort>          st_effort()          { return CssBlock.of(MONO); }
        public CssBlock<StudioStyles.st_overall_pct>     st_overall_pct()     { return CssBlock.of(MONO); }
        /** The prose headings nest under st_doc. */
        public CssBlock<StudioStyles.st_doc> st_doc() { return CssBlock.of("h1, h2, h3, h4 {\n" + MONO.indent(4) + "}\n"); }
    }
}
