package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

import static hue.captains.singapura.js.homing.design.Box.Control.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted.*;
import static hue.captains.singapura.js.homing.design.Feedback.Danger.*;
import static hue.captains.singapura.js.homing.design.Interaction.Inert.*;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive.*;
import static hue.captains.singapura.js.homing.design.Layer.Base.*;
import static hue.captains.singapura.js.homing.design.Layer.Raised.*;
import static hue.captains.singapura.js.homing.design.Layer.Recessed.*;
import static hue.captains.singapura.js.homing.design.Structure.Cap.*;
import static hue.captains.singapura.js.homing.design.Text.Body.*;
import static hue.captains.singapura.js.homing.design.Text.Caption.*;
import static hue.captains.singapura.js.homing.design.Text.Heading.*;
import static hue.captains.singapura.js.homing.design.Text.Kicker.*;
import static hue.captains.singapura.js.homing.design.Text.Label.*;

/**
 * The workspace switcher's detail pane: a heading, a list of workspaces, a
 * rename row and a maintenance row. Structure only; the list is a recessed
 * field whose edge answers focus, the buttons are edged controls, the danger
 * button wears the danger ink and edge, an unavailable button is inert.
 */
public record WorkspaceSwitcherStyles() implements CssGroup<WorkspaceSwitcherStyles> {
    public static final WorkspaceSwitcherStyles INSTANCE = new WorkspaceSwitcherStyles();

    public record ws_detail() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 8px;
            height: 100%;
            min-height: 0;
            """; }
    }
    public record ws_head() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new label_type_weight(), new heading_color_ink(), new label_type_scale()); }
        @Override public String body() { return ""; }
    }
    public record ws_sub() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale(), new muted_color_ink()); }
        @Override public String body() { return ""; }
    }
    /** The list is a recessed field; its edge answers focus (the raised edge's focus slot). */
    public record ws_list() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new recessed_color_surface(), new raised_color_edge(), new raised_shape_rule(), new control_shape_corner()); }
        @Override public String body() { return """
            flex: 1 1 auto;
            min-height: 0;
            overflow-y: auto;
            padding: 4px 0;
            outline: none;
            """; }
    }
    /** Kept for the switcher's JS, which applies it on focus; the design's focus slot on the edge does the painting. */
    public record ws_list_focus() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String pseudoState() { return ":focus"; }
        @Override public String body() { return ""; }
    }
    public record ws_note() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale(), new muted_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record ws_row() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 8px;
            flex-wrap: wrap;
            """; }
    }
    public record ws_input() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new recessed_color_surface(), new body_color_ink(), new raised_color_edge(), new raised_shape_rule(), new control_shape_corner(), new caption_type_scale()); }
        @Override public String body() { return """
            flex: 1 1 160px;
            min-width: 0;
            font: inherit;
            padding: 4px 8px;
            """; }
    }
    public record ws_btn() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new base_color_surface(), new body_color_ink(), new raised_color_edge(), new raised_shape_rule(), new control_shape_corner(), new caption_type_scale(), new interactive_affordance_cursor()); }
        @Override public String body() { return """
            font: inherit;
            padding: 4px 12px;
            """; }
    }
    /** The destructive action: applied beside ws_btn; the danger ink and edge win by order. */
    public record ws_btn_danger() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new danger_color_ink(), new danger_color_edge()); }
        @Override public String body() { return ""; }
    }
    public record ws_btn_off() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new inert_effect_opacity(), new inert_affordance_cursor()); }
        @Override public String body() { return ""; }
    }
    public record ws_maint() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new cap_color_edge(), new cap_shape_rule()); }
        @Override public String body() { return """
            padding-top: 8px;
            """; }
    }

    @Override
    public List<CssClass<WorkspaceSwitcherStyles>> cssClasses() {
        return List.of(
                new ws_detail(), new ws_head(), new ws_sub(),
                new ws_list(), new ws_list_focus(), new ws_note(),
                new ws_row(), new ws_input(),
                new ws_btn(), new ws_btn_danger(), new ws_btn_off(),
                new ws_maint()
        );
    }
}
