package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Box.Container;
import static hue.captains.singapura.js.homing.design.Box.Control;
import static hue.captains.singapura.js.homing.design.Box.Inline;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary;
import static hue.captains.singapura.js.homing.design.Emphasis.Secondary;
import static hue.captains.singapura.js.homing.design.Feedback.Danger;
import static hue.captains.singapura.js.homing.design.Feedback.Success;
import static hue.captains.singapura.js.homing.design.Feedback.Warning;
import static hue.captains.singapura.js.homing.design.Interaction.Dragging;
import static hue.captains.singapura.js.homing.design.Interaction.Focus;
import static hue.captains.singapura.js.homing.design.Interaction.Inert;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Layer.Recessed;
import static hue.captains.singapura.js.homing.design.Structure.Detent;
import static hue.captains.singapura.js.homing.design.Structure.Tick;
import static hue.captains.singapura.js.homing.design.Pairing.OnDanger;
import static hue.captains.singapura.js.homing.design.Pairing.OnPrimary;
import static hue.captains.singapura.js.homing.design.Pairing.OnSecondary;
import static hue.captains.singapura.js.homing.design.Pairing.OnSuccess;
import static hue.captains.singapura.js.homing.design.Pairing.OnWarning;
import static hue.captains.singapura.js.homing.design.Target.Affordance;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Effect;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Size;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Caption;
import static hue.captains.singapura.js.homing.design.Text.Code;
import static hue.captains.singapura.js.homing.design.Text.Heading;
import static hue.captains.singapura.js.homing.design.Text.Kicker;
import static hue.captains.singapura.js.homing.design.Text.Label;
import static hue.captains.singapura.js.homing.design.Text.Link;

/**
 * The elements' classes. Every colour, face, edge, corner and motion is a
 * design word worn; the bodies hold layout and nothing that could be a
 * value. A button is a control that is primary or plain and shows a focus
 * ring, and wears one colour word — primary, secondary, danger, warning,
 * success, or plain — complete: the surface, the ink on it, the edge, all
 * scaled together by the element's extent; a card is a raised box with a
 * heading, a line and a link.
 */
public record ElementStyles() implements CssGroup<ElementStyles> {

    public static final ElementStyles INSTANCE = new ElementStyles();

    // ── Button ────────────────────────────────────────────────────────────────

    /**
     * What every button is: the base button — {@code Control.Button.Base}, the
     * control that completes a task — interactive, labelled. Its rule carries
     * the ring on focus and leaves the edge to the colour word; its density
     * (inset, gap, least width) and its label's type are the design's, and
     * grow with the element's size.
     */
    public record el_button() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Control.Button.Base.class, Shape.Rule.class), of(Control.Button.Base.class, Shape.Corner.class), of(Control.Button.Base.class, Color.Edge.class),
                           of(Control.Button.Base.class, Size.Inset.class), of(Control.Button.Base.class, Size.Gap.class), of(Control.Button.Base.class, Size.Extent.class),
                           of(Label.class, Type.Scale.class), of(Heading.class, Type.Face.class),
                           of(Interactive.class, Affordance.Cursor.class), of(Interactive.class, Motion.Ease.class));
        }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Control.Button.Base.class, Size.Inset.class), of(Control.Button.Base.class, Size.Gap.class), of(Control.Button.Base.class, Size.Extent.class), of(Label.class, Type.Scale.class)); }
        @Override public String body() { return """
            font: inherit;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            """;
        }
    }

    /**
     * The primary button: the accent surface, the ink for it, its edge — a
     * semantic surface complete, every visual surface of it scaled together by
     * the element's extent: at 1 the word as bound, at 0 the design's neutral,
     * at −1 the meaning turned the other way. Each design anchors each.
     */
    public record el_button_primary() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class), of(OnPrimary.class, Color.Ink.class), of(Primary.class, Color.Edge.class)); }
        @Override public List<? extends Wearable> extents() { return wears(); }
        @Override public String body() { return ""; }
    }

    /** The secondary button: the secondary surface, the ink for it, its edge; scaled as the primary is. */
    public record el_button_secondary() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Secondary.class, Color.Surface.class), of(OnSecondary.class, Color.Ink.class), of(Secondary.class, Color.Edge.class)); }
        @Override public List<? extends Wearable> extents() { return wears(); }
        @Override public String body() { return ""; }
    }

    /** The danger button; at −1 the design's safety, whatever that looks like there. */
    public record el_button_danger() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Danger.class, Color.Surface.class), of(OnDanger.class, Color.Ink.class), of(Danger.class, Color.Edge.class)); }
        @Override public List<? extends Wearable> extents() { return wears(); }
        @Override public String body() { return ""; }
    }

    /** The warning button; at −1 the design's calm. */
    public record el_button_warning() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Warning.class, Color.Surface.class), of(OnWarning.class, Color.Ink.class), of(Warning.class, Color.Edge.class)); }
        @Override public List<? extends Wearable> extents() { return wears(); }
        @Override public String body() { return ""; }
    }

    /** The success button; at −1 the design's failure. */
    public record el_button_success() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Success.class, Color.Surface.class), of(OnSuccess.class, Color.Ink.class), of(Success.class, Color.Edge.class)); }
        @Override public List<? extends Wearable> extents() { return wears(); }
        @Override public String body() { return ""; }
    }

    /** The plain button: raised, in body ink. Not a semantic surface; it does not scale. */
    public record el_button_plain() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class)); }
        @Override public String body() { return ""; }
    }

    /**
     * A button that is on: what the design does to an interactive thing on
     * hover and on press — a lift and a shadow, a shift, a glow, a wobble —
     * worn only while on, so an off button neither lifts nor presses.
     */
    public record el_button_on() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Interactive.class, Motion.Transform.class), of(Interactive.class, Shape.Shadow.class)); }
        @Override public String body() { return ""; }
    }

    /** A button that is off: inert, and says so. Toggled beside {@code disabled}, in place of {@link el_button_on}. */
    public record el_button_off() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inert.class, Effect.Opacity.class), of(Inert.class, Affordance.Cursor.class)); }
        @Override public String body() { return ""; }
    }

    // ── Slider ────────────────────────────────────────────────────────────────

    /** The slider: a row of label, rail and readout — {@code Control.Slider} for its air and its gap. */
    public record el_slider() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Control.Slider.class, Size.Inset.class), of(Control.Slider.class, Size.Gap.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            box-sizing: border-box;
            """;
        }
    }

    /** The label before the rail; a caller stacking sliders gives them one label width, {@code --sl-label}, so the rails align. */
    public record el_slider_label() implements CssClass<ElementStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sl-label")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Scale.class)); }
        @Override public String body() { return """
            flex: none;
            inline-size: var(--sl-label, auto);
            """;
        }
    }

    /**
     * The rail: the box the hand works in — the control's measure inline,
     * the least height its knob needs — holding the track, the detent and
     * the knob at their places; the fractions of the length are set on it
     * — {@code --sl-value}, {@code --sl-rest}, {@code --sl-from},
     * {@code --sl-span} — and read by the parts that draw them.
     */
    public record el_slider_rail() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Control.Slider.class, Size.Extent.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
            position: relative;
            flex: none;
            touch-action: none;
            user-select: none;
            """;
        }
    }

    /** The track: the groove, sunk, across the rail at its middle. */
    public record el_slider_track() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Control.Slider.Track.class, Size.Extent.class), of(Control.Slider.Track.class, Shape.Corner.class), of(Control.Slider.Track.class, Shape.Rule.class),
                           of(Recessed.class, Color.Surface.class), of(Muted.class, Color.Edge.class));
        }
        @Override public String body() { return """
            position: absolute;
            inset-inline: 0;
            top: 50%;
            translate: 0 -50%;
            box-sizing: border-box;
            overflow: hidden;
            pointer-events: none;
            """;
        }
    }

    /** The fill: the track's inside from the detent to the value, in the primary surface. */
    public record el_slider_fill() implements CssClass<ElementStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sl-from"), new CssVar("--sl-span")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class)); }
        @Override public String body() { return """
            position: absolute;
            top: 0;
            bottom: 0;
            left: var(--sl-from, 0%);
            width: var(--sl-span, 0%);
            """;
        }
    }

    /** The detent: the notch at the rest, drawn in muted ink at the design's width and height. */
    public record el_slider_detent() implements CssClass<ElementStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sl-rest")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Detent.class, Size.Extent.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            position: absolute;
            top: 50%;
            left: var(--sl-rest, 0%);
            translate: -50% -50%;
            background-color: currentColor;
            pointer-events: none;
            """;
        }
    }

    /**
     * The knob: the box the hand takes and the keys go to — role slider,
     * the focusable part — at the design's extent, ringed on focus by the
     * knob's own rule (no border of its own), lifting under the hand as the
     * design has a dragged thing lift. Its look is its face's.
     */
    public record el_slider_knob() implements CssClass<ElementStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sl-value")); }
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Control.Slider.Knob.class, Size.Extent.class), of(Control.Slider.Knob.class, Shape.Rule.class), of(Control.class, Color.Edge.class),
                           of(Dragging.class, Affordance.Cursor.class), of(Interactive.class, Motion.Ease.class));
        }
        @Override public String body() { return """
            position: absolute;
            top: 50%;
            left: var(--sl-value, 0%);
            translate: -50% -50%;
            box-sizing: border-box;
            outline: none;
            """;
        }
    }

    /**
     * The face: the knob's silhouette and surface — raised, the control's
     * rule and edge by lineage, the design's corner, and its clip, which
     * cuts it to whatever the design draws a knob as — or its absence: a
     * design whose knob is the mark alone gives the face no opacity, and
     * the glyph sits on the track by itself. It fills the box, under the
     * mark.
     */
    public record el_slider_face() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Control.Slider.Knob.class, Shape.Corner.class), of(Control.Slider.Knob.class, Shape.Clip.class), of(Control.Slider.Knob.class, Effect.Opacity.class),
                           of(Control.class, Shape.Rule.class), of(Raised.class, Color.Surface.class), of(Control.class, Color.Edge.class), of(Raised.class, Shape.Shadow.class),
                           of(Interactive.class, Motion.Ease.class));
        }
        @Override public String body() { return """
            position: absolute;
            inset: 0;
            box-sizing: border-box;
            """;
        }
    }

    /** The mark on the knob: an {@code Icon}, centred over the face, at the size the design gives a knob's mark — the knob's own, when it is the knob. */
    public record el_slider_mark() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Control.Slider.Knob.class, Type.Scale.class)); }
        @Override public String body() { return """
            position: absolute;
            top: 50%;
            left: 50%;
            translate: -50% -50%;
            """;
        }
    }

    /** The knob in the hand: the dragged word's lift, on the box, so the face and the mark rise together. */
    public record el_slider_held() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Dragging.class, Motion.Transform.class)); }
        @Override public String body() { return ""; }
    }

    /** The face in the hand: the dragged word's shadow — nothing, when the face is not there. */
    public record el_slider_face_held() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Dragging.class, Shape.Shadow.class)); }
        @Override public String body() { return ""; }
    }

    /** The knob of the current slider of a group: the ring drawn now, whether or not the knob has the focus. */
    public record el_slider_knob_current() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Focus.class, Shape.Rule.class), of(Focus.class, Color.Edge.class)); }
        @Override public String body() { return ""; }
    }

    /** The readout after the rail: the value as the caller formats it, in a steady face, right-aligned. */
    public record el_slider_readout() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Scale.class), of(Code.class, Type.Face.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            flex: none;
            min-width: 4ch;
            text-align: end;
            white-space: nowrap;
            """;
        }
    }

    // ── Slider, vertical: a fader ─────────────────────────────────────────────
    //
    // The same words, turned: the rail takes a vertical writing mode, so the
    // design's extent — its length inline, its room across — stands up with
    // it, and the parts are placed from the bottom by the same fractions. A
    // vertical slider's knob is the cap, wide across the track and low along
    // it; the mark on it is set upright again.

    /** The slider stood up: label above, rail, readout below. */
    public record el_slider_vertical() implements CssClass<ElementStyles> {
        @Override public String body() { return """
            flex-direction: column;
            align-items: center;
            """;
        }
    }

    /** The rail stood up: the design's inline length runs downward, its block room across. */
    public record el_slider_rail_vertical() implements CssClass<ElementStyles> {
        @Override public String body() { return "writing-mode: vertical-lr;"; }
    }

    /** A rail with a scale beside it leaves the room the ticks' captions take, after it in the block direction: below a flat one, beside a standing one. */
    public record el_slider_rail_ticked() implements CssClass<ElementStyles> {
        @Override public String body() { return "margin-block-end: 2.2em;"; }
    }

    /** The track stood up: across the rail's middle, top to bottom. */
    public record el_slider_track_vertical() implements CssClass<ElementStyles> {
        @Override public String body() { return """
            inset-inline: auto;
            top: 0;
            bottom: 0;
            left: 50%;
            translate: -50% 0;
            """;
        }
    }

    /** The fill stood up: from the detent's height to the value's. */
    public record el_slider_fill_vertical() implements CssClass<ElementStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sl-from"), new CssVar("--sl-span")); }
        @Override public String body() { return """
            left: 0;
            right: 0;
            top: auto;
            width: auto;
            bottom: var(--sl-from, 0%);
            height: var(--sl-span, 0%);
            """;
        }
    }

    /** The detent stood up: at its height, across the track. */
    public record el_slider_detent_vertical() implements CssClass<ElementStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sl-rest")); }
        @Override public String body() { return """
            top: auto;
            left: 50%;
            bottom: var(--sl-rest, 0%);
            translate: -50% 50%;
            """;
        }
    }

    /**
     * The cap: a vertical slider's knob — the box the hand takes and the
     * keys go to, at the cap's extent (low along the track, wide across it),
     * ringed on focus by the cap's own rule, placed at the value's height.
     */
    public record el_slider_cap() implements CssClass<ElementStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sl-value")); }
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Control.Slider.Cap.class, Size.Extent.class), of(Control.Slider.Cap.class, Shape.Rule.class), of(Control.class, Color.Edge.class),
                           of(Dragging.class, Affordance.Cursor.class), of(Interactive.class, Motion.Ease.class));
        }
        @Override public String body() { return """
            position: absolute;
            left: 50%;
            bottom: var(--sl-value, 0%);
            translate: -50% 50%;
            box-sizing: border-box;
            display: flex;
            align-items: center;
            justify-content: center;
            outline: none;
            """;
        }
    }

    /** The cap's face: always there — a fader is held by its cap — raised, the control's rule and edge, the cap's corner. */
    public record el_slider_cap_face() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Control.Slider.Cap.class, Shape.Corner.class), of(Control.class, Shape.Rule.class),
                           of(Raised.class, Color.Surface.class), of(Control.class, Color.Edge.class), of(Raised.class, Shape.Shadow.class), of(Interactive.class, Motion.Ease.class));
        }
        @Override public String body() { return """
            position: absolute;
            inset: 0;
            box-sizing: border-box;
            """;
        }
    }

    /** The mark on the cap, upright again, at the size the design gives a cap's mark. */
    public record el_slider_cap_mark() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Control.Slider.Cap.class, Type.Scale.class)); }
        @Override public String body() { return """
            position: relative;
            writing-mode: horizontal-tb;
            """;
        }
    }

    /**
     * A tick of a scale: a line at a value, its caption beside it; placed
     * after the rail in the block direction — below a flat slider, in a
     * column; beside a standing one, in a row. Upright whatever the rail.
     */
    public record el_slider_tick() implements CssClass<ElementStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sl-tick")); }
        @Override public String body() { return """
            position: absolute;
            left: var(--sl-tick, 0%);
            top: 100%;
            translate: -50% 0;
            margin-top: 2px;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 2px;
            writing-mode: horizontal-tb;
            pointer-events: none;
            """;
        }
    }

    /** A tick beside a standing rail: at its height, to the right, a row. */
    public record el_slider_tick_vertical() implements CssClass<ElementStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sl-tick")); }
        @Override public String body() { return """
            left: 100%;
            top: auto;
            bottom: var(--sl-tick, 0%);
            translate: 0 50%;
            margin-top: 0;
            margin-left: 3px;
            flex-direction: row;
            gap: 4px;
            """;
        }
    }

    /** The tick's line: the design's tick, along and across the track, in muted ink. */
    public record el_slider_tick_line() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Tick.class, Size.Extent.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            flex: none;
            background-color: currentColor;
            """;
        }
    }

    /** The tick's line beside a standing rail: turned with it, so along is up and across is level. */
    public record el_slider_tick_line_vertical() implements CssClass<ElementStyles> {
        @Override public String body() { return "writing-mode: vertical-lr;"; }
    }

    /** The tick's caption: its number, small and muted, in a steady face. */
    public record el_slider_tick_label() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Code.class, Type.Face.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            line-height: 1;
            white-space: nowrap;
            """;
        }
    }

    /** A slider that is off: inert, and says so. */
    public record el_slider_off() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inert.class, Effect.Opacity.class), of(Inert.class, Affordance.Cursor.class)); }
        @Override public String body() { return "pointer-events: none;"; }
    }

    // ── Slider group ──────────────────────────────────────────────────────────

    /**
     * The group: sliders that share the keys, one holder in the party for
     * all of them and one of them current. A framed column, the header then
     * the body, wearing the container's frame and the pane's room, so a
     * design draws it as it draws a pane.
     */
    public record el_slider_group() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Container.class, Shape.Corner.class), of(Container.class, Shape.Rule.class), of(Container.class, Color.Edge.class),
                           of(Container.Pane.class, Size.Inset.class), of(Container.Pane.class, Size.Gap.class),
                           of(Raised.class, Color.Surface.class), of(Interactive.class, Motion.Ease.class));
        }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Container.Pane.class, Size.Inset.class), of(Container.Pane.class, Size.Gap.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            box-sizing: border-box;
            min-width: 0;
            outline: none;
            """;
        }
    }

    /** The group holding the keys: the ring drawn now, on the frame. */
    public record el_slider_group_held() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Focus.class, Shape.Rule.class), of(Focus.class, Color.Edge.class)); }
        @Override public String body() { return ""; }
    }

    /** The header: the title; a press on it makes the group the holder of the keys. */
    public record el_slider_group_header() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Interactive.class, Affordance.Cursor.class), of(Container.Pane.class, Size.Gap.class)); }
        @Override public String body() { return """
            flex: none;
            display: flex;
            align-items: baseline;
            min-width: 0;
            user-select: none;
            """;
        }
    }

    /** The title in the header: a heading. */
    public record el_slider_group_title() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Face.class), of(Heading.class, Type.Scale.class), of(Heading.class, Type.Weight.class), of(Heading.class, Color.Ink.class)); }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Heading.class, Type.Scale.class)); }
        @Override public String body() { return """
            margin: 0;
            min-width: 0;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            """;
        }
    }

    /** The body: the sliders, stacked. */
    public record el_slider_group_body() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Container.Pane.class, Size.Gap.class)); }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Container.Pane.class, Size.Gap.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            min-width: 0;
            """;
        }
    }

    /** The body laid across: the sliders side by side, as faders on a mixer, each with the room its scale takes. */
    public record el_slider_group_body_across() implements CssClass<ElementStyles> {
        @Override public String body() { return """
            flex-direction: row;
            align-items: flex-start;
            flex-wrap: wrap;
            """;
        }
    }

    // ── Card ──────────────────────────────────────────────────────────────────

    /**
     * The card: {@code Container.Card.Base} — a raised box whose measure is its
     * own: the design's inline size, grown by the element's size, its block
     * size following its aspect — square at 0, the design's widest at +1, its
     * tallest at −1; a host may cap it, never stretch it. What is inside fits it: the head and the foot are fixed, the
     * body takes the rest and scrolls. Hover, press and focus as the design
     * gives an interactive thing; whether a press does anything is the caller's,
     * and only a card with an action is focusable ({@link el_card_action}).
     */
    public record el_card() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Container.Card.Base.class, Shape.Corner.class), of(Container.Card.Base.class, Shape.Rule.class), of(Container.Card.Base.class, Color.Edge.class),
                           of(Container.Card.Base.class, Size.Inset.class), of(Container.Card.Base.class, Size.Gap.class), of(Container.Card.Base.class, Size.Extent.class), of(Container.Card.Base.class, Size.Proportion.class),
                           of(Raised.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Effect.Filter.class),
                           of(Interactive.class, Motion.Transform.class), of(Interactive.class, Shape.Shadow.class), of(Interactive.class, Motion.Ease.class));
        }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Container.Card.Base.class, Size.Inset.class), of(Container.Card.Base.class, Size.Gap.class), of(Container.Card.Base.class, Size.Extent.class)); }
        @Override public List<? extends Wearable> aspects() { return List.of(of(Container.Card.Base.class, Size.Proportion.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            box-sizing: border-box;
            flex: none;
            max-inline-size: 100%;
            min-height: 0;
            overflow: hidden;
            """;
        }
    }

    /** A card with an action: a pointer over it; the caller made it focusable. */
    public record el_card_action() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return ""; }
    }

    /** The head: the title and, beside it, a badge; fixed. */
    public record el_card_head() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Container.Card.Base.class, Size.Gap.class)); }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Container.Card.Base.class, Size.Gap.class)); }
        @Override public String body() { return """
            flex: none;
            display: flex;
            align-items: baseline;
            min-width: 0;
            """;
        }
    }

    public record el_card_title() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Face.class), of(Heading.class, Type.Scale.class), of(Heading.class, Type.Weight.class), of(Heading.class, Color.Ink.class)); }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Heading.class, Type.Scale.class)); }
        @Override public String body() { return """
            margin: 0;
            min-width: 0;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            """;
        }
    }

    /** A short upper-case tag beside a title: an inline box on the secondary surface, as the studio's badges are. */
    public record el_badge() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Secondary.class, Color.Surface.class), of(Heading.class, Color.Ink.class), of(Inline.class, Size.Inset.class), of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(Inline.class, Shape.Corner.class)); }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Inline.class, Size.Inset.class), of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return """
            margin-left: auto;
            flex: none;
            """;
        }
    }

    /** The body: what is inside, bounded by the card — it takes what the head and the foot leave and scrolls beyond it. */
    public record el_card_body() implements CssClass<ElementStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-height: 0;
            overflow: auto;
            """;
        }
    }

    public record el_card_text() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return "margin: 0;"; }
    }

    /** The foot: the link, and later the actions; fixed at the bottom. */
    public record el_card_foot() implements CssClass<ElementStyles> {
        @Override public String body() { return """
            flex: none;
            display: flex;
            align-items: baseline;
            """;
        }
    }

    /** The card's link: a link, its ring the link's own on focus. */
    public record el_card_link() implements CssClass<ElementStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Link.class, Color.Ink.class), of(Link.class, Type.Decoration.class), of(Link.class, Motion.Ease.class), of(Caption.class, Type.Scale.class)); }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return ""; }
    }

    @Override
    public List<CssClass<ElementStyles>> cssClasses() {
        return List.of(new el_button(), new el_button_primary(), new el_button_secondary(), new el_button_danger(), new el_button_warning(), new el_button_success(),
                       new el_button_plain(), new el_button_on(), new el_button_off(),
                       new el_slider(), new el_slider_label(), new el_slider_rail(), new el_slider_track(), new el_slider_fill(), new el_slider_detent(),
                       new el_slider_knob(), new el_slider_face(), new el_slider_mark(), new el_slider_held(), new el_slider_face_held(), new el_slider_knob_current(), new el_slider_readout(),
                       new el_slider_vertical(), new el_slider_rail_vertical(), new el_slider_rail_ticked(), new el_slider_track_vertical(), new el_slider_fill_vertical(), new el_slider_detent_vertical(),
                       new el_slider_cap(), new el_slider_cap_face(), new el_slider_cap_mark(), new el_slider_tick(), new el_slider_tick_vertical(), new el_slider_tick_line(), new el_slider_tick_line_vertical(), new el_slider_tick_label(),
                       new el_slider_off(),
                       new el_slider_group(), new el_slider_group_held(), new el_slider_group_header(), new el_slider_group_title(), new el_slider_group_body(), new el_slider_group_body_across(),
                       new el_card(), new el_card_action(), new el_card_head(), new el_card_title(), new el_badge(), new el_card_body(), new el_card_text(), new el_card_foot(), new el_card_link());
    }
}
