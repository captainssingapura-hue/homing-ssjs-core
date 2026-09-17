package hue.captains.singapura.js.homing.studio.base.ui;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.Box.Control.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary.*;
import static hue.captains.singapura.js.homing.design.Interaction.Focus.*;
import static hue.captains.singapura.js.homing.design.Interaction.Inert.*;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive.*;
import static hue.captains.singapura.js.homing.design.Layer.Base.*;
import static hue.captains.singapura.js.homing.design.Layer.Inverted.*;
import static hue.captains.singapura.js.homing.design.Layer.Overlay.*;
import static hue.captains.singapura.js.homing.design.Layer.Raised.*;
import static hue.captains.singapura.js.homing.design.Pairing.OnInverted.*;
import static hue.captains.singapura.js.homing.design.Pairing.OnInvertedMuted.*;
import static hue.captains.singapura.js.homing.design.Pairing.OnPrimary.*;
import static hue.captains.singapura.js.homing.design.Structure.Cap.*;
import static hue.captains.singapura.js.homing.design.Structure.Hairline.*;
import static hue.captains.singapura.js.homing.design.Text.Body.*;
import static hue.captains.singapura.js.homing.design.Text.Caption.*;
import static hue.captains.singapura.js.homing.design.Text.Kicker.*;
import static hue.captains.singapura.js.homing.design.Text.Label.*;

/**
 * The system dialog: a scrim over the page, a frame centred on it with a
 * title bar, a body and an action row. Structure only; the frame wears the
 * base layer and the overlay's shadow, the title bar the inverted layer, and
 * a focused frame wears the focus ring.
 */
public record SystemDialogStyles() implements CssGroup<SystemDialogStyles> {
    public static final SystemDialogStyles INSTANCE = new SystemDialogStyles();

    public record sd_scrim() implements CssClass<SystemDialogStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new overlay_effect_filter()); }
        @Override public String body() { return """
            position: fixed;
            inset: 0;
            z-index: 10010;
            """; }
    }
    /** The frame is centred by its own transform — placement, which is the component's, not motion. */
    public record sd_frame() implements CssClass<SystemDialogStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sd-w"), new CssVar("--sd-h")); }
        @Override public List<CssClass<?>> wears() { return List.of(new base_color_surface(), new body_color_ink(), new raised_color_edge(), new raised_shape_rule(), new raised_shape_corner(), new overlay_shape_shadow()); }
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
        @Override public List<CssClass<?>> wears() { return List.of(new focus_color_edge(), new focus_shape_shadow()); }
        @Override public String body() { return ""; }
    }
    public record sd_title() implements CssClass<SystemDialogStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new inverted_color_surface(), new hairline_color_edge(), new hairline_shape_rule()); }
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
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new on_inverted_muted_color_ink()); }
        @Override public String body() { return """
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            """; }
    }
    public record sd_close() implements CssClass<SystemDialogStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new on_inverted_color_ink(), new interactive_affordance_cursor(), new label_type_scale()); }
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
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new cap_color_edge(), new cap_shape_rule()); }
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
        @Override public List<CssClass<?>> wears() { return List.of(new base_color_surface(), new body_color_ink(), new raised_color_edge(), new raised_shape_rule(), new control_shape_corner(), new caption_type_scale(), new interactive_affordance_cursor()); }
        @Override public String body() { return """
            font: inherit;
            padding: 4px 12px;
            """; }
    }
    /** The primary action: on the primary surface, in its ink. Applied beside sd_action; its surface, ink and edge win by order. */
    public record sd_action_primary() implements CssClass<SystemDialogStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_surface(), new primary_color_edge(), new on_primary_color_ink(), new label_type_weight()); }
        @Override public String body() { return ""; }
    }
    public record sd_action_off() implements CssClass<SystemDialogStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new inert_effect_opacity(), new inert_affordance_cursor()); }
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
