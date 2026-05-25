package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.ClickTarget;
import hue.captains.singapura.js.homing.core.Component;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Cue;
import hue.captains.singapura.js.homing.core.Layer;
import hue.captains.singapura.js.homing.core.MediaGated;
import hue.captains.singapura.js.homing.core.Prose;
import hue.captains.singapura.js.homing.core.Reset;
import hue.captains.singapura.js.homing.core.State;
import hue.captains.singapura.js.homing.core.SvgRef;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeAudio;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeOverlay;
import hue.captains.singapura.js.homing.core.ThemeVariables;

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
 * <p>The card-shape change rides on {@code @layer theme}
 * (via {@link ThemeOverlay} chunks) — that's the layer designed to win
 * against component-tier rules in {@code StudioStyles}, exactly the case
 * the cascade ladder (Defect 0003 resolution) was built for.</p>
 *
 * <p>Activate via {@code ?theme=retro-90s} on any studio URL.</p>
 */
public record HomingRetro90s() implements Theme {

    public static final HomingRetro90s INSTANCE = new HomingRetro90s();

    @Override public String slug()  { return "retro-90s"; }
    /** Installs universal pointer-events:none + whitelist so the desktop
     *  icons (.w95-icon) receive hover/click. Workspace-style apps opt
     *  out via {@link AppModule#acceptsBackdropInteractivity()}. */
    @Override public boolean backdropInteractivity() { return true; }
    @Override public String label() { return "Retro 90s"; }

    /** Win95 desktop backdrop — rendered as inline DOM so the iconic icons
     *  (My Computer, My Documents, Network Neighborhood, Recycle Bin)
     *  participate in the host document's CSS cascade and can receive
     *  per-icon {@code :hover} effects, the same pattern Maple Bridge uses
     *  for its moon. */
    @Override
    public SvgRef<?> backdrop() {
        return new SvgRef<>(HomingRetro90sBg.INSTANCE, new HomingRetro90sBg.desktop());
    }

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

    public record Vars() implements ThemeVariables<HomingRetro90s> {
        public static final Vars INSTANCE = new Vars();
        @Override public HomingRetro90s theme() { return HomingRetro90s.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        // Windows-95-era palette: desktop teal chassis, VGA-blue windows,
        // light-grey task bars. Hex values are the actual period defaults
        // (Win95 desktop teal #008080, VGA blue #0000A8, Win95 chrome #C0C0C0).
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — teal page background, blue card windows, grey chrome.
                Map.entry(StudioVars.COLOR_SURFACE,          "#008080"),  // Win95 desktop teal
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,   "#0000A8"),  // VGA blue — the iconic card window
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED, "#006666"),  // deeper teal — recessed wells
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED, "#C0C0C0"),  // Win95 chrome grey — header/footer task bar

                // Text — white on teal/blue surfaces, black on grey task bars,
                // cyan field-labels, amber for the link/highlight role.
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#FFFFFF"),  // bright white
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#55FFFF"),  // bright cyan — labels
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#000000"),  // black on grey
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#800000"),  // dark red — session badge
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#FFFF55"),  // bright amber
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#FFFFFF"),

                // Borders — solid white for window edges, amber for emphasis.
                Map.entry(StudioVars.COLOR_BORDER,          "#FFFFFF"),
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS, "#FFFF55"),

                // Accent — amber. Classic terminal highlight colour.
                Map.entry(StudioVars.COLOR_ACCENT,          "#FFFF55"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS, "#FFFFFF"),
                Map.entry(StudioVars.COLOR_ACCENT_ON,       "#0000A8"),

                // Spacing — tighter than default; terminals don't breathe.
                Map.entry(StudioVars.SPACE_1, "2px"),
                Map.entry(StudioVars.SPACE_2, "4px"),
                Map.entry(StudioVars.SPACE_3, "8px"),
                Map.entry(StudioVars.SPACE_4, "12px"),
                Map.entry(StudioVars.SPACE_5, "16px"),
                Map.entry(StudioVars.SPACE_6, "20px"),
                Map.entry(StudioVars.SPACE_7, "28px"),
                Map.entry(StudioVars.SPACE_8, "36px"),

                // Radius — zero. Pure rectangles. The 1990s had no rounded corners.
                Map.entry(StudioVars.RADIUS_SM, "0"),
                Map.entry(StudioVars.RADIUS_MD, "0"),
                Map.entry(StudioVars.RADIUS_LG, "0")
        );
    }

    public record Globals() implements ThemeGlobals<HomingRetro90s> {
        public static final Globals INSTANCE = new Globals();
        @Override public HomingRetro90s theme() { return HomingRetro90s.INSTANCE; }

        /** Back-compat handle — concatenated CSS for clients still reading {@code css()}.
         *  The framework prefers {@link #chunks()} when present. */
        @Override public String css() {
            return HomingDefault.STRUCTURAL_CSS + DARK_OVERRIDE + BACKDROP_DESKTOP
                 + SCANLINES + CARD_RESHAPE + WINDOW_PANES;
        }

        /** Tier-tagged content. Card-reshape and CRT scanlines ride on
         *  {@link ThemeOverlay} — that's the tier that wins against
         *  component-layer base rules (Defect 0003 cascade ladder). */
        @Override
        public Map<Class<? extends Layer>, String> chunks() {
            return Map.of(
                    Reset.class,        HomingDefault.STRUCTURAL_CHUNKS.get(Reset.class),
                    Component.class,    HomingDefault.STRUCTURAL_CHUNKS.get(Component.class),
                    Prose.class,        HomingDefault.STRUCTURAL_CHUNKS.get(Prose.class),
                    State.class,        HomingDefault.STRUCTURAL_CHUNKS.get(State.class),
                    MediaGated.class,   HomingDefault.STRUCTURAL_CHUNKS.get(MediaGated.class),
                    // Retro-90s overrides land in @layer theme:
                    // backdrop + scanlines + card windows + reading-window panes + dark
                    ThemeOverlay.class,
                            BACKDROP_DESKTOP + SCANLINES + CARD_RESHAPE + WINDOW_PANES + DARK_OVERRIDE
            );
        }

        /** Dark mode — the desktop goes black-teal, windows go near-black-blue.
         *  Phosphor amber stays; the workstation just dims for night shift. */
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        --color-surface:           #003636;
                        --color-surface-raised:    #000060;
                        --color-surface-recessed:  #002020;
                        --color-surface-inverted:  #585858;

                        --color-text-primary:            #FFFFFF;
                        --color-text-muted:              #55FFFF;
                        --color-text-on-inverted:        #FFFFFF;
                        --color-text-on-inverted-muted:  #FF5555;
                        --color-text-link:               #FFFF55;
                        --color-text-link-hover:         #FFFFFF;

                        --color-border:           #A8A8A8;
                        --color-border-emphasis:  #FFFF55;

                        --color-accent:           #FFFF55;
                        --color-accent-emphasis:  #FFFFFF;
                        --color-accent-on:        #000060;
                    }
                }
                """;

        /**
         * Inline-DOM SVG desktop backdrop + per-icon hover plumbing. The
         * framework injects {@code <div class="theme-backdrop"><svg>…desktop…</svg></div>}
         * as the first child of {@code <body>}; this CSS positions it
         * fixed-cover, gives it the Win95 teal surface, and wires the four
         * iconic icons (My Computer, My Documents, Network Neighborhood,
         * Recycle Bin) to scale-up + glow on hover.
         *
         * <p>Same pattern as Maple Bridge's moon, with one twist: the
         * {@code scale} CSS property (independent of {@code transform})
         * composes cleanly with each icon group's existing
         * {@code transform="translate(…)"} positioning. Using {@code transform: scale}
         * would overwrite the translation; using {@code scale} keeps them
         * composed.</p>
         *
         * <p>Pointer-events plumbing mirrors Maple Bridge — universal
         * {@code body, body *} pass-through so the backdrop (at
         * {@code z-index:-1}) can receive hover, then selective restoration
         * on user-interactive elements + the icon class.</p>
         */
        private static final String BACKDROP_DESKTOP = """
                /* HomingDefault's structural CSS sets a solid body background
                   that would mask the backdrop SVG. Make body transparent so
                   the .theme-backdrop becomes the visible page surface. */
                html, body { background: transparent; }
                /* Inline-DOM atmospheric layer — fixed-cover, behind everything. */
                .theme-backdrop {
                    position: fixed;
                    inset: 0;
                    z-index: -1;
                    pointer-events: none;
                    overflow: hidden;
                    background: var(--color-surface);
                }
                .theme-backdrop svg {
                    width: 100%;
                    height: 100%;
                    display: block;
                }
                /* Retro 90s opts out of the doc-reader column slab: it uses
                 * the per-pane "Notepad window" metaphor (see WINDOW_PANES
                 * below) for .st-doc / .st-sidebar / .st-doc-meta instead.
                 * A parchment slab around three Notepad windows would be a
                 * window-inside-window. Scope matches the framework's slab
                 * selector exactly — both target `.st-main:has(.st-doc-meta)`. */
                .st-main:has(.st-doc-meta) {
                    background-color: transparent;
                    border-radius: 0;
                    box-shadow: none;
                }

                /* Pointer-events plumbing — body and ALL descendants pass
                   through to the backdrop's z-index:-1 layer, then we
                   re-enable receive on the user-interactive surfaces and
                   the icons themselves. Same trade-off as Maple Bridge:
                   text selection in prose is impaired; clicks/links/keyboard
                   nav all work. */
                /* Gated by :not(.homing-bg-passive) — workspace-style apps
                 * opt out and reclaim event surface. The backdrop icons
                 * (.w95-icon) stay interactive regardless because the
                 * .theme-backdrop tree is unaffected by the body-class
                 * gate. See AppModule.acceptsBackdropInteractivity. */
                body:not(.homing-bg-passive),
                body:not(.homing-bg-passive) * { pointer-events: none; }
                body:not(.homing-bg-passive) a,
                body:not(.homing-bg-passive) button,
                body:not(.homing-bg-passive) input,
                body:not(.homing-bg-passive) select,
                body:not(.homing-bg-passive) textarea,
                body:not(.homing-bg-passive) label,
                body:not(.homing-bg-passive) .st-header,
                body:not(.homing-bg-passive) .st-card,
                body:not(.homing-bg-passive) .st-list-item,
                body:not(.homing-bg-passive) .st-toc-item,
                /* Reading panes get pointer-events so text inside the Notepad-
                 * style windows can be selected and scrolled normally.
                 * Universal descendants of these panes are also re-enabled. */
                body:not(.homing-bg-passive) .st-doc,
                body:not(.homing-bg-passive) .st-doc *,
                body:not(.homing-bg-passive) .st-sidebar,
                body:not(.homing-bg-passive) .st-sidebar *,
                body:not(.homing-bg-passive) .st-doc-meta,
                body:not(.homing-bg-passive) .st-doc-meta *,
                /* The icon group AND every descendant must be hover-targetable.
                   `body *` directly hits each <rect>/<text>/<path> inside the
                   icon, pinning their pointer-events to none and breaking the
                   hover chain (a child with `none` can't be the hit target,
                   so :hover never fires on its parent .w95-icon). Listing
                   descendants here overrides that per-element. */
                .theme-backdrop .w95-icon,
                .theme-backdrop .w95-icon * { pointer-events: auto; }

                /* Icon hover — subtle enlarge. No glow, no double-shadow:
                   Win95 desktop icons didn't dramatically light up on hover,
                   they just felt slightly more "selectable". A 6% scale-up is
                   noticeable enough to signal interactivity without making
                   the desktop feel reactive or animated.

                   transform-box: fill-box anchors the scale to each icon's
                   own bbox centre. `scale` (not `transform: scale(…)`)
                   composes with each <g>'s existing `transform="translate(…)"`
                   attribute — using transform would overwrite the translation
                   and launch the icon off-position. */
                .theme-backdrop .w95-icon {
                    cursor: pointer;
                    transform-box: fill-box;
                    transform-origin: center;
                    transition: scale 160ms ease;
                }
                .theme-backdrop .w95-icon:hover {
                    scale: 1.06;
                }
                """;

        /**
         * CRT scanlines + monospace body font. The scanline effect is a
         * fixed-position pseudo-element on {@code body::before} — horizontal
         * stripes at 3px pitch, pointer-events: none so it doesn't block
         * clicks. Stays in place during scroll (fixed positioning) because
         * real CRTs don't scroll their refresh pattern.
         */
        private static final String SCANLINES = """
                html, body {
                    font-family: "Courier New", "Consolas", "Lucida Console", monospace;
                    font-size: 13px;
                    letter-spacing: 0;
                }
                body { position: relative; }
                body::before {
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
                """;

        /**
         * Card-shape mutation — turn the rounded-corner left-accented
         * "Maven Central tile" into a Windows-95 window. Drops the radius,
         * replaces the left accent with a navy-gradient title-bar strip
         * (via {@code ::before}), adds a Fixed3D-style inset white bevel,
         * tightens padding.
         *
         * <p>Each rule wins against the matching component-tier base rule by
         * {@code @layer} ordering, not by selector specificity — which is why
         * these rules stay short and readable instead of inflating to
         * {@code .st-card.st-card} or chasing {@code !important}.</p>
         */
        private static final String CARD_RESHAPE = """
                .st-card {
                    background: var(--color-surface-raised);
                    border: 1px solid var(--color-border);
                    border-left: 1px solid var(--color-border);
                    border-radius: 0;
                    padding: 0;
                    overflow: hidden;
                    /* Fixed3D bevel — 1px inset white edge inside the white border,
                     * giving the classic Win95 sunken/raised window look. */
                    box-shadow:
                        inset 1px 1px 0 rgba(255, 255, 255, 0.6),
                        inset -1px -1px 0 rgba(0, 0, 0, 0.4);
                    min-height: 130px;
                    color: #FFFFFF;
                }
                .st-card::before {
                    /* Decorative title bar — Win95 active-window navy gradient.
                     * The card's actual title (.st-card-title) renders inside
                     * the body below; this strip is pure visual chrome, with
                     * a window-control glyph as the only "content". */
                    content: "▸";
                    display: block;
                    background: linear-gradient(to right, #000080 0%, #1084D0 100%);
                    color: #FFFFFF;
                    padding: 1px 8px;
                    font-weight: 700;
                    font-size: 12px;
                    line-height: 16px;
                    border-bottom: 1px solid var(--color-border);
                    letter-spacing: 1px;
                }
                .st-card > * {
                    padding-left: 10px;
                    padding-right: 10px;
                }
                .st-card > *:first-child { padding-top: 8px; }
                .st-card > *:last-child  { padding-bottom: 8px; }
                .st-card-title {
                    font-family: "Courier New", "Consolas", monospace;
                    font-weight: 700;
                    color: var(--color-text-link);
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    font-size: 14px;
                }
                .st-card-summary {
                    color: #FFFFFF;
                    font-size: 12px;
                }
                .st-card-meta {
                    background: var(--color-surface-recessed);
                    border-top: 1px solid var(--color-border);
                    color: var(--color-text-muted);
                    font-size: 11px;
                    padding: 2px 10px;
                    margin: 0;
                }
                .st-card-link {
                    color: var(--color-text-link);
                    letter-spacing: 1px;
                }
                .st-card-featured {
                    /* Keep the iconic blue-window look on the featured card too;
                     * its full-width grid placement already differentiates it. */
                    background: var(--color-surface-raised);
                    border-left: 1px solid var(--color-border);
                }
                /* Header band echoes the workstation title strip — grey task bar
                 * with black caption text, classic Win95 chrome. */
                .st-header {
                    background: var(--color-surface-inverted);
                    color: var(--color-text-on-inverted);
                    border-bottom: 1px solid var(--color-border);
                    box-shadow: none;
                }
                /* Footer echoes the F-key bar at the bottom of the workbench. */
                .st-footer {
                    background: var(--color-surface-inverted);
                    color: var(--color-text-on-inverted);
                    border-top: 1px solid var(--color-border);
                    font-family: "Courier New", monospace;
                }
                """;

        /**
         * Reading-pane window-chrome — wrap {@code .st-doc} and {@code .st-sidebar}
         * as Win95 application windows (Notepad-ish), so long-form prose sits on
         * a readable cream surface with black text inside a properly-bevelled
         * window frame instead of floating on the teal desktop.
         *
         * <p>Why this is needed in Retro 90s specifically: most themes inherit
         * the framework-default {@code .st-main} column slab (parchment-on-body
         * surface, readable contrast). Retro 90s opts out of that slab because
         * the Win95 desktop should bleed through behind the catalogue cards —
         * but the same opt-out leaves doc reading panes exposed on the desktop
         * teal with no contrast surface beneath them.</p>
         *
         * <p>The fix: per-pane window chrome. {@code .st-doc} becomes the
         * "Document Reader" window; {@code .st-sidebar} becomes the "Outline"
         * window. Both get a navy-gradient title bar via {@code ::before}, a
         * Fixed3D bevel, cream content surface, and a period-accurate
         * Tahoma/MS-Sans-Serif body font (Win95's actual system UI typeface,
         * not the monospace the rest of the chrome rides on — long-form prose
         * needs proportional letterforms to be comfortably readable).</p>
         *
         * <p>Code blocks and inline code inside the doc retain monospace so
         * the chrome/code distinction stays clear within the window.</p>
         */
        private static final String WINDOW_PANES = """
                /* === Document Reader window ============================ */
                .st-doc {
                    background: #FFFFE1;                  /* cream "notepad" surface */
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
                }
                .st-doc::before {
                    /* Win95 active-window title bar — navy gradient, white
                     * caption. Negative horizontal margin breaks the title bar
                     * out of the parent's horizontal padding so it spans the
                     * full window width flush to the bevel. */
                    content: "📄  Document Reader";
                    display: block;
                    margin: 0 -16px 14px;
                    background: linear-gradient(to right, #000080 0%, #1084D0 100%);
                    color: #FFFFFF;
                    font-family: "Tahoma", "MS Sans Serif", sans-serif;
                    font-weight: 700;
                    font-size: 12px;
                    letter-spacing: 0.5px;
                    padding: 2px 8px;
                    border-bottom: 1px solid #000040;
                }
                /* Inline code + code blocks keep monospace — chrome stays
                 * monospace, prose is sans, code is mono. Three-way separation. */
                .st-doc pre,
                .st-doc code,
                .st-doc kbd,
                .st-doc samp {
                    font-family: "Courier New", "Consolas", "Lucida Console", monospace;
                }
                /* Headings get a period-accurate "bold sans" look against the
                 * cream body — overriding the framework's default Georgia
                 * serif which would clash with the Win95 motif. */
                .st-doc h1, .st-doc h2, .st-doc h3, .st-doc h4 {
                    font-family: "Tahoma", "MS Sans Serif", "Arial", sans-serif;
                    color: #000080;
                }
                /* Body-paragraph links rebind to navy underlined — the
                 * canonical "hyperlink" colour pair of the era. */
                .st-doc a {
                    color: #0000EE;
                    text-decoration: underline;
                }
                .st-doc a:visited { color: #551A8B; }
                .st-doc blockquote {
                    border-left: 3px solid #808080;
                    background: #FFFFCC;
                    color: #000000;
                }

                /* === Outline (sidebar) window ========================== */
                .st-sidebar {
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
                }
                .st-sidebar::before {
                    content: "📑  Outline";
                    display: block;
                    margin: 0 -12px 10px;
                    background: linear-gradient(to right, #000080 0%, #1084D0 100%);
                    color: #FFFFFF;
                    font-family: "Tahoma", "MS Sans Serif", sans-serif;
                    font-weight: 700;
                    font-size: 12px;
                    letter-spacing: 0.5px;
                    padding: 2px 8px;
                    border-bottom: 1px solid #000040;
                }
                .st-sidebar-title { color: #000080; font-weight: 700; }

                /* === Doc-meta strip (status bar) ======================= */
                /* The breadcrumb/title strip above the document — styled as
                 * a Win95 status bar (grey chassis, sunken bevel, monospace
                 * caption). Sits between the page header and the reading
                 * windows, completing the workstation-app metaphor. */
                .st-doc-meta {
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
                }
                """;
    }
}
