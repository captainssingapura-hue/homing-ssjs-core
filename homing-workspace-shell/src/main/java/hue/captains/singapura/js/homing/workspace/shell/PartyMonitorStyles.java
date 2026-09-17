package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

import static hue.captains.singapura.js.homing.design.Box.Control.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted.*;
import static hue.captains.singapura.js.homing.design.Feedback.Warning.*;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive.*;
import static hue.captains.singapura.js.homing.design.Layer.Base.*;
import static hue.captains.singapura.js.homing.design.Layer.Raised.*;
import static hue.captains.singapura.js.homing.design.Structure.Hairline.*;
import static hue.captains.singapura.js.homing.design.Text.Body.*;
import static hue.captains.singapura.js.homing.design.Text.Caption.*;
import static hue.captains.singapura.js.homing.design.Text.Code.*;
import static hue.captains.singapura.js.homing.design.Text.Heading.*;
import static hue.captains.singapura.js.homing.design.Text.Label.*;

/**
 * The party monitor: a head over a tree of the live DOM parties. Structure
 * only; the head is a raised band, the tree is set in the code face, a
 * leak note is a warning.
 */
public record PartyMonitorStyles() implements CssGroup<PartyMonitorStyles> {
    public static final PartyMonitorStyles INSTANCE = new PartyMonitorStyles();

    public record pm_root() implements CssClass<PartyMonitorStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new caption_type_scale(), new body_color_ink()); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            height: 100%;
            min-height: 0;
            """; }
    }
    public record pm_head() implements CssClass<PartyMonitorStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new hairline_color_edge(), new hairline_shape_rule()); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            """; }
    }
    public record pm_title() implements CssClass<PartyMonitorStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new label_type_weight(), new heading_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record pm_count() implements CssClass<PartyMonitorStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink()); }
        @Override public String body() { return """
            flex: 1;
            """; }
    }
    public record pm_btn() implements CssClass<PartyMonitorStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new base_color_surface(), new body_color_ink(), new raised_color_edge(), new raised_shape_rule(), new control_shape_corner(), new interactive_affordance_cursor()); }
        @Override public String body() { return """
            font: inherit;
            padding: 2px 10px;
            """; }
    }
    public record pm_note() implements CssClass<PartyMonitorStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink(), new caption_type_treatment()); }
        @Override public String body() { return """
            padding: 4px 12px;
            """; }
    }
    /** A leak: the note in the warning ink, upright. Applied beside pm_note; its ink wins by order, and the treatment is the caption's. */
    public record pm_note_leaked() implements CssClass<PartyMonitorStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new warning_color_ink(), new label_type_weight()); }
        @Override public String body() { return ""; }
    }
    public record pm_tree() implements CssClass<PartyMonitorStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new code_type_face()); }
        @Override public String body() { return """
            flex: 1;
            min-height: 0;
            overflow: auto;
            padding: 8px 12px;
            """; }
    }

    @Override
    public List<CssClass<PartyMonitorStyles>> cssClasses() {
        return List.of(
                new pm_root(), new pm_head(), new pm_title(), new pm_count(), new pm_btn(),
                new pm_note(), new pm_note_leaked(), new pm_tree()
        );
    }
}
