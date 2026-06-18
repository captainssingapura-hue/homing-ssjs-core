package hue.captains.singapura.js.homing.studio.base.css;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;
import hue.captains.singapura.js.homing.core.InLayer;
import hue.captains.singapura.js.homing.core.Layout;

import java.util.List;

public record StudioStyles() implements CssGroup<StudioStyles> {
    public static final StudioStyles INSTANCE = new StudioStyles();

    public record st_root() implements CssClass<StudioStyles>, InLayer<Layout> {
        @Override public String body() { return """
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            """;
        }
    }
    public record st_header() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            background: var(--color-surface-inverted);
            border-bottom: 2px solid var(--color-border-emphasis);
            padding: 14px 32px;
            display: flex;
            align-items: center;
            gap: 24px;
            flex: 0 0 auto;
            position: sticky;
            top: 0;
            z-index: 50;
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
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 10px;
            text-decoration: none;
            color: var(--color-text-on-inverted);
            """;
        }
    }
    public record st_brand_dot() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            width: 12px;
            height: 12px;
            background: var(--color-accent);
            """;
        }
    }
    /** Wrapper for a typed SVG logo (StudioBrand.logo). Fixed 22×22 box; the
     *  child SVG is sized to fill the box — no per-app sizing required.
     *  {@code overflow:hidden} is the safety net: if a consumer ships an SVG
     *  without width/height attrs (browsers default it to 300×150) the
     *  wrapper still clips to 22×22 and won't blow out the header layout.
     *  The transition pairs with the {@code .st-brand-logo:hover} rule in
     *  STRUCTURAL_CSS for a small playful enlarge-on-hover. */
    public record st_brand_logo() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            width: 22px;
            height: 22px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            overflow: hidden;
            transform-origin: center;
            transition: transform 160ms ease;
            """;
        }
    }
    public record st_brand_word() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-family: "Georgia", serif;
            font-style: italic;
            font-size: 22px;
            color: var(--color-text-on-inverted);
            line-height: 1;
            """;
        }
    }
    public record st_breadcrumbs() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            color: var(--color-text-on-inverted-muted);
            font-size: 13px;
            display: flex;
            align-items: center;
            gap: 8px;
            """;
        }
    }
    public record st_crumb() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            color: var(--color-text-on-inverted-muted);
            text-decoration: none;
            """;
        }
    }
    public record st_crumb_sep() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            color: var(--color-text-muted);
            """;
        }
    }
    public record st_main() implements CssClass<StudioStyles>, InLayer<Layout> {
        @Override public String body() { return """
            flex: 1;
            max-width: 1280px;
            width: 100%;
            margin: 0 auto;
            padding: 36px 32px 64px;
            box-sizing: border-box;
            /* The doc-reader page gets a "page on a desk" column slab applied
             * via @layer component (HomingDefault.COMPONENT_CSS targets
             * `.st-main:has(.st-doc-meta)`); catalogue/doc-browser/themes-intro/
             * plan-host pages stay slab-less because they don't carry the
             * doc-meta marker. Cards on those pages keep their own
             * surface-raised fill without losing contrast to a column bg. */
            """;
        }
    }
    public record st_kicker() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-size: 12px;
            letter-spacing: 4px;
            color: var(--color-text-link-hover);
            font-weight: 700;
            text-transform: uppercase;
            margin: 0 0 12px 0;
            """;
        }
    }
    public record st_title() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-family: "Georgia", serif;
            font-size: 44px;
            font-weight: 700;
            color: var(--color-text-link);
            margin: 0 0 12px 0;
            line-height: 1.1;
            letter-spacing: -0.5px;
            """;
        }
    }
    public record st_subtitle() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-size: 17px;
            color: var(--color-text-muted);
            margin: 0 0 32px 0;
            max-width: 760px;
            line-height: 1.55;
            font-style: italic;
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
        @Override public String body() { return """
            font-family: "Georgia", serif;
            font-size: 12px;
            font-weight: 700;
            color: var(--color-text-link);
            letter-spacing: 5px;
            text-transform: uppercase;
            margin: 0 0 16px 0;
            padding-bottom: 8px;
            border-bottom: 2px solid var(--color-border-emphasis);
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
    // Vertical-list layout — used for prose-like rows (objectives, acceptance,
    // decisions). Counterpart to st-grid (which lays out card tiles).
    public record st_list() implements CssClass<StudioStyles>, InLayer<Layout> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 10px;
            """;
        }
    }
    public record st_list_item() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            background: var(--color-surface-raised);
            border: 1px solid var(--color-border);
            border-left: 3px solid var(--color-border-emphasis);
            border-radius: 4px;
            padding: 12px 16px;
            display: flex;
            gap: 14px;
            align-items: flex-start;
            text-decoration: none;
            color: inherit;
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
        @Override public String body() { return """
            font-family: "Georgia", serif;
            font-size: 16px;
            font-weight: 700;
            color: var(--color-text-link);
            margin: 0 0 4px 0;
            line-height: 1.3;
            """;
        }
    }
    public record st_list_item_desc() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-size: 13px;
            color: var(--color-text-muted);
            line-height: 1.5;
            margin: 0;
            """;
        }
    }
    public record st_list_item_met() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            color: #4a7c4a;
            """;
        }
    }
    public record st_card() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            background: var(--color-surface-raised);
            border: 1px solid var(--color-border);
            border-left: 4px solid var(--color-border-emphasis);
            border-radius: 4px;
            padding: 18px 20px;
            cursor: pointer;
            text-decoration: none;
            color: inherit;
            display: flex;
            flex-direction: column;
            box-shadow: 0 1px 3px color-mix(in srgb, var(--color-text-link) 4%, transparent);
            transition: transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease;
            min-height: 150px;
            """;
        }
    }
    public record st_card_featured() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            grid-column: 1 / -1;
            background: var(--color-surface-inverted);
            color: var(--color-text-on-inverted-muted);
            border-left-color: var(--color-border-emphasis);
            flex-direction: row;
            align-items: center;
            gap: 24px;
            padding: 20px 28px;
            min-height: auto;
            """;
        }
    }
    public record st_card_title() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-family: "Georgia", serif;
            font-size: 18px;
            font-weight: 700;
            color: var(--color-text-link);
            margin: 0 0 6px 0;
            line-height: 1.25;
            """;
        }
    }
    public record st_card_summary() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-size: 13px;
            color: var(--color-text-muted);
            line-height: 1.5;
            margin: 0;
            flex: 1;
            """;
        }
    }
    public record st_card_meta() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 14px;
            padding-top: 12px;
            border-top: 1px solid var(--color-border);
            font-size: 11px;
            color: var(--color-text-muted);
            """;
        }
    }
    public record st_card_link() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            color: var(--color-text-link-hover);
            font-weight: 700;
            font-size: 11px;
            letter-spacing: 1.5px;
            text-transform: uppercase;
            """;
        }
    }
    public record st_badge() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: inline-block;
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 1.5px;
            padding: 3px 8px;
            border-radius: 2px;
            text-transform: uppercase;
            """;
        }
    }
    public record st_badge_whitepaper() implements CssClass<StudioStyles> {
        @Override public String body() { return "background: var(--color-surface-inverted); color: var(--color-accent);"; }
    }
    public record st_badge_brochure() implements CssClass<StudioStyles> {
        @Override public String body() { return "background: var(--color-accent); color: var(--color-accent-on);"; }
    }
    public record st_badge_rfc() implements CssClass<StudioStyles> {
        @Override public String body() { return "background: var(--color-surface-inverted); color: var(--color-text-on-inverted-muted);"; }
    }
    public record st_badge_brand() implements CssClass<StudioStyles> {
        @Override public String body() { return "background: var(--color-text-on-inverted-muted); color: var(--color-text-link);"; }
    }
    public record st_badge_session() implements CssClass<StudioStyles> {
        @Override public String body() { return "background: var(--color-surface-recessed); color: var(--color-text-primary); border: 1px solid var(--color-border);"; }
    }
    public record st_badge_reference() implements CssClass<StudioStyles> {
        @Override public String body() { return "background: var(--color-border); color: var(--color-text-link);"; }
    }
    public record st_badge_rename() implements CssClass<StudioStyles> {
        @Override public String body() { return "background: var(--color-accent-emphasis); color: var(--color-text-on-inverted-muted);"; }
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
    public record st_search() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            flex: 1;
            min-width: 280px;
            padding: 10px 16px;
            border: 1px solid var(--color-border);
            border-radius: 4px;
            font-family: inherit;
            font-size: 14px;
            color: var(--color-text-primary);
            background: var(--color-surface-raised);
            transition: border-color 160ms ease, box-shadow 160ms ease;
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
    public record st_filter_btn() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-family: inherit;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 1.5px;
            padding: 6px 12px;
            border: 1px solid var(--color-border);
            background: var(--color-surface-raised);
            color: var(--color-text-primary);
            cursor: pointer;
            border-radius: 3px;
            text-transform: uppercase;
            transition: all 140ms ease;
            """;
        }
    }
    public record st_filter_btn_active() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            background: var(--color-surface-inverted);
            color: var(--color-text-on-inverted);
            border-color: var(--color-surface-inverted);
            """;
        }
    }
    public record st_layout() implements CssClass<StudioStyles>, InLayer<Layout> {
        @Override public String body() { return """
            display: grid;
            grid-template-columns: 260px 1fr;
            gap: 32px;
            margin-top: 8px;
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
            """;
        }
    }
    public record st_sidebar_title() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-size: 11px;
            letter-spacing: 4px;
            color: var(--color-text-link-hover);
            font-weight: 700;
            text-transform: uppercase;
            margin: 0 0 12px 0;
            """;
        }
    }
    public record st_toc() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 2px;
            border-left: 1px solid var(--color-border);
            """;
        }
    }
    public record st_toc_item() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: block;
            padding: 4px 12px;
            font-size: 13px;
            color: var(--color-text-muted);
            text-decoration: none;
            line-height: 1.4;
            border-left: 2px solid transparent;
            margin-left: -1px;
            transition: color 140ms ease, border-color 140ms ease;
            """;
        }
    }
    public record st_toc_h1() implements CssClass<StudioStyles> {
        @Override public String body() { return "font-weight: 700; color: var(--color-text-link); padding-left: 12px;"; }
    }
    public record st_toc_h2() implements CssClass<StudioStyles> {
        @Override public String body() { return "padding-left: 24px;"; }
    }
    public record st_toc_h3() implements CssClass<StudioStyles> {
        @Override public String body() { return "padding-left: 36px; font-size: 12px;"; }
    }
    public record st_toc_active() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            color: var(--color-text-link);
            border-left-color: var(--color-border-emphasis);
            background: var(--color-surface-recessed);
            """;
        }
    }
    public record st_doc() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-size: 16px;
            line-height: 1.7;
            color: var(--color-text-primary);
            max-width: 820px;
            """;
        }
    }
    public record st_doc_meta() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            margin-bottom: 24px;
            padding-bottom: 20px;
            border-bottom: 1px solid var(--color-border);
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
            """;
        }
    }
    public record st_loading() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            text-align: center;
            padding: 48px 16px;
            color: var(--color-text-muted);
            font-style: italic;
            font-size: 14px;
            """;
        }
    }
    public record st_error() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            background: rgba(220, 38, 38, 0.06);
            border: 1px solid rgba(220, 38, 38, 0.3);
            border-left: 4px solid #DC2626;
            padding: 16px 20px;
            border-radius: 4px;
            color: #7F1D1D;
            margin: 16px 0;
            """;
        }
    }
    // A scrollable doc-content pane host — used by the studio workspace's
    // DocContentWidget to mount an opened doc inside a workspace pane (the pane
    // host doesn't scroll on its own, so the widget root owns overflow).
    public record st_doc_pane() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            height: 100%;
            overflow: auto;
            box-sizing: border-box;
            padding: 16px 20px;
            """;
        }
    }
    // The muted "nothing opened yet" notice for a doc-content pane — a readable,
    // left-aligned hint (distinct from st_loading's centered transient notice).
    public record st_doc_empty() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            color: var(--color-text-muted);
            font-style: italic;
            line-height: 1.5;
            padding: 24px 8px;
            max-width: 640px;
            """;
        }
    }
    // -----------------------------------------------------------------------
    // RFC 0020 — TableDoc rendering.
    // Slim table primitives: th/td borders + padding via RFC 0017 tokens.
    // -----------------------------------------------------------------------
    public record st_table() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            width: 100%;
            border-collapse: collapse;
            margin: 16px 0;
            font-size: 14px;
            color: var(--color-text-primary);
            background: var(--color-surface-base);
            """;
        }
    }
    public record st_thead() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            background: var(--color-surface-recessed);
            """;
        }
    }
    public record st_th() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            text-align: left;
            padding: 10px 14px;
            border-bottom: 2px solid var(--color-border-emphasis);
            font-weight: 600;
            color: var(--color-text-primary);
            """;
        }
    }
    public record st_td() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            padding: 10px 14px;
            border-bottom: 1px solid var(--color-border);
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
    public record st_td_badge_success() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: inline-block;
            padding: 2px 8px;
            border-radius: 3px;
            font-size: 11px;
            font-weight: 600;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            background: rgba(34, 139, 34, 0.12);
            color: #1B5E20;
            border: 1px solid rgba(34, 139, 34, 0.35);
            """;
        }
    }
    public record st_td_badge_warning() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: inline-block;
            padding: 2px 8px;
            border-radius: 3px;
            font-size: 11px;
            font-weight: 600;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            background: rgba(202, 138, 4, 0.12);
            color: #713F12;
            border: 1px solid rgba(202, 138, 4, 0.35);
            """;
        }
    }
    public record st_td_badge_error() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: inline-block;
            padding: 2px 8px;
            border-radius: 3px;
            font-size: 11px;
            font-weight: 600;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            background: rgba(220, 38, 38, 0.10);
            color: #7F1D1D;
            border: 1px solid rgba(220, 38, 38, 0.35);
            """;
        }
    }

    // -----------------------------------------------------------------------
    // RFC 0020 — ImageDoc rendering (Raw tier; no theming on the raster
    // itself — chrome around it is themed).
    // -----------------------------------------------------------------------
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
        @Override public String body() { return """
            max-width: 100%;
            height: auto;
            border-radius: 4px;
            box-shadow: 0 1px 4px color-mix(in srgb, var(--color-text-link) 8%, transparent);
            """;
        }
    }
    public record st_image_caption() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            margin-top: 8px;
            font-size: 13px;
            color: var(--color-text-muted);
            text-align: center;
            """;
        }
    }

    public record st_footer() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            margin-top: 64px;
            padding-top: 24px;
            border-top: 1px solid var(--color-border);
            color: var(--color-text-muted);
            font-size: 12px;
            """;
        }
    }
    public record st_app_pill() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            background: var(--color-surface-raised);
            border: 1px solid var(--color-border);
            border-left: 4px solid var(--color-border-emphasis);
            padding: 22px 24px;
            border-radius: 4px;
            text-decoration: none;
            color: inherit;
            display: flex;
            align-items: center;
            gap: 18px;
            box-shadow: 0 1px 3px color-mix(in srgb, var(--color-text-link) 5%, transparent);
            transition: all 160ms ease;
            """;
        }
    }
    public record st_app_pill_dark() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            background: var(--color-surface-inverted);
            color: var(--color-text-on-inverted-muted);
            border-left-color: var(--color-border-emphasis);
            """;
        }
    }
    public record st_app_pill_icon() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            flex: 0 0 56px;
            height: 56px;
            background: var(--color-accent);
            color: var(--color-text-link);
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: "Georgia", serif;
            font-style: italic;
            font-size: 28px;
            font-weight: 700;
            """;
        }
    }
    public record st_app_pill_label() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-family: "Georgia", serif;
            font-size: 19px;
            font-weight: 700;
            color: var(--color-text-link);
            margin: 0 0 4px 0;
            """;
        }
    }
    public record st_app_pill_desc() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-size: 13px;
            color: var(--color-text-muted);
            margin: 0;
            line-height: 1.5;
            """;
        }
    }

    // RFC implementation tracker
    public record st_overall_progress() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            background: var(--color-surface-inverted);
            color: var(--color-text-on-inverted-muted);
            border-left: 4px solid var(--color-border-emphasis);
            padding: 18px 24px;
            border-radius: 4px;
            margin: 0 0 24px 0;
            display: flex;
            align-items: center;
            gap: 24px;
            """;
        }
    }
    public record st_overall_bar() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            flex: 1;
            height: 12px;
            background: color-mix(in srgb, var(--color-text-link) 12%, transparent);
            border-radius: 6px;
            overflow: hidden;
            """;
        }
    }
    public record st_overall_fill() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            height: 100%;
            background: linear-gradient(90deg, var(--color-accent), var(--color-accent-emphasis));
            transition: width 280ms ease;
            """;
        }
    }
    public record st_overall_pct() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-family: "Georgia", serif;
            font-size: 28px;
            font-weight: 700;
            color: var(--color-accent);
            flex: 0 0 auto;
            """;
        }
    }
    public record st_step_card() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            background: var(--color-surface-raised);
            border: 1px solid var(--color-border);
            border-left: 4px solid var(--color-border-emphasis);
            border-radius: 4px;
            padding: 18px 22px;
            margin-bottom: 12px;
            text-decoration: none;
            color: inherit;
            display: block;
            box-shadow: 0 1px 3px color-mix(in srgb, var(--color-text-link) 4%, transparent);
            transition: transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease;
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
        @Override public String body() { return """
            font-family: "Georgia", serif;
            font-style: italic;
            font-size: 13px;
            color: var(--color-text-link-hover);
            font-weight: 700;
            flex: 0 0 auto;
            letter-spacing: 1px;
            """;
        }
    }
    public record st_step_label() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-family: "Georgia", serif;
            font-size: 18px;
            font-weight: 700;
            color: var(--color-text-link);
            margin: 0;
            flex: 1;
            """;
        }
    }
    public record st_step_summary() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-size: 13px;
            color: var(--color-text-muted);
            margin: 0 0 10px 0;
            line-height: 1.5;
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
        @Override public String body() { return """
            flex: 1;
            height: 6px;
            background: var(--color-surface-recessed);
            border-radius: 3px;
            overflow: hidden;
            """;
        }
    }
    public record st_step_progress_fill() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            height: 100%;
            background: var(--color-accent);
            transition: width 280ms ease;
            """;
        }
    }
    public record st_step_meta() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: flex;
            gap: 10px;
            align-items: center;
            flex: 0 0 auto;
            font-size: 11px;
            color: var(--color-text-muted);
            """;
        }
    }
    public record st_status_badge() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: inline-block;
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 1.5px;
            padding: 3px 8px;
            border-radius: 2px;
            text-transform: uppercase;
            """;
        }
    }
    public record st_status_not_started() implements CssClass<StudioStyles> {
        @Override public String body() { return "background: var(--color-border); color: var(--color-text-primary);"; }
    }
    public record st_status_in_progress() implements CssClass<StudioStyles> {
        @Override public String body() { return "background: var(--color-accent); color: var(--color-accent-on);"; }
    }
    public record st_status_blocked() implements CssClass<StudioStyles> {
        @Override public String body() { return "background: #FECACA; color: #7F1D1D;"; }
    }
    public record st_status_done() implements CssClass<StudioStyles> {
        @Override public String body() { return "background: #BBF7D0; color: #14532D;"; }
    }
    public record st_panel() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            background: var(--color-surface-raised);
            border: 1px solid var(--color-border);
            border-radius: 4px;
            padding: 18px 22px;
            margin-bottom: 16px;
            box-shadow: 0 1px 3px color-mix(in srgb, var(--color-text-link) 4%, transparent);
            """;
        }
    }
    public record st_panel_title() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-family: "Georgia", serif;
            font-size: 12px;
            font-weight: 700;
            color: var(--color-text-link-hover);
            letter-spacing: 4px;
            text-transform: uppercase;
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
        @Override public String body() { return """
            display: flex;
            align-items: flex-start;
            gap: 10px;
            padding: 6px 0;
            color: var(--color-text-primary);
            font-size: 14px;
            line-height: 1.5;
            """;
        }
    }
    public record st_task_done() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            color: var(--color-text-muted);
            text-decoration: line-through;
            """;
        }
    }
    public record st_task_box() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            flex: 0 0 16px;
            width: 16px;
            height: 16px;
            border: 1.5px solid var(--color-text-muted);
            border-radius: 3px;
            margin-top: 2px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            color: var(--color-text-on-inverted);
            background: var(--color-surface-raised);
            """;
        }
    }
    public record st_dep() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            display: inline-block;
            margin: 4px 6px 4px 0;
            padding: 4px 10px;
            background: var(--color-surface-recessed);
            border: 1px solid var(--color-border);
            border-radius: 3px;
            font-size: 12px;
            color: var(--color-text-link);
            text-decoration: none;
            """;
        }
    }
    public record st_acceptance() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            color: var(--color-text-primary);
            line-height: 1.6;
            font-size: 14px;
            """;
        }
    }
    public record st_effort() implements CssClass<StudioStyles> {
        @Override public String body() { return """
            font-family: "Georgia", serif;
            font-style: italic;
            color: var(--color-text-link-hover);
            font-size: 14px;
            """;
        }
    }

    @Override
    public CssImportsFor<StudioStyles> cssImports() {
        return CssImportsFor.none(this);
    }

    @Override
    public List<CssClass<StudioStyles>> cssClasses() {
        return List.of(
                new st_root(), new st_header(), new st_nav(),
                new st_brand(), new st_brand_dot(), new st_brand_logo(), new st_brand_word(),
                new st_breadcrumbs(), new st_crumb(), new st_crumb_sep(),
                new st_main(), new st_kicker(), new st_title(), new st_subtitle(),
                new st_section(), new st_section_title(),
                new st_grid(),
                new st_list(), new st_list_item(), new st_list_item_marker(),
                new st_list_item_body(), new st_list_item_label(), new st_list_item_desc(),
                new st_list_item_met(),
                new st_card(), new st_card_featured(),
                new st_card_title(), new st_card_summary(), new st_card_meta(), new st_card_link(),
                new st_badge(),
                new st_badge_whitepaper(), new st_badge_brochure(), new st_badge_rfc(),
                new st_badge_brand(), new st_badge_session(), new st_badge_reference(), new st_badge_rename(),
                new st_search_wrap(), new st_search(),
                new st_filter(), new st_filter_btn(), new st_filter_btn_active(),
                new st_layout(), new st_sidebar(), new st_sidebar_title(),
                new st_toc(), new st_toc_item(), new st_toc_h1(), new st_toc_h2(), new st_toc_h3(), new st_toc_active(),
                new st_doc(), new st_doc_meta(),
                new st_loading(), new st_error(), new st_doc_pane(), new st_doc_empty(), new st_footer(),
                new st_app_pill(), new st_app_pill_dark(),
                new st_app_pill_icon(), new st_app_pill_label(), new st_app_pill_desc(),
                // rfc tracker
                new st_overall_progress(), new st_overall_bar(), new st_overall_fill(), new st_overall_pct(),
                new st_step_card(), new st_step_head(), new st_step_id(), new st_step_label(),
                new st_step_summary(), new st_step_progress(), new st_step_progress_bar(), new st_step_progress_fill(),
                new st_step_meta(),
                new st_status_badge(),
                new st_status_not_started(), new st_status_in_progress(), new st_status_blocked(), new st_status_done(),
                new st_panel(), new st_panel_title(),
                new st_task_list(), new st_task_item(), new st_task_done(), new st_task_box(),
                new st_dep(), new st_acceptance(), new st_effort(),
                // RFC 0020 — TableDoc + ImageDoc
                new st_table(), new st_thead(), new st_th(), new st_td(),
                new st_td_align_left(), new st_td_align_center(), new st_td_align_right(),
                new st_td_badge_success(), new st_td_badge_warning(), new st_td_badge_error(),
                new st_image_figure(), new st_image_img(), new st_image_caption()
        );
    }
}
