package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.Cue;
import hue.captains.singapura.js.homing.core.Envelope;
import hue.captains.singapura.js.homing.core.MembraneCue;
import hue.captains.singapura.js.homing.core.NoiseCue;
import hue.captains.singapura.js.homing.core.Note;
import hue.captains.singapura.js.homing.core.NoteDuration;
import hue.captains.singapura.js.homing.core.NoteHit;
import hue.captains.singapura.js.homing.core.OscCue;
import hue.captains.singapura.js.homing.core.OscType;

import java.util.List;

/**
 * Stdlib of reusable typed audio cues. Themes pick from these constants
 * to wire their {@link ClickTarget} bindings; theme implementers who
 * need something bespoke construct fresh {@link Cue} record literals.
 *
 * <p>Each constant is a stateless value — same instance is safely
 * referenced from any number of theme bindings. RFC 0007.</p>
 *
 * <p>Volume convention: all cues run between -22 dB and -8 dB. Anything
 * louder competes with the user's other audio (browser tab music, etc.);
 * anything quieter is hard to hear over typical ambient noise.</p>
 */
public final class Cues {

    private Cues() {}

    // The shared vocal pitch palette lives in homing-core's
    // {@link hue.captains.singapura.js.homing.core.VocalPalette#NOTES} —
    // 11 pitches from C3 to B5 with G4 at the centre. Cues that opt in
    // (set {@code useVocalPalette: true}) are baked once per palette
    // pitch; the runtime selects per trigger (hash for hover, random
    // for click). Theme authors don't reference the palette directly —
    // they just flip the flag on the cues that should use it.

    // ----------------------------------------------------------------
    //  Bell-likes — long-decay sinusoidal partials, characteristic
    //  shimmer from the detuned upper partial.
    // ----------------------------------------------------------------

    /** A bronze temple bell. Four partials at bell-like ratios — the
     *  top partial detuned by 80 ms for the natural asymmetric beating
     *  real bronze produces.  Long decay (~5 seconds) lets the sound
     *  bloom and fade.  Used by Maple Bridge for {@code .mb-temple}. */
    public static final Cue TEMPLE_BELL = new OscCue(
            OscType.SINE,
            new Envelope(0.002, 3.5, 0.0, 2.5),
            -10.0,
            List.of(
                    new NoteHit(Note.C4, NoteDuration.HALF, 0),
                    new NoteHit(Note.G4, NoteDuration.HALF, 0),
                    new NoteHit(Note.E5, NoteDuration.HALF, 0),
                    new NoteHit(Note.C5, NoteDuration.HALF, 80)
            )
    );

    /** A smaller, brighter chime — wind-chime-like, suitable for hover-
     *  scale equivalents like Maple Bridge's moon. Higher partials,
     *  shorter decay than {@link #TEMPLE_BELL}. */
    public static final Cue SOFT_CHIME = new OscCue(
            OscType.SINE,
            new Envelope(0.001, 1.6, 0.0, 1.2),
            -14.0,
            List.of(
                    new NoteHit(Note.G5, NoteDuration.QUARTER, 0),
                    new NoteHit(Note.D6, NoteDuration.QUARTER, 0),
                    new NoteHit(Note.B6, NoteDuration.QUARTER, 60)
            )
    );

    // ----------------------------------------------------------------
    //  Click-likes — sharp attack, near-zero decay, terminal release.
    // ----------------------------------------------------------------

    /** Classic Win95 mouse click — bright, sharp, square wave. Matches
     *  the moving-animal demo's {@code moveSynth} preset.  Used by
     *  Retro 90s on the four desktop icons. */
    public static final Cue WIN95_CLICK = new OscCue(
            OscType.SQUARE,
            new Envelope(0.001, 0.04, 0.0, 0.01),
            -22.0,
            List.of(new NoteHit(Note.A6, NoteDuration.THIRTY_SECOND, 0))
    );

    /** Win95 system "ding" — slightly longer, more melodic. Triangle
     *  wave for warmth.  Used by Retro 90s for the recycle-bin icon. */
    public static final Cue WIN95_DING = new OscCue(
            OscType.TRIANGLE,
            new Envelope(0.005, 0.12, 0.0, 0.08),
            -18.0,
            List.of(
                    new NoteHit(Note.E5, NoteDuration.SIXTEENTH, 0),
                    new NoteHit(Note.A5, NoteDuration.SIXTEENTH, 80)
            )
    );

    /** A tiny tick — minimal feedback for sidebar / TOC items. Quieter
     *  than the icon clicks so rapid navigation doesn't get noisy. */
    public static final Cue TICK = new OscCue(
            OscType.SQUARE,
            new Envelope(0.0005, 0.02, 0.0, 0.005),
            -28.0,
            List.of(new NoteHit(Note.C7, NoteDuration.SIXTY_FOURTH, 0))
    );

    // ----------------------------------------------------------------
    //  Membrane / thud — drum-like, percussive.
    // ----------------------------------------------------------------

    /** A soft card tap — drum-like membrane hit, low-pitched, very
     *  quick decay.  Suitable for catalogue cards: present but not
     *  intrusive on every navigation. */
    public static final Cue CARD_THUD = new MembraneCue(
            0.06, 2,
            new Envelope(0.005, 0.12, 0.0, 0.08),
            -18.0,
            List.of(new NoteHit(Note.C4, NoteDuration.SIXTEENTH, 0))
    );

    // ----------------------------------------------------------------
    //  Noise — un-pitched, atmospheric.
    // ----------------------------------------------------------------

    /** A brief crackle — un-pitched noise burst, suitable for lamp /
     *  flame elements (Maple Bridge's temple window lamp). The
     *  envelope keeps it short to avoid sounding like static. */
    public static final Cue LAMP_CRACKLE = new NoiseCue(
            new Envelope(0.001, 0.18, 0.0, 0.05),
            -20.0,
            List.of(new NoteHit(Note.C5, NoteDuration.EIGHTH, 0))
    );

    // ----------------------------------------------------------------
    //  Hover feedback — short, quiet sine pings suitable for the
    //  cursor entering a card / list item / nav element. The runtime
    //  throttles these globally to ≤1 per 400 ms; volumes are tuned
    //  so a stray hover doesn't dominate the soundscape.
    // ----------------------------------------------------------------

    /** A soft sine tink — sine wave in the vocal palette. Each card gets
     *  a stable pitch within a session via hash; the page reads as a
     *  hushed chorus where each card has its own note. Pairs with
     *  parchment / paper / nocturne aesthetics. The NoteHit's note is
     *  irrelevant for paletteMode=VOCAL — palette overrides. */
    public static final Cue HOVER_TINK = new OscCue(
            OscType.SINE,
            new Envelope(0.001, 0.18, 0.0, 0.1),
            -24.0,
            hue.captains.singapura.js.homing.core.PaletteMode.VOCAL,
            List.of(new NoteHit(Note.G4, NoteDuration.THIRTY_SECOND, 0))
    );

    /** A quieter still hover — triangle wave in the vocal palette. For
     *  list items / TOC entries where the cursor sweep should feel
     *  barely audible, like a whispered "noticed." */
    public static final Cue HOVER_BREATH = new OscCue(
            OscType.TRIANGLE,
            new Envelope(0.002, 0.12, 0.0, 0.08),
            -28.0,
            hue.captains.singapura.js.homing.core.PaletteMode.VOCAL,
            List.of(new NoteHit(Note.G4, NoteDuration.SIXTY_FOURTH, 0))
    );

    /** Win95 selection bleep — short square wave in the vocal palette.
     *  Period-accurate for Retro 90s' system feedback. */
    public static final Cue HOVER_BLEEP = new OscCue(
            OscType.SQUARE,
            new Envelope(0.001, 0.05, 0.0, 0.02),
            -22.0,
            hue.captains.singapura.js.homing.core.PaletteMode.VOCAL,
            List.of(new NoteHit(Note.G4, NoteDuration.THIRTY_SECOND, 0))
    );

    // ----------------------------------------------------------------
    //  Chord hover cues — paletteMode=CHORD. Each card plays a full
    //  diatonic chord (C major / D minor / E minor / F major / G major /
    //  A minor) instead of a single note. 6 chord identities; hovering
    //  the same card always plays the same chord within a session
    //  (hash-stable), different cards play different chords. Sweep
    //  across the catalogue → pop-progression voicing.
    // ----------------------------------------------------------------

    /** Soft triangle chord — Maple Bridge's card hover. Same envelope
     *  shape as HOVER_TINK but plays a full triad. Sits behind the doc
     *  content as a gentle harmonic atmosphere. */
    public static final Cue HOVER_CHORD_CLEAN = new OscCue(
            OscType.TRIANGLE,
            new Envelope(0.008, 0.2, 0.25, 0.5),
            -28.0,
            hue.captains.singapura.js.homing.core.PaletteMode.CHORD,
            List.of(new NoteHit(Note.G4, NoteDuration.HALF, 0))
    );

    /** Chiptune square chord — Retro 90s' card hover. Period-correct
     *  Genesis / NES-style triad chord. Brighter, more bite than the
     *  clean triangle version. */
    public static final Cue HOVER_CHORD_RETRO = new OscCue(
            OscType.SQUARE,
            new Envelope(0.002, 0.08, 0.2, 0.15),
            -26.0,
            hue.captains.singapura.js.homing.core.PaletteMode.CHORD,
            List.of(new NoteHit(Note.G4, NoteDuration.QUARTER, 0))
    );

    // ----------------------------------------------------------------
    //  Clean electric power chords with glass shimmer — triangle wave,
    //  no distortion, soft attack + long release for the bell-like
    //  ring-out feel. Stacks root + fifth + octave + high-fifth — the
    //  top partial gives the "glass" sparkle without dominating.
    //  Quieter than the distorted version it replaces; sits behind
    //  the drums in the mix instead of fighting them.
    // ----------------------------------------------------------------

    /** Low clean chord — E2 root, B3 shimmer on top. Triangle wave +
     *  zero distortion + soft attack gives a bell-meets-clean-electric
     *  timbre. Pairs naturally with the drum kit when the user is
     *  drumming with one hand and strumming with the other. */
    public static final Cue ELECTRIC_CLEAN_LOW = new OscCue(
            OscType.TRIANGLE,
            new Envelope(0.008, 0.12, 0.35, 0.6),
            -26.0,
            hue.captains.singapura.js.homing.core.PaletteMode.NONE,
            0.0,     // clean — no distortion
            List.of(
                    new NoteHit(Note.E2, NoteDuration.HALF, 0),
                    new NoteHit(Note.B2, NoteDuration.HALF, 0),
                    new NoteHit(Note.E3, NoteDuration.HALF, 0),
                    new NoteHit(Note.B3, NoteDuration.HALF, 0)   // high-fifth shimmer
            )
    );

    /** Mid clean chord — A2 root, E4 shimmer. */
    public static final Cue ELECTRIC_CLEAN_MID = new OscCue(
            OscType.TRIANGLE,
            new Envelope(0.008, 0.12, 0.35, 0.6),
            -26.0,
            hue.captains.singapura.js.homing.core.PaletteMode.NONE,
            0.0,
            List.of(
                    new NoteHit(Note.A2, NoteDuration.HALF, 0),
                    new NoteHit(Note.E3, NoteDuration.HALF, 0),
                    new NoteHit(Note.A3, NoteDuration.HALF, 0),
                    new NoteHit(Note.E4, NoteDuration.HALF, 0)   // shimmer
            )
    );

    /** High clean chord — D3 root, A4 shimmer. The brightest of the three. */
    public static final Cue ELECTRIC_CLEAN_HIGH = new OscCue(
            OscType.TRIANGLE,
            new Envelope(0.008, 0.12, 0.35, 0.6),
            -26.0,
            hue.captains.singapura.js.homing.core.PaletteMode.NONE,
            0.0,
            List.of(
                    new NoteHit(Note.D3, NoteDuration.HALF, 0),
                    new NoteHit(Note.A3, NoteDuration.HALF, 0),
                    new NoteHit(Note.D4, NoteDuration.HALF, 0),
                    new NoteHit(Note.A4, NoteDuration.HALF, 0)   // shimmer
            )
    );

    // ----------------------------------------------------------------
    //  Jazz drum kit (RFC 0008) — 8 typed cues covering a standard
    //  jazz configuration: kick + snare + hi-hat + 3 toms + 2 cymbals.
    //  Membrane synths for the drums (pitched, drum-like body); noise
    //  synths for the cymbals + hi-hat (un-pitched metallic shimmer).
    // ----------------------------------------------------------------

    /** Bass drum — deep, fast pitch sweep. Tuned a touch lower than
     *  the Retro 90s card thud for that "front-of-the-stage" weight. */
    public static final Cue KICK = new MembraneCue(
            0.04, 4,
            new Envelope(0.001, 0.4, 0.0, 0.1),
            -8.0,
            List.of(new NoteHit(Note.C2, NoteDuration.QUARTER, 0))
    );

    /** Snare — sharp white noise burst, the crack of a stick on coated
     *  head. Slightly longer decay than hi-hat to leave room for the
     *  body of the sound. */
    public static final Cue SNARE = new NoiseCue(
            new Envelope(0.001, 0.15, 0.0, 0.05),
            -10.0,
            List.of(new NoteHit(Note.A4, NoteDuration.SIXTEENTH, 0))
    );

    /** Hi-hat (closed) — tight, bright. The shortest of the cymbals,
     *  controlled by the foot pedal in real life; here just a quick
     *  high-frequency noise tick. */
    public static final Cue HIHAT_CLOSED = new NoiseCue(
            new Envelope(0.001, 0.04, 0.0, 0.02),
            -16.0,
            List.of(new NoteHit(Note.C6, NoteDuration.THIRTY_SECOND, 0))
    );

    /** High tom — mid-pitched, quick decay. Pitch G3 fits in the
     *  "treble" register of a typical jazz kit's three-tom setup. */
    public static final Cue TOM_HIGH = new MembraneCue(
            0.05, 3,
            new Envelope(0.001, 0.2, 0.0, 0.08),
            -12.0,
            List.of(new NoteHit(Note.G3, NoteDuration.EIGHTH, 0))
    );

    /** Low tom — slightly lower, slightly longer than the high tom.
     *  Pitched E3 to sit comfortably between the high tom and the
     *  floor tom in a three-tom fill. */
    public static final Cue TOM_LOW = new MembraneCue(
            0.05, 3,
            new Envelope(0.001, 0.22, 0.0, 0.1),
            -12.0,
            List.of(new NoteHit(Note.E3, NoteDuration.EIGHTH, 0))
    );

    /** Floor tom — the lowest pitched drum on the kit. Long decay
     *  for the chest-resonating "boom" of a real floor tom. */
    public static final Cue FLOOR_TOM = new MembraneCue(
            0.06, 3,
            new Envelope(0.001, 0.28, 0.0, 0.12),
            -12.0,
            List.of(new NoteHit(Note.C3, NoteDuration.EIGHTH, 0))
    );

    /** Ride cymbal — long noise tail, the "ping" then bloom of bronze.
     *  Half-second decay captures the characteristic jazz-ride wash
     *  without overwhelming the rest of the kit. */
    public static final Cue RIDE = new NoiseCue(
            new Envelope(0.002, 0.5, 0.0, 0.5),
            -14.0,
            List.of(new NoteHit(Note.G6, NoteDuration.HALF, 0))
    );

    /** Crash cymbal — biggest, brightest, longest. Over a second of
     *  decay; placed in a fill, the crash is meant to ring out and
     *  reset the dynamic. */
    public static final Cue CRASH = new NoiseCue(
            new Envelope(0.001, 1.2, 0.0, 0.8),
            -14.0,
            List.of(new NoteHit(Note.A6, NoteDuration.WHOLE, 0))
    );
}
