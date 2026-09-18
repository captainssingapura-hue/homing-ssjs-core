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
 * Neumorphism — one material, moulded. Nothing is drawn on the surface;
 * everything is pressed out of it or into it. A raised thing is convex: lit
 * from the top-left, shaded to the bottom-right, both shadows the surface's
 * own tone. A pressed, selected or recessed thing is concave: the same two
 * shadows, inset. No rules, no borders — an edge is where the light turns —
 * and every corner is rounded, the way a moulded thing is.
 *
 * <p>A physique only: the two tones are computed from whatever surface the
 * palette provides — {@code color-mix} of that surface toward black and
 * toward white — so any palette moulds. Its own palette,
 * {@link SeedPalette#CLAY}, is the classic: one grey-blue clay, the raised
 * layer the same colour as the page.</p>
 */
final class NeumorphismDesign {

    private NeumorphismDesign() {}

    static final String DISPLAY_FACE = "\"Nunito\", \"Quicksand\", \"Varela Round\", \"Segoe UI\", system-ui, sans-serif";
    static final String BODY_FACE    = "\"Nunito\", \"Segoe UI\", system-ui, sans-serif";
    static final String EASE = "box-shadow 180ms ease, transform 180ms ease, background-color 180ms ease, color 120ms ease";

    // ── the material, by reference: the surface the palette provides, turned toward the light and away from it ──
    static final String SURFACE_REF = of(Base.class, Color.Surface.class).var("background-color");
    static final String ACCENT_REF  = of(Primary.class, Color.Surface.class).var("background-color");
    static final String SHADE = "color-mix(in srgb, " + SURFACE_REF + " 80%, black)";
    static final String LIGHT = "color-mix(in srgb, " + SURFACE_REF + " 55%, white)";

    /** Convex: pressed out of the surface by {@code px}, blurred twice as far. */
    static String convex(int px) { return px + "px " + px + "px " + (2 * px) + "px " + SHADE + ", -" + px + "px -" + px + "px " + (2 * px) + "px " + LIGHT; }
    /** Concave: pressed into the surface by {@code px}. */
    static String concave(int px) { return "inset " + px + "px " + px + "px " + (2 * px) + "px " + SHADE + ", inset -" + px + "px -" + px + "px " + (2 * px) + "px " + LIGHT; }
    /** The same relief for a thing that paints no box-shadow — a table row — as a filter. */
    static String relief(int px) { return "drop-shadow(" + px + "px " + px + "px " + (2 * px) + "px " + SHADE + ") drop-shadow(-" + px + "px -" + px + "px " + (2 * px) + "px " + LIGHT + ")"; }

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // ── layers: moulded, not drawn ──────────────────────────────
            rule(of(Base.class, Shape.Rule.class), "0", "none"),
            one(of(Base.class, Shape.Corner.class), "0"),
            rule(of(Raised.class, Shape.Rule.class), "0", "none"),
            one(of(Raised.class, Shape.Corner.class), "16px"),
            Map.entry(of(Raised.class, Shape.Shadow.class), Impl.Bindings
                    .of(convex(8))
                    .at(State.FOCUS, convex(8) + ", 0 0 0 3px color-mix(in srgb, " + ACCENT_REF + " 35%, transparent)")),
            one(of(Recessed.class, Shape.Corner.class), "12px"),

            // ── text: rounded, friendly, bold where it matters ─────────
            one(of(Body.class, Type.Face.class), BODY_FACE),
            one(of(Heading.class, Type.Face.class), DISPLAY_FACE),
            one(of(Heading.class, Type.Weight.class), "800"),
            one(of(Display.class, Type.Face.class), DISPLAY_FACE),
            one(of(Display.class, Type.Weight.class), "900"),
            scale(of(Display.class, Type.Scale.class), "42px", "1.1"),
            treatment(of(Display.class, Type.Treatment.class), "-0.01em", null, null),
            treatment(of(Lede.class, Type.Treatment.class), null, null, null),
            one(of(Kicker.class, Type.Face.class), DISPLAY_FACE),
            one(of(Kicker.class, Type.Weight.class), "800"),
            treatment(of(Kicker.class, Type.Treatment.class), "0.12em", "uppercase", null),
            treatment(of(Caption.class, Type.Treatment.class), null, null, null),
            one(of(Label.class, Type.Weight.class), "700"),
            one(of(Numeral.class, Type.Face.class), DISPLAY_FACE),
            one(of(Numeral.class, Type.Weight.class), "900"),
            treatment(of(Numeral.class, Type.Treatment.class), null, null, null),
            one(of(House.class, Type.Face.class), DISPLAY_FACE),
            treatment(of(House.class, Type.Treatment.class), "0.02em", null, null),
            decoration(of(Link.class, Type.Decoration.class), "none"),
            one(of(Code.class, Shape.Corner.class), "6px"),

            // ── interaction: a control is a pill pressed out; pressing it presses it in ──
            one(of(Interactive.class, Motion.Ease.class), EASE),
            Map.entry(of(Interactive.class, Motion.Transform.class), Impl.Bindings.none()
                    .at(State.HOVER, "translateY(-1px)")),
            Map.entry(of(Interactive.class, Shape.Shadow.class), Impl.Bindings
                    .of(convex(5))
                    .at(State.HOVER, convex(7))
                    .at(State.ACTIVE, concave(4))
                    .at(State.SELECTED, concave(5))
                    .at(State.HIGHLIGHTED, convex(6))),
            Map.entry(of(Interactive.class, Effect.Filter.class), Impl.Bindings.none()
                    .at(State.SELECTED, "filter", relief(4))
                    .at(State.HIGHLIGHTED, "filter", relief(3))),
            Map.entry(of(Interactive.class, Shape.Rule.class), Impl.Bindings.none()
                    .at(State.REST, "border-width", "0").at(State.REST, "border-style", "none")
                    .at(State.FOCUS, "outline-width", "3px").at(State.FOCUS, "outline-style", "solid").at(State.FOCUS, "outline-offset", "2px")),
            // Selectable — a row, a cell, an option: flush with the surface until it is the one, then pressed INTO it
            one(of(Selectable.class, Motion.Ease.class), EASE),
            silence(of(Selectable.class, Motion.Transform.class)),        // a moulded thing does not move; it presses in
            Map.entry(of(Selectable.class, Shape.Shadow.class), Impl.Bindings.none()
                    .at(State.REST, "none")
                    .at(State.HOVER, convex(3))
                    .at(State.ACTIVE, concave(3))
                    .at(State.SELECTED, concave(5))
                    .at(State.HIGHLIGHTED, convex(4))),
            Map.entry(of(Selectable.class, Effect.Filter.class), Impl.Bindings.none()
                    .at(State.SELECTED, "filter", relief(4))
                    .at(State.HIGHLIGHTED, "filter", relief(3))),
            Map.entry(of(Selectable.class, Shape.Rule.class), Impl.Bindings.none()
                    .at(State.REST, "border-width", "0").at(State.REST, "border-style", "none")
                    .at(State.FOCUS, "outline-width", "3px").at(State.FOCUS, "outline-style", "solid").at(State.FOCUS, "outline-offset", "-3px")),
            // the Selected semantic in depth — concave, for a component that says "lifted" with a class of its own
            one(of(Selected.class, Shape.Shadow.class), concave(5)),
            silence(of(Selected.class, Motion.Transform.class)),
            Map.entry(of(Selected.class, Effect.Filter.class), Impl.Bindings.none().at(State.REST, "filter", relief(4))),
            one(of(Current.class, Shape.Shadow.class), concave(3)),
            one(of(Focus.class, Shape.Shadow.class), convex(10) + ", 0 0 0 3px color-mix(in srgb, " + ACCENT_REF + " 35%, transparent)"),
            one(of(Overlay.class, Shape.Shadow.class), convex(14)),
            Map.entry(of(Overlay.class, Effect.Filter.class), Impl.Bindings.none().at(State.REST, "backdrop-filter", "blur(6px)")),
            one(of(Inert.class, Effect.Opacity.class), "0.5"),
            one(of(Inert.class, Affordance.Cursor.class), "not-allowed"),

            // ── structure: no lines; a divider is a groove, a rail a fold ──
            rule(of(Divider.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            rule(of(Hairline.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            rule(of(Cap.class, Shape.Rule.class), "1px 0 0 0", "solid"),
            rule(of(Spine.class, Shape.Rule.class), "0 0 0 1px", "solid"),
            rule(of(Marker.class, Shape.Rule.class), "0 0 0 3px", "solid"),
            rule(of(Bar.class, Shape.Rule.class), "0", "none"),
            rule(of(Rail.class, Shape.Rule.class), "0 1px 0 0", "solid"),

            // ── boxes: everything is a pill ─────────────────────────────
            one(of(Control.class, Shape.Corner.class), "12px"),
            rule(of(Control.class, Shape.Rule.class), "0", "none"),
            one(of(Inline.class, Shape.Corner.class), "8px"),
            rule(of(Inline.class, Shape.Rule.class), "0", "none"),

            // ── prose: soft type, grooves for rules ─────────────────────
            body(of(Prose.class, Type.Face.class), """
                h1, h2, h3, h4 { font-family: %s; }
                code, pre { font-family: %s; }
                """.formatted(DISPLAY_FACE, EditorialDesign.MONO_FACE)),
            body(of(Prose.class, Type.Weight.class), "h1, h2, h3, h4, th { font-weight: 800; }\n"),
            body(of(Prose.class, Type.Treatment.class), """
                h4 { letter-spacing: 0.08em; text-transform: uppercase; }
                """),
            body(of(Prose.class, Type.Decoration.class), "a { text-decoration-line: underline; text-underline-offset: 3px; text-decoration-thickness: 2px; }\n"),
            body(of(Prose.class, Shape.Rule.class), """
                h1 { border-width: 0; }
                blockquote { border-width: 0 0 0 4px; border-style: solid; }
                th, td { border-width: 0 0 1px 0; border-style: solid; }
                th { border-width: 0; }
                hr { border-width: 1px 0 0 0; border-style: solid; }
                """),
            body(of(Prose.class, Shape.Corner.class), "code { border-radius: 6px; }\npre { border-radius: 12px; }\n")
    );
}
