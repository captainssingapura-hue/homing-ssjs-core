package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.Component;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Layer;
import hue.captains.singapura.js.homing.core.MediaGated;
import hue.captains.singapura.js.homing.core.Prose;
import hue.captains.singapura.js.homing.core.Reset;
import hue.captains.singapura.js.homing.core.State;
import hue.captains.singapura.js.homing.core.ThemeOverlay;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;

import java.util.Map;

/**
 * The default Homing theme — the visual identity that ships with
 * {@code homing-studio-base}. Any consumer depending on this module gets
 * working CSS out of the box without designing their own theme.
 *
 * <p>RFC 0002-ext1 Phase 10 — restructured into identity record + nested
 * {@link Vars} and {@link Globals} singletons. The framework serves these
 * at independently-cacheable routes ({@code /theme-vars?theme=default},
 * {@code /theme-globals?theme=default}); per-CssGroup CSS files served
 * by {@code /css-content} no longer carry the cascade.</p>
 */
public record HomingDefault() implements Theme {

    public static final HomingDefault INSTANCE = new HomingDefault();

    @Override public String slug()  { return "default"; }
    @Override public String label() { return "Default"; }
    @Override public String group() { return "Neutral"; }

    // -------------------------------------------------------------------
    // Vars — the variable values for this theme. Served at /theme-vars.
    // Single semantic layer (--color-*, --space-*, --radius-*) — each role
    // gets a concrete value directly, with no intermediate primitive layer.
    // -------------------------------------------------------------------

    public record Vars() implements ThemeVariables<HomingDefault> {
        public static final Vars INSTANCE = new Vars();
        @Override public HomingDefault theme() { return HomingDefault.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        // Semantic-only — no primitive layer. Each role has an independent
        // value per theme, eliminating the "primitive doing double duty"
        // class of bug. Light values here; dark overrides in Globals.@media.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces
                Map.entry(StudioVars.COLOR_SURFACE,          "#FAFBFD"),
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,   "#FFFFFF"),
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED, "#F1F4F9"),
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED, "#111936"),

                // Text
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#3B4A6B"),
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#64748B"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#FFFFFF"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#CADCFC"),
                Map.entry(StudioVars.COLOR_TEXT_TITLE,              "#1E2761"),   // title := link, unchanged
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#1E2761"),
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#C8921E"),

                // Borders
                Map.entry(StudioVars.COLOR_BORDER,          "#E2E8F0"),
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS, "#F4B942"),

                // Accent
                Map.entry(StudioVars.COLOR_ACCENT,          "#F4B942"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS, "#C8921E"),
                Map.entry(StudioVars.COLOR_ACCENT_ON,       "#111936"),

                // Spacing scale
                Map.entry(StudioVars.SPACE_1, "4px"),
                Map.entry(StudioVars.SPACE_2, "8px"),
                Map.entry(StudioVars.SPACE_3, "12px"),
                Map.entry(StudioVars.SPACE_4, "16px"),
                Map.entry(StudioVars.SPACE_5, "20px"),
                Map.entry(StudioVars.SPACE_6, "24px"),
                Map.entry(StudioVars.SPACE_7, "32px"),
                Map.entry(StudioVars.SPACE_8, "40px"),

                // Radius scale
                Map.entry(StudioVars.RADIUS_SM, "4px"),
                Map.entry(StudioVars.RADIUS_MD, "8px"),
                Map.entry(StudioVars.RADIUS_LG, "12px")
        );
    }

    // -------------------------------------------------------------------
    // Globals — non-class-keyed CSS rules. Served at /theme-globals.
    // Two surviving use cases: descendant selectors over content the
    // framework didn't class (markdown-rendered HTML), and conditional
    // var definitions under media queries (system-preference dark mode).
    // -------------------------------------------------------------------

    /**
     * Theme-agnostic structural CSS shared across every studio theme — html/body
     * resets, multi-property hover rules, descendant selectors over
     * markdown-rendered content. References semantic tokens only, so resolves
     * correctly under any theme's primitive cascade.
     *
     * <p>Each theme's {@link ThemeGlobals#css()} composes its own
     * {@code @media (prefers-color-scheme: dark)} primitive override + this
     * shared structural string.</p>
     */
    /** Tier: RESET — html / body baseline. Lowest priority; everything overrides this. */
    private static final String RESET_CSS = """
                html, body {
                    margin: 0;
                    padding: 0;
                    background: var(--color-surface);
                    color: var(--color-text-primary);
                    font-family: "Calibri", "Segoe UI", system-ui, sans-serif;
                    min-height: 100vh;
                }
                """;

    /** Tier: COMPONENT — descendant rules / variant styling. Same priority as StudioStyles classes. */
    private static final String COMPONENT_CSS = """
                /* Doc-reader column slab — `.st-doc-meta` is uniquely emitted
                 * by DocReaderRenderer, so `:has(.st-doc-meta)` scopes the
                 * slab to the reading page only. Catalogue / doc-browser /
                 * themes-intro / plan-host pages stay slab-less so their
                 * cards keep visual contrast on the body bg. */
                .st-main:has(.st-doc-meta) {
                    background-color: var(--color-surface-raised);
                    border-radius: 6px;
                    box-shadow: 0 2px 24px color-mix(in srgb, var(--color-text-primary) 8%, transparent);
                }
                .st-brand-logo svg { width: 100%; height: 100%; display: block; }
                .st-card-featured .st-card-title {
                    color: var(--color-text-on-inverted);
                    font-size: 22px;
                }
                .st-card-featured .st-card-summary {
                    color: var(--color-text-on-inverted-muted);
                    font-size: 14px;
                    margin-top: 4px;
                }
                .st-card-featured .st-card-meta {
                    border: none;
                    padding: 0;
                    margin: 0;
                    flex-direction: column;
                    align-items: flex-end;
                    gap: 8px;
                }
                .st-card-featured .st-card-link {
                    color: var(--color-accent);
                }
                .st-app-pill-dark .st-app-pill-icon {
                    background: var(--color-accent);
                    color: var(--color-accent-on);
                }
                .st-app-pill-dark .st-app-pill-label {
                    color: var(--color-text-on-inverted);
                }
                .st-app-pill-dark .st-app-pill-desc {
                    color: var(--color-text-on-inverted-muted);
                }
                .st-task-done .st-task-box {
                    background: var(--color-accent);
                    border-color: var(--color-accent-emphasis);
                    color: var(--color-accent-on);
                    font-weight: 700;
                }
                .st-footer code {
                    font-family: "Consolas", "Courier New", monospace;
                    background: var(--color-surface-recessed);
                    color: var(--color-text-link);
                    padding: 1px 6px;
                    border-radius: 3px;
                }
                """;

    /** Tier: PROSE — descendant selectors over markdown-rendered content inside .st-doc. */
    private static final String PROSE_CSS = """
                .st-doc h1, .st-doc h2, .st-doc h3, .st-doc h4 {
                    font-family: "Georgia", serif;
                    color: var(--color-text-link);
                    margin: 1.6em 0 0.6em 0;
                    line-height: 1.25;
                    scroll-margin-top: 24px;
                }
                .st-doc h1 { font-size: 32px; border-bottom: 2px solid var(--color-border-emphasis); padding-bottom: 8px; margin-top: 0; }
                .st-doc h2 { font-size: 24px; }
                .st-doc h3 { font-size: 19px; }
                .st-doc h4 { font-size: 16px; color: var(--color-text-link-hover); letter-spacing: 1px; text-transform: uppercase; }
                .st-doc p  { margin: 0 0 1em 0; }
                .st-doc ul, .st-doc ol { margin: 0 0 1em 0; padding-left: 1.5em; }
                .st-doc li { margin: 0.3em 0; }
                .st-doc a { color: var(--color-text-link-hover); text-decoration: underline; text-underline-offset: 2px; }
                .st-doc blockquote {
                    margin: 1em 0;
                    padding: 4px 0 4px 18px;
                    border-left: 3px solid var(--color-border-emphasis);
                    color: var(--color-text-muted);
                    font-style: italic;
                }
                .st-doc code {
                    font-family: "Consolas", "Courier New", monospace;
                    font-size: 0.92em;
                    background: var(--color-surface-recessed);
                    color: var(--color-text-link);
                    padding: 1px 6px;
                    border-radius: 3px;
                }
                .st-doc pre {
                    background: var(--color-surface-inverted);
                    color: var(--color-text-on-inverted-muted);
                    padding: 14px 18px;
                    border-radius: 4px;
                    overflow-x: auto;
                    margin: 1em 0;
                    font-size: 13px;
                    line-height: 1.5;
                }
                .st-doc pre code {
                    background: transparent;
                    color: inherit;
                    padding: 0;
                    font-size: inherit;
                }
                .st-doc table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 1em 0;
                    font-size: 14px;
                }
                .st-doc th, .st-doc td {
                    text-align: left;
                    padding: 8px 12px;
                    border-bottom: 1px solid var(--color-border);
                    vertical-align: top;
                }
                .st-doc th {
                    background: var(--color-surface-inverted);
                    color: var(--color-text-on-inverted);
                    font-weight: 700;
                    border: none;
                }
                .st-doc tr:nth-child(even) td { background: var(--color-surface-recessed); }
                .st-doc hr {
                    border: none;
                    border-top: 1px solid var(--color-border);
                    margin: 2em 0;
                }
                .st-doc img { max-width: 100%; }
                """;

    /** Tier: STATE — :hover, :focus, :active. Wins over base component styling. */
    private static final String STATE_CSS = """
                .st-brand:hover .st-brand-logo { transform: scale(1.18); }
                .st-crumb:hover { color: var(--color-accent); }
                .st-search:focus {
                    outline: none;
                    border-color: var(--color-border-emphasis);
                    box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-accent) 18%, transparent);
                }
                .st-filter-btn:hover {
                    border-color: var(--color-border-emphasis);
                    color: var(--color-text-link-hover);
                }
                .st-filter-btn-active:hover {
                    background: var(--color-surface-inverted);
                    color: var(--color-accent);
                    border-color: var(--color-surface-inverted);
                }
                .st-card:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 6px 16px color-mix(in srgb, var(--color-text-link) 12%, transparent);
                    border-left-color: var(--color-accent-emphasis);
                }
                .st-toc-item:hover {
                    color: var(--color-text-link);
                    border-left-color: var(--color-border-emphasis);
                }
                .st-doc a:hover { color: var(--color-text-link); }
                .st-app-pill:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 8px 18px color-mix(in srgb, var(--color-text-link) 12%, transparent);
                    border-left-color: var(--color-accent-emphasis);
                }
                .st-app-pill-dark:hover {
                    background: var(--color-surface-inverted);
                }
                .st-step-card:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 6px 16px color-mix(in srgb, var(--color-text-link) 10%, transparent);
                    border-left-color: var(--color-accent-emphasis);
                }
                """;

    /** Tier: MEDIA_GATED — @media queries (responsive, print, dark-mode override).
     *  The print block no longer needs `!important`; cascade layers resolve the
     *  load-order ambiguity (Defect 0003). */
    private static final String MEDIA_GATED_CSS = """
                @media (max-width: 920px) {
                    .st-layout { grid-template-columns: 1fr; }
                    .st-sidebar { display: none; }
                }
                @media print {
                    .st-layout                     { grid-template-columns: 1fr; }
                    .st-sidebar                    { display: none; }
                    .st-doc                        { max-width: none; }
                    .st-main                       { max-width: none; padding: 12px 0; }
                    .theme-backdrop                { display: none; }
                    #__theme_picker_slot__         { display: none; }
                    .st-header                     { position: static; }
                }
                """;

    /**
     * Back-compat handle. The framework's CSS-serving action prefers
     * {@link Globals#chunks()}; this constant remains so any code that
     * concatenates STRUCTURAL_CSS as a string (e.g. {@code DARK_OVERRIDE +
     * STRUCTURAL_CSS}) keeps working without modification. New themes
     * should NOT reference this — return {@link #STRUCTURAL_CHUNKS} (or
     * a merge with theme-specific chunks) from their {@code chunks()}.
     */
    public static final String STRUCTURAL_CSS =
            RESET_CSS + COMPONENT_CSS + PROSE_CSS + STATE_CSS + MEDIA_GATED_CSS;

    /** Tier-tagged version of {@link #STRUCTURAL_CSS}. New themes consume this. */
    public static final Map<Class<? extends Layer>, String> STRUCTURAL_CHUNKS = Map.of(
            Reset.class,      RESET_CSS,
            Component.class,  COMPONENT_CSS,
            Prose.class,      PROSE_CSS,
            State.class,      STATE_CSS,
            MediaGated.class, MEDIA_GATED_CSS
    );

    public record Globals() implements ThemeGlobals<HomingDefault> {
        public static final Globals INSTANCE = new Globals();
        @Override public HomingDefault theme() { return HomingDefault.INSTANCE; }
        @Override public String css() { return DARK_OVERRIDE + STRUCTURAL_CSS; }

        /** Tier-tagged content. The framework wraps each chunk in
         *  {@code @layer X { … }} (Defect 0003 — cascade ordering). DARK_OVERRIDE
         *  lives in the theme tier because it re-binds tokens; structural rules
         *  ride on {@link #STRUCTURAL_CHUNKS}. */
        @Override
        public Map<Class<? extends Layer>, String> chunks() {
            return Map.of(
                    Reset.class,        RESET_CSS,
                    Component.class,    COMPONENT_CSS,
                    Prose.class,        PROSE_CSS,
                    State.class,        STATE_CSS,
                    MediaGated.class,   MEDIA_GATED_CSS,
                    ThemeOverlay.class, DARK_OVERRIDE
            );
        }

        /** Light/dark adaptation for HomingDefault. Without a primitive layer,
         *  the @media block re-binds semantic tokens directly — each role gets
         *  an independent dark value, no overloading possible. */
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: light dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        /* Surfaces — inverted. */
                        --color-surface:           #0F1320;
                        --color-surface-raised:    #1A1F36;
                        --color-surface-recessed:  #232943;
                        --color-surface-inverted:  #111936;   /* kept dark — header bg */

                        /* Text — inverted. */
                        --color-text-primary:            #E2E8F0;
                        --color-text-muted:              #94A3B8;
                        --color-text-on-inverted:        #E2E8F0;
                        --color-text-on-inverted-muted:  #B8C9F2;
                        --color-text-title:               #8FA3D8;
                        --color-text-link:               #8FA3D8;   /* lifted navy */
                        --color-text-link-hover:         #E0A833;   /* lifted amber-dk */

                        /* Borders. */
                        --color-border:           #2D3454;
                        --color-border-emphasis:  #F4B942;          /* kept gold */

                        /* Accent — gold reads well on both modes. */
                        --color-accent:           #F4B942;
                        --color-accent-emphasis:  #E0A833;          /* lifted amber-dk */
                        --color-accent-on:        #111936;          /* dark text on gold */
                    }
                }
                """;
    }
}
