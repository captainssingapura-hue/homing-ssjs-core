package hue.captains.singapura.js.homing.studio.base.css;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.InLayer;
import hue.captains.singapura.js.homing.core.Layout;
import hue.captains.singapura.js.homing.core.Reset;

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

/**
 * The studio's chrome and reading surfaces — <b>structure only</b>. Every
 * class here says where its parts sit and how they arrange; what an element
 * <i>means</i> and where that shows is declared through {@link CssClass#wears()},
 * as design classes a design fulfils. No body in this group paints: no
 * colour, no face, no corner, no shadow, no motion. A design never learns
 * these classes exist; it fulfils the design classes they wear.
 *
 * <p>Two things remain on the structural side deliberately: the paddings
 * (density is a plane this pass leaves to a later one); and one habit
 * changed: a variant of a parent ({@code st_app_pill_dark}, {@code st_task_done})
 * no longer reaches its children through a nested selector — the children
 * have variants of their own, and the builder applies them together.</p>
 */
public record StudioStyles() implements CssGroup<StudioStyles> {
    public static final StudioStyles INSTANCE = new StudioStyles();

    /** The structure of a table-cell badge; its colour is the class beside it. */
    static final String TD_BADGE = "display: inline-block; padding: 2px 8px;";

    /** The page reset — {@code html, body}: the one rule over elements no class reaches. Structure only; the root wears the page's meaning. */
    public record st_page() implements CssClass<StudioStyles>, InLayer<Reset> {
        @Override public String selector() { return "html, body"; }
        @Override public String body() { return """
            margin: 0;
            padding: 0;
            min-height: 100vh;
            @media print {
                #__theme_picker_slot__ { display: none; }
            }
            """;
        }
    }

    /** The root every studio page mounts on: the base layer, the body's ink and face. */
    public record st_root() implements CssClass<StudioStyles>, InLayer<Layout> {
        @Override public List<CssClass<?>> wears() { return List.of(new base_color_surface(), new base_color_scrollbar(), new body_color_ink(), new body_type_face()); }
        @Override public String body() { return """
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            """;
        }
    }
    public record st_header() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new inverted_color_surface(), new divider_color_edge(), new divider_shape_rule()); }
        @Override public String body() { return """
            padding: 14px 32px;
            display: flex;
            align-items: center;
            gap: 24px;
            flex: 0 0 auto;
            position: sticky;
            top: 0;
            z-index: 50;
            @media print { & { position: static; } }
            """;
        }
    }
    public record st_nav() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            margin-left: auto;
            display: flex;
            gap: 4px;
            align-items: center;
            """;
        }
    }
    public record st_brand() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new on_inverted_color_ink(), new link_type_decoration()); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 10px;
            """;
        }
    }
    public record st_brand_dot() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_surface()); }
        @Override public String body() { return """
            width: 12px;
            height: 12px;
            """;
        }
    }
    /** Wrapper for a typed SVG logo (StudioBrand.logo): a fixed 22×22 box the child fills; overflow hidden clips an unsized SVG. */
    public record st_brand_logo() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new interactive_motion_ease(), new interactive_motion_transform()); }
        @Override public String body() { return """
            width: 22px;
            height: 22px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            overflow: hidden;
            svg { width: 100%; height: 100%; display: block; }
            """;
        }
    }
    /** The house word beside the mark: the brand's own setting. */
    public record st_brand_word() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new on_inverted_color_ink(), new house_type_face(), new house_type_scale(), new house_type_treatment()); }
        @Override public String body() { return ""; }
    }
    public record st_breadcrumbs() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new on_inverted_muted_color_ink(), new caption_type_scale()); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 8px;
            """;
        }
    }
    public record st_crumb() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new on_inverted_muted_color_ink(), new link_type_decoration(), new link_motion_ease()); }
        @Override public String body() { return ""; }
    }
    public record st_crumb_sep() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_main() implements CssClass<StudioStyles>, InLayer<Layout> {
        @Override public String body() { return """
            flex: 1;
            max-width: 1280px;
            width: 100%;
            margin: 0 auto;
            padding: 36px 32px 64px;
            box-sizing: border-box;
            @media print { & { max-width: none; padding: 12px 0; } }
            """;
        }
    }
    /** The reading-page slab: a main with a document in it sits on the raised layer. Applied beside st_main by the document renderers. */
    public record st_main_slab() implements CssClass<StudioStyles>, InLayer<Layout> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new raised_shape_corner(), new raised_shape_shadow()); }
        @Override public String body() { return ""; }
    }
    public record st_kicker() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new kicker_color_ink()); }
        @Override public String body() { return """
            margin: 0 0 12px 0;
            """;
        }
    }
    public record st_title() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new display_type_face(), new display_type_scale(), new display_type_weight(), new display_type_treatment(), new display_color_ink()); }
        @Override public String body() { return """
            margin: 0 0 12px 0;
            """;
        }
    }
    public record st_subtitle() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new lede_color_ink(), new lede_type_scale(), new lede_type_treatment()); }
        @Override public String body() { return """
            margin: 0 0 32px 0;
            max-width: 760px;
            """;
        }
    }
    public record st_section() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            margin-top: 40px;
            """;
        }
    }
    public record st_section_title() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_face(), new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new heading_color_ink(), new divider_color_edge(), new divider_shape_rule()); }
        @Override public String body() { return """
            margin: 0 0 16px 0;
            padding-bottom: 8px;
            display: inline-block;
            """;
        }
    }
    public record st_grid() implements CssClass<StudioStyles>, InLayer<Layout> {
        @Override public String body() { return """
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 16px;
            """;
        }
    }
    public record st_list() implements CssClass<StudioStyles>, InLayer<Layout> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 10px;
            """;
        }
    }
    public record st_list_item() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new raised_shape_corner(), new bar_color_edge(), new bar_shape_rule(), new body_color_ink(), new link_type_decoration()); }
        @Override public String body() { return """
            padding: 12px 16px;
            display: flex;
            gap: 14px;
            align-items: flex-start;
            """;
        }
    }
    public record st_list_item_marker() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            flex-shrink: 0;
            display: flex;
            align-items: center;
            min-height: 24px;
            """;
        }
    }
    public record st_list_item_body() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            flex: 1;
            min-width: 0;
            """;
        }
    }
    public record st_list_item_label() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new heading_type_face(), new heading_type_weight(), new heading_color_ink(), new label_type_scale()); }
        @Override public String body() { return """
            margin: 0 0 4px 0;
            """;
        }
    }
    public record st_list_item_desc() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new caption_type_scale(), new muted_color_ink()); }
        @Override public String body() { return """
            margin: 0;
            """;
        }
    }
    public record st_list_item_met() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new success_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_card() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new raised_shape_corner(), new bar_color_edge(), new bar_shape_rule(),
                new body_color_ink(), new link_type_decoration(), new interactive_shape_shadow(), new interactive_motion_ease(), new interactive_motion_transform(), new interactive_affordance_cursor()); }
        @Override public String body() { return """
            padding: 18px 20px;
            display: flex;
            flex-direction: column;
            min-height: 150px;
            """;
        }
    }
    public record st_card_title() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new heading_type_face(), new heading_type_scale(), new heading_type_weight(), new heading_color_ink()); }
        @Override public String body() { return """
            margin: 0 0 6px 0;
            """;
        }
    }
    public record st_card_summary() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new caption_type_scale(), new muted_color_ink()); }
        @Override public String body() { return """
            margin: 0;
            flex: 1;
            """;
        }
    }
    public record st_card_meta() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new cap_color_edge(), new cap_shape_rule(), new caption_type_scale(), new muted_color_ink()); }
        @Override public String body() { return """
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 14px;
            padding-top: 12px;
            """;
        }
    }
    public record st_card_link() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new kicker_color_ink()); }
        @Override public String body() { return ""; }
    }
    /** A badge: inline, small, loud. Its colour comes from the class beside it. */
    public record st_badge() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new inline_shape_corner()); }
        @Override public String body() { return """
            display: inline-block;
            padding: 3px 8px;
            """;
        }
    }
    public record st_badge_whitepaper() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new inverted_color_surface(), new primary_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_badge_brochure() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_surface(), new on_primary_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_badge_rfc() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new inverted_color_surface(), new on_inverted_muted_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_badge_brand() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new secondary_color_surface(), new heading_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_badge_session() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new recessed_color_surface(), new body_color_ink(), new raised_color_edge(), new raised_shape_rule()); }
        @Override public String body() { return ""; }
    }
    public record st_badge_reference() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new tertiary_color_surface(), new heading_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_badge_rename() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new secondary_color_surface(), new on_inverted_muted_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_search_wrap() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            margin: 8px 0 24px 0;
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
            """;
        }
    }
    /** The search field: a raised control whose edge answers focus. */
    public record st_search() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new raised_color_edge(), new raised_shape_rule(), new raised_shape_corner(), new raised_shape_shadow(),
                new body_color_ink(), new body_type_face(), new label_type_scale(), new interactive_motion_ease()); }
        @Override public String body() { return """
            flex: 1;
            min-width: 280px;
            padding: 10px 16px;
            &:focus { outline: none; }
            """;
        }
    }
    public record st_filter() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: flex;
            gap: 6px;
            flex-wrap: wrap;
            """;
        }
    }
    /** A filter chip: a small raised control the pointer can press. */
    public record st_filter_btn() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new raised_color_edge(), new raised_shape_rule(), new control_shape_corner(),
                new body_color_ink(), new body_type_face(), new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(),
                new interactive_affordance_cursor(), new interactive_motion_ease()); }
        @Override public String body() { return """
            padding: 6px 12px;
            """;
        }
    }
    /** The active filter chip: selected. Applied beside st_filter_btn; the selected surface, ink and edge win by order. */
    public record st_filter_btn_active() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new selected_color_surface(), new selected_color_ink(), new selected_color_edge()); }
        @Override public String body() { return ""; }
    }
    public record st_layout() implements CssClass<StudioStyles>, InLayer<Layout> {
        @Override public String body() { return """
            display: grid;
            grid-template-columns: 260px 1fr;
            gap: 32px;
            margin-top: 8px;
            @media (max-width: 920px) { & { grid-template-columns: 1fr; } }
            @media print { & { grid-template-columns: 1fr; } }
            """;
        }
    }
    public record st_sidebar() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            position: sticky;
            top: 24px;
            align-self: start;
            max-height: calc(100vh - 48px);
            overflow-y: auto;
            padding: 4px 8px 4px 4px;
            @media (max-width: 920px) { & { display: none; } }
            @media print { & { display: none; } }
            """;
        }
    }
    public record st_sidebar_title() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new kicker_color_ink()); }
        @Override public String body() { return """
            margin: 0 0 12px 0;
            """;
        }
    }
    public record st_toc() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new spine_color_edge(), new spine_shape_rule()); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 2px;
            """;
        }
    }
    /** A contents entry: a quiet link whose leading marker lights on hover. */
    public record st_toc_item() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new caption_type_scale(), new link_color_ink(), new link_type_decoration(), new link_motion_ease(), new marker_color_edge(), new marker_shape_rule()); }
        @Override public String body() { return """
            display: block;
            padding: 4px 12px;
            margin-left: -1px;
            """;
        }
    }
    public record st_toc_h1() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new heading_type_weight(), new heading_color_ink()); }
        @Override public String body() { return "padding-left: 12px;"; }
    }
    public record st_toc_h2() implements CssClass<StudioStyles> {
        @Override public String body() { return "padding-left: 24px;"; }
    }
    public record st_toc_h3() implements CssClass<StudioStyles> {
        @Override public String body() { return "padding-left: 36px;"; }
    }
    public record st_toc_active() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new current_color_ink(), new current_color_surface(), new current_color_edge()); }
        @Override public String body() { return ""; }
    }
    public record st_mermaid() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new raised_color_edge(), new raised_shape_rule(), new raised_shape_corner()); }
        @Override public String body() { return """
            margin: 16px 0;
            padding: 12px;
            overflow-x: auto;
            text-align: center;
            """;
        }
    }
    public record st_mermaid_note() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink(), new caption_type_scale(), new caption_type_treatment()); }
        @Override public String body() { return """
            margin: 6px 0 0;
            text-align: left;
            """;
        }
    }
    /** The section the reader is in: current, marked by its surface and an inset line along its leading edge. */
    public record st_doc_section_active() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new current_color_surface(), new current_shape_shadow()); }
        @Override public String body() { return ""; }
    }
    /** The document: rendered markdown, no class on anything inside. Its prose is set by the design, per element, inside the one class it wears. */
    public record st_doc() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new prose_color_ink(), new prose_type_scale(), new prose_type_face(), new prose_type_weight(), new prose_type_treatment(),
                new prose_type_decoration(), new prose_color_surface(), new prose_color_edge(), new prose_shape_rule(), new prose_shape_corner()); }
        @Override public String body() { return """
            max-width: 820px;
            h1, h2, h3, h4 { margin: 1.6em 0 0.6em 0; scroll-margin-top: 24px; }
            h1 { padding-bottom: 8px; margin-top: 0; }
            p  { margin: 0 0 1em 0; }
            ul, ol { margin: 0 0 1em 0; padding-left: 1.5em; }
            li { margin: 0.3em 0; }
            blockquote { margin: 1em 0; padding: 4px 0 4px 18px; }
            code { padding: 1px 6px; }
            pre { padding: 14px 18px; overflow-x: auto; margin: 1em 0; }
            pre code { padding: 0; }
            table { width: 100%; border-collapse: collapse; margin: 1em 0; }
            th, td { text-align: left; padding: 8px 12px; vertical-align: top; }
            hr { margin: 2em 0; }
            img { max-width: 100%; }
            @media print { & { max-width: none; } }
            """;
        }
    }
    public record st_doc_category() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink(), new kicker_type_scale(), new kicker_type_treatment()); }
        @Override public String body() { return """
            margin-left: 12px;
            """;
        }
    }
    public record st_doc_meta() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new hairline_color_edge(), new hairline_shape_rule()); }
        @Override public String body() { return """
            margin-bottom: 24px;
            padding-bottom: 20px;
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
            """;
        }
    }
    public record st_loading() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink(), new caption_type_treatment(), new label_type_scale()); }
        @Override public String body() { return """
            text-align: center;
            padding: 48px 16px;
            """;
        }
    }
    public record st_error() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new danger_color_surface(), new danger_color_edge(), new bar_shape_rule(), new raised_shape_corner(), new danger_color_ink()); }
        @Override public String body() { return """
            padding: 16px 20px;
            margin: 16px 0;
            """;
        }
    }
    public record st_doc_pane() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            height: 100%;
            overflow: auto;
            box-sizing: border-box;
            padding: 16px 20px;
            """;
        }
    }
    public record st_doc_empty() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink(), new caption_type_treatment()); }
        @Override public String body() { return """
            padding: 24px 8px;
            max-width: 640px;
            """;
        }
    }
    public record st_table() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new base_color_surface(), new body_color_ink(), new label_type_scale()); }
        @Override public String body() { return """
            width: 100%;
            border-collapse: collapse;
            margin: 16px 0;
            """;
        }
    }
    public record st_thead() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new recessed_color_surface()); }
        @Override public String body() { return ""; }
    }
    public record st_th() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new divider_color_edge(), new divider_shape_rule(), new body_color_ink(), new label_type_weight()); }
        @Override public String body() { return """
            text-align: left;
            padding: 10px 14px;
            """;
        }
    }
    public record st_td() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new hairline_color_edge(), new hairline_shape_rule()); }
        @Override public String body() { return """
            padding: 10px 14px;
            vertical-align: top;
            """;
        }
    }
    public record st_td_align_left() implements CssClass<StudioStyles> {
        @Override public String body() { return "text-align: left;"; }
    }
    public record st_td_align_center() implements CssClass<StudioStyles> {
        @Override public String body() { return "text-align: center;"; }
    }
    public record st_td_align_right() implements CssClass<StudioStyles> {
        @Override public String body() { return "text-align: right;"; }
    }
    /** A feedback badge in a table cell: inline, small, edged, and coloured by what it says. */
    public record st_td_badge_success() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new success_color_surface(), new success_color_ink(), new success_color_edge(), new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new inline_shape_corner(), new inline_shape_rule()); }
        @Override public String body() { return TD_BADGE; }
    }
    public record st_td_badge_warning() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new warning_color_surface(), new warning_color_ink(), new warning_color_edge(), new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new inline_shape_corner(), new inline_shape_rule()); }
        @Override public String body() { return TD_BADGE; }
    }
    public record st_td_badge_error() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new danger_color_surface(), new danger_color_ink(), new danger_color_edge(), new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new inline_shape_corner(), new inline_shape_rule()); }
        @Override public String body() { return TD_BADGE; }
    }
    public record st_td_strong() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new heading_type_weight()); }
        @Override public String body() { return ""; }
    }
    public record st_td_muted() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_effect_opacity(), new caption_type_treatment()); }
        @Override public String body() { return ""; }
    }
    public record st_image_figure() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            margin: 24px 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            """;
        }
    }
    public record st_image_img() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_shape_corner(), new raised_shape_shadow()); }
        @Override public String body() { return """
            max-width: 100%;
            height: auto;
            """;
        }
    }
    public record st_image_caption() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new caption_type_scale(), new muted_color_ink()); }
        @Override public String body() { return """
            margin-top: 8px;
            text-align: center;
            """;
        }
    }
    public record st_footer() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new cap_color_edge(), new cap_shape_rule(), new muted_color_ink(), new kicker_type_scale(),
                new code_type_face(), new code_color_surface(), new code_color_ink(), new code_shape_corner()); }
        @Override public String body() { return """
            margin-top: 64px;
            padding-top: 24px;
            code { padding: 1px 6px; }
            """;
        }
    }
    public record st_app_pill() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new raised_shape_corner(), new bar_color_edge(), new bar_shape_rule(),
                new body_color_ink(), new link_type_decoration(), new interactive_shape_shadow(), new interactive_motion_ease(), new interactive_motion_transform(), new interactive_affordance_cursor()); }
        @Override public String body() { return """
            padding: 22px 24px;
            display: flex;
            align-items: center;
            gap: 18px;
            """;
        }
    }
    /** The dark pill: on the inverted layer. Its children wear their own dark variants, applied by the builder beside their class. */
    public record st_app_pill_dark() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new inverted_color_surface(), new on_inverted_muted_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_app_pill_icon_dark() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_surface(), new on_primary_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_app_pill_label_dark() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new on_inverted_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_app_pill_desc_dark() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new on_inverted_muted_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_app_pill_icon() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_surface(), new heading_color_ink(), new raised_shape_corner(), new numeral_type_face(), new numeral_type_weight(), new numeral_type_scale(), new numeral_type_treatment()); }
        @Override public String body() { return """
            flex: 0 0 56px;
            height: 56px;
            display: flex;
            align-items: center;
            justify-content: center;
            """;
        }
    }
    public record st_app_pill_label() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new heading_type_face(), new heading_type_scale(), new heading_type_weight(), new heading_color_ink()); }
        @Override public String body() { return """
            margin: 0 0 4px 0;
            """;
        }
    }
    public record st_app_pill_desc() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new caption_type_scale(), new muted_color_ink()); }
        @Override public String body() { return """
            margin: 0;
            """;
        }
    }
    public record st_overall_progress() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new inverted_color_surface(), new on_inverted_muted_color_ink(), new bar_color_edge(), new bar_shape_rule(), new raised_shape_corner()); }
        @Override public String body() { return """
            padding: 18px 24px;
            margin: 0 0 24px 0;
            display: flex;
            align-items: center;
            gap: 24px;
            """;
        }
    }
    public record st_overall_bar() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new recessed_color_surface(), new recessed_shape_corner()); }
        @Override public String body() { return """
            flex: 1;
            height: 12px;
            overflow: hidden;
            """;
        }
    }
    public record st_overall_fill() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_surface(), new primary_motion_ease()); }
        @Override public String body() { return """
            height: 100%;
            """;
        }
    }
    public record st_overall_pct() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new numeral_type_face(), new numeral_type_weight(), new numeral_type_scale(), new primary_color_ink()); }
        @Override public String body() { return """
            flex: 0 0 auto;
            """;
        }
    }
    public record st_step_card() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new raised_shape_corner(), new bar_color_edge(), new bar_shape_rule(),
                new body_color_ink(), new link_type_decoration(), new interactive_shape_shadow(), new interactive_motion_ease(), new interactive_motion_transform()); }
        @Override public String body() { return """
            padding: 18px 22px;
            margin-bottom: 12px;
            display: block;
            """;
        }
    }
    public record st_step_head() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: baseline;
            gap: 14px;
            margin-bottom: 6px;
            """;
        }
    }
    public record st_step_id() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_face(), new kicker_type_weight(), new kicker_type_treatment(), new kicker_color_ink(), new caption_type_scale()); }
        @Override public String body() { return """
            flex: 0 0 auto;
            """;
        }
    }
    public record st_step_label() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new heading_type_face(), new heading_type_scale(), new heading_type_weight(), new heading_color_ink()); }
        @Override public String body() { return """
            margin: 0;
            flex: 1;
            """;
        }
    }
    public record st_step_summary() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new caption_type_scale(), new muted_color_ink()); }
        @Override public String body() { return """
            margin: 0 0 10px 0;
            """;
        }
    }
    public record st_step_progress() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 12px;
            margin-top: 10px;
            """;
        }
    }
    public record st_step_progress_bar() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new recessed_color_surface(), new recessed_shape_corner()); }
        @Override public String body() { return """
            flex: 1;
            height: 6px;
            overflow: hidden;
            """;
        }
    }
    public record st_step_progress_fill() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_surface(), new primary_motion_ease()); }
        @Override public String body() { return """
            height: 100%;
            """;
        }
    }
    public record st_step_meta() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new caption_type_scale(), new muted_color_ink()); }
        @Override public String body() { return """
            display: flex;
            gap: 10px;
            align-items: center;
            flex: 0 0 auto;
            """;
        }
    }
    public record st_status_badge() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new inline_shape_corner()); }
        @Override public String body() { return """
            display: inline-block;
            padding: 3px 8px;
            """;
        }
    }
    public record st_status_not_started() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new tertiary_color_surface(), new body_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_status_in_progress() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_surface(), new on_primary_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_status_blocked() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new danger_color_surface(), new danger_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_status_done() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new success_color_surface(), new success_color_ink()); }
        @Override public String body() { return ""; }
    }
    public record st_panel() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new raised_color_edge(), new raised_shape_rule(), new raised_shape_corner(), new raised_shape_shadow()); }
        @Override public String body() { return """
            padding: 18px 22px;
            margin-bottom: 16px;
            """;
        }
    }
    public record st_panel_title() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_face(), new kicker_type_scale(), new kicker_type_weight(), new kicker_type_treatment(), new kicker_color_ink()); }
        @Override public String body() { return """
            margin: 0 0 12px 0;
            """;
        }
    }
    public record st_task_list() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            list-style: none;
            margin: 0;
            padding: 0;
            """;
        }
    }
    public record st_task_item() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new body_color_ink(), new label_type_scale()); }
        @Override public String body() { return """
            display: flex;
            align-items: flex-start;
            gap: 10px;
            padding: 6px 0;
            """;
        }
    }
    /** A done task: muted and struck; its box wears the done variant, applied by the builder. */
    public record st_task_done() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new muted_color_ink(), new muted_type_decoration()); }
        @Override public String body() { return ""; }
    }
    public record st_task_box_done() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new primary_color_surface(), new primary_color_edge(), new on_primary_color_ink(), new heading_type_weight()); }
        @Override public String body() { return ""; }
    }
    public record st_task_box() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new raised_color_surface(), new muted_color_edge(), new control_shape_rule(), new code_shape_corner(), new on_inverted_color_ink(), new caption_type_scale()); }
        @Override public String body() { return """
            flex: 0 0 16px;
            width: 16px;
            height: 16px;
            margin-top: 2px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            """;
        }
    }
    public record st_dep() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new recessed_color_surface(), new raised_color_edge(), new raised_shape_rule(), new code_shape_corner(), new caption_type_scale(), new heading_color_ink(), new link_type_decoration()); }
        @Override public String body() { return """
            display: inline-block;
            margin: 4px 6px 4px 0;
            padding: 4px 10px;
            """;
        }
    }
    public record st_acceptance() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new body_color_ink(), new label_type_scale()); }
        @Override public String body() { return ""; }
    }
    public record st_effort() implements CssClass<StudioStyles> {
        @Override public List<CssClass<?>> wears() { return List.of(new kicker_type_face(), new kicker_color_ink(), new label_type_scale(), new lede_type_treatment()); }
        @Override public String body() { return ""; }
    }

    @Override
    public List<CssClass<StudioStyles>> cssClasses() {
        return List.of(
                new st_page(), new st_root(), new st_header(), new st_nav(),
                new st_brand(), new st_brand_dot(), new st_brand_logo(), new st_brand_word(),
                new st_breadcrumbs(), new st_crumb(), new st_crumb_sep(),
                new st_main(), new st_main_slab(), new st_kicker(), new st_title(), new st_subtitle(),
                new st_section(), new st_section_title(),
                new st_grid(),
                new st_list(), new st_list_item(), new st_list_item_marker(),
                new st_list_item_body(), new st_list_item_label(), new st_list_item_desc(),
                new st_list_item_met(),
                new st_card(),
                new st_card_title(), new st_card_summary(), new st_card_meta(), new st_card_link(),
                new st_badge(),
                new st_badge_whitepaper(), new st_badge_brochure(), new st_badge_rfc(),
                new st_badge_brand(), new st_badge_session(), new st_badge_reference(), new st_badge_rename(),
                new st_search_wrap(), new st_search(),
                new st_filter(), new st_filter_btn(), new st_filter_btn_active(),
                new st_layout(), new st_sidebar(), new st_sidebar_title(),
                new st_toc(), new st_toc_item(), new st_toc_h1(), new st_toc_h2(), new st_toc_h3(), new st_toc_active(),
                new st_mermaid(),
                new st_mermaid_note(),
                new st_doc_section_active(),
                new st_doc(), new st_doc_meta(), new st_doc_category(),
                new st_loading(), new st_error(), new st_doc_pane(), new st_doc_empty(), new st_footer(),
                new st_app_pill(), new st_app_pill_dark(),
                new st_app_pill_icon(), new st_app_pill_label(), new st_app_pill_desc(),
                new st_app_pill_icon_dark(), new st_app_pill_label_dark(), new st_app_pill_desc_dark(),
                new st_overall_progress(), new st_overall_bar(), new st_overall_fill(), new st_overall_pct(),
                new st_step_card(), new st_step_head(), new st_step_id(), new st_step_label(),
                new st_step_summary(), new st_step_progress(), new st_step_progress_bar(), new st_step_progress_fill(),
                new st_step_meta(),
                new st_status_badge(),
                new st_status_not_started(), new st_status_in_progress(), new st_status_blocked(), new st_status_done(),
                new st_panel(), new st_panel_title(),
                new st_task_list(), new st_task_item(), new st_task_done(), new st_task_box(), new st_task_box_done(),
                new st_dep(), new st_acceptance(), new st_effort(),
                new st_table(), new st_thead(), new st_th(), new st_td(),
                new st_td_align_left(), new st_td_align_center(), new st_td_align_right(),
                new st_td_badge_success(), new st_td_badge_warning(), new st_td_badge_error(),
                new st_td_strong(), new st_td_muted(),
                new st_image_figure(), new st_image_img(), new st_image_caption()
        );
    }
}
