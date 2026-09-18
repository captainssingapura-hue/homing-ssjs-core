package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Pairing.*;
import static hue.captains.singapura.js.homing.design.Text.*;


/**
 * The widget picker: a grid of tiles, then a params form. Structure only;
 * a tile is an edged control on the base layer, the primary action is on
 * the primary surface, a disabled tile is inert.
 */
public record WidgetPickerStyles() implements CssGroup<WidgetPickerStyles> {
    public static final WidgetPickerStyles INSTANCE = new WidgetPickerStyles();

    public record hwp_grid() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
                gap: 8px;
                padding: 8px;
                """; }
    }
    public record hwp_group_label() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(Muted.class, Color.Ink.class), of(Body.class, Type.Face.class)); }
        @Override public String body() { return """
                grid-column: 1 / -1;
                padding: 6px 2px 2px;
                """; }
    }
    public record hwp_tile() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Raised.class, Shape.Corner.class), of(Body.class, Color.Ink.class), of(Interactive.class, Affordance.Cursor.class), of(Interactive.class, Motion.Ease.class)); }
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 4px;
                padding: 10px 6px;
                """; }
    }
    public record hwp_tile_disabled() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inert.class, Effect.Opacity.class), of(Inert.class, Affordance.Cursor.class)); }
        @Override public String body() { return ""; }
    }
    public record hwp_tile_icon() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Numeral.class, Type.Scale.class)); }
        @Override public String body() { return ""; }
    }
    public record hwp_tile_label() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
                text-align: center;
                """; }
    }
    public record hwp_tile_desc() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Kicker.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                text-align: center;
                """; }
    }
    public record hwp_form() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                padding: 12px;
                display: flex;
                flex-direction: column;
                gap: 8px;
                """; }
    }
    public record hwp_form_row() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                gap: 3px;
                """; }
    }
    public record hwp_form_label() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    public record hwp_form_input() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
                padding: 5px 8px;
                """; }
    }
    public record hwp_form_actions() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                display: flex;
                justify-content: flex-end;
                gap: 6px;
                margin-top: 4px;
                """; }
    }
    public record hwp_form_btn() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
                padding: 5px 12px;
                """; }
    }
    /** The primary action: applied beside hwp_form_btn; its surface, ink and edge win by order. */
    public record hwp_form_btn_primary() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class), of(Primary.class, Color.Edge.class), of(OnPrimary.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    @Override
    public List<CssClass<WidgetPickerStyles>> cssClasses() {
        return List.of(
                new hwp_grid(),
                new hwp_group_label(),
                new hwp_tile(),
                new hwp_tile_disabled(),
                new hwp_tile_icon(),
                new hwp_tile_label(),
                new hwp_tile_desc(),
                new hwp_form(),
                new hwp_form_row(),
                new hwp_form_label(),
                new hwp_form_input(),
                new hwp_form_actions(),
                new hwp_form_btn(),
                new hwp_form_btn_primary()
        );
    }
}
