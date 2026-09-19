package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.Mode;
import hue.captains.singapura.js.homing.design.Palette;
import hue.captains.singapura.js.homing.design.State;

import java.util.Map;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Feedback.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Pairing.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;
import static hue.captains.singapura.js.homing.studio.themes.Bind.*;

/**
 * Synthwave — the colours of a 1984 that never happened. Night is the native
 * register: a deep violet sky with a magenta sun low at the top and a cyan
 * grid running to the horizon; plates are darker violet, edged in neon;
 * magenta is the one voice, cyan the second, a chrome yellow the alarm.
 * Daylight is the same road at noon: a pale lavender page with the grid at
 * a whisper and the neons deepened until they read. Written out, not seeded:
 * a sun and a grid are not thirteen colours.
 */
public record SynthwavePalette() implements Palette {

    public static final SynthwavePalette INSTANCE = new SynthwavePalette();

    public static final DesignId ID = new DesignId("synthwave");

    /** Retro-Futurism's — the void its neon frames glow against; also the dark-and-neon Neo-Futurism. */
    @Override public DesignId id() { return ID; }
    @Override public DesignId anchor() { return HomingRetroFuturism.ID; }
    @Override public Set<DesignId> compatible() { return COMPATIBLE; }
    private static final Set<DesignId> COMPATIBLE = Set.of(HomingNeoFuturism.ID);
    @Override public String label() { return "Synthwave"; }
    @Override public String inspiration() { return "A magenta sun over a cyan grid — the colours of a 1984 that never happened."; }

    @Override public Impl impl(DesignClass<?> pair) { return pair.onColourPlane() ? WORDS.get(pair) : null; }

    // ── the night, and the noon ──────────────────────────────────────────
    static final String SKY = "#F6EFFF",     SKY_D = "#0B0221";
    static final String PLATE = "#FFFFFF",   PLATE_D = "#150A33";
    static final String WELL = "#EFE6FF",    WELL_D = "#1D1140";
    static final String VOID = "#2A0A4A",    VOID_D = "#05010F";
    static final String TEXT = "#2A0A4A",    TEXT_D = "#F5E9FF";
    static final String MUTED = "#7A5FA0",   MUTED_D = "#B8A6D9";
    static final String ON_VOID = "#F5E9FF", ON_VOID_MUTED = "#C9B3EA";
    static final String MAGENTA = "#E6007E", MAGENTA_D = "#FF2D95";
    static final String CYAN = "#0092A8",    CYAN_D = "#00F0FF";
    static final String ON_MAGENTA = "#FFFFFF", ON_MAGENTA_D = "#0B0221";
    static final String HAIR = "rgba(230, 0, 126, 0.18)", HAIR_D = "rgba(255, 45, 149, 0.28)";
    static final String SUN   = "radial-gradient(ellipse 60% 40% at 50% 0%, rgba(230, 0, 126, 0.16), transparent 70%)";
    static final String SUN_D = "radial-gradient(ellipse 60% 40% at 50% 0%, rgba(255, 45, 149, 0.45), transparent 70%)";
    static final String GRID   = "linear-gradient(rgba(0, 146, 168, 0.10) 1px, transparent 1px), linear-gradient(90deg, rgba(0, 146, 168, 0.10) 1px, transparent 1px)";
    static final String GRID_D = "linear-gradient(rgba(0, 240, 255, 0.14) 1px, transparent 1px), linear-gradient(90deg, rgba(0, 240, 255, 0.14) 1px, transparent 1px)";

    // interactive: nothing at rest; hover a magenta haze, selected the sun itself, current a thin line of cyan
    static final Impl INTERACTIVE_SURFACE = Impl.Bindings.none()
            .at(State.REST, "background-color", "transparent")
            .at(State.HOVER, "background-color", "rgba(230, 0, 126, 0.10)")
            .at(State.SELECTED, "background-color", MAGENTA)
            .at(State.CURRENT, "background-color", "rgba(0, 146, 168, 0.10)")
            .at(State.HIGHLIGHTED, "background-color", "rgba(0, 146, 168, 0.22)")
            .in(Mode.DARK, State.HOVER, "background-color", "rgba(255, 45, 149, 0.16)")
            .in(Mode.DARK, State.SELECTED, "background-color", MAGENTA_D)
            .in(Mode.DARK, State.CURRENT, "background-color", "rgba(0, 240, 255, 0.10)")
            .in(Mode.DARK, State.HIGHLIGHTED, "background-color", "rgba(0, 240, 255, 0.22)");
    static final Impl INTERACTIVE_INK = Impl.Bindings.of("inherit")
            .at(State.SELECTED, ON_MAGENTA).at(State.CURRENT, CYAN).at(State.HIGHLIGHTED, CYAN)
            .in(Mode.DARK, State.SELECTED, ON_MAGENTA_D).in(Mode.DARK, State.CURRENT, CYAN_D).in(Mode.DARK, State.HIGHLIGHTED, CYAN_D);
    static final Impl INTERACTIVE_EDGE = Impl.Bindings.none()
            .at(State.REST, "border-color", "transparent")
            .at(State.HOVER, "border-color", MAGENTA)
            .at(State.SELECTED, "border-color", MAGENTA)
            .at(State.CURRENT, "border-color", CYAN)
            .at(State.HIGHLIGHTED, "border-color", CYAN)
            .at(State.CHECKED, "border-color", MAGENTA)
            .at(State.FOCUS, "outline-color", CYAN)
            .in(Mode.DARK, State.HOVER, "border-color", MAGENTA_D)
            .in(Mode.DARK, State.SELECTED, "border-color", MAGENTA_D)
            .in(Mode.DARK, State.CURRENT, "border-color", CYAN_D)
            .in(Mode.DARK, State.HIGHLIGHTED, "border-color", CYAN_D)
            .in(Mode.DARK, State.CHECKED, "border-color", MAGENTA_D)
            .in(Mode.DARK, State.FOCUS, "outline-color", CYAN_D);

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // ── layers: the sky with a sun and a grid; plates of darker violet ──
            Map.entry(of(Base.class, Color.Surface.class), Impl.Bindings.none()
                    .at(State.REST, "background-color", SKY)
                    .at(State.REST, "background-image", SUN + ", " + GRID)
                    .at(State.REST, "background-size", "100% 100%, 40px 40px, 40px 40px")
                    .at(State.REST, "background-repeat", "no-repeat, repeat, repeat")
                    .in(Mode.DARK, State.REST, "background-color", SKY_D)
                    .in(Mode.DARK, State.REST, "background-image", SUN_D + ", " + GRID_D)),
            one(of(Base.class, Color.Scrollbar.class), MAGENTA + " transparent", MAGENTA_D + " transparent"),
            surface(of(Raised.class, Color.Surface.class), PLATE, PLATE_D),
            Map.entry(of(Raised.class, Color.Edge.class), Impl.Bindings.none()
                    .at(State.REST, "border-color", "rgba(230, 0, 126, 0.55)").at(State.FOCUS, "border-color", CYAN)
                    .in(Mode.DARK, State.REST, "border-color", "rgba(255, 45, 149, 0.6)").in(Mode.DARK, State.FOCUS, "border-color", CYAN_D)),
            surface(of(Recessed.class, Color.Surface.class), WELL, WELL_D),
            surface(of(Inverted.class, Color.Surface.class), VOID, VOID_D),

            // ── inks: the sun for titles, the grid for links ────────────
            one(of(Body.class, Color.Ink.class), TEXT, TEXT_D),
            one(of(Heading.class, Color.Ink.class), MAGENTA, MAGENTA_D),
            one(of(Display.class, Color.Ink.class), MAGENTA, MAGENTA_D),
            one(of(Lede.class, Color.Ink.class), MUTED, MUTED_D),
            one(of(Kicker.class, Color.Ink.class), CYAN, CYAN_D),
            Map.entry(of(Link.class, Color.Ink.class), Impl.Bindings.of(CYAN).at(State.HOVER, MAGENTA)
                    .in(Mode.DARK, State.REST, CYAN_D).in(Mode.DARK, State.HOVER, MAGENTA_D)),
            surface(of(Code.class, Color.Surface.class), WELL, "#120A2A"),
            one(of(Code.class, Color.Ink.class), CYAN, CYAN_D),

            // ── emphasis ────────────────────────────────────────────────
            surface(of(Primary.class, Color.Surface.class), MAGENTA, MAGENTA_D),
            one(of(Primary.class, Color.Ink.class), MAGENTA, MAGENTA_D),
            edge(of(Primary.class, Color.Edge.class), MAGENTA, MAGENTA_D),
            one(of(OnPrimary.class, Color.Ink.class), ON_MAGENTA, ON_MAGENTA_D),
            surface(of(Secondary.class, Color.Surface.class), CYAN, CYAN_D),
            surface(of(Tertiary.class, Color.Surface.class), "#D9C8F5", "#2A1656"),
            one(of(Muted.class, Color.Ink.class), MUTED, MUTED_D),
            edge(of(Muted.class, Color.Edge.class), MUTED, MUTED_D),

            // ── pairings ────────────────────────────────────────────────
            one(of(OnInverted.class, Color.Ink.class), ON_VOID),
            states(of(OnInvertedMuted.class, Color.Ink.class), ON_VOID_MUTED, CYAN_D),
            edge(of(OnInverted.class, Color.Edge.class), "rgba(255, 45, 149, 0.5)"),

            // ── feedback: chrome yellow for the alarm, mint and hot pink ─
            surface(of(Danger.class, Color.Surface.class), "rgba(255, 56, 96, 0.14)", "rgba(255, 56, 96, 0.22)"),
            one(of(Danger.class, Color.Ink.class), "#C40036", "#FF6B8F"),
            edge(of(Danger.class, Color.Edge.class), "#FF3860"),
            surface(of(Success.class, Color.Surface.class), "rgba(61, 255, 176, 0.16)", "rgba(61, 255, 176, 0.18)"),
            scaled(of(Success.class, Color.Ink.class), "#C40036", "#FF6B8F", MUTED, MUTED_D, "#0B7A55", "#3DFFB0"),   // scales: danger's ink at -1, muted at 0
            edge(of(Success.class, Color.Edge.class), "#3DFFB0"),
            surface(of(Warning.class, Color.Surface.class), "rgba(255, 183, 0, 0.18)", "rgba(255, 183, 0, 0.22)"),
            one(of(Warning.class, Color.Ink.class), "#8A5F00", "#FFB700"),
            edge(of(Warning.class, Color.Edge.class), "#FFB700"),

            // ── interaction — Interactive and Selectable in the one word ─
            Map.entry(of(Interactive.class, Color.Surface.class), INTERACTIVE_SURFACE),
            Map.entry(of(Selectable.class,  Color.Surface.class), INTERACTIVE_SURFACE),
            Map.entry(of(Interactive.class, Color.Ink.class), INTERACTIVE_INK),
            Map.entry(of(Selectable.class,  Color.Ink.class), INTERACTIVE_INK),
            Map.entry(of(Interactive.class, Color.Edge.class), INTERACTIVE_EDGE),
            Map.entry(of(Selectable.class,  Color.Edge.class), INTERACTIVE_EDGE),
            surface(of(Selected.class, Color.Surface.class), MAGENTA, MAGENTA_D),
            one(of(Selected.class, Color.Ink.class), ON_MAGENTA, ON_MAGENTA_D),
            edge(of(Selected.class, Color.Edge.class), MAGENTA, MAGENTA_D),
            surface(of(Current.class, Color.Surface.class), "rgba(0, 146, 168, 0.10)", "rgba(0, 240, 255, 0.10)"),
            one(of(Current.class, Color.Ink.class), CYAN, CYAN_D),
            edge(of(Current.class, Color.Edge.class), CYAN, CYAN_D),
            ring(of(Focus.class, Color.Edge.class), CYAN, CYAN_D),

            // ── structure: neon rules ───────────────────────────────────
            edge(of(Divider.class, Color.Edge.class), MAGENTA, MAGENTA_D),
            edge(of(Hairline.class, Color.Edge.class), HAIR, HAIR_D),
            edge(of(Cap.class, Color.Edge.class), HAIR, HAIR_D),
            edge(of(Spine.class, Color.Edge.class), HAIR, HAIR_D),
            edge(of(Rail.class, Color.Edge.class), HAIR, HAIR_D),
            edge(of(Lattice.class, Color.Edge.class), HAIR, HAIR_D),
            surface(of(Backdrop.class, Color.Surface.class), "rgba(42, 10, 74, 0.55)", "rgba(5, 1, 15, 0.65)"),
            edgeHover(of(Marker.class, Color.Edge.class), "transparent", CYAN),
            edgeHover(of(Bar.class, Color.Edge.class), HAIR + " " + HAIR + " " + HAIR + " " + MAGENTA, HAIR + " " + HAIR + " " + HAIR + " " + CYAN),

            // ── the veil: scanlines ─────────────────────────────────────
            surfaceImage(of(Overlay.class, Color.Surface.class), "rgba(5, 1, 15, 0.55)", "repeating-linear-gradient(0deg, transparent 0 2px, rgba(0, 0, 0, 0.35) 2px 3px)"),

            // ── prose, by reference ─────────────────────────────────────
            body(of(Prose.class, Color.Ink.class), """
                color: %s;
                h1, h2, h3, h4 { color: %s; }
                a { color: %s; }
                a:hover { color: %s; }
                blockquote { color: %s; }
                code { color: %s; }
                pre { color: %s; }
                pre code { color: inherit; }
                th { color: %s; }
                """.formatted(of(Body.class, Color.Ink.class).var(), of(Heading.class, Color.Ink.class).var(),
                              of(Link.class, Color.Ink.class).var(), of(Link.class, Color.Ink.class).var(State.HOVER),
                              of(Muted.class, Color.Ink.class).var(), of(Code.class, Color.Ink.class).var(),
                              of(Kicker.class, Color.Ink.class).var(), of(OnInverted.class, Color.Ink.class).var())),
            body(of(Prose.class, Color.Surface.class), """
                code { background-color: %s; }
                pre { background-color: %s; }
                pre code { background-color: transparent; }
                th { background-color: %s; }
                tr:nth-child(even) td { background-color: %s; }
                """.formatted(of(Code.class, Color.Surface.class).var("background-color"), of(Inverted.class, Color.Surface.class).var("background-color"),
                              of(Inverted.class, Color.Surface.class).var("background-color"), of(Recessed.class, Color.Surface.class).var("background-color"))),
            body(of(Prose.class, Color.Edge.class), """
                h1, h2 { border-color: %s; }
                blockquote { border-color: %s; }
                th, td, hr { border-color: %s; }
                """.formatted(of(Divider.class, Color.Edge.class).var("border-color"), of(Primary.class, Color.Edge.class).var("border-color"), of(Hairline.class, Color.Edge.class).var("border-color")))
    );
}
