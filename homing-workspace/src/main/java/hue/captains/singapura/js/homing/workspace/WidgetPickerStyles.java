package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

import static hue.captains.singapura.js.homing.design.Box.Control.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary.*;
import static hue.captains.singapura.js.homing.design.Interaction.Inert.*;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive.*;
import static hue.captains.singapura.js.homing.design.Layer.Base.*;
import static hue.captains.singapura.js.homing.design.Layer.Raised.*;
import static hue.captains.singapura.js.homing.design.Pairing.OnPrimary.*;
import static hue.captains.singapura.js.homing.design.Text.Body.*;
import static hue.captains.singapura.js.homing.design.Text.Caption.*;
import static hue.captains.singapura.js.homing.design.Text.Kicker.*;
import static hue.captains.singapura.js.homing.design.Text.Label.*;
import static hue.captains.singapura.js.homing.design.Text.Numeral.*;

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
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new muted_color_ink(), new body_type_face()); }
        @Override public String body() { return """
                grid-column: 1 / -1;
                padding: 6px 2px 2px;
                """; }
    }
    public record hwp_tile() implements CssClass<WidgetPickerStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new base_color_surface(), new raised_color_edge(), new raised_shape_rule(), new raised_shape_corner(), new body_color_ink(), new interactive_affordance_cursor(), new interactive_motion_ease()); }
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 4px;
                padding: 10px 6px;
                """; }
    }
    public record hwp_tile_disabled() implements CssClass<WidgetPickerStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new inert_effect_opacity(), new inert_affordance_cursor()); }
        @Override public String body() { return ""; }
    }
    public record hwp_tile_icon() implements CssClass<WidgetPickerStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new numeral_type_scale()); }
        @Override public String body() { return ""; }
    }
    public record hwp_tile_label() implements CssClass<WidgetPickerStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new body_type_face(), new caption_type_scale()); }
        @Override public String body() { return """
                text-align: center;
                """; }
    }
    public record hwp_tile_desc() implements CssClass<WidgetPickerStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new body_type_face(), new kicker_type_scale(), new muted_color_ink()); }
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
        @Override public List<CssClass<?>> wears() { return List.of(new body_type_face(), new caption_type_scale(), new muted_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record hwp_form_input() implements CssClass<WidgetPickerStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new base_color_surface(), new raised_color_edge(), new raised_shape_rule(), new control_shape_corner(), new body_color_ink(), new body_type_face(), new caption_type_scale()); }
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
        @Override public List<CssClass<?>> wears() { return List.of(new base_color_surface(), new raised_color_edge(), new raised_shape_rule(), new control_shape_corner(), new body_color_ink(), new body_type_face(), new caption_type_scale(), new interactive_affordance_cursor()); }
        @Override public String body() { return """
                padding: 5px 12px;
                """; }
    }
    /** The primary action: applied beside hwp_form_btn; its surface, ink and edge win by order. */
    public record hwp_form_btn_primary() implements CssClass<WidgetPickerStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_surface(), new primary_color_edge(), new on_primary_color_ink()); }
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
