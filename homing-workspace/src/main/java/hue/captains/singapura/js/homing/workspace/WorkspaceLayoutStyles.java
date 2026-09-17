package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

import static hue.captains.singapura.js.homing.design.Box.Control.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Tertiary.*;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive.*;
import static hue.captains.singapura.js.homing.design.Layer.Base.*;
import static hue.captains.singapura.js.homing.design.Layer.Raised.*;
import static hue.captains.singapura.js.homing.design.Structure.Cap.*;
import static hue.captains.singapura.js.homing.design.Structure.Hairline.*;
import static hue.captains.singapura.js.homing.design.Text.Body.*;
import static hue.captains.singapura.js.homing.design.Text.Caption.*;
import static hue.captains.singapura.js.homing.design.Text.Kicker.*;
import static hue.captains.singapura.js.homing.design.Text.Label.*;

/**
 * The workspace frame: a ribbon over the content, a footer under it, and the
 * fullscreen and locked states. Structure only; each element wears what it
 * means — the frame is the base layer edged like a raised thing, the ribbon
 * and footer are raised bands with a line between them and the content, the
 * buttons are ghost controls that show a surface only when hovered.
 */
public record WorkspaceLayoutStyles() implements CssGroup<WorkspaceLayoutStyles> {
    public static final WorkspaceLayoutStyles INSTANCE = new WorkspaceLayoutStyles();

    public record wl_root() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> dependsOn() { return List.of(new StudioStyles.st_main()); }
        @Override public List<CssClass<?>> wears() { return List.of(new base_color_surface(), new raised_color_edge(), new raised_shape_rule(), new raised_shape_corner()); }
        @Override public String body() { return """
                position: relative;
                display: flex;
                flex-direction: column;
                width: 100%;
                height: 100%;
                overflow: hidden;
                box-sizing: border-box;
                """; }
    }
    public record wl_ribbon() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new hairline_color_edge(), new hairline_shape_rule(), new body_color_ink(), new body_type_face(), new caption_type_scale()); }
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 8px;
                padding: 4px 10px;
                flex-shrink: 0;
                """; }
    }
    public record wl_ribbon_title() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new label_type_weight(), new body_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record wl_ribbon_items() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 6px;
                margin-left: 16px;
                flex: 1;
                """; }
    }
    /** A ghost control: no surface and no edge until the hover class beside it says so. */
    public record wl_ribbon_button() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new body_color_ink(), new body_type_face(), new label_type_scale(), new control_shape_corner(), new interactive_affordance_cursor()); }
        @Override public String body() { return """
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 26px;
                height: 22px;
                border: 1px solid transparent;
                background: transparent;
                line-height: 1;
                """; }
    }
    /** The hovered ghost control: the interactive surface and edge, applied by the ribbon while the pointer is over it. */
    public record wl_ribbon_button_hover() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new interactive_color_surface(), new interactive_color_edge()); }
        @Override public String body() { return ""; }
    }
    public record wl_ribbon_separator() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new tertiary_color_surface()); }
        @Override public String body() { return """
                width: 1px;
                height: 16px;
                """; }
    }
    public record wl_ribbon_label() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink(), new caption_type_scale()); }
        @Override public String body() { return ""; }
    }
    public record wl_ribbon_fs() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new body_color_ink(), new body_type_face(), new label_type_scale(), new control_shape_corner(), new interactive_affordance_cursor()); }
        @Override public String body() { return """
                margin-left: auto;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 26px;
                height: 22px;
                border: 1px solid transparent;
                background: transparent;
                line-height: 1;
                """; }
    }
    public record wl_content() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                flex: 1;
                min-height: 0;
                position: relative;
                overflow: hidden;
                """; }
    }
    public record wl_footer() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new cap_color_edge(), new cap_shape_rule(), new muted_color_ink(), new body_type_face(), new kicker_type_scale()); }
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 8px;
                padding: 3px 10px;
                flex-shrink: 0;
                """; }
    }
    public record wl_footer_separator() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new tertiary_color_surface()); }
        @Override public String body() { return """
                width: 1px;
                height: 12px;
                """; }
    }
    public record wl_footer_button() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new body_color_ink(), new body_type_face(), new caption_type_scale(), new control_shape_corner(), new interactive_affordance_cursor()); }
        @Override public String body() { return """
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 22px;
                height: 18px;
                border: 1px solid transparent;
                background: transparent;
                line-height: 1;
                """; }
    }
    public record wl_workspace_active() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return ""; /* marker — rules apply via body selector below */ }
    }
    public record wl_body_locked() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> dependsOn() { return List.of(new StudioStyles.st_root()); }
        @Override public String body() { return """
                height: 100vh !important;
                overflow: hidden !important;
                """; }
    }
    public record wl_fullscreen_active() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return ""; /* marker class — rules live in companion records */ }
    }
    /** The frame gone fullscreen: flush with the viewport, so it wears the base layer's absence of edge and corner. */
    public record wl_root_fullscreen() implements CssClass<WorkspaceLayoutStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new base_shape_rule(), new base_shape_corner()); }
        @Override public String body() { return """
                position: fixed;
                inset: 0;
                z-index: 9000;
                height: 100vh;
                """; }
    }
    public record wl_chrome_hidden() implements CssClass<WorkspaceLayoutStyles> {
        @Override public String body() { return """
                display: none !important;
                """; }
    }

    @Override
    public List<CssClass<WorkspaceLayoutStyles>> cssClasses() {
        return List.of(
                new wl_root(),
                new wl_ribbon(),
                new wl_ribbon_title(),
                new wl_ribbon_items(),
                new wl_ribbon_button(),
                new wl_ribbon_button_hover(),
                new wl_ribbon_separator(),
                new wl_ribbon_label(),
                new wl_ribbon_fs(),
                new wl_content(),
                new wl_footer(),
                new wl_footer_separator(),
                new wl_footer_button(),
                new wl_workspace_active(),
                new wl_body_locked(),
                new wl_fullscreen_active(),
                new wl_root_fullscreen(),
                new wl_chrome_hidden()
        );
    }
}
