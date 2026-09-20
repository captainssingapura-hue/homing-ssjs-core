package hue.captains.singapura.js.homing.ui.floating;

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
import static hue.captains.singapura.js.homing.design.Emphasis.Primary;
import static hue.captains.singapura.js.homing.design.Interaction.Dragging;
import static hue.captains.singapura.js.homing.design.Interaction.Focus;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Layer.Inverted;
import static hue.captains.singapura.js.homing.design.Layer.Overlay;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Layer.Recessed;
import static hue.captains.singapura.js.homing.design.Pairing.OnInverted;
import static hue.captains.singapura.js.homing.design.Pairing.OnPrimary;
import static hue.captains.singapura.js.homing.design.Structure.Hairline;
import static hue.captains.singapura.js.homing.design.Target.Affordance;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Size;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Caption;
import static hue.captains.singapura.js.homing.design.Text.Heading;
import static hue.captains.singapura.js.homing.design.Text.Label;

/**
 * The floating pane's styles: the desk it floats on, the frame, the head that
 * moves it, the body that holds what it shows, the grip that sizes it. The
 * frame is {@code Container.Pane.Floating}; its place and its measure are its
 * user's, carried in runtime variables the pane sets, never the design's.
 * Every look is a design word; the bodies hold layout only.
 */
public record FloatingStyles() implements CssGroup<FloatingStyles> {

    public static final FloatingStyles INSTANCE = new FloatingStyles();

    /** The desk: a recessed floor filling its host (a flex box), where the panes are placed. */
    public record fp_desk() implements CssClass<FloatingStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Recessed.class, Color.Surface.class)); }
        @Override public String body() { return """
            position: relative;
            flex: 1 1 auto;
            align-self: stretch;
            min-width: 0;
            min-height: 0;
            overflow: hidden;
            """;
        }
    }

    /**
     * The frame: a raised box above the desk with the overlay's shadow, at the
     * place and measure its user gave it — {@code --fp-x}, {@code --fp-y},
     * {@code --fp-w}, {@code --fp-h} — and on the stack at {@code --fp-z}.
     * Its rule carries the ring on focus; its corner and rule are a container's.
     */
    public record fp_frame() implements CssClass<FloatingStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--fp-x"), new CssVar("--fp-y"), new CssVar("--fp-w"), new CssVar("--fp-h"), new CssVar("--fp-z")); }
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Container.Pane.Floating.class, Shape.Corner.class), of(Container.Pane.Floating.class, Shape.Rule.class), of(Container.Pane.Floating.class, Color.Edge.class),
                           of(Raised.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class),
                           of(Overlay.class, Shape.Shadow.class));
        }
        @Override public String body() { return """
            position: absolute;
            left: var(--fp-x);
            top: var(--fp-y);
            inline-size: var(--fp-w);
            block-size: var(--fp-h);
            z-index: var(--fp-z);
            display: flex;
            flex-direction: column;
            box-sizing: border-box;
            min-width: 0;
            min-height: 0;
            overflow: hidden;
            """;
        }
    }

    /**
     * The frame while the hand is on its head: it wears the interactive word,
     * and since the head is inside it the frame is hovered — so the design's
     * own hover lifts it. Off the head, the class is gone, and the pointer
     * over the body lifts nothing.
     */
    public record fp_hoverable() implements CssClass<FloatingStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Interactive.class, Motion.Transform.class), of(Interactive.class, Shape.Shadow.class), of(Interactive.class, Motion.Ease.class)); }
        @Override public String body() { return ""; }
    }

    /** The frame in the hand: what the design does to a thing being dragged — higher than hover, moving. */
    public record fp_held() implements CssClass<FloatingStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Dragging.class, Motion.Transform.class), of(Dragging.class, Shape.Shadow.class), of(Interactive.class, Motion.Ease.class)); }
        @Override public String body() { return ""; }
    }

    /** The active pane: the ring drawn now, on its edge and as its glow. */
    public record fp_active() implements CssClass<FloatingStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Focus.class, Color.Edge.class), of(Focus.class, Shape.Shadow.class)); }
        @Override public String body() { return ""; }
    }

    /** The head: the inverted bar that names the pane and moves it; a pane's air is its head's. */
    public record fp_head() implements CssClass<FloatingStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Inverted.class, Color.Surface.class), of(OnInverted.class, Color.Ink.class), of(Hairline.class, Color.Edge.class), of(Hairline.class, Shape.Rule.class),
                           of(Container.Pane.Floating.class, Size.Inset.class), of(Container.Pane.Floating.class, Size.Gap.class), of(Dragging.class, Affordance.Cursor.class), of(Heading.class, Type.Face.class));
        }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Container.Pane.Floating.class, Size.Inset.class), of(Container.Pane.Floating.class, Size.Gap.class)); }
        @Override public String body() { return """
            flex: none;
            display: flex;
            align-items: center;
            min-width: 0;
            user-select: none;
            touch-action: none;
            """;
        }
    }

    /** The head while the hand holds it: lit by extent, as the splitter's handle is. */
    public record fp_head_held() implements CssClass<FloatingStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class), of(OnPrimary.class, Color.Ink.class)); }
        @Override public List<? extends Wearable> extents() { return wears(); }
        @Override public String body() { return ""; }
    }

    /** The title, one line, cut with an ellipsis. */
    public record fp_title() implements CssClass<FloatingStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Scale.class), of(Label.class, Type.Weight.class)); }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Label.class, Type.Scale.class)); }
        @Override public String body() { return """
            flex: 1 1 auto;
            min-width: 0;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            """;
        }
    }

    /** The cross: a small control on the inverted bar, its edge the bar's, its ring a control's. */
    public record fp_close() implements CssClass<FloatingStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(OnInverted.class, Color.Ink.class), of(OnInverted.class, Color.Edge.class), of(Control.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Control.class, Color.Edge.class),
                           of(Interactive.class, Affordance.Cursor.class), of(Interactive.class, Motion.Ease.class), of(Inline.class, Size.Inset.class), of(Caption.class, Type.Scale.class));
        }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Inline.class, Size.Inset.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
            flex: none;
            font: inherit;
            line-height: 1;
            background: transparent;
            margin-inline-start: auto;
            """;
        }
    }

    /** The body: what the pane holds, filling what the head leaves, scrolling beyond it. */
    public record fp_body() implements CssClass<FloatingStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-height: 0;
            display: flex;
            flex-direction: column;
            overflow: auto;
            """;
        }
    }

    /** The grip: the corner that sizes the pane; draws nothing, catches the hand. */
    public record fp_grip() implements CssClass<FloatingStyles> {
        @Override public String body() { return """
            position: absolute;
            right: 0;
            bottom: 0;
            width: 16px;
            height: 16px;
            cursor: nwse-resize;
            touch-action: none;
            """;
        }
    }

    @Override
    public List<CssClass<FloatingStyles>> cssClasses() {
        return List.of(new fp_desk(), new fp_frame(), new fp_hoverable(), new fp_held(), new fp_active(), new fp_head(), new fp_head_held(), new fp_title(), new fp_close(), new fp_body(), new fp_grip());
    }
}
