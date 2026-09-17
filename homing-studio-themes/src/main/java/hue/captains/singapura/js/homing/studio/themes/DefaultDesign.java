package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.ImplProvider;

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
import static hue.captains.singapura.js.homing.design.Text.Caption.*;
import static hue.captains.singapura.js.homing.design.Text.Code.*;
import static hue.captains.singapura.js.homing.design.Text.Display.*;
import static hue.captains.singapura.js.homing.design.Text.Heading.*;
import static hue.captains.singapura.js.homing.design.Text.Kicker.*;
import static hue.captains.singapura.js.homing.design.Text.Label.*;
import static hue.captains.singapura.js.homing.design.Text.Lede.*;
import static hue.captains.singapura.js.homing.design.Text.Link.*;
import static hue.captains.singapura.js.homing.design.Text.Numeral.*;
import static hue.captains.singapura.js.homing.design.Text.Prose.*;
import static hue.captains.singapura.js.homing.studio.themes.Bind.*;

/**
 * The house design — what the studio looks like out of the box, said as
 * bindings over the design classes its components wear. Every value here was
 * a token or a literal in a class body until the components stopped painting;
 * now the design owns it, light and dark, and no component knows.
 */
final class DefaultDesign {

    private DefaultDesign() {}

    // ── the house's colours, light / dark ──────────────────────────────
    static final String SURFACE = "#FAFBFD",  SURFACE_D = "#0F1320";
    static final String RAISED  = "#FFFFFF",  RAISED_D  = "#1A1F36";
    static final String RECESSED = "#F1F4F9", RECESSED_D = "#232943";
    static final String INVERTED = "#111936";
    static final String TEXT = "#3B4A6B",     TEXT_D = "#E2E8F0";
    static final String MUTED = "#64748B",    MUTED_D = "#94A3B8";
    static final String ON_INVERTED = "#FFFFFF", ON_INVERTED_D = "#E2E8F0";
    static final String ON_INVERTED_MUTED = "#CADCFC", ON_INVERTED_MUTED_D = "#B8C9F2";
    static final String NAVY = "#1E2761",     NAVY_D = "#8FA3D8";
    static final String AMBER = "#C8921E",    AMBER_D = "#E0A833";
    static final String BORDER = "#E2E8F0",   BORDER_D = "#2D3454";
    static final String GOLD = "#F4B942";
    static final String ON_GOLD = "#111936";

    static final String DISPLAY_FACE = StudioFonts.DISPLAY;
    static final String BODY_FACE    = StudioFonts.BODY;
    static final String MONO_FACE    = "ui-monospace, SFMono-Regular, Menlo, Consolas, monospace";

    static final List<ImplProvider<?>> PROVIDERS = List.of(
            // ── layers ──────────────────────────────────────────────────
            surface(base_color_surface.class, SURFACE, SURFACE_D),
            one(base_color_scrollbar.class, MUTED + " " + SURFACE, MUTED_D + " " + SURFACE_D),
            surface(raised_color_surface.class, RAISED, RAISED_D),
            edgeFocus(raised_color_edge.class, BORDER, GOLD),
            rule(raised_shape_rule.class, "1px", "solid"),
            one(raised_shape_corner.class, "4px"),
            ImplProvider.of(raised_shape_shadow.class, hue.captains.singapura.js.homing.design.Impl.Bindings
                    .of("0 1px 3px color-mix(in srgb, " + NAVY + " 4%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "0 0 0 3px color-mix(in srgb, " + GOLD + " 18%, transparent)")),
            surface(recessed_color_surface.class, RECESSED, RECESSED_D),
            one(recessed_shape_corner.class, "6px"),
            surface(inverted_color_surface.class, INVERTED),

            // ── text ────────────────────────────────────────────────────
            one(body_color_ink.class, TEXT, TEXT_D),
            one(body_type_face.class, BODY_FACE),
            one(heading_color_ink.class, NAVY, NAVY_D),
            one(heading_type_face.class, DISPLAY_FACE),
            one(heading_type_weight.class, "700"),
            scale(heading_type_scale.class, "18px", "1.25"),
            one(display_color_ink.class, NAVY, NAVY_D),
            one(display_type_face.class, DISPLAY_FACE),
            one(display_type_weight.class, "700"),
            scale(display_type_scale.class, "44px", "1.1"),
            treatment(display_type_treatment.class, "-0.5px", null, null),
            one(lede_color_ink.class, MUTED, MUTED_D),
            scale(lede_type_scale.class, "17px", "1.55"),
            treatment(lede_type_treatment.class, null, null, "italic"),
            one(kicker_color_ink.class, AMBER, AMBER_D),
            one(kicker_type_face.class, DISPLAY_FACE),
            one(kicker_type_weight.class, "700"),
            scale(kicker_type_scale.class, "11px", "1.4"),
            treatment(kicker_type_treatment.class, "2px", "uppercase", null),
            scale(caption_type_scale.class, "13px", "1.5"),
            treatment(caption_type_treatment.class, null, null, "italic"),
            scale(label_type_scale.class, "14px", "1.5"),
            one(label_type_weight.class, "600"),
            one(numeral_type_face.class, DISPLAY_FACE),
            one(numeral_type_weight.class, "700"),
            scale(numeral_type_scale.class, "28px", "1"),
            treatment(numeral_type_treatment.class, null, null, "italic"),
            one(house_type_face.class, DISPLAY_FACE),
            scale(house_type_scale.class, "22px", "1"),
            treatment(house_type_treatment.class, null, null, "italic"),
            states(link_color_ink.class, MUTED, NAVY),
            decoration(link_type_decoration.class, "none"),
            one(link_motion_ease.class, "color 140ms ease, border-color 140ms ease"),
            one(code_type_face.class, MONO_FACE),
            surface(code_color_surface.class, RECESSED, RECESSED_D),
            one(code_color_ink.class, NAVY, NAVY_D),
            one(code_shape_corner.class, "3px"),

            // ── emphasis ────────────────────────────────────────────────
            surface(primary_color_surface.class, GOLD),
            one(primary_color_ink.class, GOLD),
            one(primary_motion_ease.class, "width 280ms ease"),
            one(on_primary_color_ink.class, ON_GOLD),
            surface(secondary_color_surface.class, AMBER, AMBER_D),
            surface(tertiary_color_surface.class, BORDER, BORDER_D),
            one(muted_color_ink.class, MUTED, MUTED_D),
            edge(muted_color_edge.class, MUTED, MUTED_D),
            one(muted_effect_opacity.class, "0.62"),
            decoration(muted_type_decoration.class, "line-through"),

            // ── pairings ────────────────────────────────────────────────
            one(on_inverted_color_ink.class, ON_INVERTED, ON_INVERTED_D),
            states(on_inverted_muted_color_ink.class, ON_INVERTED_MUTED, GOLD),

            // ── feedback ────────────────────────────────────────────────
            surface(danger_color_surface.class, "rgba(220, 38, 38, 0.10)"),
            one(danger_color_ink.class, "#7F1D1D", "#FCA5A5"),
            edge(danger_color_edge.class, "rgba(220, 38, 38, 0.35) rgba(220, 38, 38, 0.35) rgba(220, 38, 38, 0.35) #DC2626"),
            surface(success_color_surface.class, "rgba(34, 139, 34, 0.12)"),
            one(success_color_ink.class, "#1B5E20", "#86EFAC"),
            edge(success_color_edge.class, "rgba(34, 139, 34, 0.35)"),
            surface(warning_color_surface.class, "rgba(202, 138, 4, 0.12)"),
            one(warning_color_ink.class, "#713F12", "#FDE68A"),
            edge(warning_color_edge.class, "rgba(202, 138, 4, 0.35)"),

            // ── interaction ─────────────────────────────────────────────
            one(interactive_motion_ease.class, "transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease"),
            ImplProvider.of(interactive_motion_transform.class, hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.HOVER, "translateY(-2px)")),
            states(interactive_shape_shadow.class,
                    "0 1px 3px color-mix(in srgb, " + NAVY + " 4%, transparent)",
                    "0 6px 16px color-mix(in srgb, " + NAVY + " 12%, transparent)"),
            one(interactive_affordance_cursor.class, "pointer"),
            surface(selected_color_surface.class, INVERTED),
            states(selected_color_ink.class, ON_INVERTED, GOLD),
            edge(selected_color_edge.class, INVERTED),
            surface(current_color_surface.class, "color-mix(in srgb, " + GOLD + " 7%, transparent)"),
            one(current_color_ink.class, NAVY, NAVY_D),
            edge(current_color_edge.class, GOLD),
            one(current_shape_shadow.class, "inset 3px 0 0 color-mix(in srgb, " + GOLD + " 60%, transparent)"),

            // ── structure ───────────────────────────────────────────────
            edge(divider_color_edge.class, GOLD),
            rule(divider_shape_rule.class, "0 0 2px 0", "solid"),
            edge(hairline_color_edge.class, BORDER, BORDER_D),
            rule(hairline_shape_rule.class, "0 0 1px 0", "solid"),
            edge(cap_color_edge.class, BORDER, BORDER_D),
            rule(cap_shape_rule.class, "1px 0 0 0", "solid"),
            edge(spine_color_edge.class, BORDER, BORDER_D),
            rule(spine_shape_rule.class, "0 0 0 1px", "solid"),
            edgeHover(marker_color_edge.class, "transparent", GOLD),
            rule(marker_shape_rule.class, "0 0 0 2px", "solid"),
            edgeHover(bar_color_edge.class, BORDER + " " + BORDER + " " + BORDER + " " + GOLD, BORDER + " " + BORDER + " " + BORDER + " " + AMBER),
            rule(bar_shape_rule.class, "1px 1px 1px 4px", "solid"),

            // ── boxes ───────────────────────────────────────────────────
            one(control_shape_corner.class, "3px"),
            rule(control_shape_rule.class, "1.5px", "solid"),
            one(inline_shape_corner.class, "2px"),
            rule(inline_shape_rule.class, "1px", "solid"),

            // ── prose: the document's elements, set by the design ───────
            body(prose_color_ink.class, """
                color: %s;
                h1, h2, h3 { color: %s; }
                h4 { color: %s; }
                a { color: %s; }
                a:hover { color: %s; }
                blockquote { color: %s; }
                code { color: %s; }
                pre { color: %s; }
                pre code { color: inherit; }
                th { color: %s; }
                """.formatted(TEXT, NAVY, AMBER, AMBER, NAVY, MUTED, NAVY, ON_INVERTED_MUTED, ON_INVERTED)),
            body(prose_type_face.class, """
                h1, h2, h3, h4 { font-family: %s; }
                code, pre { font-family: %s; }
                """.formatted(DISPLAY_FACE, MONO_FACE)),
            body(prose_type_scale.class, """
                font-size: 16px;
                line-height: 1.7;
                h1, h2, h3, h4 { line-height: 1.25; }
                h1 { font-size: 32px; }
                h2 { font-size: 24px; }
                h3 { font-size: 19px; }
                h4 { font-size: 16px; }
                code { font-size: 0.92em; }
                pre { font-size: 13px; line-height: 1.5; }
                pre code { font-size: inherit; }
                table { font-size: 14px; }
                """),
            body(prose_type_weight.class, "th { font-weight: 700; }\n"),
            body(prose_type_treatment.class, """
                h4 { letter-spacing: 1px; text-transform: uppercase; }
                blockquote { font-style: italic; }
                """),
            body(prose_type_decoration.class, "a { text-decoration-line: underline; text-underline-offset: 2px; }\n"),
            body(prose_color_surface.class, """
                code { background-color: %s; }
                pre { background-color: %s; }
                pre code { background-color: transparent; }
                th { background-color: %s; }
                tr:nth-child(even) td { background-color: %s; }
                """.formatted(RECESSED, INVERTED, INVERTED, RECESSED)),
            body(prose_color_edge.class, """
                h1 { border-color: %s; }
                blockquote { border-color: %s; }
                th, td { border-color: %s; }
                hr { border-color: %s; }
                """.formatted(GOLD, GOLD, BORDER, BORDER)),
            body(prose_shape_rule.class, """
                h1 { border-width: 0 0 2px 0; border-style: solid; }
                blockquote { border-width: 0 0 0 3px; border-style: solid; }
                th, td { border-width: 0 0 1px 0; border-style: solid; }
                th { border-width: 0; }
                hr { border-width: 1px 0 0 0; border-style: solid; }
                """),
            body(prose_shape_corner.class, """
                code { border-radius: 3px; }
                pre { border-radius: 4px; }
                """)
    );
}
