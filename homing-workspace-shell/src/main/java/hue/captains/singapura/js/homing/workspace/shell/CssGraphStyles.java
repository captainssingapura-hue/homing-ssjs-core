package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

import static hue.captains.singapura.js.homing.design.Box.Control.*;
import static hue.captains.singapura.js.homing.design.Box.Inline.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary.*;
import static hue.captains.singapura.js.homing.design.Feedback.Danger.*;
import static hue.captains.singapura.js.homing.design.Feedback.Success.*;
import static hue.captains.singapura.js.homing.design.Interaction.Inert.*;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive.*;
import static hue.captains.singapura.js.homing.design.Layer.Base.*;
import static hue.captains.singapura.js.homing.design.Layer.Raised.*;
import static hue.captains.singapura.js.homing.design.Layer.Recessed.*;
import static hue.captains.singapura.js.homing.design.Pairing.OnPrimary.*;
import static hue.captains.singapura.js.homing.design.Structure.Hairline.*;
import static hue.captains.singapura.js.homing.design.Text.Body.*;
import static hue.captains.singapura.js.homing.design.Text.Caption.*;
import static hue.captains.singapura.js.homing.design.Text.Heading.*;
import static hue.captains.singapura.js.homing.design.Text.Kicker.*;
import static hue.captains.singapura.js.homing.design.Text.Label.*;
import static hue.captains.singapura.js.homing.design.Text.Link.*;

/**
 * The CSS graph workbench: a head, a bar of controls, and the waves of nodes.
 * Structure only; a node is a raised card, a badge is an inline mark, a
 * prior badge is on the primary surface, a sheet's state is feedback.
 */
public record CssGraphStyles() implements CssGroup<CssGraphStyles> {
    public static final CssGraphStyles INSTANCE = new CssGraphStyles();

    public record cg_root() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new caption_type_scale(), new body_color_ink()); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            height: 100%;
            min-height: 0;
            """; }
    }
    public record cg_head() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new hairline_color_edge(), new hairline_shape_rule()); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            flex: 0 0 auto;
            """; }
    }
    public record cg_title() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new heading_type_weight()); }
        @Override public String body() { return ""; }
    }
    public record cg_worn() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink()); }
        @Override public String body() { return """
            margin-left: auto;
            """; }
    }
    public record cg_btn() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new body_color_ink(), new raised_color_edge(), new raised_shape_rule(), new control_shape_corner(), new kicker_type_scale(), new interactive_affordance_cursor()); }
        @Override public String body() { return """
            font: inherit;
            padding: 2px 8px;
            """; }
    }
    public record cg_bar() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new hairline_color_edge(), new hairline_shape_rule()); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            flex: 0 0 auto;
            """; }
    }
    public record cg_select() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new base_color_surface(), new body_color_ink(), new raised_color_edge(), new raised_shape_rule(), new control_shape_corner(), new kicker_type_scale()); }
        @Override public String body() { return """
            font: inherit;
            padding: 2px 4px;
            """; }
    }
    public record cg_note() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink()); }
        @Override public String body() { return """
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            """; }
    }
    public record cg_note_err() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new danger_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record cg_waves() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            display: flex;
            gap: 12px;
            padding: 12px;
            overflow: auto;
            flex: 1 1 auto;
            align-items: flex-start;
            """; }
    }
    public record cg_wave() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 8px;
            min-width: 180px;
            """; }
    }
    public record cg_wave_head() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new muted_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record cg_node() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new raised_color_edge(), new raised_shape_rule(), new control_shape_corner()); }
        @Override public String body() { return """
            padding: 8px;
            display: flex;
            flex-direction: column;
            gap: 2px;
            """; }
    }
    public record cg_node_id() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new label_type_weight()); }
        @Override public String body() { return """
            word-break: break-all;
            """; }
    }
    public record cg_badge() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale(), new kicker_type_treatment(), new inline_shape_corner(), new recessed_color_surface(), new muted_color_ink()); }
        @Override public String body() { return """
            display: inline-block;
            padding: 0 4px;
            margin-right: 4px;
            """; }
    }
    /** The prior's badge: applied beside cg_badge; the primary surface and ink win by order. */
    public record cg_badge_prior() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_surface(), new on_primary_color_ink()); }
        @Override public String body() { return ""; }
    }
    /** An unknown node's badge: no surface, a dashed inline edge. */
    public record cg_badge_unknown() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_edge(), new inline_shape_rule()); }
        @Override public String body() { return """
            background: transparent;
            """; }
    }
    public record cg_deps() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink(), new kicker_type_scale()); }
        @Override public String body() { return ""; }
    }
    public record cg_sheets() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale()); }
        @Override public String body() { return ""; }
    }
    public record cg_sheet() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new link_motion_ease()); }
        @Override public String body() { return """
            display: inline-block;
            margin-right: 8px;
            """; }
    }
    public record cg_sheet_pending() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new inert_effect_opacity(), new caption_type_treatment()); }
        @Override public String body() { return ""; }
    }
    public record cg_sheet_landed() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record cg_sheet_applied() implements CssClass<CssGraphStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new success_color_ink(), new label_type_weight()); }
        @Override public String body() { return ""; }
    }

    @Override
    public List<CssClass<CssGraphStyles>> cssClasses() {
        return List.of(
                new cg_root(), new cg_head(), new cg_title(), new cg_worn(), new cg_btn(),
                new cg_bar(), new cg_select(), new cg_note(), new cg_note_err(),
                new cg_waves(), new cg_wave(), new cg_wave_head(),
                new cg_node(), new cg_node_id(), new cg_badge(), new cg_badge_prior(), new cg_badge_unknown(),
                new cg_deps(), new cg_sheets(), new cg_sheet(), new cg_sheet_pending(), new cg_sheet_landed(), new cg_sheet_applied()
        );
    }
}
