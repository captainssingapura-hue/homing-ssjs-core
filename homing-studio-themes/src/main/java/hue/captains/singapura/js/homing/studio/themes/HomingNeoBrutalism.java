package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.theme.type.HomingFonts;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.ImplProvider;

import java.util.Map;

/**
 * Brutalist theme — riso-print order form: ink on paper, a grid behind
 * everything, hard 4px rules, zero radius, solid offset shadows, and one
 * yellow ({@code #FFE800}) that shouts. Blue ({@code #2B4CFF}) for links,
 * red ({@code #FF3B21}) for hover and the destructive edge.
 *
 * <p>Built to find out how far a theme can go on CSS alone. Every rule is
 * as a per-class override (RFC 0066) and touches only classes the studio
 * already renders — no module, no markup, no script:</p>
 *
 * <ul>
 *   <li><b>Press-into-shadow.</b> Cards, list rows, buttons and dialog
 *       actions carry a solid offset shadow; {@code :active} translates the
 *       element by the shadow's offset and zeroes the shadow, with a
 *       {@code steps(2)} transition so it snaps rather than eases. Hover
 *       lifts the other way.</li>
 *   <li><b>Stamped labels.</b> The kicker becomes the rotated yellow box;
 *       section, panel and sidebar titles become inverted ink tags; the
 *       page title goes Arial Black, uppercase, tight.</li>
 *   <li><b>Loud focus.</b> The search field inverts to yellow and grows a
 *       shadow on focus; every button gets a 4px blue outline on
 *       {@code :focus-visible}. Affordance is kept and amplified, not
 *       stripped.</li>
 *   <li><b>Hatching for the inert.</b> Disabled actions and the dialog scrim
 *       use a 45° repeating gradient; progress fills are hatched ink on
 *       yellow.</li>
 * </ul>
 *
 * <p>What CSS could not reach is recorded where it was found: the shared
 * tree's rows (the picker's list, the catalogue tree) are inline-styled by
 * {@code TreeRendererModule} and keep their soft selection tint under every
 * theme. Everything else on the page follows.</p>
 *
 * <p>Dark mode swaps ink and paper — the shadows go white, the yellow stays.</p>
 */
public record HomingNeoBrutalism() implements Design {

    public static final HomingNeoBrutalism INSTANCE = new HomingNeoBrutalism();

    /** Over Default: its own word where it has one, Default's for the rest. */
    @Override public java.util.Optional<Design> base() { return java.util.Optional.of(HomingDefault.INSTANCE); }
    @Override public java.util.List<ImplProvider<?>> providers() { return NeoBrutalismDesign.PROVIDERS; }

    @Override public String slug()  { return "neo-brutalism"; }
    @Override public String label() { return "Neo-Brutalism"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "A riso-print order form — ink rules, offset shadows, one loud yellow."; }

    public record Palette() implements GlobalColorPalette.Provision<HomingNeoBrutalism> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingNeoBrutalism theme() { return HomingNeoBrutalism.INSTANCE; }
        @Override public Map<CssVar, String> values() { return Merged.ALL; }

        /**
         * RFC 0066 Law 2 — the theme's own vocabulary as EXTRAS on the palette
         * body: derived from the semantic tokens so the dark flip carries them
         * (ink is the text colour, paper the surface, a shadow a solid offset
         * block of ink). Prefixed, so they shadow nothing the palette declares;
         * read only by this theme's overrides, never by a declared body.
         */
        static final CssVar BRU_INK       = new CssVar("--bru-ink");
        static final CssVar BRU_PAPER     = new CssVar("--bru-paper");
        static final CssVar BRU_RULE      = new CssVar("--bru-rule");
        static final CssVar BRU_RULE_3    = new CssVar("--bru-rule-3");
        static final CssVar BRU_SHADOW    = new CssVar("--bru-shadow");
        static final CssVar BRU_SHADOW_MD = new CssVar("--bru-shadow-md");
        static final CssVar BRU_SHADOW_SM = new CssVar("--bru-shadow-sm");
        static final CssVar BRU_SNAP      = new CssVar("--bru-snap");
        static final CssVar BRU_HATCH     = new CssVar("--bru-hatch");
        private static final Map<CssVar, String> EXTRAS = Map.ofEntries(
                Map.entry(BRU_INK,       "var(--color-text-primary)"),
                Map.entry(BRU_PAPER,     "var(--color-surface)"),
                Map.entry(BRU_RULE,      "4px solid var(--bru-ink)"),
                Map.entry(BRU_RULE_3,    "3px solid var(--bru-ink)"),
                Map.entry(BRU_SHADOW,    "8px 8px 0 var(--bru-ink)"),
                Map.entry(BRU_SHADOW_MD, "5px 5px 0 var(--bru-ink)"),
                Map.entry(BRU_SHADOW_SM, "4px 4px 0 var(--bru-ink)"),
                Map.entry(BRU_SNAP,      "transform 70ms steps(2), box-shadow 70ms steps(2), background 100ms"),
                Map.entry(BRU_HATCH,     "repeating-linear-gradient(45deg, color-mix(in srgb, var(--bru-ink) 22%, transparent) 0 7px, transparent 7px 14px)")
        );
        /** A holder, so the merge runs after VALUES (declared below) is set. */
        private static final class Merged {
            static final Map<CssVar, String> ALL;
            static {
                var m = new java.util.LinkedHashMap<CssVar, String>(VALUES);
                m.putAll(EXTRAS);
                ALL = java.util.Collections.unmodifiableMap(m);
            }
        }
            @Override public Map<CssVar, String> darkValues() { return DARK; }

            /** The re-binding under prefers-color-scheme: dark — the colour roles only;
             *  the scales are the same job in both modes. */
            private static final Map<CssVar, String> DARK = Map.ofEntries(
                    Map.entry(HomingVars.COLOR_SURFACE,               "#0B0B0B"),
                    Map.entry(HomingVars.COLOR_SURFACE_RAISED,        "#0B0B0B"),
                    Map.entry(HomingVars.COLOR_SURFACE_RECESSED,      "#1E1E1E"),
                    Map.entry(HomingVars.COLOR_SURFACE_INVERTED,      "#FFFFFF"),
                    Map.entry(HomingVars.COLOR_TEXT_PRIMARY,          "#FFFFFF"),
                    Map.entry(HomingVars.COLOR_TEXT_MUTED,            "#BDBDBD"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,      "#000000"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#3A3A3A"),
                    Map.entry(HomingVars.COLOR_TEXT_TITLE,            "#FFFFFF"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK,             "#7D93FF"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,       "#FF6A55"),
                    Map.entry(HomingVars.COLOR_BORDER,                "#2E2E2E"),
                    Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,       "#FFFFFF"),
                    Map.entry(HomingVars.COLOR_ACCENT,                "#FFE800"),
                    Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,       "#FF6A55"),
                    Map.entry(HomingVars.COLOR_ACCENT_ON,             "#000000")
            );

        // Ink, paper, and the three riso drums — yellow, blue, red. The
        // hairline border is the grid's grey; the overlay hardens to ink
        // wherever a rule is meant to be seen.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — paper page, ink masthead.
                Map.entry(HomingVars.COLOR_SURFACE,          "#FFFFFF"),
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,   "#FFFFFF"),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED, "#EFEFEF"),
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED, "#000000"),

                // Text — ink; muted stays well inside legibility.
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           "#000000"),
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             "#4A4A4A"),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       "#FFFFFF"),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#D9D9D9"),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,             "#000000"),
                Map.entry(HomingVars.COLOR_TEXT_LINK,              "#2B4CFF"),  // riso blue
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        "#FF3B21"),  // riso red

                // Borders — grid grey for hairlines, ink for emphasis.
                Map.entry(HomingVars.COLOR_BORDER,          "#D9D9D9"),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS, "#000000"),

                // Accent — the yellow; red when pressed; ink on it.
                Map.entry(HomingVars.COLOR_ACCENT,          "#FFE800"),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS, "#FF3B21"),
                Map.entry(HomingVars.COLOR_ACCENT_ON,       "#000000"),

                // Spacing — the default scale.
                Map.entry(HomingVars.SPACE_1, "4px"),
                Map.entry(HomingVars.SPACE_2, "8px"),
                Map.entry(HomingVars.SPACE_3, "12px"),
                Map.entry(HomingVars.SPACE_4, "16px"),
                Map.entry(HomingVars.SPACE_5, "20px"),
                Map.entry(HomingVars.SPACE_6, "24px"),
                Map.entry(HomingVars.SPACE_7, "32px"),
                Map.entry(HomingVars.SPACE_8, "40px"),

                // Radius — none. A corner is a corner.
                Map.entry(HomingVars.RADIUS_SM, "0px"),
                Map.entry(HomingVars.RADIUS_MD, "0px"),
                Map.entry(HomingVars.RADIUS_LG, "0px")
        );
    }

    /** A grotesque body and a black display — the riso form's two voices;
     *  code keeps the house mono. */
    public record Fonts() implements GlobalTypePalette.Provision<HomingNeoBrutalism> {
        public static final Fonts INSTANCE = new Fonts();
        @Override public HomingNeoBrutalism theme() { return HomingNeoBrutalism.INSTANCE; }
        @Override public Map<CssVar, String> values() {
            return Map.of(HomingFonts.FONT_BODY,    "Helvetica, Arial, sans-serif",
                          HomingFonts.FONT_DISPLAY, "\"Arial Black\", \"Helvetica Neue\", Helvetica, Arial, sans-serif",
                          HomingFonts.FONT_MONO,    StudioFonts.MONO);
        }
    }


}
