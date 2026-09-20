package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Layer.Overlay;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Structure.Cap;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Effect;
import static hue.captains.singapura.js.homing.design.Target.Shape;

/**
 * The dialog's own styles — what a dialog adds to the floating pane it is
 * built on: the scrim that owns the page behind a modal one, the layer over
 * the viewport that is its desk of one, the float that takes the hand
 * through that layer, and the action foot. The frame, the head, the body and
 * the grip are the pane's.
 */
public record DialogStyles() implements CssGroup<DialogStyles> {

    public static final DialogStyles INSTANCE = new DialogStyles();

    /** Over everything but the frame; the filter is the overlay's word. */
    public record dl_scrim() implements CssClass<DialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Overlay.class, Effect.Filter.class)); }
        @Override public String body() { return """
            position: fixed;
            inset: 0;
            z-index: 10010;
            """;
        }
    }

    /** The layer the dialog floats in: the viewport, as a desk of one; the hand passes through it to the page unless it lands on the float. */
    public record dl_layer() implements CssClass<DialogStyles> {
        @Override public String body() { return """
            position: fixed;
            inset: 0;
            z-index: 10011;
            pointer-events: none;
            """;
        }
    }

    /** The dialog's pane in the layer: the hand lands on it. */
    public record dl_float() implements CssClass<DialogStyles> {
        @Override public String body() { return "pointer-events: auto;"; }
    }

    /** The action foot: raised, capped off from the body, buttons to the right. */
    public record dl_actions() implements CssClass<DialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Cap.class, Color.Edge.class), of(Cap.class, Shape.Rule.class)); }
        @Override public String body() { return """
            flex: 0 0 auto;
            display: flex;
            align-items: center;
            justify-content: flex-end;
            gap: 8px;
            padding: 8px 12px;
            """;
        }
    }

    @Override
    public List<CssClass<DialogStyles>> cssClasses() {
        return List.of(new dl_scrim(), new dl_layer(), new dl_float(), new dl_actions());
    }
}
