package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Interaction.Focus;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Layer.Base;
import static hue.captains.singapura.js.homing.design.Layer.Inverted;
import static hue.captains.singapura.js.homing.design.Layer.Overlay;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Pairing.OnInverted;
import static hue.captains.singapura.js.homing.design.Pairing.OnInvertedMuted;
import static hue.captains.singapura.js.homing.design.Structure.Cap;
import static hue.captains.singapura.js.homing.design.Structure.Hairline;
import static hue.captains.singapura.js.homing.design.Target.Affordance;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Effect;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Kicker;
import static hue.captains.singapura.js.homing.design.Text.Label;

/**
 * The dialog's classes: a scrim, a frame with a title bar, a body and an
 * action row. Every colour, edge, corner, shadow and filter is a design word
 * worn; the frame's size is data, set per open through two runtime vars.
 * The action buttons are the elements' own and wear nothing here.
 *
 * <p>Prefixed {@code dl_}, not {@code sd_}: the studio's dialog and this one
 * will share a page while the studio swaps, and a class name is a class
 * name.</p>
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

    /** The frame, centred; its size is data. */
    public record dl_frame() implements CssClass<DialogStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--dl-w"), new CssVar("--dl-h")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Raised.class, Effect.Filter.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class), of(Overlay.class, Shape.Shadow.class)); }
        @Override public String body() { return """
            position: fixed;
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
            width: var(--dl-w, 640px);
            height: var(--dl-h, 400px);
            max-width: calc(100vw - 24px);
            max-height: calc(100vh - 24px);
            z-index: 10011;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            outline: none;
            """;
        }
    }

    /** A modal frame glows: the focus ring and its shadow, on the whole dialog. */
    public record dl_glow() implements CssClass<DialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Focus.class, Color.Edge.class), of(Focus.class, Shape.Shadow.class)); }
        @Override public String body() { return ""; }
    }

    /** The title bar: inverted, a hairline under it. */
    public record dl_title() implements CssClass<DialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inverted.class, Color.Surface.class), of(Hairline.class, Color.Edge.class), of(Hairline.class, Shape.Rule.class)); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            height: 28px;
            padding: 0 12px;
            flex-shrink: 0;
            user-select: none;
            """;
        }
    }

    public record dl_title_label() implements CssClass<DialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(OnInvertedMuted.class, Color.Ink.class)); }
        @Override public String body() { return """
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            """;
        }
    }

    /** The close cross on the bar. */
    public record dl_close() implements CssClass<DialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(OnInverted.class, Color.Ink.class), of(Interactive.class, Affordance.Cursor.class), of(Label.class, Type.Scale.class)); }
        @Override public String body() { return """
            font: inherit;
            line-height: 1;
            padding: 0 4px;
            background: transparent;
            border: 0;
            """;
        }
    }

    /** The body: whatever the content builds, scrolling on its own. */
    public record dl_body() implements CssClass<DialogStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-height: 0;
            display: flex;
            flex-direction: column;
            overflow: auto;
            """;
        }
    }

    /** The action row: raised, capped off from the body, buttons to the right. */
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
        return List.of(new dl_scrim(), new dl_frame(), new dl_glow(),
                       new dl_title(), new dl_title_label(), new dl_close(), new dl_body(), new dl_actions());
    }
}
