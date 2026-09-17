package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssBlock;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.Map;

/**
 * Maple Bridge — a tier-3 layered theme inspired by Zhang Ji's Tang-dynasty
 * poem 枫桥夜泊 ("Night Mooring at Maple Bridge"). A night sky as the page
 * surface; the studio chrome rides over it on parchment.
 *
 * <p>Until RFC 0064 the sky was a full-page inline SVG nocturne the framework
 * injected behind the chrome, with a moon that grew on hover. That backdrop
 * is retired: the server no longer knows which theme a page wears, so a part
 * only the server could render was a part that only sometimes applied. The
 * illustration and its per-element interaction are the subject of a later,
 * proper design; what remains here is the palette and a gradient.</p>
 *
 * <p>Activate via {@code ?theme=maple-bridge} on any studio URL, or pick it.</p>
 */
public record HomingMapleBridge() implements Theme {

    public static final HomingMapleBridge INSTANCE = new HomingMapleBridge();

    @Override public String slug()  { return "maple-bridge"; }
    @Override public String label() { return "Maple Bridge"; }
    @Override public String group() { return "Nature"; }
    @Override public String inspiration() { return "A Tang-dynasty nocturne, after Zhang Ji's 枫桥夜泊."; }

    public record Palette() implements GlobalColorPalette.Provision<HomingMapleBridge> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingMapleBridge theme() { return HomingMapleBridge.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
            @Override public Map<CssVar, String> darkValues() { return DARK; }

            /** The re-binding under prefers-color-scheme: dark — the colour roles only;
             *  the scales are the same job in both modes. */
            private static final Map<CssVar, String> DARK = Map.ofEntries(
                    Map.entry(HomingVars.COLOR_SURFACE,               "#0A131E"),   // water-bot
                    Map.entry(HomingVars.COLOR_SURFACE_RAISED,        "#1B2D44"),   // sky-mid night
                    Map.entry(HomingVars.COLOR_SURFACE_RECESSED,      "#0D1620"),   // mountain-near
                    Map.entry(HomingVars.COLOR_SURFACE_INVERTED,      "#0E1620"),   // temple
                    Map.entry(HomingVars.COLOR_TEXT_PRIMARY,          "#CBD9E8"),
                    Map.entry(HomingVars.COLOR_TEXT_MUTED,            "#7A89A0"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,      "#BCC7D6"),   // mist
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#7A89A0"),
                    Map.entry(HomingVars.COLOR_TEXT_TITLE,            "#F5E3B0"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK,             "#F5E3B0"),   // moon
                    Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,       "#F0DCA8"),
                    Map.entry(HomingVars.COLOR_BORDER,                "#3A5070"),   // ripple
                    Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,       "#F5E3B0"),
                    Map.entry(HomingVars.COLOR_ACCENT,                "#F5E3B0"),
                    Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,       "#D9A35A"),   // temple-window
                    Map.entry(HomingVars.COLOR_ACCENT_ON,             "#0A131E")
            );

        // Light mode (dawn) — distilled from the SVG's dawn palette but
        // independent of it. Page surfaces are translucent-ish warm tones
        // that read well against the dawn scene; cards/header are opaque
        // surfaces that sit cleanly over the illustration.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                Map.entry(HomingVars.COLOR_SURFACE,                "#E8C9A0"),  // sky-bot dawn
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,         "#F5E7C8"),  // raised paper
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED,       "#D4B896"),  // sky-mid dawn
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED,       "#3A4250"),  // temple slate

                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           "#2A2418"),  // deep ink
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             "#5A5040"),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       "#FFF5DC"),  // dawn moon
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#C0A878"),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,              "#8A6A3A"),   // title := link, unchanged
                Map.entry(HomingVars.COLOR_TEXT_LINK,              "#8A6A3A"),  // amber window
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        "#4A5466"),  // mountain-near

                Map.entry(HomingVars.COLOR_BORDER,                 "#C0A878"),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,        "#8A6A3A"),

                Map.entry(HomingVars.COLOR_ACCENT,                 "#8A6A3A"),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,        "#4A5466"),
                Map.entry(HomingVars.COLOR_ACCENT_ON,              "#FFF5DC"),

                Map.entry(HomingVars.SPACE_1, "4px"),
                Map.entry(HomingVars.SPACE_2, "8px"),
                Map.entry(HomingVars.SPACE_3, "12px"),
                Map.entry(HomingVars.SPACE_4, "16px"),
                Map.entry(HomingVars.SPACE_5, "20px"),
                Map.entry(HomingVars.SPACE_6, "24px"),
                Map.entry(HomingVars.SPACE_7, "32px"),
                Map.entry(HomingVars.SPACE_8, "40px"),
                Map.entry(HomingVars.RADIUS_SM, "3px"),
                Map.entry(HomingVars.RADIUS_MD, "6px"),
                Map.entry(HomingVars.RADIUS_LG, "10px")
        );
    }

    /** RFC 0066 — the theme's word on the studio's classes: the night behind
     *  the parchment, and the reading page lit from behind. */
    public record Studio() implements StudioStyles.Overrides<HomingMapleBridge> {
        public static final Studio INSTANCE = new Studio();
        @Override public HomingMapleBridge theme() { return HomingMapleBridge.INSTANCE; }

        /** The night sky on {@code html}, fixed; {@code body} lets it through.
         *  The declared rule is {@code html, body}; the nested selectors pick
         *  each element out of the pair. */
        public CssBlock<StudioStyles.st_page> st_page() { return CssBlock.of("""
                &:is(html) {
                    background: linear-gradient(180deg, #0A131E 0%, #12213A 45%, #24405C 100%);
                    background-attachment: fixed;
                }
                &:is(body) { background: transparent; }
                """); }

        /** The doc-reader slab goes slightly translucent so the night bleeds a
         *  hint through the parchment — the reading page only, as declared. */
        public CssBlock<StudioStyles.st_main> st_main() { return CssBlock.of("""
                &:has(.st-doc-meta) {
                    background-color: color-mix(in srgb, var(--color-surface-raised) 92%, transparent);
                }
                """); }

        public CssBlock<StudioStyles.st_doc> st_doc() { return CssBlock.of("padding: 28px 32px;"); }
        public CssBlock<StudioStyles.st_sidebar> st_sidebar() { return CssBlock.of("padding: 16px;"); }
    }
    /** The house faces — this theme has no typographic identity of its own. */
    public record Fonts() implements GlobalTypePalette.Provision<HomingMapleBridge> {
        public static final Fonts INSTANCE = new Fonts();
        @Override public HomingMapleBridge theme() { return HomingMapleBridge.INSTANCE; }
        @Override public Map<CssVar, String> values() { return StudioFonts.HOUSE; }
    }
}
