package hue.captains.singapura.js.homing.studio.base.ui;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;
import hue.captains.singapura.js.homing.core.CssVar;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Pairing.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;


/**
 * The system dialog: a scrim over the page, a frame centred on it with a
 * title bar, a body and an action row. Structure only; the frame wears the
 * base layer and the overlay's shadow, the title bar the inverted layer, and
 * a focused frame wears the focus ring.
 */
public record SystemDialogStyles() implements CssGroup<SystemDialogStyles> {
    public static final SystemDialogStyles INSTANCE = new SystemDialogStyles();

    public record sd_scrim() implements CssClass<SystemDialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Overlay.class, Effect.Filter.class)); }
        @Override public String body() { return """
            position: fixed;
            inset: 0;
            z-index: 10010;
            """; }
    }
    /** The frame is centred by its own transform — placement, which is the component's, not motion. */
    public record sd_frame() implements CssClass<SystemDialogStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sd-w"), new CssVar("--sd-h")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class), of(Overlay.class, Shape.Shadow.class)); }
        @Override public String body() { return """
            position: fixed;
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
            width: var(--sd-w, 640px);
            height: var(--sd-h, 400px);
            z-index: 10011;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            outline: none;
            """; }
    }
    /** The focused frame: the focus ring, as edge and glow. Applied beside sd_frame. */
    public record sd_glow() implements CssClass<SystemDialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Focus.class, Color.Edge.class), of(Focus.class, Shape.Shadow.class)); }
        @Override public String body() { return ""; }
    }
    public record sd_title() implements CssClass<SystemDialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inverted.class, Color.Surface.class), of(Hairline.class, Color.Edge.class), of(Hairline.class, Shape.Rule.class)); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            height: 28px;
            padding: 0 12px;
            flex-shrink: 0;
            user-select: none;
            """; }
    }
    public record sd_title_label() implements CssClass<SystemDialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(OnInvertedMuted.class, Color.Ink.class)); }
        @Override public String body() { return """
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            """; }
    }
    public record sd_close() implements CssClass<SystemDialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(OnInverted.class, Color.Ink.class), of(Interactive.class, Affordance.Cursor.class), of(Label.class, Type.Scale.class)); }
        @Override public String body() { return """
            font: inherit;
            line-height: 1;
            padding: 0 4px;
            background: transparent;
            border: 0;
            """; }
    }
    public record sd_body() implements CssClass<SystemDialogStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-height: 0;
            display: flex;
            flex-direction: column;
            overflow: auto;
            """; }
    }
    public record sd_actions() implements CssClass<SystemDialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Cap.class, Color.Edge.class), of(Cap.class, Shape.Rule.class)); }
        @Override public String body() { return """
            flex: 0 0 auto;
            display: flex;
            align-items: center;
            justify-content: flex-end;
            gap: 8px;
            padding: 8px 12px;
            """; }
    }
    public record sd_action() implements CssClass<SystemDialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Caption.class, Type.Scale.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
            font: inherit;
            padding: 4px 12px;
            """; }
    }
    /** The primary action: on the primary surface, in its ink. Applied beside sd_action; its surface, ink and edge win by order. */
    public record sd_action_primary() implements CssClass<SystemDialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class), of(Primary.class, Color.Edge.class), of(OnPrimary.class, Color.Ink.class), of(Label.class, Type.Weight.class)); }
        @Override public String body() { return ""; }
    }
    public record sd_action_off() implements CssClass<SystemDialogStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inert.class, Effect.Opacity.class), of(Inert.class, Affordance.Cursor.class)); }
        @Override public String body() { return ""; }
    }

    @Override
    public List<CssClass<SystemDialogStyles>> cssClasses() {
        return List.of(
                new sd_scrim(), new sd_frame(), new sd_glow(),
                new sd_title(), new sd_title_label(), new sd_close(),
                new sd_body(),
                new sd_actions(), new sd_action(), new sd_action_primary(), new sd_action_off()
        );
    }
}
