package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.Component;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Layer;
import hue.captains.singapura.js.homing.core.MediaGated;
import hue.captains.singapura.js.homing.core.Prose;
import hue.captains.singapura.js.homing.core.Reset;
import hue.captains.singapura.js.homing.core.State;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeOverlay;
import hue.captains.singapura.js.homing.core.ThemeVariables;

import java.util.Map;

/**
 * Brutalist theme — riso-print order form: ink on paper, a grid behind
 * everything, hard 4px rules, zero radius, solid offset shadows, and one
 * yellow ({@code #FFE800}) that shouts. Blue ({@code #2B4CFF}) for links,
 * red ({@code #FF3B21}) for hover and the destructive edge.
 *
 * <p>Built to find out how far a theme can go on CSS alone. Every rule is
 * in the {@code @layer theme} overlay and touches only classes the studio
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
public record HomingBrutalist() implements Theme {

    public static final HomingBrutalist INSTANCE = new HomingBrutalist();

    @Override public String slug()  { return "brutalist"; }
    @Override public String label() { return "Brutalist"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "A riso-print order form — ink rules, offset shadows, one loud yellow."; }

    public record Vars() implements ThemeVariables<HomingBrutalist> {
        public static final Vars INSTANCE = new Vars();
        @Override public HomingBrutalist theme() { return HomingBrutalist.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        // Ink, paper, and the three riso drums — yellow, blue, red. The
        // hairline border is the grid's grey; the overlay hardens to ink
        // wherever a rule is meant to be seen.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — paper page, ink masthead.
                Map.entry(StudioVars.COLOR_SURFACE,          "#FFFFFF"),
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,   "#FFFFFF"),
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED, "#EFEFEF"),
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED, "#000000"),

                // Text — ink; muted stays well inside legibility.
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#000000"),
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#4A4A4A"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#FFFFFF"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#D9D9D9"),
                Map.entry(StudioVars.COLOR_TEXT_TITLE,             "#000000"),
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#2B4CFF"),  // riso blue
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#FF3B21"),  // riso red

                // Borders — grid grey for hairlines, ink for emphasis.
                Map.entry(StudioVars.COLOR_BORDER,          "#D9D9D9"),
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS, "#000000"),

                // Accent — the yellow; red when pressed; ink on it.
                Map.entry(StudioVars.COLOR_ACCENT,          "#FFE800"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS, "#FF3B21"),
                Map.entry(StudioVars.COLOR_ACCENT_ON,       "#000000"),

                // Spacing — the default scale.
                Map.entry(StudioVars.SPACE_1, "4px"),
                Map.entry(StudioVars.SPACE_2, "8px"),
                Map.entry(StudioVars.SPACE_3, "12px"),
                Map.entry(StudioVars.SPACE_4, "16px"),
                Map.entry(StudioVars.SPACE_5, "20px"),
                Map.entry(StudioVars.SPACE_6, "24px"),
                Map.entry(StudioVars.SPACE_7, "32px"),
                Map.entry(StudioVars.SPACE_8, "40px"),

                // Radius — none. A corner is a corner.
                Map.entry(StudioVars.RADIUS_SM, "0px"),
                Map.entry(StudioVars.RADIUS_MD, "0px"),
                Map.entry(StudioVars.RADIUS_LG, "0px")
        );
    }

    public record Globals() implements ThemeGlobals<HomingBrutalist> {
        public static final Globals INSTANCE = new Globals();
        @Override public HomingBrutalist theme() { return HomingBrutalist.INSTANCE; }

        @Override public String css() {
            return HomingDefault.STRUCTURAL_CSS + DARK_OVERRIDE + TOKENS + SHEET + TYPE
                 + PLATES + BUTTONS + FIELDS + MARKS + PROSE + DIALOG + PICKER;
        }

        /** Structural chunks from the default theme; everything brutalist
         *  rides on {@code @layer theme}, above state and media, so the press
         *  and focus rules win over the default hover/focus set. */
        @Override
        public Map<Class<? extends Layer>, String> chunks() {
            return Map.of(
                    Reset.class,        HomingDefault.STRUCTURAL_CHUNKS.get(Reset.class),
                    Component.class,    HomingDefault.STRUCTURAL_CHUNKS.get(Component.class),
                    Prose.class,        HomingDefault.STRUCTURAL_CHUNKS.get(Prose.class),
                    State.class,        HomingDefault.STRUCTURAL_CHUNKS.get(State.class),
                    MediaGated.class,   HomingDefault.STRUCTURAL_CHUNKS.get(MediaGated.class),
                    ThemeOverlay.class,
                            DARK_OVERRIDE + TOKENS + SHEET + TYPE + PLATES + BUTTONS
                          + FIELDS + MARKS + PROSE + DIALOG + PICKER
            );
        }

        /** Dark mode — ink and paper trade places; the drums stay. The
         *  masthead becomes a paper band on the ink page so it still reads
         *  as the inverted element. */
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: light dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        --color-surface:           #0B0B0B;
                        --color-surface-raised:    #0B0B0B;
                        --color-surface-recessed:  #1E1E1E;
                        --color-surface-inverted:  #FFFFFF;

                        --color-text-primary:            #FFFFFF;
                        --color-text-muted:              #BDBDBD;
                        --color-text-on-inverted:        #000000;
                        --color-text-on-inverted-muted:  #3A3A3A;
                        --color-text-title:              #FFFFFF;
                        --color-text-link:               #7D93FF;
                        --color-text-link-hover:         #FF6A55;

                        --color-border:           #2E2E2E;
                        --color-border-emphasis:  #FFFFFF;

                        --color-accent:           #FFE800;
                        --color-accent-emphasis:  #FF6A55;
                        --color-accent-on:        #000000;
                    }
                }
                """;

        /** The theme's own vocabulary, derived from the semantic tokens so
         *  the dark flip carries it: ink is the text colour, paper the
         *  surface, and a shadow is a solid offset block of ink. */
        private static final String TOKENS = """
                :root {
                    --bru-ink:    var(--color-text-primary);
                    --bru-paper:  var(--color-surface);
                    --bru-rule:   4px solid var(--bru-ink);
                    --bru-rule-3: 3px solid var(--bru-ink);
                    --bru-shadow:    8px 8px 0 var(--bru-ink);
                    --bru-shadow-md: 5px 5px 0 var(--bru-ink);
                    --bru-shadow-sm: 4px 4px 0 var(--bru-ink);
                    --bru-snap: transform 70ms steps(2), box-shadow 70ms steps(2), background 100ms;
                    --bru-hatch: repeating-linear-gradient(45deg,
                        color-mix(in srgb, var(--bru-ink) 22%, transparent) 0 7px,
                        transparent 7px 14px);
                    --bru-display: "Arial Black", "Helvetica Neue", Helvetica, Arial, sans-serif;
                }
                """;

        /** The sheet — grid paper behind everything, and the reading page as
         *  a column with a rule down each side. */
        private static final String SHEET = """
                html, body {
                    font-family: Helvetica, Arial, sans-serif;
                    font-weight: 500;
                    background-image:
                        linear-gradient(color-mix(in srgb, var(--bru-ink) 14%, transparent) 1px, transparent 1px),
                        linear-gradient(90deg, color-mix(in srgb, var(--bru-ink) 14%, transparent) 1px, transparent 1px);
                    background-size: 28px 28px;
                }
                ::selection { background: var(--color-text-link); color: var(--bru-paper); }

                .st-header {
                    border-bottom: 4px solid var(--color-accent);
                    box-shadow: 0 4px 0 var(--bru-ink);
                }
                .st-main:has(.st-doc-meta) {
                    background-color: var(--bru-paper);
                    border-left: var(--bru-rule);
                    border-right: var(--bru-rule);
                    border-radius: 0;
                    box-shadow: none;
                }
                .st-doc-meta { border-bottom: var(--bru-rule); }
                .st-footer {
                    border-top: var(--bru-rule);
                    color: var(--bru-ink);
                    font-size: 13px;
                    max-width: 60ch;
                }
                .st-footer code { border-radius: 0; }
                """;

        /** Type — display faces set heavy and uppercase so they read as
         *  stamped; the kicker is the rotated yellow box from the masthead;
         *  section titles are inverted tags. */
        private static final String TYPE = """
                .st-brand-word {
                    font-family: var(--bru-display);
                    font-style: normal;
                    font-weight: 900;
                    text-transform: uppercase;
                    letter-spacing: -0.04em;
                }
                .st-brand-dot { border: 2px solid var(--color-text-on-inverted); }
                .st-title {
                    font-family: var(--bru-display);
                    font-weight: 900;
                    font-size: clamp(38px, 6vw, 68px);
                    line-height: 0.92;
                    letter-spacing: -0.05em;
                    text-transform: uppercase;
                    color: var(--bru-ink);
                    margin-bottom: 18px;
                }
                .st-kicker {
                    display: inline-block;
                    font-family: var(--bru-display);
                    font-weight: 900;
                    font-size: 13px;
                    letter-spacing: 0.02em;
                    color: var(--bru-ink);
                    background: var(--color-accent);
                    border: var(--bru-rule);
                    padding: 2px 10px;
                    margin: 0 0 18px 4px;
                    transform: rotate(-2deg);
                }
                .st-subtitle {
                    font-style: normal;
                    color: var(--bru-ink);
                    border-left: 10px solid var(--color-text-link-hover);
                    padding-left: 14px;
                    max-width: 52ch;
                }
                .st-section-title, .st-panel-title, .st-sidebar-title {
                    display: inline-block;
                    font-family: var(--bru-display);
                    font-weight: 900;
                    font-size: 12px;
                    letter-spacing: 0.04em;
                    color: var(--bru-paper);
                    background: var(--bru-ink);
                    border: 0;
                    padding: 5px 11px;
                }
                .st-card-title, .st-list-item-label, .st-step-label {
                    font-family: var(--bru-display);
                    font-weight: 900;
                    letter-spacing: -0.02em;
                    color: var(--bru-ink);
                }
                .st-step-id, .st-effort, .st-card-link {
                    font-family: var(--bru-display);
                    font-style: normal;
                    font-weight: 900;
                    color: var(--bru-ink);
                }
                .st-crumb:hover { color: var(--color-accent); text-decoration: underline; text-decoration-thickness: 3px; }
                """;

        /** Plates — anything that sits on the page as a block: cards, rows,
         *  panels, pills. Hard rule, offset shadow, and the press. */
        private static final String PLATES = """
                .st-card, .st-list-item, .st-step-card, .st-app-pill, .st-panel, .st-dep {
                    background: var(--bru-paper);
                    border: var(--bru-rule);
                    border-radius: 0;
                    box-shadow: var(--bru-shadow-md);
                    transition: var(--bru-snap);
                }
                .st-card:hover, .st-list-item[href]:hover, .st-step-card:hover, .st-app-pill:hover {
                    transform: translate(-2px, -2px);
                    box-shadow: var(--bru-shadow);
                    border-color: var(--bru-ink);
                }
                .st-card:active, .st-list-item[href]:active, .st-step-card:active, .st-app-pill:active {
                    transform: translate(5px, 5px);
                    box-shadow: 0 0 0 var(--bru-ink);
                }
                .st-card:focus-visible, .st-list-item:focus-visible, .st-step-card:focus-visible, .st-app-pill:focus-visible {
                    outline: 4px solid var(--color-text-link);
                    outline-offset: 4px;
                }
                .st-card-featured, .st-app-pill-dark {
                    background: var(--color-surface-inverted);
                    box-shadow: 5px 5px 0 var(--color-accent);
                }
                .st-card-featured:hover, .st-app-pill-dark:hover {
                    background: var(--color-surface-inverted);
                    box-shadow: 8px 8px 0 var(--color-accent);
                }
                .st-card-featured .st-card-title, .st-app-pill-dark .st-app-pill-label { color: var(--color-text-on-inverted); }
                .st-card-meta { border-top: 2px solid var(--bru-ink); color: var(--bru-ink); }
                .st-app-pill-icon { border: var(--bru-rule-3); border-radius: 0; }
                .st-dep { box-shadow: 3px 3px 0 var(--bru-ink); border-width: 2px; font-weight: 700; }
                .st-doc-section-active {
                    background-color: color-mix(in srgb, var(--color-accent) 30%, transparent);
                    box-shadow: inset 6px 0 0 var(--bru-ink);
                }

                .st-toc-item {
                    border-left: 4px solid transparent;
                    font-weight: 700;
                    color: var(--bru-ink);
                    transition: none;
                }
                .st-toc-item:hover { border-left-color: var(--bru-ink); color: var(--bru-ink); }
                .st-toc-active { border-left-color: var(--bru-ink); background: var(--color-accent); color: var(--bru-ink); }

                .st-table { border: var(--bru-rule); border-collapse: collapse; }
                .st-thead { background: var(--bru-ink); }
                .st-th {
                    background: var(--bru-ink);
                    color: var(--bru-paper);
                    font-family: var(--bru-display);
                    font-weight: 900;
                    font-size: 12px;
                    letter-spacing: 0.04em;
                    text-transform: uppercase;
                    border: 0;
                }
                .st-td { border-bottom: 2px solid var(--bru-ink); }
                """;

        /** Buttons — every clickable the studio renders as a button: filters,
         *  dialog actions, the picker's header button, the shell's and the
         *  workbench's. Hard border, offset shadow, yellow on hover, pressed
         *  into the shadow on active, blue outline on keyboard focus,
         *  hatched when inert. */
        private static final String BUTTONS = """
                .st-filter-btn, .sd-action, .ws-btn, .cg-btn, .pm-btn {
                    font-family: var(--bru-display);
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
                }
                .st-filter-btn:hover, .sd-action:hover, .ws-btn:hover, .cg-btn:hover, .pm-btn:hover {
                    background: var(--color-accent);
                    color: var(--bru-ink);
                    border-color: var(--bru-ink);
                    transform: translate(-2px, -2px);
                    box-shadow: 6px 6px 0 var(--bru-ink);
                }
                .st-filter-btn:active, .sd-action:active, .ws-btn:active, .cg-btn:active, .pm-btn:active {
                    transform: translate(4px, 4px);
                    box-shadow: 0 0 0 var(--bru-ink);
                }
                .st-filter-btn:focus-visible, .sd-action:focus-visible, .ws-btn:focus-visible,
                .cg-btn:focus-visible, .pm-btn:focus-visible, .tp-btn:focus-visible, .sd-close:focus-visible {
                    outline: 4px solid var(--color-text-link);
                    outline-offset: 4px;
                }
                .st-filter-btn-active, .sd-action-primary {
                    background: var(--color-accent);
                    color: var(--bru-ink);
                    border-color: var(--bru-ink);
                }
                .st-filter-btn-active:hover, .sd-action-primary:hover {
                    background: var(--bru-paper);
                    color: var(--bru-ink);
                }
                .ws-btn-danger { background: var(--color-text-link-hover); color: var(--bru-paper); }
                .ws-btn-danger:hover { background: var(--bru-ink); color: var(--bru-paper); }
                .sd-action-off, .ws-btn-off, .st-filter-btn[disabled], .sd-action[disabled],
                .ws-btn[disabled], .cg-btn[disabled], .pm-btn[disabled] {
                    background: var(--bru-hatch), var(--bru-paper);
                    color: var(--color-text-muted);
                    box-shadow: none;
                    opacity: 1;
                    cursor: not-allowed;
                }
                .sd-action-off:hover, .ws-btn-off:hover, .st-filter-btn[disabled]:hover, .sd-action[disabled]:hover,
                .ws-btn[disabled]:hover, .cg-btn[disabled]:hover, .pm-btn[disabled]:hover,
                .sd-action-off:active, .ws-btn-off:active, .sd-action[disabled]:active {
                    background: var(--bru-hatch), var(--bru-paper);
                    color: var(--color-text-muted);
                    transform: none;
                    box-shadow: none;
                }

                /* The picker's button sits on the ink masthead — paper rules,
                   a yellow shadow, and the same press. */
                .tp-btn {
                    font-family: var(--bru-display);
                    font-weight: 900;
                    font-size: 11px;
                    text-transform: uppercase;
                    border: 2px solid var(--color-text-on-inverted);
                    border-radius: 0;
                    box-shadow: 3px 3px 0 var(--color-accent);
                    padding: 4px 10px;
                    transition: var(--bru-snap);
                }
                .tp-btn:hover {
                    background: var(--color-accent);
                    color: var(--color-accent-on);
                    border-color: var(--color-accent);
                    transform: translate(-2px, -2px);
                    box-shadow: 5px 5px 0 var(--color-text-on-inverted);
                }
                .tp-btn:hover .tp-btn-label { color: var(--color-accent-on); }
                .tp-btn:active { transform: translate(3px, 3px); box-shadow: 0 0 0 var(--color-accent); }
                """;

        /** Fields — the search box and the shell's input invert to yellow on
         *  focus and grow a shadow, so the focused field is the loudest thing
         *  on the page. */
        private static final String FIELDS = """
                .st-search, .ws-input {
                    font-weight: 600;
                    color: var(--bru-ink);
                    background: var(--bru-paper);
                    border: var(--bru-rule);
                    border-radius: 0;
                    box-shadow: inset 5px 5px 0 color-mix(in srgb, var(--bru-ink) 9%, transparent);
                    transition: none;
                }
                .st-search:focus, .ws-input:focus {
                    outline: none;
                    background: var(--color-accent);
                    border-color: var(--bru-ink);
                    box-shadow: var(--bru-shadow-md);
                }
                .st-search::placeholder, .ws-input::placeholder { color: var(--color-text-muted); font-weight: 500; }
                """;

        /** Marks — badges, task boxes, status chips, progress bars. Bordered,
         *  square, and the fills are hatched. */
        private static final String MARKS = """
                .st-badge, .st-status-badge,
                .st-td-badge-success, .st-td-badge-warning, .st-td-badge-error {
                    border: 2px solid var(--bru-ink);
                    border-radius: 0;
                    font-weight: 900;
                    letter-spacing: 0.06em;
                }
                .st-badge-reference { background: var(--bru-paper); color: var(--color-text-link); }
                .st-status-not-started { background: var(--bru-paper); color: var(--bru-ink); }
                .st-task-box {
                    width: 18px;
                    height: 18px;
                    flex-basis: 18px;
                    border: var(--bru-rule-3);
                    border-radius: 0;
                    background: var(--bru-paper);
                    color: var(--bru-paper);
                }
                .st-task-done .st-task-box {
                    background: var(--bru-ink);
                    border-color: var(--bru-ink);
                    color: var(--bru-paper);
                }
                .st-overall-bar, .st-step-progress-bar {
                    border: var(--bru-rule-3);
                    border-radius: 0;
                    background: var(--bru-paper);
                    height: 16px;
                }
                .st-step-progress-bar { height: 10px; border-width: 2px; }
                .st-overall-fill, .st-step-progress-fill {
                    background:
                        repeating-linear-gradient(45deg, var(--bru-ink) 0 5px, transparent 5px 10px),
                        var(--color-accent);
                    transition: width 200ms steps(4);
                }
                .st-overall-pct { font-family: var(--bru-display); font-weight: 900; }
                """;

        /** Prose — markdown the framework did not class. Headings go display
         *  face; links underline heavy and highlight on hover; code blocks are
         *  ink plates with a yellow shadow. */
        private static final String PROSE = """
                .st-doc h1, .st-doc h2, .st-doc h3 {
                    font-family: var(--bru-display);
                    font-weight: 900;
                    letter-spacing: -0.03em;
                    color: var(--bru-ink);
                    line-height: 1.05;
                }
                .st-doc h1 { text-transform: uppercase; border-bottom: var(--bru-rule); padding-bottom: 10px; }
                .st-doc h2 { text-transform: uppercase; }
                .st-doc h4 {
                    display: inline-block;
                    font-family: var(--bru-display);
                    font-weight: 900;
                    font-size: 12px;
                    letter-spacing: 0.04em;
                    color: var(--bru-paper);
                    background: var(--bru-ink);
                    padding: 4px 10px;
                }
                .st-doc a {
                    color: var(--color-text-link);
                    text-decoration-thickness: 3px;
                    text-underline-offset: 3px;
                }
                .st-doc a:hover { background: var(--color-accent); color: var(--bru-ink); }
                .st-doc blockquote {
                    border-left: 10px solid var(--color-text-link-hover);
                    padding-left: 14px;
                    color: var(--bru-ink);
                    font-style: normal;
                }
                .st-doc code { border-radius: 0; color: var(--bru-ink); }
                .st-doc pre {
                    border: var(--bru-rule);
                    border-radius: 0;
                    background: var(--bru-ink);
                    color: var(--bru-paper);
                    box-shadow: var(--bru-shadow-md);
                }
                .st-doc pre code { color: inherit; }
                .st-doc table { border: var(--bru-rule); }
                .st-doc th {
                    background: var(--bru-ink);
                    color: var(--bru-paper);
                    font-family: var(--bru-display);
                    font-weight: 900;
                    font-size: 12px;
                    text-transform: uppercase;
                    letter-spacing: 0.04em;
                }
                .st-doc td { border-bottom: 2px solid var(--bru-ink); }
                .st-doc hr { border-top: var(--bru-rule); }
                .st-doc img { border: var(--bru-rule); box-shadow: var(--bru-shadow-md); }
                """;

        /** The system dialog — a plate with a deep shadow, an ink title bar,
         *  and a hatched scrim in place of the blur. */
        private static final String DIALOG = """
                .sd-scrim {
                    background: var(--bru-hatch);
                    backdrop-filter: brightness(0.7);
                }
                .sd-frame {
                    border: var(--bru-rule);
                    border-radius: 0;
                    box-shadow: 12px 12px 0 var(--bru-ink);
                }
                .sd-glow {
                    border-color: var(--bru-ink);
                    box-shadow: 12px 12px 0 var(--color-accent);
                }
                .sd-title {
                    height: 34px;
                    background: var(--bru-ink);
                    border-bottom: var(--bru-rule);
                }
                .sd-title-label {
                    font-family: var(--bru-display);
                    font-weight: 900;
                    font-size: 12px;
                    letter-spacing: 0.04em;
                    color: var(--bru-paper);
                }
                .sd-close { color: var(--bru-paper); font-weight: 900; }
                .sd-actions {
                    border-top: var(--bru-rule);
                    background: var(--bru-paper);
                    padding: 12px 14px;
                    gap: 14px;
                }
                """;

        /** The picker — inline frame, preview frame, and the in-use chip. */
        private static final String PICKER = """
                .tp-inline { border: var(--bru-rule); border-radius: 0; box-shadow: var(--bru-shadow); }
                .tp-inline-head {
                    font-family: var(--bru-display);
                    font-weight: 900;
                    text-transform: uppercase;
                    border-bottom: var(--bru-rule);
                }
                .tp-preview-frame { border: var(--bru-rule); border-radius: 0; }
                .tp-preview-loading { border-radius: 0; }
                .tp-preview-name { font-family: var(--bru-display); font-weight: 900; }
                .tp-current {
                    color: var(--bru-ink);
                    background: var(--color-accent);
                    border: 2px solid var(--bru-ink);
                    padding: 1px 6px;
                    font-weight: 900;
                }
                .md-nav { border-right: var(--bru-rule); }

                @media (prefers-reduced-motion: reduce) {
                    .st-card, .st-list-item, .st-step-card, .st-app-pill, .st-panel,
                    .st-filter-btn, .sd-action, .ws-btn, .cg-btn, .pm-btn, .tp-btn { transition: none; }
                }
                """;
    }
}
