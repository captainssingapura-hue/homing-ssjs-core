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
import static hue.captains.singapura.js.homing.design.Icon.Check;
import static hue.captains.singapura.js.homing.design.Icon.Close;
import static hue.captains.singapura.js.homing.design.Icon.Disclose;
import static hue.captains.singapura.js.homing.design.Icon.Grip;
import static hue.captains.singapura.js.homing.design.Icon.Pin;
import static hue.captains.singapura.js.homing.design.Icon.Settings;
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
                    .at(State.ACTIVE, "translateY(-2px) rotate(-1deg)")               // pressed is grabbed: the paper lifts and tilts, as Dragging does
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
            // in the hand: tilted in the hand
            one(of(Dragging.class, Motion.Transform.class), "translateY(-2px) rotate(-1deg)"),
            silence(of(Dragging.class, Shape.Shadow.class)),
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
            withKeysMark(ruleWithFocusRing(of(Control.class, Shape.Rule.class), "2px", "solid", "2px", "dashed", "-3px"),
                         "3px", "solid", "3px", "2px", "dashed", "5px", "2px", "dotted", "3px"),   // gone over twice when held; the marker's maybe when proposed
            outline(of(DropTarget.class, Shape.Rule.class), "2px", "dotted", "-4px"),
            // the button's density: a hand-drawn box, a little uneven in its growth
            inset(of(Control.Button.class, Size.Inset.class), "8px", "16px", 1.35),
            gap(of(Control.Button.class, Size.Gap.class), "8px", 1.35),
            minWidth(of(Control.Button.class, Size.Extent.class), "56px", 1.35),
            // the tab: a paper tab, its top corners wobbled, the pen a touch heavier on the selected one
            one(of(Control.Tab.class, Shape.Corner.class), "8px 6px 0 0 / 6px 8px 0 0"),
            withKeysMark(Map.entry(of(Control.Tab.class, Shape.Rule.class), Impl.Bindings.none()
                    .at(State.REST, "border-width", "2px").at(State.REST, "border-style", "solid")
                    .at(State.SELECTED, "border-width", "3px")
                    .at(State.FOCUS, "outline-width", "2px").at(State.FOCUS, "outline-style", "dashed").at(State.FOCUS, "outline-offset", "-3px")),
                         "3px", "solid", "3px", "2px", "dashed", "5px", "2px", "dotted", "3px"),   // a chip is drawn over twice too - the tab says it precisely, so it must say the keys as well
            inset(of(Control.Tab.class, Size.Inset.class), "0", "12px", 1.35),
            gap(of(Control.Tab.class, Size.Gap.class), "8px", 1.35),
            measure(of(Control.Tab.class, Size.Extent.class), "160px", 1.35),
            proportion(of(Control.Tab.class, Size.Proportion.class), "5", 1.5),
            // a slider: the track's length and the room its knob needs; the groove, the knob, the notch
            extent(of(Control.Slider.class, Size.Extent.class), "220px", "26px", 1.35),
            inset(of(Control.Slider.class, Size.Inset.class), "4px", "0", 1.35),
            gap(of(Control.Slider.class, Size.Gap.class), "12px", 1.35),
            thickness(of(Control.Slider.Track.class, Size.Extent.class), "5px", 1.35),
            one(of(Control.Slider.Track.class, Shape.Corner.class), "6px 4px 5px 7px / 4px 6px 7px 5px"),
            rule(of(Control.Slider.Track.class, Shape.Rule.class), "2px", "solid"),
            square(of(Control.Slider.Knob.class, Size.Extent.class), "18px", 1.35),
            one(of(Control.Slider.Knob.class, Shape.Corner.class), "50% 45% 55% 50% / 45% 55% 50% 50%"),
            ruleWithFocusRing(of(Control.Slider.Knob.class, Shape.Rule.class), "0", "none", "2px", "dashed", "3px"),   // the box: no rule of its own, a ring on focus; the rule is the face's, by lineage
            one(of(Control.Slider.Knob.class, Shape.Clip.class), "none"),
            one(of(Control.Slider.Knob.class, Effect.Opacity.class), "1"),   // a faced knob, the mark inside it
            scale(of(Control.Slider.Knob.class, Type.Scale.class), "12px", "1", 1.35),   // the mark's size
            // a vertical slider's cap: low along the track, wide across it; and the ticks of a scale beside a track
            extent(of(Control.Slider.Cap.class, Size.Extent.class), "15px", "30px", 1.35),
            one(of(Control.Slider.Cap.class, Shape.Corner.class), "5px 3px 4px 6px / 3px 5px 6px 4px"),
            ruleWithFocusRing(of(Control.Slider.Cap.class, Shape.Rule.class), "0", "none", "2px", "dashed", "3px"),
            scale(of(Control.Slider.Cap.class, Type.Scale.class), "12px", "1", 1.35),
            extent(of(Tick.class, Size.Extent.class), "2px", "8px", 1.35),
            extent(of(Detent.class, Size.Extent.class), "2px", "14px", 1.35),
            // a menu: a container of options that opens at a point; an option: one row of it
            one(of(Container.Menu.class, Shape.Corner.class), "8px 6px 7px 9px / 6px 8px 9px 7px"),
            inset(of(Container.Menu.class, Size.Inset.class), "8px", "0", 1.35),
            gap(of(Container.Menu.class, Size.Gap.class), "2px", 1.35),
            minWidth(of(Container.Menu.class, Size.Extent.class), "200px", 1.35),
            one(of(Control.Option.class, Shape.Corner.class), "6px 4px 5px 7px / 4px 6px 7px 5px"),
            inset(of(Control.Option.class, Size.Inset.class), "7px", "12px", 1.35),
            gap(of(Control.Option.class, Size.Gap.class), "10px", 1.35),
            // the icons a marker would draw: stickers where a symbol is too clean — the rest fall back to Editorial's
            glyph(of(Check.class, Type.Glyph.class), "✔"),
            glyph(of(Disclose.class, Type.Glyph.class), "➤"),
            glyph(of(Close.class, Type.Glyph.class), "✖"),
            glyph(of(Pin.class, Type.Glyph.class), "📌"),
            glyph(of(Settings.class, Type.Glyph.class), "🔧"),
            glyph(of(Grip.class, Type.Glyph.class), "⁘"),
            // a container, and the card: a hand-cut card, its corners uneven
            one(of(Container.class, Shape.Corner.class), WOBBLE_PLATE),
            withKeysMark(ruleWithFocusRing(of(Container.class, Shape.Rule.class), "2px", "solid", "2px", "dashed", "-3px"),
                         "3px", "solid", "4px", "2px", "dashed", "6px", "2px", "dotted", "4px"),
            inset(of(Container.Card.class, Size.Inset.class), "16px", "18px", 1.35),
            gap(of(Container.Card.class, Size.Gap.class), "8px", 1.35),
            measure(of(Container.Card.class, Size.Extent.class), "280px", 1.35),
            proportion(of(Container.Card.class, Size.Proportion.class), 2),
            // a panel: the head underlined by hand, gone over twice
            gap(of(Container.Panel.class, Size.Gap.class), "0", 1.35),
            inset(of(Container.Panel.Head.class, Size.Inset.class), "10px", "16px", 1.35),
            gap(of(Container.Panel.Head.class, Size.Gap.class), "10px", 1.35),
            rule(of(Container.Panel.Head.class, Shape.Rule.class), "0 0 3px 0", "double"),
            inset(of(Container.Panel.Body.class, Size.Inset.class), "14px", "16px", 1.35),
            gap(of(Container.Panel.Body.class, Size.Gap.class), "10px", 1.35),
            inset(of(Inline.class, Size.Inset.class), "2px", "8px", 1.35),
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
