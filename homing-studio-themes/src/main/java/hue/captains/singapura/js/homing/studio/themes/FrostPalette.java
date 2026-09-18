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
 * Frost — the colours of glass. The page is an aurora: a soft gradient that
 * every plate above it blurs; the plates themselves are white at a fraction,
 * rimmed in a lighter white, so what shows through is the page tinted. Not a
 * seed palette: a gradient and translucency are not thirteen colours, so this
 * one is written out. Under any physique it is the aurora with plates of
 * glass; under {@link GlassmorphismDesign} the plates also blur.
 */
public record FrostPalette() implements Palette {

    public static final FrostPalette INSTANCE = new FrostPalette();

    public static final DesignId ID = new DesignId("frost");

    /** Glassmorphism's — the aurora and the glass are what its blur shows through; also chrome over an aurora, and Editorial's flat plates over one. */
    @Override public DesignId id() { return ID; }
    @Override public DesignId anchor() { return HomingGlassmorphism.ID; }
    @Override public Set<DesignId> compatible() { return COMPATIBLE; }
    private static final Set<DesignId> COMPATIBLE = Set.of(HomingNeoFuturism.ID, HomingEditorial.ID);
    @Override public String label() { return "Frost"; }
    @Override public String inspiration() { return "An aurora behind plates of frosted glass — white at a fraction, rimmed in light."; }

    @Override public Impl impl(DesignClass<?> pair) { return pair.onColourPlane() ? WORDS.get(pair) : null; }

    // ── the aurora, and the glass over it ────────────────────────────────
    static final String AURORA   = "linear-gradient(135deg, #C7D8F7 0%, #E9D5F4 45%, #CDEFE6 100%)";
    static final String AURORA_D = "linear-gradient(135deg, #14204A 0%, #3A1E5C 50%, #0F3A3C 100%)";
    static final String PAGE = "#DDE7F5",  PAGE_D = "#0F1630";
    static final String GLASS = "rgba(255, 255, 255, 0.32)",  GLASS_D = "rgba(255, 255, 255, 0.08)";
    static final String THIN  = "rgba(255, 255, 255, 0.18)",  THIN_D  = "rgba(255, 255, 255, 0.05)";
    static final String SMOKE = "rgba(24, 32, 64, 0.55)",     SMOKE_D = "rgba(8, 12, 30, 0.6)";
    static final String RIM   = "rgba(255, 255, 255, 0.55)",  RIM_D   = "rgba(255, 255, 255, 0.18)";
    static final String HAIR  = "rgba(27, 35, 64, 0.12)",     HAIR_D  = "rgba(255, 255, 255, 0.10)";
    static final String TEXT  = "#1B2340",  TEXT_D  = "#E8ECFA";
    static final String MUTED = "#4E5A7A",  MUTED_D = "#A5AFD0";
    static final String ON_SMOKE = "#FFFFFF", ON_SMOKE_MUTED = "rgba(255, 255, 255, 0.7)";
    static final String BLUE  = "#5B7CFF",  BLUE_D  = "#6F8CFF";      // primary
    static final String INK_BLUE = "#3452C8", INK_BLUE_D = "#9DB2FF"; // a link, a kicker
    static final String VIOLET = "#7A3FD6", VIOLET_D = "#C89BFF";
    static final String ON_BLUE = "#FFFFFF";

    // interactive: nothing at rest; hover is a breath of glass, selected a clear pane, current a thin one
    static final Impl INTERACTIVE_SURFACE = Impl.Bindings.none()
            .at(State.REST, "background-color", "transparent")
            .at(State.HOVER, "background-color", "rgba(255, 255, 255, 0.28)")
            .at(State.SELECTED, "background-color", "rgba(255, 255, 255, 0.55)")
            .at(State.CURRENT, "background-color", "rgba(255, 255, 255, 0.20)")
            .at(State.HIGHLIGHTED, "background-color", "rgba(91, 124, 255, 0.22)")
            .in(Mode.DARK, State.HOVER, "background-color", "rgba(255, 255, 255, 0.10)")
            .in(Mode.DARK, State.SELECTED, "background-color", "rgba(255, 255, 255, 0.22)")
            .in(Mode.DARK, State.CURRENT, "background-color", "rgba(255, 255, 255, 0.07)")
            .in(Mode.DARK, State.HIGHLIGHTED, "background-color", "rgba(111, 140, 255, 0.28)");
    static final Impl INTERACTIVE_INK = Impl.Bindings.of("inherit")
            .at(State.SELECTED, TEXT).at(State.CURRENT, INK_BLUE).at(State.HIGHLIGHTED, INK_BLUE)
            .in(Mode.DARK, State.SELECTED, "#FFFFFF").in(Mode.DARK, State.CURRENT, INK_BLUE_D).in(Mode.DARK, State.HIGHLIGHTED, INK_BLUE_D);
    static final Impl INTERACTIVE_EDGE = Impl.Bindings.none()
            .at(State.REST, "border-color", "transparent")
            .at(State.HOVER, "border-color", "rgba(255, 255, 255, 0.6)")
            .at(State.SELECTED, "border-color", "rgba(255, 255, 255, 0.85)")
            .at(State.CURRENT, "border-color", BLUE)
            .at(State.HIGHLIGHTED, "border-color", BLUE)
            .at(State.CHECKED, "border-color", BLUE)
            .at(State.FOCUS, "outline-color", BLUE)
            .in(Mode.DARK, State.HOVER, "border-color", "rgba(255, 255, 255, 0.25)")
            .in(Mode.DARK, State.SELECTED, "border-color", "rgba(255, 255, 255, 0.45)")
            .in(Mode.DARK, State.CURRENT, "border-color", BLUE_D)
            .in(Mode.DARK, State.HIGHLIGHTED, "border-color", BLUE_D)
            .in(Mode.DARK, State.CHECKED, "border-color", BLUE_D)
            .in(Mode.DARK, State.FOCUS, "outline-color", BLUE_D);

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // ── layers: the aurora, and glass at three thicknesses ─────
            Map.entry(of(Base.class, Color.Surface.class), Impl.Bindings.none()
                    .at(State.REST, "background-color", PAGE).at(State.REST, "background-image", AURORA)
                    .in(Mode.DARK, State.REST, "background-color", PAGE_D).in(Mode.DARK, State.REST, "background-image", AURORA_D)),
            one(of(Base.class, Color.Scrollbar.class), "rgba(27, 35, 64, 0.35) transparent", "rgba(255, 255, 255, 0.3) transparent"),
            surface(of(Raised.class, Color.Surface.class), GLASS, GLASS_D),
            Map.entry(of(Raised.class, Color.Edge.class), Impl.Bindings.none()
                    .at(State.REST, "border-color", RIM).at(State.FOCUS, "border-color", BLUE)
                    .in(Mode.DARK, State.REST, "border-color", RIM_D).in(Mode.DARK, State.FOCUS, "border-color", BLUE_D)),
            surface(of(Recessed.class, Color.Surface.class), THIN, THIN_D),
            surface(of(Inverted.class, Color.Surface.class), SMOKE, SMOKE_D),

            // ── inks ────────────────────────────────────────────────────
            one(of(Body.class, Color.Ink.class), TEXT, TEXT_D),
            one(of(Heading.class, Color.Ink.class), TEXT, "#FFFFFF"),
            one(of(Display.class, Color.Ink.class), TEXT, "#FFFFFF"),
            one(of(Lede.class, Color.Ink.class), MUTED, MUTED_D),
            one(of(Kicker.class, Color.Ink.class), INK_BLUE, INK_BLUE_D),
            Map.entry(of(Link.class, Color.Ink.class), Impl.Bindings.of(INK_BLUE).at(State.HOVER, VIOLET)
                    .in(Mode.DARK, State.REST, INK_BLUE_D).in(Mode.DARK, State.HOVER, VIOLET_D)),
            surface(of(Code.class, Color.Surface.class), THIN, THIN_D),
            one(of(Code.class, Color.Ink.class), VIOLET, VIOLET_D),

            // ── emphasis ────────────────────────────────────────────────
            surface(of(Primary.class, Color.Surface.class), BLUE, BLUE_D),
            one(of(Primary.class, Color.Ink.class), INK_BLUE, INK_BLUE_D),
            edge(of(Primary.class, Color.Edge.class), "rgba(91, 124, 255, 0.8)", "rgba(111, 140, 255, 0.8)"),
            one(of(OnPrimary.class, Color.Ink.class), ON_BLUE),
            surface(of(Secondary.class, Color.Surface.class), "#B85CFF", "#A06BFF"),
            surface(of(Tertiary.class, Color.Surface.class), "rgba(255, 255, 255, 0.4)", "rgba(255, 255, 255, 0.12)"),
            one(of(Muted.class, Color.Ink.class), MUTED, MUTED_D),
            edge(of(Muted.class, Color.Edge.class), HAIR, HAIR_D),

            // ── pairings ────────────────────────────────────────────────
            one(of(OnInverted.class, Color.Ink.class), ON_SMOKE),
            states(of(OnInvertedMuted.class, Color.Ink.class), ON_SMOKE_MUTED, ON_SMOKE),
            edge(of(OnInverted.class, Color.Edge.class), "rgba(255, 255, 255, 0.35)"),

            // ── feedback: tinted glass ──────────────────────────────────
            surface(of(Danger.class, Color.Surface.class), "rgba(255, 77, 109, 0.18)", "rgba(255, 77, 109, 0.25)"),
            one(of(Danger.class, Color.Ink.class), "#B3123A", "#FF8FA6"),
            edge(of(Danger.class, Color.Edge.class), "rgba(255, 77, 109, 0.6)"),
            surface(of(Success.class, Color.Surface.class), "rgba(35, 196, 140, 0.2)", "rgba(35, 196, 140, 0.25)"),
            one(of(Success.class, Color.Ink.class), "#0E7A55", "#7BE8C0"),
            edge(of(Success.class, Color.Edge.class), "rgba(35, 196, 140, 0.6)"),
            surface(of(Warning.class, Color.Surface.class), "rgba(255, 190, 60, 0.25)", "rgba(255, 190, 60, 0.28)"),
            one(of(Warning.class, Color.Ink.class), "#8A5A00", "#FFD37A"),
            edge(of(Warning.class, Color.Edge.class), "rgba(255, 190, 60, 0.6)"),

            // ── interaction — Interactive and Selectable in the one word ─
            Map.entry(of(Interactive.class, Color.Surface.class), INTERACTIVE_SURFACE),
            Map.entry(of(Selectable.class,  Color.Surface.class), INTERACTIVE_SURFACE),
            Map.entry(of(Interactive.class, Color.Ink.class), INTERACTIVE_INK),
            Map.entry(of(Selectable.class,  Color.Ink.class), INTERACTIVE_INK),
            Map.entry(of(Interactive.class, Color.Edge.class), INTERACTIVE_EDGE),
            Map.entry(of(Selectable.class,  Color.Edge.class), INTERACTIVE_EDGE),
            surface(of(Selected.class, Color.Surface.class), "rgba(255, 255, 255, 0.55)", "rgba(255, 255, 255, 0.22)"),
            one(of(Selected.class, Color.Ink.class), TEXT, "#FFFFFF"),
            edge(of(Selected.class, Color.Edge.class), "rgba(255, 255, 255, 0.85)", "rgba(255, 255, 255, 0.45)"),
            surface(of(Current.class, Color.Surface.class), "rgba(255, 255, 255, 0.2)", "rgba(255, 255, 255, 0.07)"),
            one(of(Current.class, Color.Ink.class), INK_BLUE, INK_BLUE_D),
            edge(of(Current.class, Color.Edge.class), BLUE, BLUE_D),
            edge(of(Focus.class, Color.Edge.class), BLUE, BLUE_D),

            // ── structure: rims of light, hairlines of shadow ───────────
            edge(of(Divider.class, Color.Edge.class), "rgba(255, 255, 255, 0.6)", "rgba(255, 255, 255, 0.2)"),
            edge(of(Hairline.class, Color.Edge.class), HAIR, HAIR_D),
            edge(of(Cap.class, Color.Edge.class), RIM, RIM_D),
            edge(of(Spine.class, Color.Edge.class), HAIR, HAIR_D),
            edge(of(Rail.class, Color.Edge.class), HAIR, HAIR_D),
            edgeHover(of(Marker.class, Color.Edge.class), "transparent", BLUE),
            edgeHover(of(Bar.class, Color.Edge.class), RIM + " " + RIM + " " + RIM + " " + BLUE, RIM + " " + RIM + " " + RIM + " " + VIOLET),

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
                              of(OnInverted.class, Color.Ink.class).var(), of(Kicker.class, Color.Ink.class).var())),
            body(of(Prose.class, Color.Surface.class), """
                code { background-color: %s; }
                pre { background-color: %s; }
                pre code { background-color: transparent; }
                th { background-color: %s; }
                tr:nth-child(even) td { background-color: %s; }
                """.formatted(of(Recessed.class, Color.Surface.class).var("background-color"), of(Inverted.class, Color.Surface.class).var("background-color"),
                              of(Recessed.class, Color.Surface.class).var("background-color"), of(Recessed.class, Color.Surface.class).var("background-color"))),
            body(of(Prose.class, Color.Edge.class), """
                h1, h2 { border-color: %s; }
                blockquote { border-color: %s; }
                th, td, hr { border-color: %s; }
                """.formatted(of(Divider.class, Color.Edge.class).var("border-color"), of(Primary.class, Color.Edge.class).var("border-color"), of(Hairline.class, Color.Edge.class).var("border-color")))
    );
}
