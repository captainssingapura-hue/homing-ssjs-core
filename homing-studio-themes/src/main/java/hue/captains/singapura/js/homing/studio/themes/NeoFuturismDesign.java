package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.Mode;
import hue.captains.singapura.js.homing.design.State;

import java.util.Map;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Brand.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Feedback.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Pairing.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;
import static hue.captains.singapura.js.homing.studio.themes.Bind.*;

/**
 * Neo-Futurism over the house design: cool glass and deep space. Surfaces
 * are near-white in light and near-black in dark, every raised thing a thin
 * luminous plate; the one colour is an electric cyan that reads as light
 * rather than paint — hairlines, focus and hover all glow with it, and the
 * primary action is a solid bar of it with ink on top. Violet is the second
 * voice, magenta the alarm. Type is a wide geometric display set light and
 * tracked, over a quiet humanist body. Motion is a slow decelerating ease;
 * nothing snaps. Said as bindings — the design's own word where it has one,
 * Default's for the rest — and never as a class name of any component.
 */
final class NeoFuturismDesign {

    private NeoFuturismDesign() {}

    // ── the palette, light / dark ─────────────────────────────────────
    static final String SURFACE  = "#F3F6FB", SURFACE_D  = "#070A12";
    static final String RAISED   = "#FFFFFF", RAISED_D   = "#0E1424";
    static final String RECESSED = "#E8EDF6", RECESSED_D = "#0A0F1C";
    static final String INVERTED = "#0B1020", INVERTED_D = "#0B1020";   // the masthead stays space in both modes; the filament divides it
    static final String TEXT     = "#141B2D", TEXT_D     = "#DCE6F5";
    static final String MUTED    = "#5E6B85", MUTED_D    = "#7C8AA6";
    static final String ON_INVERTED = "#E8F1FF", ON_INVERTED_D = "#E8F1FF";
    static final String ON_INVERTED_MUTED = "#8FA6C8", ON_INVERTED_MUTED_D = "#8FA6C8";
    static final String BORDER   = "#D6DEEB", BORDER_D   = "#1E2A44";

    static final String CYAN     = "#00C2E0", CYAN_D     = "#00E5FF";   // the light
    static final String CYAN_INK = "#007F94", CYAN_INK_D = "#5CF0FF";   // cyan you can read
    static final String ON_CYAN  = "#04121A";
    static final String VIOLET   = "#6C4DFF", VIOLET_D   = "#8B6CFF";
    static final String MAGENTA  = "#E5177A", MAGENTA_D  = "#FF2D95";
    static final String MINT     = "#00A878", MINT_D     = "#00FFA3";
    static final String AMBER    = "#E09B00", AMBER_D    = "#FFC53D";

    static final String DISPLAY_FACE = "\"Bahnschrift\", \"Eurostile\", \"Avenir Next\", \"Futura\", \"Segoe UI\", sans-serif";
    static final String BODY_FACE    = "\"Segoe UI Variable Text\", \"Segoe UI\", \"Inter\", system-ui, sans-serif";

    static final String EASE = "transform 220ms cubic-bezier(0.2, 0.8, 0.2, 1), box-shadow 220ms cubic-bezier(0.2, 0.8, 0.2, 1), "
                             + "border-color 220ms ease, background-color 220ms ease, color 160ms ease";

    /** A tint of the light: {@code color-mix} of cyan into transparent, at a percentage. */
    static String glow(String cyan, int pct) { return "color-mix(in srgb, " + cyan + " " + pct + "%, transparent)"; }
    /** A luminous plate: a hairline of the light around, and a soft drop beneath. */
    static String plate(String cyan, int ring, String drop) { return "0 0 0 1px " + glow(cyan, ring) + ", " + drop; }

    // ── the palette, by reference: the light is whatever the palette makes primary; the plate and the glow are that light,
    //    and the drop beneath is the palette's space (its inverted surface), thinned in daylight and deep at night ──
    static final String LIGHT_REF = of(Primary.class, Color.Surface.class).var("background-color");
    static final String SPACE_REF = of(Inverted.class, Color.Surface.class).var("background-color");
    static final String DROP   = "0 12px 32px " + glow(SPACE_REF, 10);
    static final String DROP_D = "0 12px 32px " + glow(SPACE_REF, 55);

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // ── layers: glass over space ────────────────────────────────
            surface(of(Base.class, Color.Surface.class), SURFACE, SURFACE_D),
            one(of(Base.class, Color.Scrollbar.class), glow(CYAN, 45) + " transparent", glow(CYAN_D, 45) + " transparent"),
            rule(of(Base.class, Shape.Rule.class), "0", "none"),
            one(of(Base.class, Shape.Corner.class), "0"),
            surface(of(Raised.class, Color.Surface.class), RAISED, RAISED_D),
            edgeFocus(of(Raised.class, Color.Edge.class), BORDER, CYAN),
            rule(of(Raised.class, Shape.Rule.class), "1px", "solid"),
            one(of(Raised.class, Shape.Corner.class), "2px"),
            Map.entry(of(Raised.class, Shape.Shadow.class), Impl.Bindings
                    .of(plate(LIGHT_REF, 14, DROP))
                    .at(State.FOCUS, "0 0 0 3px " + glow(LIGHT_REF, 35))
                    .in(Mode.DARK, State.REST, plate(LIGHT_REF, 18, DROP_D))),
            surface(of(Recessed.class, Color.Surface.class), RECESSED, RECESSED_D),
            one(of(Recessed.class, Shape.Corner.class), "2px"),
            surface(of(Inverted.class, Color.Surface.class), INVERTED, INVERTED_D),

            // ── text: wide and light on top, quiet beneath ──────────────
            one(of(Body.class, Color.Ink.class), TEXT, TEXT_D),
            one(of(Body.class, Type.Face.class), BODY_FACE),
            one(of(Heading.class, Color.Ink.class), TEXT, TEXT_D),
            one(of(Heading.class, Type.Face.class), DISPLAY_FACE),
            one(of(Heading.class, Type.Weight.class), "600"),
            one(of(Display.class, Color.Ink.class), TEXT, TEXT_D),
            one(of(Display.class, Type.Face.class), DISPLAY_FACE),
            one(of(Display.class, Type.Weight.class), "300"),
            scale(of(Display.class, Type.Scale.class), "46px", "1.05"),
            treatment(of(Display.class, Type.Treatment.class), "0.08em", "uppercase", null),
            one(of(Lede.class, Color.Ink.class), MUTED, MUTED_D),
            treatment(of(Lede.class, Type.Treatment.class), "0.01em", null, null),
            one(of(Kicker.class, Color.Ink.class), CYAN_INK, CYAN_INK_D),
            one(of(Kicker.class, Type.Face.class), DISPLAY_FACE),
            one(of(Kicker.class, Type.Weight.class), "600"),
            treatment(of(Kicker.class, Type.Treatment.class), "0.22em", "uppercase", null),
            treatment(of(Caption.class, Type.Treatment.class), "0.02em", null, null),
            one(of(Numeral.class, Type.Face.class), DISPLAY_FACE),
            one(of(Numeral.class, Type.Weight.class), "300"),
            treatment(of(Numeral.class, Type.Treatment.class), "0.04em", null, null),
            one(of(House.class, Type.Face.class), DISPLAY_FACE),
            treatment(of(House.class, Type.Treatment.class), "0.18em", "uppercase", null),
            states(of(Link.class, Color.Ink.class), CYAN_INK, VIOLET),
            decoration(of(Link.class, Type.Decoration.class), "none"),
            one(of(Code.class, Color.Ink.class), VIOLET, VIOLET_D),
            surface(of(Code.class, Color.Surface.class), RECESSED, RECESSED_D),
            one(of(Code.class, Shape.Corner.class), "2px"),

            // ── emphasis: the light, and the second voice ───────────────
            surface(of(Primary.class, Color.Surface.class), CYAN, CYAN_D),
            one(of(Primary.class, Color.Ink.class), CYAN_INK, CYAN_INK_D),
            edge(of(Primary.class, Color.Edge.class), CYAN, CYAN_D),
            one(of(OnPrimary.class, Color.Ink.class), ON_CYAN),
            surface(of(Secondary.class, Color.Surface.class), VIOLET, VIOLET_D),
            surface(of(Tertiary.class, Color.Surface.class), BORDER, BORDER_D),
            one(of(Muted.class, Color.Ink.class), MUTED, MUTED_D),
            edge(of(Muted.class, Color.Edge.class), BORDER, BORDER_D),
            one(of(Muted.class, Effect.Opacity.class), "0.55"),

            // ── pairings ────────────────────────────────────────────────
            one(of(OnInverted.class, Color.Ink.class), ON_INVERTED, ON_INVERTED_D),
            states(of(OnInvertedMuted.class, Color.Ink.class), ON_INVERTED_MUTED, CYAN_D),
            edge(of(OnInverted.class, Color.Edge.class), glow(CYAN_D, 30), glow(CYAN_D, 35)),

            // ── feedback: lit from within ───────────────────────────────
            surface(of(Danger.class, Color.Surface.class), glow(MAGENTA, 10), glow(MAGENTA_D, 14)),
            one(of(Danger.class, Color.Ink.class), MAGENTA, MAGENTA_D),
            edge(of(Danger.class, Color.Edge.class), glow(MAGENTA, 45) + " " + glow(MAGENTA, 45) + " " + glow(MAGENTA, 45) + " " + MAGENTA,
                                                     glow(MAGENTA_D, 45) + " " + glow(MAGENTA_D, 45) + " " + glow(MAGENTA_D, 45) + " " + MAGENTA_D),
            surface(of(Success.class, Color.Surface.class), glow(MINT, 12), glow(MINT_D, 12)),
            one(of(Success.class, Color.Ink.class), MINT, MINT_D),
            edge(of(Success.class, Color.Edge.class), glow(MINT, 45), glow(MINT_D, 45)),
            surface(of(Warning.class, Color.Surface.class), glow(AMBER, 12), glow(AMBER_D, 12)),
            one(of(Warning.class, Color.Ink.class), AMBER, AMBER_D),
            edge(of(Warning.class, Color.Edge.class), glow(AMBER, 45), glow(AMBER_D, 45)),

            // ── interaction: the glow comes up ──────────────────────────
            one(of(Interactive.class, Motion.Ease.class), EASE),
            // the glow comes up: hover faintly, selected fully, highlighted in between; a row glows by filter
            Map.entry(of(Interactive.class, Motion.Transform.class), Impl.Bindings.none()
                    .at(State.HOVER, "translateY(-1px) scale(1.01)")
                    .at(State.ACTIVE, "scale(0.985)")
                    .at(State.SELECTED, "translateY(-1px)")
                    .at(State.HIGHLIGHTED, "translateY(-1px)")),
            Map.entry(of(Interactive.class, Shape.Shadow.class), Impl.Bindings
                    .of(plate(LIGHT_REF, 14, DROP))
                    .at(State.HOVER, "0 0 0 1px " + glow(LIGHT_REF, 60) + ", 0 0 28px " + glow(LIGHT_REF, 30) + ", " + DROP)
                    .at(State.ACTIVE, "0 0 0 1px " + glow(LIGHT_REF, 60) + ", 0 0 10px " + glow(LIGHT_REF, 25))
                    .at(State.SELECTED, "0 0 0 1px " + LIGHT_REF + ", 0 0 36px " + glow(LIGHT_REF, 45) + ", " + DROP)
                    .at(State.HIGHLIGHTED, "0 0 0 1px " + glow(LIGHT_REF, 70) + ", 0 0 20px " + glow(LIGHT_REF, 35))
                    .in(Mode.DARK, State.REST, plate(LIGHT_REF, 18, DROP_D))
                    .in(Mode.DARK, State.HOVER, "0 0 0 1px " + glow(LIGHT_REF, 70) + ", 0 0 32px " + glow(LIGHT_REF, 35) + ", " + DROP_D)),
            Map.entry(of(Interactive.class, Effect.Filter.class), Impl.Bindings.none()
                    .at(State.SELECTED, "filter", "drop-shadow(0 0 14px " + glow(LIGHT_REF, 60) + ")")
                    .at(State.HIGHLIGHTED, "filter", "drop-shadow(0 0 10px " + glow(LIGHT_REF, 45) + ")")),
            // nothing at rest; hover lights faintly, selected is the light itself, current a filament
            Map.entry(of(Interactive.class, Color.Surface.class), Impl.Bindings.none()
                    .at(State.REST, "background-color", "transparent")
                    .at(State.HOVER, "background-color", glow(CYAN, 10))
                    .at(State.SELECTED, "background-color", CYAN)
                    .at(State.CURRENT, "background-color", glow(CYAN, 6))
                    .at(State.HIGHLIGHTED, "background-color", glow(CYAN, 22))
                    .in(Mode.DARK, State.HIGHLIGHTED, "background-color", glow(CYAN_D, 24))
                    .in(Mode.DARK, State.HOVER, "background-color", glow(CYAN_D, 12))
                    .in(Mode.DARK, State.SELECTED, "background-color", CYAN_D)
                    .in(Mode.DARK, State.CURRENT, "background-color", glow(CYAN_D, 8))),
            Map.entry(of(Interactive.class, Color.Ink.class), Impl.Bindings.of("inherit")
                    .at(State.SELECTED, ON_CYAN).at(State.CURRENT, CYAN_INK)
                    .at(State.HIGHLIGHTED, CYAN_INK).in(Mode.DARK, State.HIGHLIGHTED, CYAN_INK_D)
                    .in(Mode.DARK, State.CURRENT, CYAN_INK_D)),
            Map.entry(of(Interactive.class, Color.Edge.class), Impl.Bindings.none()
                    .at(State.REST, "border-color", "transparent")
                    .at(State.HOVER, "border-color", glow(CYAN, 35))
                    .at(State.SELECTED, "border-color", CYAN)
                    .at(State.CURRENT, "border-color", glow(CYAN, 55))
                    .at(State.HIGHLIGHTED, "border-color", CYAN)
                    .in(Mode.DARK, State.HIGHLIGHTED, "border-color", CYAN_D)
                    .at(State.CHECKED, "border-color", CYAN)
                    .at(State.FOCUS, "outline-color", CYAN)
                    .in(Mode.DARK, State.HOVER, "border-color", glow(CYAN_D, 40))
                    .in(Mode.DARK, State.SELECTED, "border-color", CYAN_D)
                    .in(Mode.DARK, State.CURRENT, "border-color", glow(CYAN_D, 55))
                    .in(Mode.DARK, State.CHECKED, "border-color", CYAN_D)
                    .in(Mode.DARK, State.FOCUS, "outline-color", CYAN_D)),
            Map.entry(of(Interactive.class, Shape.Rule.class), Impl.Bindings.none()
                    .at(State.REST, "border-width", "1px").at(State.REST, "border-style", "solid")
                    .at(State.FOCUS, "outline-width", "1px").at(State.FOCUS, "outline-style", "solid").at(State.FOCUS, "outline-offset", "-1px")),
            surface(of(Selected.class, Color.Surface.class), CYAN, CYAN_D),
            one(of(Selected.class, Color.Ink.class), ON_CYAN),
            edge(of(Selected.class, Color.Edge.class), CYAN, CYAN_D),
            surface(of(Current.class, Color.Surface.class), glow(CYAN, 8), glow(CYAN_D, 10)),
            one(of(Current.class, Color.Ink.class), CYAN_INK, CYAN_INK_D),
            edge(of(Current.class, Color.Edge.class), glow(CYAN, 50), glow(CYAN_D, 55)),
            // the Selected semantic in depth — the light itself, glowing
            one(of(Selected.class, Shape.Shadow.class), "0 0 0 1px " + LIGHT_REF + ", 0 0 36px " + glow(LIGHT_REF, 45)),
            one(of(Selected.class, Motion.Transform.class), "translateY(-1px)"),
            Map.entry(of(Selected.class, Effect.Filter.class), Impl.Bindings.none().at(State.REST, "filter", "drop-shadow(0 0 14px " + glow(LIGHT_REF, 60) + ")")),
            one(of(Current.class, Shape.Shadow.class), "inset 2px 0 0 " + LIGHT_REF + ", 0 0 16px " + glow(LIGHT_REF, 20)),

            // ── structure: every line is a filament ─────────────────────
            edge(of(Divider.class, Color.Edge.class), glow(CYAN, 55), glow(CYAN_D, 55)),
            rule(of(Divider.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            edge(of(Hairline.class, Color.Edge.class), BORDER, BORDER_D),
            rule(of(Hairline.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            edge(of(Cap.class, Color.Edge.class), glow(CYAN, 35), glow(CYAN_D, 35)),
            rule(of(Cap.class, Shape.Rule.class), "1px 0 0 0", "solid"),
            edge(of(Spine.class, Color.Edge.class), BORDER, BORDER_D),
            rule(of(Spine.class, Shape.Rule.class), "0 0 0 1px", "solid"),
            edgeHover(of(Marker.class, Color.Edge.class), "transparent", CYAN),
            rule(of(Marker.class, Shape.Rule.class), "0 0 0 2px", "solid"),
            edgeHover(of(Bar.class, Color.Edge.class), BORDER + " " + BORDER + " " + BORDER + " " + CYAN, BORDER + " " + BORDER + " " + BORDER + " " + VIOLET),
            rule(of(Bar.class, Shape.Rule.class), "1px 1px 1px 3px", "solid"),
            edge(of(Rail.class, Color.Edge.class), BORDER, BORDER_D),
            rule(of(Rail.class, Shape.Rule.class), "0 1px 0 0", "solid"),

            // ── the veil: frosted space ─────────────────────────────────
            one(of(Overlay.class, Shape.Shadow.class), "0 24px 64px " + glow(SPACE_REF, 35) + ", 0 0 0 1px " + glow(LIGHT_REF, 30), "0 24px 64px " + glow(SPACE_REF, 70) + ", 0 0 0 1px " + glow(LIGHT_REF, 35)),
            Map.entry(of(Overlay.class, Effect.Filter.class), Impl.Bindings.none().at(State.REST, "backdrop-filter", "blur(14px) saturate(1.4) brightness(0.7)")),
            edge(of(Focus.class, Color.Edge.class), CYAN, CYAN_D),
            one(of(Focus.class, Shape.Shadow.class), "0 24px 64px " + glow(SPACE_REF, 35) + ", 0 0 0 1px " + LIGHT_REF + ", 0 0 40px " + glow(LIGHT_REF, 35),
                                                     "0 24px 64px " + glow(SPACE_REF, 70) + ", 0 0 0 1px " + LIGHT_REF + ", 0 0 48px " + glow(LIGHT_REF, 40)),
            one(of(Inert.class, Effect.Opacity.class), "0.4"),
            one(of(Inert.class, Affordance.Cursor.class), "not-allowed"),

            // ── boxes ───────────────────────────────────────────────────
            one(of(Control.class, Shape.Corner.class), "2px"),
            rule(of(Control.class, Shape.Rule.class), "1px", "solid"),
            one(of(Inline.class, Shape.Corner.class), "1px"),
            rule(of(Inline.class, Shape.Rule.class), "1px", "solid"),

            // ── prose under the light — by reference, so it follows the palette worn ────────────────────────────────────────
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
            body(of(Prose.class, Type.Face.class), """
                h1, h2, h3, h4 { font-family: %s; }
                code, pre { font-family: %s; }
                """.formatted(DISPLAY_FACE, DefaultDesign.MONO_FACE)),
            body(of(Prose.class, Type.Weight.class), "h1, h2 { font-weight: 300; }\nh3, h4, th { font-weight: 600; }\n"),
            body(of(Prose.class, Type.Treatment.class), """
                h1, h2 { letter-spacing: 0.06em; text-transform: uppercase; }
                h4 { letter-spacing: 0.18em; text-transform: uppercase; }
                """),
            body(of(Prose.class, Type.Decoration.class), "a { text-decoration-line: underline; text-underline-offset: 3px; text-decoration-thickness: 1px; }\n"),
            body(of(Prose.class, Color.Surface.class), """
                code { background-color: %s; }
                pre { background-color: %s; }
                pre code { background-color: transparent; }
                th { background-color: %s; }
                tr:nth-child(even) td { background-color: %s; }
                """.formatted(of(Recessed.class, Color.Surface.class).var("background-color"), SPACE_REF, glow(LIGHT_REF, 10),
                              of(Recessed.class, Color.Surface.class).var("background-color"))),
            body(of(Prose.class, Color.Edge.class), """
                h1, h2 { border-color: %s; }
                blockquote { border-color: %s; }
                th, td, hr { border-color: %s; }
                """.formatted(glow(LIGHT_REF, 55), of(Primary.class, Color.Edge.class).var("border-color"), of(Hairline.class, Color.Edge.class).var("border-color"))),
            body(of(Prose.class, Shape.Rule.class), """
                h1, h2 { border-width: 0 0 1px 0; border-style: solid; }
                blockquote { border-width: 0 0 0 2px; border-style: solid; }
                th, td { border-width: 0 0 1px 0; border-style: solid; }
                hr { border-width: 1px 0 0 0; border-style: solid; }
                """),
            body(of(Prose.class, Shape.Corner.class), "code, pre { border-radius: 2px; }\n")
    );
}
