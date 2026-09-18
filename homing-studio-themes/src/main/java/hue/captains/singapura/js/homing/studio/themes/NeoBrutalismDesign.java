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
 * Neo-Brutalism over the house design: ink and paper, a signal yellow, riso
 * blue and red; every edge a thick black rule, every corner square, every
 * raised thing on a hard offset shadow; the press is a two-step snap. Said
 * as bindings — the design's own word where it has one, Default's for the
 * rest — and never as a class name of any component.
 */
final class NeoBrutalismDesign {

    private NeoBrutalismDesign() {}

    static final String INK = "#000000", INK_D = "#FFFFFF";
    static final String PAPER = "#FFFFFF", PAPER_D = "#0B0B0B";
    static final String GREY = "#EFEFEF", GREY_D = "#1A1A1A";
    static final String MUTED = "#4A4A4A", MUTED_D = "#B8B8B8";
    static final String SIGNAL = "#FFE800";
    static final String RISO_BLUE = "#2B4CFF";
    static final String RISO_RED = "#FF3B21";
    static final String RULE = "#000000", RULE_D = "#FFFFFF";
    static final String DISPLAY_FACE = "\"Arial Black\", \"Impact\", \"Helvetica Neue\", Arial, sans-serif";
    static final String SNAP = "transform 70ms steps(2, end), box-shadow 70ms steps(2, end), background-color 70ms steps(2, end)";

    // ── the palette, by reference: the shadow is the ink, offset; the ring is the focus edge ──
    static final String INK_REF   = of(Body.class, Color.Ink.class).var();
    static final String FOCUS_REF = of(Focus.class, Color.Edge.class).var("border-color");

    static String shadow(int px) { return px + "px " + px + "px 0 " + INK_REF; }

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // ── layers: paper, and everything edged in ink ──────────────
            surface(of(Base.class, Color.Surface.class), PAPER, PAPER_D),
            one(of(Base.class, Color.Scrollbar.class), INK + " " + PAPER, INK_D + " " + PAPER_D),
            rule(of(Base.class, Shape.Rule.class), "0", "none"),
            one(of(Base.class, Shape.Corner.class), "0"),
            surface(of(Raised.class, Color.Surface.class), PAPER, PAPER_D),
            edge(of(Raised.class, Color.Edge.class), RULE, RULE_D),
            ruleWithFocusRing(of(Raised.class, Shape.Rule.class), "3px", "solid", "4px", "4px"),
            one(of(Raised.class, Shape.Corner.class), "0"),
            one(of(Raised.class, Shape.Shadow.class), shadow(5)),
            surface(of(Recessed.class, Color.Surface.class), GREY, GREY_D),
            one(of(Recessed.class, Shape.Corner.class), "0"),
            surface(of(Inverted.class, Color.Surface.class), INK, "#FFFFFF"),

            // ── text: black, heavy, tight ───────────────────────────────
            one(of(Body.class, Color.Ink.class), INK, INK_D),
            one(of(Heading.class, Color.Ink.class), INK, INK_D),
            one(of(Heading.class, Type.Face.class), DISPLAY_FACE),
            one(of(Heading.class, Type.Weight.class), "900"),
            one(of(Display.class, Color.Ink.class), INK, INK_D),
            one(of(Display.class, Type.Face.class), DISPLAY_FACE),
            one(of(Display.class, Type.Weight.class), "900"),
            treatment(of(Display.class, Type.Treatment.class), "-0.03em", "uppercase", null),
            one(of(Lede.class, Color.Ink.class), MUTED, MUTED_D),
            treatment(of(Lede.class, Type.Treatment.class), null, null, null),
            one(of(Kicker.class, Color.Ink.class), INK, INK_D),
            one(of(Kicker.class, Type.Face.class), DISPLAY_FACE),
            one(of(Kicker.class, Type.Weight.class), "900"),
            treatment(of(Kicker.class, Type.Treatment.class), "0.04em", "uppercase", null),
            one(of(Numeral.class, Type.Face.class), DISPLAY_FACE),
            one(of(Numeral.class, Type.Weight.class), "900"),
            one(of(House.class, Type.Face.class), DISPLAY_FACE),
            treatment(of(House.class, Type.Treatment.class), "-0.02em", "uppercase", null),
            states(of(Link.class, Color.Ink.class), RISO_BLUE, RISO_RED),
            surface(of(Code.class, Color.Surface.class), GREY, GREY_D),
            one(of(Code.class, Color.Ink.class), INK, INK_D),
            one(of(Code.class, Shape.Corner.class), "0"),

            // ── emphasis: the signal ────────────────────────────────────
            surface(of(Primary.class, Color.Surface.class), SIGNAL),
            one(of(Primary.class, Color.Ink.class), RISO_RED),
            one(of(OnPrimary.class, Color.Ink.class), INK),
            surface(of(Secondary.class, Color.Surface.class), RISO_RED),
            surface(of(Tertiary.class, Color.Surface.class), GREY, GREY_D),
            one(of(Muted.class, Color.Ink.class), MUTED, MUTED_D),
            edge(of(Muted.class, Color.Edge.class), INK, INK_D),

            // ── pairings ────────────────────────────────────────────────
            one(of(OnInverted.class, Color.Ink.class), PAPER, INK),
            states(of(OnInvertedMuted.class, Color.Ink.class), "#D9D9D9", SIGNAL),

            // ── feedback: flat, loud ────────────────────────────────────
            surface(of(Danger.class, Color.Surface.class), RISO_RED),
            one(of(Danger.class, Color.Ink.class), INK),
            edge(of(Danger.class, Color.Edge.class), INK),
            surface(of(Success.class, Color.Surface.class), "#7CFF6B"),
            one(of(Success.class, Color.Ink.class), INK),
            edge(of(Success.class, Color.Edge.class), INK),
            surface(of(Warning.class, Color.Surface.class), SIGNAL),
            one(of(Warning.class, Color.Ink.class), INK),
            edge(of(Warning.class, Color.Edge.class), INK),

            // ── interaction: the press ──────────────────────────────────
            one(of(Interactive.class, Motion.Ease.class), SNAP),
            Map.entry(of(Interactive.class, Motion.Transform.class), Impl.Bindings.none()
                    .at(State.HOVER, "translate(-2px, -2px)")
                    .at(State.ACTIVE, "translate(5px, 5px)")),
            states(of(Interactive.class, Shape.Shadow.class), shadow(4), shadow(8), "0 0 0 " + INK_REF),
            surface(of(Interactive.class, Color.Surface.class), SIGNAL),
            Map.entry(of(Interactive.class, Color.Edge.class), Impl.Bindings.none()
                    .at(State.REST, "border-color", INK).at(State.CHECKED, "border-color", RISO_BLUE)
                    .in(Mode.DARK, State.REST, "border-color", INK_D).in(Mode.DARK, State.CHECKED, "border-color", RISO_BLUE)),
            surface(of(Selected.class, Color.Surface.class), INK, "#FFFFFF"),
            one(of(Selected.class, Color.Ink.class), SIGNAL, INK),
            edge(of(Selected.class, Color.Edge.class), INK, "#FFFFFF"),
            surface(of(Current.class, Color.Surface.class), SIGNAL),
            one(of(Current.class, Color.Ink.class), INK),
            edge(of(Current.class, Color.Edge.class), INK),
            one(of(Current.class, Shape.Shadow.class), "inset 6px 0 0 " + INK_REF),

            // ── structure: every line is a rule ─────────────────────────
            edge(of(Divider.class, Color.Edge.class), RULE, RULE_D),
            rule(of(Divider.class, Shape.Rule.class), "0 0 4px 0", "solid"),
            edge(of(Hairline.class, Color.Edge.class), RULE, RULE_D),
            rule(of(Hairline.class, Shape.Rule.class), "0 0 2px 0", "solid"),
            edge(of(Cap.class, Color.Edge.class), RULE, RULE_D),
            rule(of(Cap.class, Shape.Rule.class), "3px 0 0 0", "solid"),
            edge(of(Spine.class, Color.Edge.class), RULE, RULE_D),
            rule(of(Spine.class, Shape.Rule.class), "0 0 0 3px", "solid"),
            edgeHover(of(Marker.class, Color.Edge.class), "transparent", INK),
            rule(of(Marker.class, Shape.Rule.class), "0 0 0 4px", "solid"),
            edge(of(Bar.class, Color.Edge.class), RULE, RULE_D),
            rule(of(Bar.class, Shape.Rule.class), "3px 3px 3px 8px", "solid"),

            edge(of(Rail.class, Color.Edge.class), RULE, RULE_D),
            rule(of(Rail.class, Shape.Rule.class), "0 3px 0 0", "solid"),
            edge(of(OnInverted.class, Color.Edge.class), PAPER, INK),
            edge(of(Primary.class, Color.Edge.class), INK),
            one(of(Overlay.class, Shape.Shadow.class), shadow(12)),
            Map.entry(of(Overlay.class, Effect.Filter.class), Impl.Bindings.none().at(State.REST, "backdrop-filter", "grayscale(1) contrast(1.4)")),
            surfaceImage(of(Overlay.class, Color.Surface.class), "transparent", "repeating-linear-gradient(45deg, color-mix(in srgb, " + INK + " 22%, transparent) 0 7px, transparent 7px 14px)"),
            edge(of(Focus.class, Color.Edge.class), RISO_BLUE),
            one(of(Focus.class, Shape.Shadow.class), shadow(12) + ", 0 0 0 4px " + FOCUS_REF),
            one(of(Inert.class, Effect.Opacity.class), "0.4"),
            one(of(Inert.class, Affordance.Cursor.class), "not-allowed"),

            // ── boxes ───────────────────────────────────────────────────
            one(of(Control.class, Shape.Corner.class), "0"),
            rule(of(Control.class, Shape.Rule.class), "3px", "solid"),
            one(of(Inline.class, Shape.Corner.class), "0"),
            rule(of(Inline.class, Shape.Rule.class), "2px", "solid"),

            // ── prose in ink ────────────────────────────────────────────
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
                """.formatted(INK, INK, RISO_BLUE, RISO_RED, MUTED, INK, PAPER, PAPER)),
            body(of(Prose.class, Type.Face.class), """
                h1, h2, h3, h4 { font-family: %s; }
                code, pre { font-family: %s; }
                """.formatted(DISPLAY_FACE, DefaultDesign.MONO_FACE)),
            body(of(Prose.class, Type.Weight.class), "h1, h2, h3, h4, th { font-weight: 900; }\n"),
            body(of(Prose.class, Type.Decoration.class), "a { text-decoration-line: underline; text-underline-offset: 3px; text-decoration-thickness: 3px; }\n"),
            body(of(Prose.class, Color.Surface.class), """
                code { background-color: %s; }
                pre { background-color: %s; }
                pre code { background-color: transparent; }
                th { background-color: %s; }
                tr:nth-child(even) td { background-color: %s; }
                """.formatted(GREY, INK, INK, GREY)),
            body(of(Prose.class, Color.Edge.class), """
                h1, blockquote, th, td, hr { border-color: %s; }
                """.formatted(INK)),
            body(of(Prose.class, Shape.Rule.class), """
                h1 { border-width: 0 0 4px 0; border-style: solid; }
                blockquote { border-width: 0 0 0 6px; border-style: solid; }
                th, td { border-width: 0 0 2px 0; border-style: solid; }
                th { border-width: 0; }
                hr { border-width: 3px 0 0 0; border-style: solid; }
                """),
            body(of(Prose.class, Shape.Corner.class), "code, pre { border-radius: 0; }\n")
    );
}
