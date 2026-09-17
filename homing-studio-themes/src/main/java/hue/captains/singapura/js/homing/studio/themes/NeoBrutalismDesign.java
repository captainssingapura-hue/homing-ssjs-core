package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.ImplProvider;
import hue.captains.singapura.js.homing.design.State;

import java.util.List;

import static hue.captains.singapura.js.homing.design.Box.Control.*;
import static hue.captains.singapura.js.homing.design.Box.Inline.*;
import static hue.captains.singapura.js.homing.design.Brand.House.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Secondary.*;
import static hue.captains.singapura.js.homing.design.Emphasis.Tertiary.*;
import static hue.captains.singapura.js.homing.design.Feedback.Danger.*;
import static hue.captains.singapura.js.homing.design.Feedback.Success.*;
import static hue.captains.singapura.js.homing.design.Feedback.Warning.*;
import static hue.captains.singapura.js.homing.design.Interaction.Current.*;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive.*;
import static hue.captains.singapura.js.homing.design.Interaction.Selected.*;
import static hue.captains.singapura.js.homing.design.Layer.Base.*;
import static hue.captains.singapura.js.homing.design.Layer.Inverted.*;
import static hue.captains.singapura.js.homing.design.Layer.Raised.*;
import static hue.captains.singapura.js.homing.design.Layer.Recessed.*;
import static hue.captains.singapura.js.homing.design.Pairing.OnInverted.*;
import static hue.captains.singapura.js.homing.design.Pairing.OnInvertedMuted.*;
import static hue.captains.singapura.js.homing.design.Pairing.OnPrimary.*;
import static hue.captains.singapura.js.homing.design.Structure.Bar.*;
import static hue.captains.singapura.js.homing.design.Structure.Cap.*;
import static hue.captains.singapura.js.homing.design.Structure.Divider.*;
import static hue.captains.singapura.js.homing.design.Structure.Hairline.*;
import static hue.captains.singapura.js.homing.design.Structure.Marker.*;
import static hue.captains.singapura.js.homing.design.Structure.Spine.*;
import static hue.captains.singapura.js.homing.design.Text.Body.*;
import static hue.captains.singapura.js.homing.design.Text.Code.*;
import static hue.captains.singapura.js.homing.design.Text.Display.*;
import static hue.captains.singapura.js.homing.design.Text.Heading.*;
import static hue.captains.singapura.js.homing.design.Text.Kicker.*;
import static hue.captains.singapura.js.homing.design.Text.Lede.*;
import static hue.captains.singapura.js.homing.design.Text.Link.*;
import static hue.captains.singapura.js.homing.design.Text.Numeral.*;
import static hue.captains.singapura.js.homing.design.Text.Prose.*;
import static hue.captains.singapura.js.homing.studio.themes.Bind.*;

/**
 * Neo-Brutalism over the house design: ink and paper, a signal yellow, riso
 * blue and red; every edge a thick black rule, every corner square, every
 * raised thing on a hard offset shadow; the press is a two-step snap. Said
 * as bindings — the design's own word where it has one, Default's for the
 * rest — and never as a class name of any component.
 */
final class NeoBrutalismDesign {

    private NeoBrutalismDesign() {}

    static final String INK = "#000000", INK_D = "#FFFFFF";
    static final String PAPER = "#FFFFFF", PAPER_D = "#0B0B0B";
    static final String GREY = "#EFEFEF", GREY_D = "#1A1A1A";
    static final String MUTED = "#4A4A4A", MUTED_D = "#B8B8B8";
    static final String SIGNAL = "#FFE800";
    static final String RISO_BLUE = "#2B4CFF";
    static final String RISO_RED = "#FF3B21";
    static final String RULE = "#000000", RULE_D = "#FFFFFF";
    static final String DISPLAY_FACE = "\"Arial Black\", \"Impact\", \"Helvetica Neue\", Arial, sans-serif";
    static final String SNAP = "transform 70ms steps(2, end), box-shadow 70ms steps(2, end), background-color 70ms steps(2, end)";

    static String shadow(int px) { return px + "px " + px + "px 0 " + INK; }

    static final List<ImplProvider<?>> PROVIDERS = List.of(
            // ── layers: paper, and everything edged in ink ──────────────
            surface(base_color_surface.class, PAPER, PAPER_D),
            one(base_color_scrollbar.class, INK + " " + PAPER, INK_D + " " + PAPER_D),
            surface(raised_color_surface.class, PAPER, PAPER_D),
            edge(raised_color_edge.class, RULE, RULE_D),
            ruleWithFocusRing(raised_shape_rule.class, "3px", "solid", "4px", "4px"),
            one(raised_shape_corner.class, "0"),
            one(raised_shape_shadow.class, shadow(5)),
            surface(recessed_color_surface.class, GREY, GREY_D),
            one(recessed_shape_corner.class, "0"),
            surface(inverted_color_surface.class, INK, "#FFFFFF"),

            // ── text: black, heavy, tight ───────────────────────────────
            one(body_color_ink.class, INK, INK_D),
            one(heading_color_ink.class, INK, INK_D),
            one(heading_type_face.class, DISPLAY_FACE),
            one(heading_type_weight.class, "900"),
            one(display_color_ink.class, INK, INK_D),
            one(display_type_face.class, DISPLAY_FACE),
            one(display_type_weight.class, "900"),
            treatment(display_type_treatment.class, "-0.03em", "uppercase", null),
            one(lede_color_ink.class, MUTED, MUTED_D),
            treatment(lede_type_treatment.class, null, null, null),
            one(kicker_color_ink.class, INK, INK_D),
            one(kicker_type_face.class, DISPLAY_FACE),
            one(kicker_type_weight.class, "900"),
            treatment(kicker_type_treatment.class, "0.04em", "uppercase", null),
            one(numeral_type_face.class, DISPLAY_FACE),
            one(numeral_type_weight.class, "900"),
            one(house_type_face.class, DISPLAY_FACE),
            treatment(house_type_treatment.class, "-0.02em", "uppercase", null),
            states(link_color_ink.class, RISO_BLUE, RISO_RED),
            surface(code_color_surface.class, GREY, GREY_D),
            one(code_color_ink.class, INK, INK_D),
            one(code_shape_corner.class, "0"),

            // ── emphasis: the signal ────────────────────────────────────
            surface(primary_color_surface.class, SIGNAL),
            one(primary_color_ink.class, RISO_RED),
            one(on_primary_color_ink.class, INK),
            surface(secondary_color_surface.class, RISO_RED),
            surface(tertiary_color_surface.class, GREY, GREY_D),
            one(muted_color_ink.class, MUTED, MUTED_D),
            edge(muted_color_edge.class, INK, INK_D),

            // ── pairings ────────────────────────────────────────────────
            one(on_inverted_color_ink.class, PAPER, INK),
            states(on_inverted_muted_color_ink.class, "#D9D9D9", SIGNAL),

            // ── feedback: flat, loud ────────────────────────────────────
            surface(danger_color_surface.class, RISO_RED),
            one(danger_color_ink.class, INK),
            edge(danger_color_edge.class, INK),
            surface(success_color_surface.class, "#7CFF6B"),
            one(success_color_ink.class, INK),
            edge(success_color_edge.class, INK),
            surface(warning_color_surface.class, SIGNAL),
            one(warning_color_ink.class, INK),
            edge(warning_color_edge.class, INK),

            // ── interaction: the press ──────────────────────────────────
            one(interactive_motion_ease.class, SNAP),
            ImplProvider.of(interactive_motion_transform.class, Impl.Bindings.none()
                    .at(State.HOVER, "translate(-2px, -2px)")
                    .at(State.ACTIVE, "translate(5px, 5px)")),
            states(interactive_shape_shadow.class, shadow(4), shadow(8), "0 0 0 " + INK),
            surface(selected_color_surface.class, INK, "#FFFFFF"),
            one(selected_color_ink.class, SIGNAL, INK),
            edge(selected_color_edge.class, INK, "#FFFFFF"),
            surface(current_color_surface.class, SIGNAL),
            one(current_color_ink.class, INK),
            edge(current_color_edge.class, INK),
            one(current_shape_shadow.class, "inset 6px 0 0 " + INK),

            // ── structure: every line is a rule ─────────────────────────
            edge(divider_color_edge.class, RULE, RULE_D),
            rule(divider_shape_rule.class, "0 0 4px 0", "solid"),
            edge(hairline_color_edge.class, RULE, RULE_D),
            rule(hairline_shape_rule.class, "0 0 2px 0", "solid"),
            edge(cap_color_edge.class, RULE, RULE_D),
            rule(cap_shape_rule.class, "3px 0 0 0", "solid"),
            edge(spine_color_edge.class, RULE, RULE_D),
            rule(spine_shape_rule.class, "0 0 0 3px", "solid"),
            edgeHover(marker_color_edge.class, "transparent", INK),
            rule(marker_shape_rule.class, "0 0 0 4px", "solid"),
            edge(bar_color_edge.class, RULE, RULE_D),
            rule(bar_shape_rule.class, "3px 3px 3px 8px", "solid"),

            // ── boxes ───────────────────────────────────────────────────
            one(control_shape_corner.class, "0"),
            rule(control_shape_rule.class, "3px", "solid"),
            one(inline_shape_corner.class, "0"),
            rule(inline_shape_rule.class, "2px", "solid"),

            // ── prose in ink ────────────────────────────────────────────
            body(prose_color_ink.class, """
                color: %s;
                h1, h2, h3, h4 { color: %s; }
                a { color: %s; }
                a:hover { color: %s; }
                blockquote { color: %s; }
                code { color: %s; }
                pre { color: %s; }
                pre code { color: inherit; }
                th { color: %s; }
                """.formatted(INK, INK, RISO_BLUE, RISO_RED, MUTED, INK, PAPER, PAPER)),
            body(prose_type_face.class, """
                h1, h2, h3, h4 { font-family: %s; }
                code, pre { font-family: %s; }
                """.formatted(DISPLAY_FACE, DefaultDesign.MONO_FACE)),
            body(prose_type_weight.class, "h1, h2, h3, h4, th { font-weight: 900; }\n"),
            body(prose_type_decoration.class, "a { text-decoration-line: underline; text-underline-offset: 3px; text-decoration-thickness: 3px; }\n"),
            body(prose_color_surface.class, """
                code { background-color: %s; }
                pre { background-color: %s; }
                pre code { background-color: transparent; }
                th { background-color: %s; }
                tr:nth-child(even) td { background-color: %s; }
                """.formatted(GREY, INK, INK, GREY)),
            body(prose_color_edge.class, """
                h1, blockquote, th, td, hr { border-color: %s; }
                """.formatted(INK)),
            body(prose_shape_rule.class, """
                h1 { border-width: 0 0 4px 0; border-style: solid; }
                blockquote { border-width: 0 0 0 6px; border-style: solid; }
                th, td { border-width: 0 0 2px 0; border-style: solid; }
                th { border-width: 0; }
                hr { border-width: 3px 0 0 0; border-style: solid; }
                """),
            body(prose_shape_corner.class, "code, pre { border-radius: 0; }\n")
    );
}
