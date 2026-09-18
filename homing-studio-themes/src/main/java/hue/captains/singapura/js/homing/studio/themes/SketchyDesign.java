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
 * Sketchy — drawn by hand, in marker, on paper. Every box is a rectangle
 * someone drew without a ruler: a 2px line and a corner that wobbles (a
 * border-radius with eight values, no two alike). Nothing casts a shadow —
 * ink on paper does not — and nothing is smooth; hover gives a thing a
 * slight tilt, as if picked up. Handwriting for the body, a sketched
 * capital for headings, a wavy underline for links, a dashed outline for
 * focus. After Bootswatch's Sketchy.
 *
 * <p>A physique only: the line's colour is the palette's ink;
 * {@link SeedPalette#MARKER} — black marker on white paper — is the one it
 * is worn in by default, and any palette draws under it.</p>
 */
final class SketchyDesign {

    private SketchyDesign() {}

    static final String DISPLAY_FACE = "\"Cabin Sketch\", \"Segoe Print\", \"Bradley Hand\", \"Chalkboard SE\", cursive";
    static final String BODY_FACE    = "\"Neucha\", \"Segoe Print\", \"Bradley Hand\", \"Comic Sans MS\", cursive";
    static final String EASE = "transform 120ms ease-out, background-color 120ms ease-out, border-color 120ms ease-out";

    // ── the wobbles: eight radii, no two alike, as Sketchy draws them ──
    static final String WOBBLE_CONTROL = "255px 25px 225px 25px / 25px 225px 25px 255px";
    static final String WOBBLE_PLATE   = "25px 25px 55px 5px / 5px 55px 25px 25px";
    static final String WOBBLE_WELL    = "45px 15px 35px 5px / 15px 5px 15px 65px";
    static final String WOBBLE_ROW     = "255px 5px 225px 5px / 25px 225px 25px 255px";
    static final String WOBBLE_TAG     = "55px 225px 15px 25px / 25px 25px 35px 355px";

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // ── layers: paper, and boxes drawn on it ────────────────────
            rule(of(Base.class, Shape.Rule.class), "0", "none"),
            one(of(Base.class, Shape.Corner.class), "0"),
            rule(of(Raised.class, Shape.Rule.class), "2px", "solid"),
            one(of(Raised.class, Shape.Corner.class), WOBBLE_PLATE),
            silence(of(Raised.class, Shape.Shadow.class)),                 // ink casts no shadow
            silence(of(Raised.class, Effect.Filter.class)),
            silence(of(Inverted.class, Effect.Filter.class)),
            one(of(Recessed.class, Shape.Corner.class), WOBBLE_WELL),

            // ── text: handwriting, and a sketched capital ───────────────
            one(of(Body.class, Type.Face.class), BODY_FACE),
            one(of(Heading.class, Type.Face.class), DISPLAY_FACE),
            one(of(Heading.class, Type.Weight.class), "700"),
            one(of(Display.class, Type.Face.class), DISPLAY_FACE),
            one(of(Display.class, Type.Weight.class), "700"),
            scale(of(Display.class, Type.Scale.class), "44px", "1.1"),
            treatment(of(Display.class, Type.Treatment.class), null, null, null),
            silence(of(Display.class, Type.Decoration.class)),
            treatment(of(Lede.class, Type.Treatment.class), null, null, null),
            one(of(Kicker.class, Type.Face.class), DISPLAY_FACE),
            one(of(Kicker.class, Type.Weight.class), "700"),
            treatment(of(Kicker.class, Type.Treatment.class), "0.06em", "uppercase", null),
            treatment(of(Caption.class, Type.Treatment.class), null, null, null),
            one(of(Label.class, Type.Weight.class), "700"),
            one(of(Numeral.class, Type.Face.class), DISPLAY_FACE),
            one(of(Numeral.class, Type.Weight.class), "700"),
            treatment(of(Numeral.class, Type.Treatment.class), null, null, null),
            one(of(House.class, Type.Face.class), DISPLAY_FACE),
            treatment(of(House.class, Type.Treatment.class), null, null, null),
            silence(of(House.class, Type.Decoration.class)),
            // a link is underlined, wavily, when pointed at — a card is a link too, and its whole face should not be scribbled under
            Map.entry(of(Link.class, Type.Decoration.class), Impl.Bindings.none()
                    .at(State.REST, "text-decoration-line", "none")
                    .at(State.HOVER, "text-decoration-line", "underline")
                    .at(State.HOVER, "text-decoration-style", "wavy")
                    .at(State.HOVER, "text-underline-offset", "3px")),
            one(of(Code.class, Shape.Corner.class), "15px"),

            // ── interaction: picked up, tilted; pressed, put down ──────
            one(of(Interactive.class, Motion.Ease.class), EASE),
            Map.entry(of(Interactive.class, Motion.Transform.class), Impl.Bindings.none()
                    .at(State.HOVER, "translateY(-1px) rotate(-0.6deg)")
                    .at(State.ACTIVE, "translateY(1px) rotate(0.4deg)")
                    .at(State.SELECTED, "rotate(-0.4deg)")
                    .at(State.HIGHLIGHTED, "rotate(0.4deg)")),
            silence(of(Interactive.class, Shape.Shadow.class)),
            silence(of(Interactive.class, Effect.Filter.class)),
            Map.entry(of(Interactive.class, Shape.Rule.class), Impl.Bindings.none()
                    .at(State.REST, "border-width", "2px").at(State.REST, "border-style", "solid")
                    .at(State.FOCUS, "outline-width", "2px").at(State.FOCUS, "outline-style", "dashed").at(State.FOCUS, "outline-offset", "3px")),
            // Selectable — a row, a cell, an option: a line drawn around the one, dashed around the ones pointed at
            one(of(Selectable.class, Motion.Ease.class), EASE),
            Map.entry(of(Selectable.class, Motion.Transform.class), Impl.Bindings.none()
                    .at(State.SELECTED, "rotate(-0.4deg)")
                    .at(State.HIGHLIGHTED, "rotate(0.4deg)")),
            silence(of(Selectable.class, Shape.Shadow.class)),
            silence(of(Selectable.class, Effect.Filter.class)),
            Map.entry(of(Selectable.class, Shape.Rule.class), Impl.Bindings.none()
                    .at(State.REST, "border-width", "2px").at(State.REST, "border-style", "solid")
                    .at(State.SELECTED, "border-width", "3px")
                    .at(State.CURRENT, "border-style", "dotted")
                    .at(State.HIGHLIGHTED, "border-style", "dashed")
                    .at(State.FOCUS, "outline-width", "2px").at(State.FOCUS, "outline-style", "dashed").at(State.FOCUS, "outline-offset", "-4px")),
            // the Selected semantic in depth — nothing; a selected thing is filled, not lifted
            silence(of(Selected.class, Shape.Shadow.class)),
            one(of(Selected.class, Motion.Transform.class), "rotate(-0.4deg)"),
            silence(of(Selected.class, Effect.Filter.class)),
            silence(of(Current.class, Shape.Shadow.class)),
            silence(of(Focus.class, Shape.Shadow.class)),
            outline(of(Focus.class, Shape.Rule.class), "2px", "dashed", "-3px"),   // the ring is drawn by hand: a dashed line just inside
            silence(of(Overlay.class, Shape.Shadow.class)),
            silence(of(Overlay.class, Effect.Filter.class)),
            one(of(Inert.class, Effect.Opacity.class), "0.5"),
            one(of(Inert.class, Affordance.Cursor.class), "not-allowed"),

            // ── structure: every line is 2px of marker ─────────────────
            rule(of(Divider.class, Shape.Rule.class), "0 0 2px 0", "solid"),
            rule(of(Hairline.class, Shape.Rule.class), "0 0 2px 0", "solid"),
            rule(of(Cap.class, Shape.Rule.class), "2px 0 0 0", "solid"),
            rule(of(Spine.class, Shape.Rule.class), "0 0 0 2px", "solid"),
            rule(of(Marker.class, Shape.Rule.class), "0 0 0 3px", "solid"),
            rule(of(Bar.class, Shape.Rule.class), "2px", "solid"),
            rule(of(Rail.class, Shape.Rule.class), "0 2px 0 0", "solid"),
            rule(of(Lattice.class, Shape.Rule.class), "0 2px 2px 0", "solid"),

            // ── boxes: drawn without a ruler ────────────────────────────
            one(of(Control.class, Shape.Corner.class), WOBBLE_CONTROL),
            rule(of(Control.class, Shape.Rule.class), "2px", "solid"),
            one(of(Inline.class, Shape.Corner.class), WOBBLE_TAG),
            rule(of(Inline.class, Shape.Rule.class), "2px", "solid"),

            // ── prose ───────────────────────────────────────────────────
            body(of(Prose.class, Type.Face.class), """
                h1, h2, h3, h4 { font-family: %s; }
                code, pre { font-family: %s; }
                """.formatted(DISPLAY_FACE, EditorialDesign.MONO_FACE)),
            body(of(Prose.class, Type.Weight.class), "h1, h2, h3, h4, th { font-weight: 700; }\n"),
            body(of(Prose.class, Type.Treatment.class), """
                h4 { letter-spacing: 0.06em; text-transform: uppercase; }
                """),
            body(of(Prose.class, Type.Decoration.class), "a { text-decoration-line: underline; text-decoration-style: wavy; text-underline-offset: 3px; }\n"),
            body(of(Prose.class, Shape.Rule.class), """
                h1, h2 { border-width: 0 0 2px 0; border-style: solid; }
                blockquote { border-width: 0 0 0 3px; border-style: solid; }
                th, td { border-width: 0 0 2px 0; border-style: solid; }
                th { border-width: 0 0 2px 0; }
                hr { border-width: 2px 0 0 0; border-style: solid; }
                """),
            body(of(Prose.class, Shape.Corner.class), "code { border-radius: 15px; }\npre { border-radius: " + WOBBLE_WELL + "; }\n")
    );
}
