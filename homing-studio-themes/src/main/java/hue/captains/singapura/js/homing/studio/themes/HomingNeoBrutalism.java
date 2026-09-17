package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssBlock;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.theme.type.HomingFonts;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.ImplProvider;
import hue.captains.singapura.js.homing.studio.base.theme.ThemePickerStyles;
import hue.captains.singapura.js.homing.studio.base.ui.MasterDetailStyles;
import hue.captains.singapura.js.homing.studio.base.ui.SystemDialogStyles;

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

    // ── RFC 0066 — the theme's word, per class ─────────────────────────────
    //
    // What used to be ten raw chunks on @layer theme is one block per class,
    // each appended inside the class's own rule after the declared body. The
    // theme's own vocabulary (--bru-*) rides on the palette provision as
    // extras derived from the semantic tokens, so the dark flip carries it.
    // The workspace's buttons and input (.ws-*, .cg-*, .pm-*) are not here:
    // studio-base cannot see workspace-shell, and a theme that has something
    // to say about another product's classes says it in an impl a module that
    // sees both can register — BrutalistWorkspace, in homing-studio-workspace.

    /** Press-into-shadow, shared by every plate and button. */
    static final String PRESS_HOVER = """
            transform: translate(-2px, -2px);
            box-shadow: var(--bru-shadow);
            border-color: var(--bru-ink);
            """;
    static final String PRESS_ACTIVE = """
            transform: translate(5px, 5px);
            box-shadow: 0 0 0 var(--bru-ink);
            """;
    static final String FOCUS_RING = """
            outline: 4px solid var(--color-text-link);
            outline-offset: 4px;
            """;
    static final String NO_MOTION = "@media (prefers-reduced-motion: reduce) { & { transition: none; } }\n";

    /** A plate — card, row, panel, pill: hard rule, offset shadow, the press. */
    static final String PLATE = """
            background: var(--bru-paper);
            border: var(--bru-rule);
            border-radius: 0;
            box-shadow: var(--bru-shadow-md);
            transition: var(--bru-snap);
            &:focus-visible {
            """ + FOCUS_RING.indent(4) + """
            }
            """ + NO_MOTION;
    static final String PLATE_PRESS = """
            &:hover {
            """ + PRESS_HOVER.indent(4) + """
            }
            &:active {
            """ + PRESS_ACTIVE.indent(4) + """
            }
            """;

    /** A display label — the stamped, heavy, uppercase face. */
    static final String DISPLAY = """
            font-family: var(--font-display);
            font-weight: 900;
            """;
    /** An inverted tag — paper on ink. */
    static final String TAG = """
            display: inline-block;
            font-family: var(--font-display);
            font-weight: 900;
            font-size: 12px;
            letter-spacing: 0.04em;
            color: var(--bru-paper);
            background: var(--bru-ink);
            border: 0;
            padding: 5px 11px;
            """;
    /** A button — hard border, offset shadow, yellow on hover, pressed on
     *  active, blue outline on keyboard focus, hatched when inert. */
    public static final String BUTTON = """
            font-family: var(--font-display);
            font-weight: 900;
            font-size: 12px;
            letter-spacing: -0.01em;
            text-transform: uppercase;
            color: var(--bru-ink);
            background: var(--bru-paper);
            border: var(--bru-rule-3);
            border-radius: 0;
            box-shadow: var(--bru-shadow-sm);
            padding: 7px 14px;
            transition: var(--bru-snap);
            &:hover {
                background: var(--color-accent);
                color: var(--bru-ink);
                border-color: var(--bru-ink);
                transform: translate(-2px, -2px);
                box-shadow: 6px 6px 0 var(--bru-ink);
            }
            &:active {
                transform: translate(4px, 4px);
                box-shadow: 0 0 0 var(--bru-ink);
            }
            &:focus-visible {
            """ + FOCUS_RING.indent(4) + """
            }
            &[disabled], &[disabled]:hover, &[disabled]:active {
                background: var(--bru-hatch), var(--bru-paper);
                color: var(--color-text-muted);
                transform: none;
                box-shadow: none;
                opacity: 1;
                cursor: not-allowed;
            }
            """ + NO_MOTION;
    /** The inert state a class asserts by name rather than by attribute. */
    public static final String INERT = """
            background: var(--bru-hatch), var(--bru-paper);
            color: var(--color-text-muted);
            box-shadow: none;
            opacity: 1;
            cursor: not-allowed;
            &:hover, &:active {
                background: var(--bru-hatch), var(--bru-paper);
                color: var(--color-text-muted);
                transform: none;
                box-shadow: none;
            }
            """;
    /** A mark — badge, chip: bordered, square, heavy. */
    static final String MARK = """
            border: 2px solid var(--bru-ink);
            border-radius: 0;
            font-weight: 900;
            letter-spacing: 0.06em;
            """;

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


    /** The system dialog — a plate with a deep shadow, an ink title bar, and a
     *  hatched scrim in place of the blur. */
    public record Dialog() implements SystemDialogStyles.Overrides<HomingNeoBrutalism> {
        public static final Dialog INSTANCE = new Dialog();
        @Override public HomingNeoBrutalism theme() { return HomingNeoBrutalism.INSTANCE; }

        public CssBlock<SystemDialogStyles.sd_scrim> sd_scrim() { return CssBlock.of("background: var(--bru-hatch);\nbackdrop-filter: brightness(0.7);\n"); }
        public CssBlock<SystemDialogStyles.sd_frame> sd_frame() { return CssBlock.of("border: var(--bru-rule);\nborder-radius: 0;\nbox-shadow: 12px 12px 0 var(--bru-ink);\n"); }
        public CssBlock<SystemDialogStyles.sd_glow>  sd_glow()  { return CssBlock.of("border-color: var(--bru-ink);\nbox-shadow: 12px 12px 0 var(--color-accent);\n"); }
        public CssBlock<SystemDialogStyles.sd_title> sd_title() { return CssBlock.of("height: 34px;\nbackground: var(--bru-ink);\nborder-bottom: var(--bru-rule);\n"); }
        public CssBlock<SystemDialogStyles.sd_title_label> sd_title_label() { return CssBlock.of(DISPLAY + "font-size: 12px;\nletter-spacing: 0.04em;\ncolor: var(--bru-paper);\n"); }
        public CssBlock<SystemDialogStyles.sd_close> sd_close() { return CssBlock.of("""
                color: var(--bru-paper);
                font-weight: 900;
                &:focus-visible {
                """ + FOCUS_RING.indent(4) + """
                }
                """); }
        public CssBlock<SystemDialogStyles.sd_actions> sd_actions() { return CssBlock.of("border-top: var(--bru-rule);\nbackground: var(--bru-paper);\npadding: 12px 14px;\ngap: 14px;\n"); }
        public CssBlock<SystemDialogStyles.sd_action> sd_action() { return CssBlock.of(BUTTON); }
        public CssBlock<SystemDialogStyles.sd_action_primary> sd_action_primary() { return CssBlock.of("""
                background: var(--color-accent);
                color: var(--bru-ink);
                border-color: var(--bru-ink);
                &:hover { background: var(--bru-paper); color: var(--bru-ink); }
                """); }
        public CssBlock<SystemDialogStyles.sd_action_off> sd_action_off() { return CssBlock.of(INERT); }
    }

    /** The picker — its header button on the ink masthead, the inline and
     *  preview frames, and the in-use chip. */
    public record Picker() implements ThemePickerStyles.Overrides<HomingNeoBrutalism> {
        public static final Picker INSTANCE = new Picker();
        @Override public HomingNeoBrutalism theme() { return HomingNeoBrutalism.INSTANCE; }

        public CssBlock<ThemePickerStyles.tp_btn> tp_btn() { return CssBlock.of(DISPLAY + """
                font-size: 11px;
                text-transform: uppercase;
                border: 2px solid var(--color-text-on-inverted);
                border-radius: 0;
                box-shadow: 3px 3px 0 var(--color-accent);
                padding: 4px 10px;
                transition: var(--bru-snap);
                &:hover {
                    background: var(--color-accent);
                    color: var(--color-accent-on);
                    border-color: var(--color-accent);
                    transform: translate(-2px, -2px);
                    box-shadow: 5px 5px 0 var(--color-text-on-inverted);
                }
                &:hover .tp-btn-label { color: var(--color-accent-on); }
                &:active { transform: translate(3px, 3px); box-shadow: 0 0 0 var(--color-accent); }
                &:focus-visible {
                """ + FOCUS_RING.indent(4) + """
                }
                """ + NO_MOTION); }
        public CssBlock<ThemePickerStyles.tp_inline> tp_inline() { return CssBlock.of("border: var(--bru-rule);\nborder-radius: 0;\nbox-shadow: var(--bru-shadow);\n"); }
        public CssBlock<ThemePickerStyles.tp_inline_head> tp_inline_head() { return CssBlock.of(DISPLAY + "text-transform: uppercase;\nborder-bottom: var(--bru-rule);\n"); }
        public CssBlock<ThemePickerStyles.tp_preview_frame>   tp_preview_frame()   { return CssBlock.of("border: var(--bru-rule);\nborder-radius: 0;\n"); }
        public CssBlock<ThemePickerStyles.tp_preview_loading> tp_preview_loading() { return CssBlock.of("border-radius: 0;"); }
        public CssBlock<ThemePickerStyles.tp_preview_name>    tp_preview_name()    { return CssBlock.of(DISPLAY); }
        public CssBlock<ThemePickerStyles.tp_current> tp_current() { return CssBlock.of("""
                color: var(--bru-ink);
                background: var(--color-accent);
                border: 2px solid var(--bru-ink);
                padding: 1px 6px;
                font-weight: 900;
                """); }
    }

    /** The master-detail nav rule. */
    public record MasterDetail() implements MasterDetailStyles.Overrides<HomingNeoBrutalism> {
        public static final MasterDetail INSTANCE = new MasterDetail();
        @Override public HomingNeoBrutalism theme() { return HomingNeoBrutalism.INSTANCE; }
        public CssBlock<MasterDetailStyles.md_nav> md_nav() { return CssBlock.of("border-right: var(--bru-rule);"); }
    }
}
