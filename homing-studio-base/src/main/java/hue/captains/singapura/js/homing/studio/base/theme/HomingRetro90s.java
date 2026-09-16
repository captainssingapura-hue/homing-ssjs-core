package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.ClickTarget;
import hue.captains.singapura.js.homing.core.CssBlock;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.core.Cue;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeAudio;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.Map;

/**
 * Retro 90s theme — Windows-95 era trading-workstation aesthetic.
 *
 * <p>Visual reference: the early-1990s financial-terminal look — Windows-95
 * desktop teal as the page chassis ({@code #008080}), VGA-blue ({@code #0000A8})
 * "windows" for the catalogue cards, navy-gradient title bars with white
 * caption text, light-grey ({@code #C0C0C0}) task-bar chrome for the header
 * and footer, monospace typography throughout.</p>
 *
 * <p>Distinguishing features beyond the palette:</p>
 *
 * <ol>
 *   <li><b>Card shape mutation.</b> {@code .st-card} is reshaped from a
 *       rounded-corner left-accented tile into a Windows-95-style window:
 *       zero border-radius, the iconic blue surface, an inset white bevel
 *       echoing the {@code BorderStyle.Fixed3D} chrome, a navy-gradient
 *       title-bar strip on top (rendered via {@code ::before}). The
 *       border-left emphasis accent is dropped — Win95 windows didn't
 *       accent one side.</li>
 *   <li><b>Body font override.</b> Courier New / Consolas / monospace across
 *       the entire studio chrome.</li>
 *   <li><b>CRT scanline overlay.</b> A subtle horizontal-line gradient on
 *       {@code body::before} simulates the phosphor refresh pattern of an
 *       early-90s CRT monitor — gentle enough to read through, present
 *       enough to set the mood.</li>
 * </ol>
 *
 * <p>The card-shape change is a set of per-class overrides (RFC 0066,
 * {@link Studio}): each block is appended inside the class's own rule after
 * the declared body, so it wins by source order at the same specificity —
 * no {@code @layer theme}, no {@code !important}, and nothing outside the
 * class it changes.</p>
 *
 * <p>Activate via {@code ?theme=retro-90s} on any studio URL.</p>
 */
public record HomingRetro90s() implements Theme {

    public static final HomingRetro90s INSTANCE = new HomingRetro90s();

    @Override public String slug()  { return "retro-90s"; }
    @Override public String label() { return "Retro 90s"; }
    @Override public String group() { return "Retro"; }
    @Override public String inspiration() { return "The Windows-95 desktop — teal, VGA blue and raised grey chrome."; }

    /** Theme-audio binding — clicks on the desktop icons fire system-
     *  click sounds; clicks on catalogue cards fire a soft membrane
     *  thud. RFC 0007. */
    @Override
    public ThemeAudio<?> audio() {
        return StandardAudio.INSTANCE;
    }

    // ===========================================================================
    //  Click targets — sealed permits enumerate every clickable element on the
    //  Retro 90s surface. Each record carries a classToken matching either an
    //  SVG class in desktop.svg or a framework CssClass name.
    // ===========================================================================

    /** Sealed surface area of audio-bound Retro-90s elements — desktop
     *  icons (click cues) + chrome elements (hover cues). */
    public sealed interface R90sTarget extends ClickTarget<HomingRetro90s>
            permits MyComputer, MyDocuments, NetworkNeighborhood, RecycleBin,
                    Card, ListItem, TocItem {}

    // Desktop icons — click cues.
    public record MyComputer()          implements R90sTarget { @Override public String classToken() { return "w95-icon-mycomputer"; } }
    public record MyDocuments()         implements R90sTarget { @Override public String classToken() { return "w95-icon-documents"; } }
    public record NetworkNeighborhood() implements R90sTarget { @Override public String classToken() { return "w95-icon-network"; } }
    public record RecycleBin()          implements R90sTarget { @Override public String classToken() { return "w95-icon-recycle"; } }

    // Chrome — Card is both click-bound (CARD_THUD) AND hover-bound
    // (HOVER_BLEEP). List + TOC items are hover-only.
    public record Card()     implements R90sTarget { @Override public String classToken() { return "st-card"; } }
    public record ListItem() implements R90sTarget { @Override public String classToken() { return "st-list-item"; } }
    public record TocItem()  implements R90sTarget { @Override public String classToken() { return "st-toc-item"; } }

    /** Retro 90s' audio spec. */
    public interface R90sAudio extends ThemeAudio<HomingRetro90s> {
        Cue myComputer();
        Cue myDocuments();
        Cue networkNeighborhood();
        Cue recycleBin();
        Cue card();

        @Override default HomingRetro90s theme() { return HomingRetro90s.INSTANCE; }

        @Override default java.util.Map<ClickTarget<HomingRetro90s>, Cue> bindings() {
            return java.util.Map.of(
                    new MyComputer(),          myComputer(),
                    new MyDocuments(),         myDocuments(),
                    new NetworkNeighborhood(), networkNeighborhood(),
                    new RecycleBin(),          recycleBin(),
                    new Card(),                card()
            );
        }
    }

    /** Standard implementation — click cues (desktop icons + card) +
     *  hover cues (Win95 selection bleep on chrome elements, with each
     *  element getting its own pitch from the shared vocal palette). */
    public record StandardAudio() implements R90sAudio {
        public static final StandardAudio INSTANCE = new StandardAudio();
        @Override public Cue myComputer()          { return Cues.WIN95_CLICK; }
        @Override public Cue myDocuments()         { return Cues.WIN95_CLICK; }
        @Override public Cue networkNeighborhood() { return Cues.WIN95_CLICK; }
        @Override public Cue recycleBin()          { return Cues.WIN95_DING; }
        @Override public Cue card()                { return Cues.CARD_THUD; }

        @Override public java.util.Map<ClickTarget<HomingRetro90s>, Cue> hoverBindings() {
            return java.util.Map.of(
                    // Cards play a chiptune chord — Genesis / NES-style triad.
                    // Each card's chord is hash-stable within a session.
                    new Card(),     Cues.HOVER_CHORD_RETRO,
                    // List + TOC items keep the single-note bleep — period-correct
                    // Win95 selection feedback for navigation surfaces.
                    new ListItem(), Cues.HOVER_BLEEP,
                    new TocItem(),  Cues.HOVER_BLEEP
            );
        }
    }

    public record Palette() implements GlobalColorPalette.Provision<HomingRetro90s> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingRetro90s theme() { return HomingRetro90s.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
            @Override public Map<CssVar, String> darkValues() { return DARK; }
            @Override public String colorScheme() { return "dark"; }

            /** The re-binding under prefers-color-scheme: dark — the colour roles only;
             *  the scales are the same job in both modes. */
            private static final Map<CssVar, String> DARK = Map.ofEntries(
                    Map.entry(HomingVars.COLOR_SURFACE,               "#003636"),
                    Map.entry(HomingVars.COLOR_SURFACE_RAISED,        "#000060"),
                    Map.entry(HomingVars.COLOR_SURFACE_RECESSED,      "#002020"),
                    Map.entry(HomingVars.COLOR_SURFACE_INVERTED,      "#585858"),
                    Map.entry(HomingVars.COLOR_TEXT_PRIMARY,          "#FFFFFF"),
                    Map.entry(HomingVars.COLOR_TEXT_MUTED,            "#55FFFF"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,      "#FFFFFF"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#FF5555"),
                    Map.entry(HomingVars.COLOR_TEXT_TITLE,            "#FFFF55"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK,             "#FFFF55"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,       "#FFFFFF"),
                    Map.entry(HomingVars.COLOR_BORDER,                "#A8A8A8"),
                    Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,       "#FFFF55"),
                    Map.entry(HomingVars.COLOR_ACCENT,                "#FFFF55"),
                    Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,       "#FFFFFF"),
                    Map.entry(HomingVars.COLOR_ACCENT_ON,             "#000060")
            );

        // Windows-95-era palette: desktop teal chassis, VGA-blue windows,
        // light-grey task bars. Hex values are the actual period defaults
        // (Win95 desktop teal #008080, VGA blue #0000A8, Win95 chrome #C0C0C0).
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — teal page background, blue card windows, grey chrome.
                Map.entry(HomingVars.COLOR_SURFACE,          "#008080"),  // Win95 desktop teal
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,   "#0000A8"),  // VGA blue — the iconic card window
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED, "#006666"),  // deeper teal — recessed wells
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED, "#C0C0C0"),  // Win95 chrome grey — header/footer task bar

                // Text — white on teal/blue surfaces, black on grey task bars,
                // cyan field-labels, amber for the link/highlight role.
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           "#FFFFFF"),  // bright white
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             "#55FFFF"),  // bright cyan — labels
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       "#000000"),  // black on grey
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#800000"),  // dark red — session badge
                Map.entry(HomingVars.COLOR_TEXT_TITLE,              "#FFFF55"),   // title := link, unchanged
                Map.entry(HomingVars.COLOR_TEXT_LINK,              "#FFFF55"),  // bright amber
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        "#FFFFFF"),

                // Borders — solid white for window edges, amber for emphasis.
                Map.entry(HomingVars.COLOR_BORDER,          "#FFFFFF"),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS, "#FFFF55"),

                // Accent — amber. Classic terminal highlight colour.
                Map.entry(HomingVars.COLOR_ACCENT,          "#FFFF55"),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS, "#FFFFFF"),
                Map.entry(HomingVars.COLOR_ACCENT_ON,       "#0000A8"),

                // Spacing — tighter than default; terminals don't breathe.
                Map.entry(HomingVars.SPACE_1, "2px"),
                Map.entry(HomingVars.SPACE_2, "4px"),
                Map.entry(HomingVars.SPACE_3, "8px"),
                Map.entry(HomingVars.SPACE_4, "12px"),
                Map.entry(HomingVars.SPACE_5, "16px"),
                Map.entry(HomingVars.SPACE_6, "20px"),
                Map.entry(HomingVars.SPACE_7, "28px"),
                Map.entry(HomingVars.SPACE_8, "36px"),

                // Radius — zero. Pure rectangles. The 1990s had no rounded corners.
                Map.entry(HomingVars.RADIUS_SM, "0"),
                Map.entry(HomingVars.RADIUS_MD, "0"),
                Map.entry(HomingVars.RADIUS_LG, "0")
        );
    }

    /**
     * RFC 0066 — the theme's word on the studio's classes. The desktop teal
     * behind everything, CRT scanlines and the monospace chrome on the page;
     * cards reshaped into Win95 windows; the reading pane and the outline as
     * Notepad-ish application windows on a cream surface; the doc-meta strip
     * as a status bar. What used to be four raw chunks on {@code @layer theme}
     * is one block per class, each inside the class's own rule.
     */
    public record Studio() implements StudioStyles.Overrides<HomingRetro90s> {
        public static final Studio INSTANCE = new Studio();
        @Override public HomingRetro90s theme() { return HomingRetro90s.INSTANCE; }

        private static final String TITLE_BAR = """
                display: block;
                background: linear-gradient(to right, #000080 0%, #1084D0 100%);
                color: #FFFFFF;
                font-family: "Tahoma", "MS Sans Serif", sans-serif;
                font-weight: 700;
                font-size: 12px;
                letter-spacing: 0.5px;
                padding: 2px 8px;
                border-bottom: 1px solid #000040;
                """;

        /**
         * The desktop as the page surface — a fixed gradient built from the
         * theme's own surface token, so it dims with the dark re-binding rather
         * than carrying a second palette; body lets it through. Monospace
         * chrome. CRT scanlines: a fixed pseudo-element on body, 3px pitch,
         * pointer-events none — real CRTs do not scroll their refresh pattern.
         */
        public CssBlock<StudioStyles.st_page> st_page() { return CssBlock.of("""
                font-family: "Courier New", "Consolas", "Lucida Console", monospace;
                font-size: 13px;
                letter-spacing: 0;
                &:is(html) {
                    background: linear-gradient(
                        180deg,
                        color-mix(in srgb, var(--color-surface) 82%, white) 0%,
                        var(--color-surface) 40%,
                        color-mix(in srgb, var(--color-surface) 72%, black) 100%);
                    background-attachment: fixed;
                }
                &:is(body) {
                    background: transparent;
                    position: relative;
                    &::before {
                        content: "";
                        position: fixed;
                        top: 0; left: 0; right: 0; bottom: 0;
                        background-image: repeating-linear-gradient(
                            to bottom,
                            rgba(0, 0, 0, 0)      0,
                            rgba(0, 0, 0, 0)      1px,
                            rgba(0, 0, 0, 0.12)   2px,
                            rgba(0, 0, 0, 0.12)   3px
                        );
                        pointer-events: none;
                        z-index: 9999;
                    }
                }
                """); }

        /** The rounded, left-accented tile becomes a Win95 window: no radius, a
         *  navy title-bar strip via ::before, a Fixed3D inset bevel. */
        public CssBlock<StudioStyles.st_card> st_card() { return CssBlock.of("""
                background: var(--color-surface-raised);
                border: 1px solid var(--color-border);
                border-left: 1px solid var(--color-border);
                border-radius: 0;
                padding: 0;
                overflow: hidden;
                box-shadow:
                    inset 1px 1px 0 rgba(255, 255, 255, 0.6),
                    inset -1px -1px 0 rgba(0, 0, 0, 0.4);
                min-height: 130px;
                color: #FFFFFF;
                &::before {
                    content: "▸";
                """ + TITLE_BAR.indent(4) + """
                    border-bottom: 1px solid var(--color-border);
                    padding: 1px 8px;
                    line-height: 16px;
                    letter-spacing: 1px;
                }
                & > * { padding-left: 10px; padding-right: 10px; }
                & > *:first-child { padding-top: 8px; }
                & > *:last-child  { padding-bottom: 8px; }
                """); }

        public CssBlock<StudioStyles.st_card_title> st_card_title() { return CssBlock.of("""
                font-family: "Courier New", "Consolas", monospace;
                font-weight: 700;
                color: var(--color-text-link);
                text-transform: uppercase;
                letter-spacing: 0.5px;
                font-size: 14px;
                """); }
        public CssBlock<StudioStyles.st_card_summary> st_card_summary() { return CssBlock.of("""
                color: #FFFFFF;
                font-size: 12px;
                """); }
        public CssBlock<StudioStyles.st_card_meta> st_card_meta() { return CssBlock.of("""
                background: var(--color-surface-recessed);
                border-top: 1px solid var(--color-border);
                color: var(--color-text-muted);
                font-size: 11px;
                padding: 2px 10px;
                margin: 0;
                """); }
        public CssBlock<StudioStyles.st_card_link> st_card_link() { return CssBlock.of("""
                color: var(--color-text-link);
                letter-spacing: 1px;
                """); }
        /** The featured card keeps the blue-window look; its full-width grid
         *  placement already differentiates it. */
        public CssBlock<StudioStyles.st_card_featured> st_card_featured() { return CssBlock.of("""
                background: var(--color-surface-raised);
                border-left: 1px solid var(--color-border);
                """); }
        /** The header echoes the workstation title strip — grey task bar. */
        public CssBlock<StudioStyles.st_header> st_header() { return CssBlock.of("""
                background: var(--color-surface-inverted);
                color: var(--color-text-on-inverted);
                border-bottom: 1px solid var(--color-border);
                box-shadow: none;
                """); }
        /** The footer echoes the F-key bar at the bottom of the workbench. */
        public CssBlock<StudioStyles.st_footer> st_footer() { return CssBlock.of("""
                background: var(--color-surface-inverted);
                color: var(--color-text-on-inverted);
                border-top: 1px solid var(--color-border);
                font-family: "Courier New", monospace;
                """); }

        /**
         * The reading pane as the "Document Reader" window: cream Notepad
         * surface, black text, a period Tahoma / MS Sans Serif body (prose
         * wants proportional letterforms; the chrome stays monospace; code
         * stays monospace — three-way), navy title bar, Fixed3D bevel. Retro
         * opts out of the framework's column slab so the desktop bleeds
         * through behind the cards, which is exactly why the reading panes
         * need their own contrast surface.
         */
        public CssBlock<StudioStyles.st_doc> st_doc() { return CssBlock.of("""
                background: #FFFFE1;
                color: #000000;
                font-family: "Tahoma", "MS Sans Serif", "Geneva", "Arial", sans-serif;
                font-size: 13px;
                line-height: 1.55;
                letter-spacing: 0;
                border: 1px solid var(--color-border);
                border-radius: 0;
                box-shadow:
                    inset 1px 1px 0 rgba(255, 255, 255, 0.8),
                    inset -1px -1px 0 rgba(0, 0, 0, 0.4);
                padding: 0 16px 16px;
                max-width: none;
                &::before {
                    content: "📄  Document Reader";
                    margin: 0 -16px 14px;
                """ + TITLE_BAR.indent(4) + """
                }
                pre, code, kbd, samp { font-family: "Courier New", "Consolas", "Lucida Console", monospace; }
                h1, h2, h3, h4 { font-family: "Tahoma", "MS Sans Serif", "Arial", sans-serif; color: #000080; }
                a { color: #0000EE; text-decoration: underline; }
                a:visited { color: #551A8B; }
                blockquote { border-left: 3px solid #808080; background: #FFFFCC; color: #000000; }
                """); }

        /** The outline as its own window. */
        public CssBlock<StudioStyles.st_sidebar> st_sidebar() { return CssBlock.of("""
                background: #FFFFE1;
                color: #000000;
                font-family: "Tahoma", "MS Sans Serif", "Geneva", "Arial", sans-serif;
                font-size: 12px;
                border: 1px solid var(--color-border);
                border-radius: 0;
                box-shadow:
                    inset 1px 1px 0 rgba(255, 255, 255, 0.8),
                    inset -1px -1px 0 rgba(0, 0, 0, 0.4);
                padding: 0 12px 12px;
                &::before {
                    content: "📑  Outline";
                    margin: 0 -12px 10px;
                """ + TITLE_BAR.indent(4) + """
                }
                """); }
        public CssBlock<StudioStyles.st_sidebar_title> st_sidebar_title() { return CssBlock.of("""
                color: #000080;
                font-weight: 700;
                """); }

        /** The doc-meta strip as a Win95 status bar: grey chassis, sunken bevel. */
        public CssBlock<StudioStyles.st_doc_meta> st_doc_meta() { return CssBlock.of("""
                background: #C0C0C0;
                color: #000000;
                font-family: "Tahoma", "MS Sans Serif", sans-serif;
                font-size: 12px;
                border: 1px solid var(--color-border);
                box-shadow:
                    inset 1px 1px 0 rgba(0, 0, 0, 0.4),
                    inset -1px -1px 0 rgba(255, 255, 255, 0.8);
                padding: 4px 10px;
                margin-bottom: 8px;
                """); }
    }
}
