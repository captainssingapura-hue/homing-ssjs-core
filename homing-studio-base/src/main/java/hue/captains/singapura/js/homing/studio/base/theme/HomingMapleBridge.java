package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.ClickTarget;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Cue;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeAudio;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;

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

    /** Theme-audio binding — clicks on the nocturne's classed elements
     *  fire a temple bell, a soft chime (moon), or a lamp crackle
     *  (temple window). See {@link MbAudio} for the spec, {@link StandardAudio}
     *  for the cue selections. RFC 0007. */
    @Override
    public ThemeAudio<?> audio() {
        return StandardAudio.INSTANCE;
    }

    // ===========================================================================
    //  Click targets — sealed permits enumerate every clickable element on the
    //  Maple Bridge surface. Each record carries a stable classToken that
    //  matches a `class="…"` attribute in nocturne.svg.
    // ===========================================================================

    /** Sealed surface area of audio-bound Maple Bridge elements — both
     *  click cues (nocturne scenery) and hover cues (chrome interactions).
     *  Adding a new one is a typed action. */
    public sealed interface MbTarget extends ClickTarget<HomingMapleBridge>
            permits Temple, Moon, TempleWindow, Card, ListItem, TocItem {}

    // Nocturne scenery — click cues.
    public record Temple()       implements MbTarget { @Override public String classToken() { return "mb-temple"; } }
    public record Moon()         implements MbTarget { @Override public String classToken() { return "mb-moon"; } }
    public record TempleWindow() implements MbTarget { @Override public String classToken() { return "mb-temple-window"; } }

    // Chrome interaction — hover cues. Bound to framework CssClasses
    // (.st-card, .st-list-item, .st-toc-item) so any page that renders
    // these structures gets the hover feedback. RFC 0008 hover extension.
    public record Card()     implements MbTarget { @Override public String classToken() { return "st-card"; } }
    public record ListItem() implements MbTarget { @Override public String classToken() { return "st-list-item"; } }
    public record TocItem()  implements MbTarget { @Override public String classToken() { return "st-toc-item"; } }

    // ===========================================================================
    //  Audio spec — the contract every Maple Bridge audio implementation
    //  must satisfy. One Cue-returning method per click target; the default
    //  bindings() implementation walks them.
    // ===========================================================================

    /** Maple Bridge's audio spec. Every implementor must provide a cue
     *  for each click target. The default {@link #bindings()} walks them
     *  in order; the compiler enforces that no method is missing. */
    public interface MbAudio extends ThemeAudio<HomingMapleBridge> {
        Cue temple();
        Cue moon();
        Cue templeWindow();

        @Override default HomingMapleBridge theme() { return HomingMapleBridge.INSTANCE; }

        @Override default java.util.Map<ClickTarget<HomingMapleBridge>, Cue> bindings() {
            return java.util.Map.of(
                    new Temple(),       temple(),
                    new Moon(),         moon(),
                    new TempleWindow(), templeWindow()
            );
        }
    }

    /** Standard implementation — picks from the shared {@link Cues} stdlib.
     *  Click cues are the nocturne scenery (temple bell, moon chime, lamp
     *  crackle). Hover cues are the chrome interactions (card / list /
     *  TOC items get a soft tink — the cursor entering a content surface
     *  feels like a brushed thumb-strum on paper). */
    public record StandardAudio() implements MbAudio {
        public static final StandardAudio INSTANCE = new StandardAudio();
        @Override public Cue temple()       { return Cues.TEMPLE_BELL; }
        @Override public Cue moon()         { return Cues.SOFT_CHIME; }
        @Override public Cue templeWindow() { return Cues.LAMP_CRACKLE; }

        @Override public java.util.Map<ClickTarget<HomingMapleBridge>, Cue> hoverBindings() {
            return java.util.Map.of(
                    // Cards play a full diatonic chord — pop-progression voicing
                    // as the cursor sweeps across the catalogue. Each card's
                    // chord is hash-stable within a session.
                    new Card(),     Cues.HOVER_CHORD_CLEAN,
                    // List + TOC items stay on single-note vocal palette —
                    // lighter touch for navigation surfaces.
                    new ListItem(), Cues.HOVER_BREATH,
                    new TocItem(),  Cues.HOVER_BREATH
            );
        }
    }

    public record Vars() implements ThemeVariables<HomingMapleBridge> {
        public static final Vars INSTANCE = new Vars();
        @Override public HomingMapleBridge theme() { return HomingMapleBridge.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        // Light mode (dawn) — distilled from the SVG's dawn palette but
        // independent of it. Page surfaces are translucent-ish warm tones
        // that read well against the dawn scene; cards/header are opaque
        // surfaces that sit cleanly over the illustration.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                Map.entry(StudioVars.COLOR_SURFACE,                "#E8C9A0"),  // sky-bot dawn
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,         "#F5E7C8"),  // raised paper
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED,       "#D4B896"),  // sky-mid dawn
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED,       "#3A4250"),  // temple slate

                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#2A2418"),  // deep ink
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#5A5040"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#FFF5DC"),  // dawn moon
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#C0A878"),
                Map.entry(StudioVars.COLOR_TEXT_TITLE,              "#8A6A3A"),   // title := link, unchanged
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#8A6A3A"),  // amber window
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#4A5466"),  // mountain-near

                Map.entry(StudioVars.COLOR_BORDER,                 "#C0A878"),
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS,        "#8A6A3A"),

                Map.entry(StudioVars.COLOR_ACCENT,                 "#8A6A3A"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS,        "#4A5466"),
                Map.entry(StudioVars.COLOR_ACCENT_ON,              "#FFF5DC"),

                Map.entry(StudioVars.SPACE_1, "4px"),
                Map.entry(StudioVars.SPACE_2, "8px"),
                Map.entry(StudioVars.SPACE_3, "12px"),
                Map.entry(StudioVars.SPACE_4, "16px"),
                Map.entry(StudioVars.SPACE_5, "20px"),
                Map.entry(StudioVars.SPACE_6, "24px"),
                Map.entry(StudioVars.SPACE_7, "32px"),
                Map.entry(StudioVars.SPACE_8, "40px"),
                Map.entry(StudioVars.RADIUS_SM, "3px"),
                Map.entry(StudioVars.RADIUS_MD, "6px"),
                Map.entry(StudioVars.RADIUS_LG, "10px")
        );
    }

    public record Globals() implements ThemeGlobals<HomingMapleBridge> {
        public static final Globals INSTANCE = new Globals();
        @Override public HomingMapleBridge theme() { return HomingMapleBridge.INSTANCE; }
        @Override public String css() {
            // Order: dark-mode overrides → shared structural cascade → our
            // SVG-background overlay. Putting the overlay last lets the
            // background-image longhand land after the structural
            // `background: var(--color-surface)` shorthand cleared it.
            return DARK_OVERRIDE + HomingDefault.STRUCTURAL_CSS + TEXTURE_OVERRIDE;
        }

        /** Dark-mode (night) palette — when the OS prefers dark. */
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: light dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        --color-surface:           #0A131E;   /* water-bot */
                        --color-surface-raised:    #1B2D44;   /* sky-mid night */
                        --color-surface-recessed:  #0D1620;   /* mountain-near */
                        --color-surface-inverted:  #0E1620;   /* temple */

                        --color-text-primary:            #CBD9E8;
                        --color-text-muted:              #7A89A0;
                        --color-text-on-inverted:        #BCC7D6;   /* mist */
                        --color-text-on-inverted-muted:  #7A89A0;
                        --color-text-title:               #F5E3B0;
                        --color-text-link:               #F5E3B0;   /* moon */
                        --color-text-link-hover:         #F0DCA8;

                        --color-border:           #3A5070;          /* ripple */
                        --color-border-emphasis:  #F5E3B0;

                        --color-accent:           #F5E3B0;
                        --color-accent-emphasis:  #D9A35A;          /* temple-window */
                        --color-accent-on:        #0A131E;
                    }
                }
                """;

        /**
         * The night sky as the page surface. The nocturne used to be a
         * full-page inline SVG the framework injected behind the chrome, with
         * universal {@code pointer-events: none} plumbing so its moon could
         * take a hover; RFC 0064 retired the injected backdrop — the server no
         * longer knows the theme a page wears, and a backdrop that only some
         * addresses got was a theme that only sometimes applied. What remains
         * is the sky itself: a fixed gradient from the zenith down to the lake,
         * deep navy to a lit horizon, so the parchment reading surface still
         * sits on a night.
         */
        private static final String TEXTURE_OVERRIDE = """
                html {
                    background: linear-gradient(180deg, #0A131E 0%, #12213A 45%, #24405C 100%);
                    background-attachment: fixed;
                }
                body {
                    background: transparent;
                }
                /* Doc-reader column slab is set framework-default (HomingDefault
                   COMPONENT_CSS targets `.st-main:has(.st-doc-meta)`). Maple
                   Bridge only adjusts the fill to be slightly translucent so
                   the night bleeds a hint through the parchment — gives the
                   surface a "lit from behind" feel on the reading page.
                   Same scope: only the doc reader. */
                .st-main:has(.st-doc-meta) {
                    background-color: color-mix(in srgb, var(--color-surface-raised) 92%, transparent);
                }

                /* Inner panes inherit the parchment from .st-main; no per-pane
                   background needed. Padding kept for reading-pane feel. */
                .st-doc {
                    padding: 28px 32px;
                }
                .st-sidebar {
                    padding: 16px;
                }
                """;
    }
}
