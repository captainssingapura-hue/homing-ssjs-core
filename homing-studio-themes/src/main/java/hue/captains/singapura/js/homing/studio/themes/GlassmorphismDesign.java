package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.State;

import java.util.Map;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Brand.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;
import static hue.captains.singapura.js.homing.studio.themes.Bind.*;

/**
 * Glassmorphism — plates of frosted glass over whatever the page is. The
 * physique is the frosting: every raised plate and the masthead blur what is
 * behind them, are rimmed by a hairline, sit on a soft wide shadow and have
 * their corners well rounded. Depth is a lift on a longer shadow; a selected
 * thing is a clearer pane, lit along its top edge. Type is a clean
 * grotesque, headings a touch tighter.
 *
 * <p>A physique only: the blur, the rim width and the shadow's softness are
 * its; what the glass is made of — how white, how thick, over what — is the
 * palette's, and {@link FrostPalette} is the one it is worn in by default.
 * Any palette makes glass under it; the aurora is Frost's.</p>
 */
final class GlassmorphismDesign {

    private GlassmorphismDesign() {}

    static final String DISPLAY_FACE = "\"Inter\", \"Segoe UI Variable Display\", \"Segoe UI\", system-ui, sans-serif";
    static final String BODY_FACE    = "\"Inter\", \"Segoe UI Variable Text\", \"Segoe UI\", system-ui, sans-serif";
    static final String EASE = "box-shadow 220ms ease, transform 220ms ease, background-color 160ms ease, border-color 160ms ease";
    static final String FROSTING = "blur(16px) saturate(1.6)";

    // ── the palette, by reference: depth is the ink's shadow, a rim is the light's ──
    static final String INK_REF   = of(Body.class, Color.Ink.class).var();
    static final String LIGHT_REF = of(Primary.class, Color.Surface.class).var("background-color");

    static String drop(int y, int blur, int pct) { return "0 " + y + "px " + blur + "px color-mix(in srgb, " + INK_REF + " " + pct + "%, transparent)"; }
    /** A pane lit along its top edge — the rim light inside the glass. */
    static final String TOP_LIGHT = "inset 0 1px 0 color-mix(in srgb, white 60%, transparent)";

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // ── layers: every plate frosts what is behind it ────────────
            rule(of(Base.class, Shape.Rule.class), "0", "none"),
            one(of(Base.class, Shape.Corner.class), "0"),
            rule(of(Raised.class, Shape.Rule.class), "1px", "solid"),
            one(of(Raised.class, Shape.Corner.class), "18px"),
            Map.entry(of(Raised.class, Shape.Shadow.class), Impl.Bindings
                    .of(drop(8, 32, 12) + ", " + TOP_LIGHT)
                    .at(State.FOCUS, drop(8, 32, 12) + ", 0 0 0 3px color-mix(in srgb, " + LIGHT_REF + " 40%, transparent)")),
            Map.entry(of(Raised.class, Effect.Filter.class), Impl.Bindings.none().at(State.REST, "backdrop-filter", FROSTING)),
            Map.entry(of(Inverted.class, Effect.Filter.class), Impl.Bindings.none().at(State.REST, "backdrop-filter", "blur(20px) saturate(1.4)")),
            one(of(Recessed.class, Shape.Corner.class), "12px"),

            // ── text: a clean grotesque, headings a touch tight ─────────
            one(of(Body.class, Type.Face.class), BODY_FACE),
            one(of(Heading.class, Type.Face.class), DISPLAY_FACE),
            one(of(Heading.class, Type.Weight.class), "600"),
            one(of(Display.class, Type.Face.class), DISPLAY_FACE),
            one(of(Display.class, Type.Weight.class), "600"),
            scale(of(Display.class, Type.Scale.class), "44px", "1.1"),
            treatment(of(Display.class, Type.Treatment.class), "-0.02em", null, null),
            treatment(of(Lede.class, Type.Treatment.class), null, null, null),
            one(of(Kicker.class, Type.Face.class), DISPLAY_FACE),
            one(of(Kicker.class, Type.Weight.class), "600"),
            treatment(of(Kicker.class, Type.Treatment.class), "0.14em", "uppercase", null),
            treatment(of(Caption.class, Type.Treatment.class), null, null, null),
            one(of(Label.class, Type.Weight.class), "600"),
            one(of(Numeral.class, Type.Face.class), DISPLAY_FACE),
            one(of(Numeral.class, Type.Weight.class), "600"),
            treatment(of(Numeral.class, Type.Treatment.class), "-0.01em", null, null),
            one(of(House.class, Type.Face.class), DISPLAY_FACE),
            treatment(of(House.class, Type.Treatment.class), "-0.01em", null, null),
            decoration(of(Link.class, Type.Decoration.class), "none"),
            one(of(Code.class, Shape.Corner.class), "6px"),

            // ── interaction: a plate lifts on a longer shadow; pressed, it settles ──
            one(of(Interactive.class, Motion.Ease.class), EASE),
            Map.entry(of(Interactive.class, Motion.Transform.class), Impl.Bindings.none()
                    .at(State.HOVER, "translateY(-2px)")
                    .at(State.ACTIVE, "translateY(0)")
                    .at(State.SELECTED, "translateY(-2px)")
                    .at(State.HIGHLIGHTED, "translateY(-1px)")),
            Map.entry(of(Interactive.class, Shape.Shadow.class), Impl.Bindings
                    .of(drop(4, 16, 10) + ", " + TOP_LIGHT)
                    .at(State.HOVER, drop(12, 32, 16) + ", " + TOP_LIGHT)
                    .at(State.ACTIVE, drop(2, 8, 10) + ", " + TOP_LIGHT)
                    .at(State.SELECTED, drop(12, 32, 18) + ", " + TOP_LIGHT)
                    .at(State.HIGHLIGHTED, "0 8px 24px color-mix(in srgb, " + LIGHT_REF + " 35%, transparent), " + TOP_LIGHT)),
            Map.entry(of(Interactive.class, Effect.Filter.class), Impl.Bindings.none()
                    .at(State.SELECTED, "filter", "drop-shadow(" + drop(8, 16, 18) + ")")
                    .at(State.HIGHLIGHTED, "filter", "drop-shadow(0 6px 14px color-mix(in srgb, " + LIGHT_REF + " 35%, transparent))")),
            Map.entry(of(Interactive.class, Shape.Rule.class), Impl.Bindings.none()
                    .at(State.REST, "border-width", "1px").at(State.REST, "border-style", "solid")
                    .at(State.FOCUS, "outline-width", "2px").at(State.FOCUS, "outline-style", "solid").at(State.FOCUS, "outline-offset", "2px")),
            // Selectable — a row, a cell, an option: nothing until it is the one, then a clearer pane, lit along its top
            one(of(Selectable.class, Motion.Ease.class), EASE),
            Map.entry(of(Selectable.class, Motion.Transform.class), Impl.Bindings.none()
                    .at(State.HOVER, "translateY(-1px)")
                    .at(State.SELECTED, "translateY(-2px)")
                    .at(State.HIGHLIGHTED, "translateY(-1px)")),
            Map.entry(of(Selectable.class, Shape.Shadow.class), Impl.Bindings.none()
                    .at(State.REST, "none")
                    .at(State.HOVER, drop(4, 14, 10))
                    .at(State.SELECTED, drop(8, 24, 16) + ", " + TOP_LIGHT)
                    .at(State.HIGHLIGHTED, "0 4px 14px color-mix(in srgb, " + LIGHT_REF + " 35%, transparent)")),
            Map.entry(of(Selectable.class, Effect.Filter.class), Impl.Bindings.none()
                    .at(State.SELECTED, "filter", "drop-shadow(" + drop(8, 16, 18) + ")")
                    .at(State.HIGHLIGHTED, "filter", "drop-shadow(0 6px 14px color-mix(in srgb, " + LIGHT_REF + " 35%, transparent))")),
            Map.entry(of(Selectable.class, Shape.Rule.class), Impl.Bindings.none()
                    .at(State.REST, "border-width", "1px").at(State.REST, "border-style", "solid")
                    .at(State.FOCUS, "outline-width", "2px").at(State.FOCUS, "outline-style", "solid").at(State.FOCUS, "outline-offset", "-2px")),
            // the Selected semantic in depth — the lifted pane, for a component that says so with a class of its own
            one(of(Selected.class, Shape.Shadow.class), drop(8, 24, 16) + ", " + TOP_LIGHT),
            one(of(Selected.class, Motion.Transform.class), "translateY(-2px)"),
            Map.entry(of(Selected.class, Effect.Filter.class), Impl.Bindings.none().at(State.REST, "filter", "drop-shadow(" + drop(8, 16, 18) + ")")),
            one(of(Current.class, Shape.Shadow.class), "inset 3px 0 0 " + LIGHT_REF),
            one(of(Focus.class, Shape.Shadow.class), drop(24, 80, 30) + ", 0 0 0 3px color-mix(in srgb, " + LIGHT_REF + " 40%, transparent)"),
            one(of(Overlay.class, Shape.Shadow.class), drop(24, 80, 35)),
            Map.entry(of(Overlay.class, Effect.Filter.class), Impl.Bindings.none().at(State.REST, "backdrop-filter", "blur(10px) saturate(1.2) brightness(0.9)")),
            one(of(Inert.class, Effect.Opacity.class), "0.45"),
            one(of(Inert.class, Affordance.Cursor.class), "not-allowed"),

            // ── structure: hairlines, one pixel everywhere ─────────────
            rule(of(Divider.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            rule(of(Hairline.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            rule(of(Cap.class, Shape.Rule.class), "1px 0 0 0", "solid"),
            rule(of(Spine.class, Shape.Rule.class), "0 0 0 1px", "solid"),
            rule(of(Marker.class, Shape.Rule.class), "0 0 0 2px", "solid"),
            rule(of(Bar.class, Shape.Rule.class), "1px 1px 1px 3px", "solid"),
            rule(of(Rail.class, Shape.Rule.class), "0 1px 0 0", "solid"),

            // ── boxes: rounded, rimmed ──────────────────────────────────
            one(of(Control.class, Shape.Corner.class), "12px"),
            rule(of(Control.class, Shape.Rule.class), "1px", "solid"),
            one(of(Inline.class, Shape.Corner.class), "8px"),
            rule(of(Inline.class, Shape.Rule.class), "1px", "solid"),

            // ── prose ───────────────────────────────────────────────────
            body(of(Prose.class, Type.Face.class), """
                h1, h2, h3, h4 { font-family: %s; }
                code, pre { font-family: %s; }
                """.formatted(DISPLAY_FACE, DefaultDesign.MONO_FACE)),
            body(of(Prose.class, Type.Weight.class), "h1, h2, h3, h4, th { font-weight: 600; }\n"),
            body(of(Prose.class, Type.Treatment.class), """
                h1, h2 { letter-spacing: -0.02em; }
                h4 { letter-spacing: 0.1em; text-transform: uppercase; }
                """),
            body(of(Prose.class, Type.Decoration.class), "a { text-decoration-line: underline; text-underline-offset: 3px; text-decoration-thickness: 1px; }\n"),
            body(of(Prose.class, Shape.Rule.class), """
                h1, h2 { border-width: 0 0 1px 0; border-style: solid; }
                blockquote { border-width: 0 0 0 3px; border-style: solid; }
                th, td { border-width: 0 0 1px 0; border-style: solid; }
                th { border-width: 0; }
                hr { border-width: 1px 0 0 0; border-style: solid; }
                """),
            body(of(Prose.class, Shape.Corner.class), "code { border-radius: 6px; }\npre { border-radius: 12px; }\n")
    );
}
