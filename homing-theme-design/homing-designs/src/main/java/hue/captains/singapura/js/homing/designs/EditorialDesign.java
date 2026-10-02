package hue.captains.singapura.js.homing.designs;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Impl;

import java.util.Map;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Brand.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Feedback.*;
import hue.captains.singapura.js.homing.design.Icon;
import static hue.captains.singapura.js.homing.design.Icon.Add;
import static hue.captains.singapura.js.homing.design.Icon.Check;
import static hue.captains.singapura.js.homing.design.Icon.Close;
import static hue.captains.singapura.js.homing.design.Icon.Detach;
import static hue.captains.singapura.js.homing.design.Icon.Disclose;
import static hue.captains.singapura.js.homing.design.Icon.Flip;
import static hue.captains.singapura.js.homing.design.Icon.Grip;
import static hue.captains.singapura.js.homing.design.Icon.Level;
import static hue.captains.singapura.js.homing.design.Icon.Pin;
import static hue.captains.singapura.js.homing.design.Icon.Remove;
import static hue.captains.singapura.js.homing.design.Icon.Reset;
import static hue.captains.singapura.js.homing.design.Icon.Rotate;
import static hue.captains.singapura.js.homing.design.Icon.Settings;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Pairing.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;
import static hue.captains.singapura.js.homing.designs.Bind.*;

/**
 * The house physique — what a page looks like out of the box, off the
 * colour plane: shape, type, motion, depth, said as bindings over the design
 * classes its components wear. Its colours are {@link SeedPalette#HOUSE}; a
 * shadow names them by reference and carries none. Every value here was a
 * token or a literal in a class body until the components stopped painting.
 */
final class EditorialDesign {

    private EditorialDesign() {}

    // The house colours live in SeedPalette.HARBOUR — this is the physique: shape, type, motion, depth.

    static final String DISPLAY_FACE = HouseFonts.DISPLAY;
    static final String BODY_FACE    = HouseFonts.BODY;
    static final String MONO_FACE    = "ui-monospace, SFMono-Regular, Menlo, Consolas, monospace";

    // ── the palette, by reference: a shadow is a colour the palette owns, offset ──
    static final String INK_REF      = of(Heading.class, Color.Ink.class).var();
    static final String PRIMARY_REF  = of(Primary.class, Color.Surface.class).var("background-color");
    static final String INVERTED_REF = of(Inverted.class, Color.Surface.class).var("background-color");

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // ── layers ──────────────────────────────────────────────────
            rule(of(Base.class, Shape.Rule.class), "0", "none"),
            one(of(Base.class, Shape.Corner.class), "0"),
            rule(of(Raised.class, Shape.Rule.class), "1px", "solid"),
            one(of(Raised.class, Shape.Corner.class), "4px"),
            Map.entry(of(Raised.class, Shape.Shadow.class), hue.captains.singapura.js.homing.design.Impl.Bindings
                    .of("0 1px 3px color-mix(in srgb, " + INK_REF + " 4%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "0 0 0 3px color-mix(in srgb, " + PRIMARY_REF + " 18%, transparent)")),
            one(of(Recessed.class, Shape.Corner.class), "6px"),
            // a plate and the masthead offer a filter slot — glass frosts it; the house says nothing there
            silence(of(Raised.class, Effect.Filter.class)),
            silence(of(Inverted.class, Effect.Filter.class)),
            // the title and the house word offer a decoration slot — a glow, a relief; the house says nothing there
            silence(of(Display.class, Type.Decoration.class)),
            silence(of(House.class, Type.Decoration.class)),

            // ── text ────────────────────────────────────────────────────
            one(of(Body.class, Type.Face.class), BODY_FACE),
            one(of(Heading.class, Type.Face.class), DISPLAY_FACE),
            one(of(Heading.class, Type.Weight.class), "700"),
            scale(of(Heading.class, Type.Scale.class), "18px", "1.25", 1.2),   // grows with the box it heads
            one(of(Display.class, Type.Face.class), DISPLAY_FACE),
            one(of(Display.class, Type.Weight.class), "700"),
            scale(of(Display.class, Type.Scale.class), "44px", "1.1"),
            treatment(of(Display.class, Type.Treatment.class), "-0.5px", null, null),
            scale(of(Lede.class, Type.Scale.class), "17px", "1.55"),
            treatment(of(Lede.class, Type.Treatment.class), null, null, "italic"),
            one(of(Kicker.class, Type.Face.class), DISPLAY_FACE),
            one(of(Kicker.class, Type.Weight.class), "700"),
            scale(of(Kicker.class, Type.Scale.class), "11px", "1.4", 1.15),
            treatment(of(Kicker.class, Type.Treatment.class), "2px", "uppercase", null),
            scale(of(Caption.class, Type.Scale.class), "13px", "1.5", 1.2),
            treatment(of(Caption.class, Type.Treatment.class), null, null, "italic"),
            scale(of(Label.class, Type.Scale.class), "14px", "1.5", 1.25),   // grows with the control it labels
            one(of(Label.class, Type.Weight.class), "600"),
            one(of(Numeral.class, Type.Face.class), DISPLAY_FACE),
            one(of(Numeral.class, Type.Weight.class), "700"),
            scale(of(Numeral.class, Type.Scale.class), "28px", "1"),
            treatment(of(Numeral.class, Type.Treatment.class), null, null, "italic"),
            one(of(House.class, Type.Face.class), DISPLAY_FACE),
            scale(of(House.class, Type.Scale.class), "22px", "1"),
            treatment(of(House.class, Type.Treatment.class), null, null, "italic"),
            decoration(of(Link.class, Type.Decoration.class), "none"),
            one(of(Link.class, Motion.Ease.class), "color 140ms ease, border-color 140ms ease"),
            one(of(Code.class, Type.Face.class), MONO_FACE),
            scale(of(Code.class, Type.Scale.class), "12px", "1.5"),
            one(of(Code.class, Shape.Corner.class), "3px"),

            // ── emphasis ────────────────────────────────────────────────
            one(of(Primary.class, Motion.Ease.class), "width 280ms ease"),
            one(of(Muted.class, Effect.Opacity.class), "0.62"),
            decoration(of(Muted.class, Type.Decoration.class), "line-through"),

            // ── pairings ────────────────────────────────────────────────

            // ── feedback ────────────────────────────────────────────────

            // ── interaction ─────────────────────────────────────────────
            one(of(Interactive.class, Motion.Ease.class), "transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease"),
            // depth is a state: hover lifts, a press sets it back down, selected and highlighted stay lifted; a row lifts by filter, since a row paints no shadow
            Map.entry(of(Interactive.class, Motion.Transform.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.HOVER, "translateY(-2px)")
                    .at(hue.captains.singapura.js.homing.design.State.ACTIVE, "translateY(0)")
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "translateY(-2px)")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "translateY(-1px)")),
            Map.entry(of(Interactive.class, Shape.Shadow.class), hue.captains.singapura.js.homing.design.Impl.Bindings
                    .of("0 1px 3px color-mix(in srgb, " + INK_REF + " 4%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.HOVER, "0 6px 16px color-mix(in srgb, " + INK_REF + " 12%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.ACTIVE, "0 1px 2px color-mix(in srgb, " + INK_REF + " 8%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "0 8px 20px color-mix(in srgb, " + INK_REF + " 18%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "0 4px 12px color-mix(in srgb, " + PRIMARY_REF + " 35%, transparent)")),
            Map.entry(of(Interactive.class, Effect.Filter.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "filter", "drop-shadow(0 6px 10px color-mix(in srgb, " + INK_REF + " 18%, transparent))")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "filter", "drop-shadow(0 3px 8px color-mix(in srgb, " + PRIMARY_REF + " 35%, transparent))")),
            one(of(Interactive.class, Affordance.Cursor.class), "pointer"),
            // a hairline that the palette colours only in a state; a 2px ring, inset, on focus
            Map.entry(of(Interactive.class, Shape.Rule.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.REST, "border-width", "1px")
                    .at(hue.captains.singapura.js.homing.design.State.REST, "border-style", "solid")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-width", "2px")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-style", "solid")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-offset", "-2px")),
            // Selectable — a row, a cell, an option: flat at rest, the same lifts as Interactive in its states
            one(of(Selectable.class, Motion.Ease.class), "transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease, background-color 160ms ease"),
            one(of(Selectable.class, Affordance.Cursor.class), "pointer"),
            Map.entry(of(Selectable.class, Motion.Transform.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.HOVER, "translateY(-1px)")
                    .at(hue.captains.singapura.js.homing.design.State.ACTIVE, "translateY(-4px)")                              // pressed is grabbed: it lifts into the hand, as Dragging does
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "translateY(-2px)")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "translateY(-1px)")),
            Map.entry(of(Selectable.class, Shape.Shadow.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.REST, "none")
                    .at(hue.captains.singapura.js.homing.design.State.HOVER, "0 4px 12px color-mix(in srgb, " + INK_REF + " 10%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.ACTIVE, "0 14px 30px color-mix(in srgb, " + INK_REF + " 20%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "0 8px 20px color-mix(in srgb, " + INK_REF + " 18%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "0 4px 12px color-mix(in srgb, " + PRIMARY_REF + " 35%, transparent)")),
            Map.entry(of(Selectable.class, Effect.Filter.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "filter", "drop-shadow(0 6px 10px color-mix(in srgb, " + INK_REF + " 18%, transparent))")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "filter", "drop-shadow(0 3px 8px color-mix(in srgb, " + PRIMARY_REF + " 35%, transparent))")),
            Map.entry(of(Selectable.class, Shape.Rule.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.REST, "border-width", "1px")
                    .at(hue.captains.singapura.js.homing.design.State.REST, "border-style", "solid")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-width", "2px")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-style", "solid")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-offset", "-2px")),
            // the Selected semantic in depth — for a component that says "lifted" with a class of its own
            one(of(Selected.class, Shape.Shadow.class), "0 8px 20px color-mix(in srgb, " + INK_REF + " 18%, transparent)"),
            one(of(Selected.class, Motion.Transform.class), "translateY(-2px)"),
            // in the hand: lifted clear of the page
            one(of(Dragging.class, Motion.Transform.class), "translateY(-4px)"),
            one(of(Dragging.class, Shape.Shadow.class), "0 14px 30px color-mix(in srgb, " + INK_REF + " 20%, transparent)"),
            Map.entry(of(Selected.class, Effect.Filter.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.REST, "filter", "drop-shadow(0 6px 10px color-mix(in srgb, " + INK_REF + " 18%, transparent))")),
            one(of(Current.class, Shape.Shadow.class), "inset 3px 0 0 color-mix(in srgb, " + PRIMARY_REF + " 60%, transparent)"),

            // ── structure ───────────────────────────────────────────────
            rule(of(Divider.class, Shape.Rule.class), "0 0 2px 0", "solid"),
            rule(of(Hairline.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            rule(of(Cap.class, Shape.Rule.class), "1px 0 0 0", "solid"),
            rule(of(Spine.class, Shape.Rule.class), "0 0 0 1px", "solid"),
            rule(of(Marker.class, Shape.Rule.class), "0 0 0 2px", "solid"),
            rule(of(Bar.class, Shape.Rule.class), "1px 1px 1px 4px", "solid"),
            rule(of(Rail.class, Shape.Rule.class), "0 1px 0 0", "solid"),
            rule(of(Lattice.class, Shape.Rule.class), "0 1px 1px 0", "solid"),
            one(of(Overlay.class, Shape.Shadow.class), "0 10px 30px color-mix(in srgb, " + INVERTED_REF + " 45%, transparent), 0 2px 6px color-mix(in srgb, " + INVERTED_REF + " 30%, transparent)"),
            Map.entry(of(Overlay.class, Effect.Filter.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none().at(hue.captains.singapura.js.homing.design.State.REST, "backdrop-filter", "brightness(0.45) blur(2px)")),
            one(of(Focus.class, Shape.Shadow.class), "0 10px 30px color-mix(in srgb, " + INVERTED_REF + " 45%, transparent), 0 0 0 1px color-mix(in srgb, " + PRIMARY_REF + " 28%, transparent), 0 0 36px color-mix(in srgb, " + PRIMARY_REF + " 30%, transparent)"),
            outline(of(Focus.class, Shape.Rule.class), "2px", "solid", "-2px"),
            outline(of(DropTarget.class, Shape.Rule.class), "2px", "dashed", "-2px"),   // a place you can drop on: a dashed ring, drawn now   // the ring, where a component draws it as one: a grid's cursor, a tree's current row
            one(of(Inert.class, Effect.Opacity.class), "0.45"),
            one(of(Inert.class, Affordance.Cursor.class), "default"),

            // ── boxes ───────────────────────────────────────────────────
            one(of(Control.class, Shape.Corner.class), "3px"),
            withKeysMark(ruleWithFocusRing(of(Control.class, Shape.Rule.class), "1.5px", "solid", "2px", "solid", "-2px"),
                         "2px", "solid", "-2px", "1px", "dashed", "3px", "1px", "solid", "-2px"),   // held: the ring; proposed: a pencil line outside; lent: the ring, thinned
            // the button's density: a button's density: it grows a step of 1.3 per unit of size, the type a gentler 1.25
            inset(of(Control.Button.class, Size.Inset.class), "8px", "18px", 1.3),
            gap(of(Control.Button.class, Size.Gap.class), "8px", 1.3),
            minWidth(of(Control.Button.class, Size.Extent.class), "56px", 1.3),
            // the tab: a browser's — a hard frame six to one, the label ellipsised within; cut at the top, flush where it meets the row
            one(of(Control.Tab.class, Shape.Corner.class), "4px 4px 0 0"),
            inset(of(Control.Tab.class, Size.Inset.class), "0", "12px", 1.3),
            gap(of(Control.Tab.class, Size.Gap.class), "8px", 1.3),
            measure(of(Control.Tab.class, Size.Extent.class), "168px", 1.3),
            proportion(of(Control.Tab.class, Size.Proportion.class), "6", 1.5),
            // a tile: one box of a grid, a mark over a label; the corner is round on every side
            one(of(Control.Tile.class, Shape.Corner.class), "4px"),
            inset(of(Control.Tile.class, Size.Inset.class), "12px", "10px", 1.3),
            gap(of(Control.Tile.class, Size.Gap.class), "6px", 1.3),
            measure(of(Control.Tile.class, Size.Extent.class), "128px", 1.3),
            proportion(of(Control.Tile.class, Size.Proportion.class), "1.4", 1.5),
            // a slider: the track's length and the room its knob needs; the groove, the knob, the notch
            extent(of(Control.Slider.class, Size.Extent.class), "220px", "24px", 1.3),
            inset(of(Control.Slider.class, Size.Inset.class), "4px", "0", 1.3),
            gap(of(Control.Slider.class, Size.Gap.class), "12px", 1.3),
            thickness(of(Control.Slider.Track.class, Size.Extent.class), "4px", 1.3),
            one(of(Control.Slider.Track.class, Shape.Corner.class), "999px"),
            rule(of(Control.Slider.Track.class, Shape.Rule.class), "1px", "solid"),
            square(of(Control.Slider.Knob.class, Size.Extent.class), "16px", 1.3),
            one(of(Control.Slider.Knob.class, Shape.Corner.class), "50%"),
            ruleWithFocusRing(of(Control.Slider.Knob.class, Shape.Rule.class), "0", "none", "2px", "solid", "2px"),   // the box: no rule of its own, a ring on focus; the rule is the face's, by lineage
            one(of(Control.Slider.Knob.class, Shape.Clip.class), "none"),
            one(of(Control.Slider.Knob.class, Effect.Opacity.class), "0"),   // the knob is its mark alone: no face
            scale(of(Control.Slider.Knob.class, Type.Scale.class), "18px", "1", 1.3),   // the mark's size
            // a vertical slider's cap: low along the track, wide across it; and the ticks of a scale beside a track
            extent(of(Control.Slider.Cap.class, Size.Extent.class), "14px", "28px", 1.3),
            one(of(Control.Slider.Cap.class, Shape.Corner.class), "3px"),
            ruleWithFocusRing(of(Control.Slider.Cap.class, Shape.Rule.class), "0", "none", "2px", "solid", "2px"),
            scale(of(Control.Slider.Cap.class, Type.Scale.class), "11px", "1", 1.3),
            extent(of(Tick.class, Size.Extent.class), "1px", "7px", 1.3),
            extent(of(Detent.class, Size.Extent.class), "2px", "12px", 1.3),
            // a menu: a container of options that opens at a point; an option: one row of it
            one(of(Container.Menu.class, Shape.Corner.class), "6px"),
            inset(of(Container.Menu.class, Size.Inset.class), "6px", "0", 1.3),
            gap(of(Container.Menu.class, Size.Gap.class), "2px", 1.3),
            minWidth(of(Container.Menu.class, Size.Extent.class), "200px", 1.3),
            one(of(Control.Option.class, Shape.Corner.class), "4px"),
            inset(of(Control.Option.class, Size.Inset.class), "7px", "12px", 1.3),
            gap(of(Control.Option.class, Size.Gap.class), "10px", 1.3),
            // the icons: what the design draws for each intent — plain symbols, set in the text's own face
            glyph(of(Check.class, Type.Glyph.class), "✓"),
            glyph(of(Disclose.class, Type.Glyph.class), "▸"),
            glyph(of(Close.class, Type.Glyph.class), "✕"),
            glyph(of(Detach.class, Type.Glyph.class), "⧉"),
            glyph(of(Rotate.class, Type.Glyph.class), "↻"),
            glyph(of(Flip.class, Type.Glyph.class), "⇋"),
            glyph(of(Add.class, Type.Glyph.class), "+"),
            glyph(of(Remove.class, Type.Glyph.class), "−"),
            glyph(of(Reset.class, Type.Glyph.class), "↺"),
            glyph(of(Pin.class, Type.Glyph.class), "⌖"),
            glyph(of(Settings.class, Type.Glyph.class), "⚙"),
            glyph(of(Grip.class, Type.Glyph.class), "⠿"),
            glyph(of(Icon.Size.class, Type.Glyph.class), "⤢"),
            glyph(of(Icon.Aspect.class, Type.Glyph.class), "⬌"),
            glyph(of(Icon.Extent.class, Type.Glyph.class), "◐"),
            glyph(of(Level.class, Type.Glyph.class), "≡"),
            glyph(of(Icon.Column.class, Type.Glyph.class), "▥"),
            glyph(of(Icon.Row.class, Type.Glyph.class), "▤"),
            glyph(of(Icon.Merge.class, Type.Glyph.class), "▣"),
            glyph(of(Icon.Within.class, Type.Glyph.class), "◉"),
            // a container, and the card: an index card: three by two, a soft corner
            one(of(Container.class, Shape.Corner.class), "6px"),
            withKeysMark(ruleWithFocusRing(of(Container.class, Shape.Rule.class), "1px", "solid", "2px", "solid", "-2px"),
                         "2px", "solid", "2px", "1px", "dashed", "5px", "1px", "solid", "2px"),   // a region is marked outside itself, clear of its own rule
            inset(of(Container.Card.class, Size.Inset.class), "18px", "20px", 1.3),
            gap(of(Container.Card.class, Size.Gap.class), "8px", 1.3),
            measure(of(Container.Card.class, Size.Extent.class), "280px", 1.3),
            proportion(of(Container.Card.class, Size.Proportion.class), 2),
            inset(of(Inline.class, Size.Inset.class), "2px", "8px", 1.3),
            // a pane: its air is its head's; the widget fills the rest
            inset(of(Container.Pane.class, Size.Inset.class), "5px", "12px", 1.3),
            gap(of(Container.Pane.class, Size.Gap.class), "8px", 1.3),
            // a dock draws no frame of its own - what holds it does, or nothing does - so all its rule says is where the keys are
            withKeysMark(rule(of(Container.Pane.class, Shape.Rule.class), "0", "none"),
                         "2px", "solid", "2px", "1px", "dashed", "5px", "1px", "solid", "2px"),
            // a float is its own frame, and keeps the container's rule under the same mark
            withKeysMark(ruleWithFocusRing(of(Container.Pane.Floating.class, Shape.Rule.class), "1px", "solid", "2px", "solid", "-2px"),
                         "2px", "solid", "2px", "1px", "dashed", "5px", "1px", "solid", "2px"),
            // a panel: a running head with a rule under it, and the page below
            // the panel's depth: a quiet press, a quiet lift; the page is paper and paper does not shout
            Map.entry(of(Container.Panel.class, Shape.Shadow.class), hue.captains.singapura.js.homing.design.Impl.Bindings.of("none")
                    .at(hue.captains.singapura.js.homing.design.State.ELEVATED,
                        "0 1px 2px color-mix(in srgb, " + INK_REF + " 8%, transparent), 0 8px 22px color-mix(in srgb, " + INK_REF + " 14%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.SUNKEN,
                        "inset 0 1px 2px color-mix(in srgb, " + INK_REF + " 10%, transparent), inset 0 8px 18px -6px color-mix(in srgb, " + INK_REF + " 16%, transparent)")),
            gap(of(Container.Panel.class, Size.Gap.class), "0px", 1.3),
            inset(of(Container.Panel.Head.class, Size.Inset.class), "8px", "14px", 1.3),
            gap(of(Container.Panel.Head.class, Size.Gap.class), "8px", 1.3),
            rule(of(Container.Panel.Head.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            inset(of(Container.Panel.Body.class, Size.Inset.class), "14px", "16px", 1.3),
            gap(of(Container.Panel.Body.class, Size.Gap.class), "10px", 1.3),
            one(of(Dragging.class, Affordance.Cursor.class), "grab"),   // a thing that can be dragged by the hand on it
            one(of(Inline.class, Shape.Corner.class), "2px"),
            rule(of(Inline.class, Shape.Rule.class), "1px", "solid"),

            // ── prose: the document's elements, set by the design ───────
            body(of(Prose.class, Type.Face.class), """
                h1, h2, h3, h4 { font-family: %s; }
                code, pre { font-family: %s; }
                """.formatted(DISPLAY_FACE, MONO_FACE)),
            body(of(Prose.class, Type.Scale.class), """
                font-size: 16px;
                line-height: 1.7;
                h1, h2, h3, h4 { line-height: 1.25; }
                h1 { font-size: 32px; }
                h2 { font-size: 24px; }
                h3 { font-size: 19px; }
                h4 { font-size: 16px; }
                code { font-size: 0.92em; }
                pre { font-size: 13px; line-height: 1.5; }
                pre code { font-size: inherit; }
                table { font-size: 14px; }
                """),
            body(of(Prose.class, Type.Weight.class), "th { font-weight: 700; }\n"),
            body(of(Prose.class, Type.Treatment.class), """
                h4 { letter-spacing: 1px; text-transform: uppercase; }
                blockquote { font-style: italic; }
                """),
            body(of(Prose.class, Type.Decoration.class), "a { text-decoration-line: underline; text-underline-offset: 2px; }\n"),
            body(of(Prose.class, Shape.Rule.class), """
                h1 { border-width: 0 0 2px 0; border-style: solid; }
                blockquote { border-width: 0 0 0 3px; border-style: solid; }
                th, td { border-width: 0 0 1px 0; border-style: solid; }
                th { border-width: 0; }
                hr { border-width: 1px 0 0 0; border-style: solid; }
                """),
            body(of(Prose.class, Shape.Corner.class), """
                code { border-radius: 3px; }
                pre { border-radius: 4px; }
                """)
    );
}
