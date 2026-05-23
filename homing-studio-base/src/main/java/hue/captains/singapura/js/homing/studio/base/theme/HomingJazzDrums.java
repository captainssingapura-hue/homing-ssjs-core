package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.ClickTarget;
import hue.captains.singapura.js.homing.core.Component;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Cue;
import hue.captains.singapura.js.homing.core.Envelope;
import hue.captains.singapura.js.homing.core.KeyCombo;
import hue.captains.singapura.js.homing.core.Layer;
import hue.captains.singapura.js.homing.core.MediaGated;
import hue.captains.singapura.js.homing.core.Note;
import hue.captains.singapura.js.homing.core.NoteDuration;
import hue.captains.singapura.js.homing.core.NoteHit;
import hue.captains.singapura.js.homing.core.OscCue;
import hue.captains.singapura.js.homing.core.OscType;
import hue.captains.singapura.js.homing.core.PaletteMode;
import hue.captains.singapura.js.homing.core.Progression;
import hue.captains.singapura.js.homing.core.Prose;
import hue.captains.singapura.js.homing.core.Reset;
import hue.captains.singapura.js.homing.core.State;
import hue.captains.singapura.js.homing.core.SvgRef;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeAudio;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeOverlay;
import hue.captains.singapura.js.homing.core.ThemeVariables;

import java.util.List;
import java.util.Map;

/**
 * Jazz Drum Kit theme — the first "instrument" theme. RFC 0008.
 *
 * <p>The backdrop SVG is a full jazz drum kit. Each drum and cymbal is a
 * typed {@link ClickTarget}; clicking any of them fires its bound {@link Cue}
 * (kick → membrane thud, snare → noise burst, hi-hat / ride / crash →
 * noise with shaped envelopes). The framework's RFC 0007 audio runtime
 * does the work; this theme contributes typed bindings and the visual kit.</p>
 *
 * <p>Phase 1 of RFC 0008: <b>click-only</b>. Keyboard play mode + theme
 * control panel + visual play feedback ship in Phase 2.</p>
 *
 * <p>Activate via {@code ?theme=jazz-drums} on any studio URL.</p>
 */
public record HomingJazzDrums() implements Theme {

    public static final HomingJazzDrums INSTANCE = new HomingJazzDrums();

    @Override public String slug()  { return "jazz-drums"; }
    /** Installs universal pointer-events:none + whitelist so drum/instrument
     *  clicks reach the backdrop. Workspace-style apps opt out via
     *  {@link AppModule#acceptsBackdropInteractivity()}. */
    @Override public boolean backdropInteractivity() { return true; }
    @Override public String label() { return "Jazz Drum Kit"; }

    /** Drum kit backdrop — the kit is the visible surface and the
     *  interactive surface at once. */
    @Override
    public SvgRef<?> backdrop() {
        return new SvgRef<>(HomingJazzDrumsBg.INSTANCE, new HomingJazzDrumsBg.kit());
    }

    /** Eight click-bound drum/cymbal cues. */
    @Override
    public ThemeAudio<?> audio() {
        return StandardAudio.INSTANCE;
    }

    // ===========================================================================
    //  Click targets — every drum + cymbal on the kit. Sealed permits
    //  enumerate the complete instrument vocabulary; adding a new piece is
    //  a typed action (new record + permit + method on JazzDrumsAudio).
    // ===========================================================================

    /** Sealed surface of audio-bound Jazz Drums elements — 8 drum/cymbal
     *  pieces (visible + click-bound) + 3 guitar power chords (keyboard-only,
     *  no SVG; their classTokens deliberately don't match any element so
     *  click events never fire them — only keydown does). */
    public sealed interface DrumTarget extends ClickTarget<HomingJazzDrums>
            permits Kick, Snare, HihatClosed, TomHigh, TomLow, FloorTom, Ride, Crash,
                    PowerLow, PowerMid, PowerHigh {}

    // Drum kit pieces — visible in the SVG, click + keyboard triggerable.
    public record Kick()        implements DrumTarget { @Override public String classToken() { return "drum-kick"; } }
    public record Snare()       implements DrumTarget { @Override public String classToken() { return "drum-snare"; } }
    public record HihatClosed() implements DrumTarget { @Override public String classToken() { return "drum-hihat"; } }
    public record TomHigh()     implements DrumTarget { @Override public String classToken() { return "drum-tom-high"; } }
    public record TomLow()      implements DrumTarget { @Override public String classToken() { return "drum-tom-low"; } }
    public record FloorTom()    implements DrumTarget { @Override public String classToken() { return "drum-floor"; } }
    public record Ride()        implements DrumTarget { @Override public String classToken() { return "drum-ride"; } }
    public record Crash()       implements DrumTarget { @Override public String classToken() { return "drum-crash"; } }

    // Guitar power chords — keyboard-only layer. classTokens are placeholders;
    // no element in the kit SVG matches them, so click events never fire.
    // Only keyBindings (U/I/O) reach them.
    public record PowerLow()  implements DrumTarget { @Override public String classToken() { return "jd-power-low"; } }
    public record PowerMid()  implements DrumTarget { @Override public String classToken() { return "jd-power-mid"; } }
    public record PowerHigh() implements DrumTarget { @Override public String classToken() { return "jd-power-high"; } }

    // ===========================================================================
    //  Audio spec — every Jazz Drums audio impl must provide a cue for each
    //  drum + cymbal. The default bindings() walks them; the compiler enforces
    //  completeness via the sealed DrumTarget hierarchy.
    // ===========================================================================

    /** Jazz Drums audio spec — 8 drum/cymbal cues + 3 power-chord cues. */
    public interface JazzDrumsAudio extends ThemeAudio<HomingJazzDrums> {
        Cue kick();
        Cue snare();
        Cue hihatClosed();
        Cue tomHigh();
        Cue tomLow();
        Cue floorTom();
        Cue ride();
        Cue crash();
        Cue powerLow();
        Cue powerMid();
        Cue powerHigh();

        @Override default HomingJazzDrums theme() { return HomingJazzDrums.INSTANCE; }

        @Override default Map<ClickTarget<HomingJazzDrums>, Cue> bindings() {
            return Map.ofEntries(
                    Map.entry(new Kick(),        kick()),
                    Map.entry(new Snare(),       snare()),
                    Map.entry(new HihatClosed(), hihatClosed()),
                    Map.entry(new TomHigh(),     tomHigh()),
                    Map.entry(new TomLow(),      tomLow()),
                    Map.entry(new FloorTom(),    floorTom()),
                    Map.entry(new Ride(),        ride()),
                    Map.entry(new Crash(),       crash()),
                    Map.entry(new PowerLow(),    powerLow()),
                    Map.entry(new PowerMid(),    powerMid()),
                    Map.entry(new PowerHigh(),   powerHigh())
            );
        }

        /** Keyboard play mode — 2D spatial mapping. The keyboard's UPPER row
         *  corresponds to the TOP of the kit (cymbals + mounted toms); the
         *  HOME row to the BOTTOM (kick / snare / floor / hi-hat foot pedal).
         *  Left columns map to the left side of the kit, right columns to
         *  the right. Hands can stay in natural typing position across both
         *  rows — drum patterns that combine cymbals + drums (kick + crash,
         *  snare + hi-hat) play with adjacent fingers, no contortions.
         *
         *  <pre>
         *   Kit visual               Keyboard
         *   ------------------       ------------------
         *   Q (Crash)     I (Ride)   ← upper-row cymbals
         *      E (TomHi)  U (TomLo)  ← upper-row toms
         *   A (Hi-hat)              ← home-row hi-hat foot pedal
         *      S (Snare)  J (Floor) ← home-row drums
         *         F (Kick)           ← centre kick
         *  </pre>
         *
         *  <p>Guitar power chords live on the right-hand home row immediately
         *  after the floor tom (J). The drummer's right hand covers J for the
         *  floor tom and H/K/L for the three guitar chords with adjacent
         *  fingers — playing drums and strumming simultaneously is one
         *  finger away. RFC 0008 Phase 2.</p>
         */
        @Override default Map<KeyCombo, ClickTarget<HomingJazzDrums>> keyBindings() {
            return Map.ofEntries(
                    // Upper row — top of the kit (cymbals + mounted toms)
                    Map.entry(KeyCombo.KEY_Q, new Crash()),         // upper-LEFT cymbal
                    Map.entry(KeyCombo.KEY_E, new TomHigh()),       // mid-LEFT tom
                    Map.entry(KeyCombo.KEY_U, new TomLow()),        // mid-RIGHT tom
                    Map.entry(KeyCombo.KEY_I, new Ride()),          // upper-RIGHT cymbal
                    // Home row — bottom of the kit (foot-pedal hi-hat + drums)
                    Map.entry(KeyCombo.KEY_A, new HihatClosed()),   // far-LEFT (hi-hat stand)
                    Map.entry(KeyCombo.KEY_S, new Snare()),         // LEFT (snare)
                    Map.entry(KeyCombo.KEY_F, new Kick()),          // CENTRE (kick)
                    Map.entry(KeyCombo.KEY_J, new FloorTom()),      // RIGHT (floor tom)
                    // Guitar power chords — right hand home row, fingers adjacent
                    // to the floor tom. Index reach-left for H, middle for K,
                    // ring for L. Pitch ascends left-to-right.
                    Map.entry(KeyCombo.KEY_H, new PowerLow()),      // E power chord (root)
                    Map.entry(KeyCombo.KEY_K, new PowerMid()),      // A power chord
                    Map.entry(KeyCombo.KEY_L, new PowerHigh())      // D power chord
            );
        }

        // ----------------------------------------------------------------
        //  Auto-play guitar — chord progressions looped through the
        //  jazzbox at the far left of the kit. RFC 0008 extension.
        // ----------------------------------------------------------------

        /** Six jazz chord progressions referencing indices into
         *  {@link hue.captains.singapura.js.homing.core.ChordPalette#CHORDS}.
         *  Each carries a mood colour the runtime applies as a subtle 15%
         *  overlay while that progression is playing. Per-cycle the runtime
         *  picks a new random progression — mood shifts with it. */
        @Override default List<Progression> progressions() {
            return List.of(
                    // ii–V–I — the canonical jazz cadence.   Dm – G  – C
                    new Progression("ii-V-I",       new int[]{1, 4, 0},          2.0, "#6a4f8b"),
                    // iii–vi–ii–V — extended back-cycle.     Em – Am – Dm – G
                    new Progression("iii-vi-ii-V",  new int[]{2, 5, 1, 4},       2.0, "#c2904a"),
                    // 50s pop — I–vi–IV–V.                   C  – Am – F  – G
                    new Progression("50s Pop",      new int[]{0, 5, 3, 4},       2.0, "#c97b63"),
                    // Modern pop — vi–IV–I–V.                Am – F  – C  – G
                    new Progression("Modern Pop",   new int[]{5, 3, 0, 4},       2.0, "#4a8f8a"),
                    // Rhythm Changes A — I–vi–ii–V cycling.  C  – Am – Dm – G
                    new Progression("Rhythm Changes", new int[]{0, 5, 1, 4},     1.5, "#3a6f4f"),
                    // Jazz Blues — classic 12-bar shape (no diminished in palette,
                    // so quick-IV substitution stands in for the IV7).
                    new Progression("Jazz Blues",   new int[]{0, 0, 0, 0, 3, 3, 0, 0, 4, 3, 0, 4}, 1.2, "#3a5f8f")
            );
        }

        /** Soft jazzbox voice — triangle wave through the chord palette
         *  rendered as a 1-3-5-3 broken-chord arpeggio (~0.72 s per chord).
         *  Each note has enough release to ring against the next, giving a
         *  legato fingerpicked feel rather than a blocky stab. */
        @Override default Cue progressionVoice() {
            return new OscCue(
                    OscType.TRIANGLE,
                    // Snappier attack + shorter decay; longer release lets
                    // each arpeggiated note overlap the next musically.
                    new Envelope(0.005, 0.2, 0.3, 0.9),
                    // Each note plays alone (not summed like a chord stab),
                    // so volume bumps to compensate. -10 dB rings clearly
                    // without overwhelming the drums.
                    -10.0,
                    PaletteMode.CHORD,
                    0.0,
                    // Template hit — pitch + duration override per chord by
                    // the bake step's arpeggio walk. Quarter-note duration
                    // matches the 0.18 s arpeggio stride.
                    List.of(new NoteHit(Note.C4, NoteDuration.QUARTER, 0))
            );
        }
    }

    /** Standard implementation — picks from the {@link Cues} stdlib.
     *  Drum cues for the 8 visible kit pieces; power-chord cues for the
     *  3 keyboard-only guitar layer. */
    public record StandardAudio() implements JazzDrumsAudio {
        public static final StandardAudio INSTANCE = new StandardAudio();
        @Override public Cue kick()         { return Cues.KICK; }
        @Override public Cue snare()        { return Cues.SNARE; }
        @Override public Cue hihatClosed()  { return Cues.HIHAT_CLOSED; }
        @Override public Cue tomHigh()      { return Cues.TOM_HIGH; }
        @Override public Cue tomLow()       { return Cues.TOM_LOW; }
        @Override public Cue floorTom()     { return Cues.FLOOR_TOM; }
        @Override public Cue ride()         { return Cues.RIDE; }
        @Override public Cue crash()        { return Cues.CRASH; }
        @Override public Cue powerLow()     { return Cues.ELECTRIC_CLEAN_LOW; }
        @Override public Cue powerMid()     { return Cues.ELECTRIC_CLEAN_MID; }
        @Override public Cue powerHigh()    { return Cues.ELECTRIC_CLEAN_HIGH; }
    }

    // ===========================================================================
    //  Theme variables — warm-stage palette. Surface is deep chamber-red;
    //  raised tones lift to lit-wood; cream text reads as stage-lit.
    // ===========================================================================

    public record Vars() implements ThemeVariables<HomingJazzDrums> {
        public static final Vars INSTANCE = new Vars();
        @Override public HomingJazzDrums theme() { return HomingJazzDrums.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — chamber-red stage, cream highlight tones.
                Map.entry(StudioVars.COLOR_SURFACE,                "#2A1414"),  // dark stage red
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,         "#3A2020"),  // lit-wood
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED,       "#1A0808"),  // deep shadow
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED,       "#C7A876"),  // brass

                // Text — cream on red, brass on cream-when-inverted.
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#EFE4C8"),  // cream
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#B89968"),  // warm tan
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#2A1414"),  // chamber-red on brass
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#5A3030"),
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#C7A876"),  // brass
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#E8D2A0"),  // brass-hi

                // Borders — warm shadow / brass emphasis.
                Map.entry(StudioVars.COLOR_BORDER,                 "#4A2A2A"),
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS,        "#C7A876"),

                // Accent — brass.
                Map.entry(StudioVars.COLOR_ACCENT,                 "#C7A876"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS,        "#E8D2A0"),
                Map.entry(StudioVars.COLOR_ACCENT_ON,              "#2A1414"),

                // Spacing — default scale.
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

    // ===========================================================================
    //  Theme globals — translucent slab on doc-reader pages so the kit is
    //  partially visible behind the reading column; backdrop framing in the
    //  gutters at wider viewports.
    // ===========================================================================

    public record Globals() implements ThemeGlobals<HomingJazzDrums> {
        public static final Globals INSTANCE = new Globals();
        @Override public HomingJazzDrums theme() { return HomingJazzDrums.INSTANCE; }

        @Override public String css() {
            return HomingDefault.STRUCTURAL_CSS + BACKDROP_LAYER + DARK_OVERRIDE;
        }

        @Override
        public Map<Class<? extends Layer>, String> chunks() {
            return Map.of(
                    Reset.class,        HomingDefault.STRUCTURAL_CHUNKS.get(Reset.class),
                    Component.class,    HomingDefault.STRUCTURAL_CHUNKS.get(Component.class),
                    Prose.class,        HomingDefault.STRUCTURAL_CHUNKS.get(Prose.class),
                    State.class,        HomingDefault.STRUCTURAL_CHUNKS.get(State.class),
                    MediaGated.class,   HomingDefault.STRUCTURAL_CHUNKS.get(MediaGated.class),
                    ThemeOverlay.class, BACKDROP_LAYER + DARK_OVERRIDE
            );
        }

        /** Backdrop positioning + translucent column slab. Same pattern as
         *  Maple Bridge — body transparent so the kit shows through; the
         *  doc-reader's column slab uses {@code color-mix} for slight
         *  bleed-through so the kit is visible BEHIND the prose at wider
         *  viewports (the kit fills the gutters cleanly). */
        private static final String BACKDROP_LAYER = """
                html, body { background: transparent; }
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
                .st-main { background-color: transparent; }
                /* Doc-reader slab — slightly translucent so the kit is
                 * visible behind the reading column (same trick as
                 * Maple Bridge). The framework-default slab from
                 * HomingDefault COMPONENT_CSS is fully opaque; we
                 * override here only on the doc-reader scope. */
                .st-main:has(.st-doc-meta) {
                    background-color: color-mix(in srgb, var(--color-surface-raised) 88%, transparent);
                }

                /* Universal pointer-events pass-through (so the backdrop
                 * receives clicks) with selective restoration on the
                 * user-interactive surfaces. Audio-bound classes get
                 * pointer-events restored by the runtime; see
                 * AppHtmlGetAction.renderAudioRuntime. */
                /* Gated by :not(.homing-bg-passive) — workspace-style apps
                 * opt out and reclaim event surface. See AppModule.acceptsBackdropInteractivity. */
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
                body:not(.homing-bg-passive) .st-doc,
                body:not(.homing-bg-passive) .st-doc *,
                body:not(.homing-bg-passive) .st-sidebar,
                body:not(.homing-bg-passive) .st-sidebar *,
                body:not(.homing-bg-passive) .st-doc-meta,
                body:not(.homing-bg-passive) .st-doc-meta * { pointer-events: auto; }

                /* RFC 0008 ext — auto-play guitar (Telecaster, far-left,
                 * resting on floor, leaning slightly left).
                 *
                 * Position lives in the SVG file as transform="translate(80 750)
                 * rotate(-10 0 0)"; CSS animations re-state that base transform
                 * because CSS transforms override the SVG attribute rather than
                 * composing with it. Keep the values in sync if the SVG moves. */
                .jd-auto-guitar, .jd-auto-guitar * { pointer-events: auto; cursor: pointer; }

                /* Idle hover — small upward lift */
                .jd-auto-guitar { transition: transform 200ms ease, filter 200ms ease; }
                .jd-auto-guitar:hover {
                    transform: translate(80px, 746px) rotate(-10deg);
                    filter: brightness(1.10);
                }

                /* Playing state — gentle bob + halo glow */
                @keyframes jd-guitar-bob {
                    0%, 100% { transform: translate(80px, 750px) rotate(-10deg); }
                    50%      { transform: translate(80px, 747px) rotate(-10deg); }
                }
                .jd-auto-guitar.autoplaying { animation: jd-guitar-bob 2.4s ease-in-out infinite; }
                .jd-auto-guitar.autoplaying .jd-auto-guitar-halo {
                    opacity: 0.32;
                    transition: opacity 400ms ease;
                }
                /* Muted-while-playing — desaturated, dimmed */
                .jd-auto-guitar.muted-autoplay { filter: grayscale(0.5) brightness(0.7); }
                .jd-auto-guitar.muted-autoplay .jd-auto-guitar-halo { opacity: 0.12; }
                """;

        /** Dark-mode adaptation — the stage dims further; brass tones stay
         *  warm. The kit's own SVG palette is independent. */
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        --color-surface:           #1A0808;
                        --color-surface-raised:    #2A1010;
                        --color-surface-recessed:  #0A0202;
                        --color-surface-inverted:  #A88858;

                        --color-text-primary:            #EFE4C8;
                        --color-text-muted:              #98785A;
                        --color-text-on-inverted:        #1A0808;
                        --color-text-on-inverted-muted:  #4A2828;
                        --color-text-link:               #C7A876;
                        --color-text-link-hover:         #EFE4C8;

                        --color-border:           #3A1A1A;
                        --color-border-emphasis:  #C7A876;

                        --color-accent:           #C7A876;
                        --color-accent-emphasis:  #E8D2A0;
                        --color-accent-on:        #1A0808;
                    }
                }
                """;
    }
}
