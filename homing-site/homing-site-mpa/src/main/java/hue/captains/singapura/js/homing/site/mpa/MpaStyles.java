package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.InLayer;
import hue.captains.singapura.js.homing.core.Layout;
import hue.captains.singapura.js.homing.core.Reset;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Box.Control;
import static hue.captains.singapura.js.homing.design.Brand.House;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Interaction.Selectable;
import static hue.captains.singapura.js.homing.design.Layer.Base;
import static hue.captains.singapura.js.homing.design.Layer.Inverted;
import static hue.captains.singapura.js.homing.design.Layer.Overlay;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Pairing.OnInverted;
import static hue.captains.singapura.js.homing.design.Pairing.OnInvertedMuted;
import static hue.captains.singapura.js.homing.design.Structure.Divider;
import static hue.captains.singapura.js.homing.design.Target.Affordance;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Effect;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Caption;
import static hue.captains.singapura.js.homing.design.Text.Kicker;
import static hue.captains.singapura.js.homing.design.Text.Link;

/**
 * The chrome's classes. Every colour, face, edge and motion is a design word
 * worn, never a value: the chrome looks like whatever design the page wears,
 * and it paints nothing of its own. The bodies hold only layout.
 *
 * <p>The words are the ones a page chrome needs and nothing more — the
 * inverted bar, the house word, the muted crumbs, a control for the
 * preferences button, a raised menu of selectable rows. A design that binds
 * the studio's vocabulary binds all of these.</p>
 */
public record MpaStyles() implements CssGroup<MpaStyles> {

    public static final MpaStyles INSTANCE = new MpaStyles();

    /** The document itself: no margin, the page fills the viewport. */
    public record mpa_page() implements CssClass<MpaStyles>, InLayer<Reset> {
        @Override public String selector() { return "html, body"; }
        @Override public String body() { return """
            margin: 0;
            padding: 0;
            min-height: 100vh;
            """;
        }
    }

    /** The page column: base surface, body ink, header over main. */
    public record mpa_root() implements CssClass<MpaStyles>, InLayer<Layout> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Base.class, Color.Scrollbar.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class)); }
        @Override public String body() { return """
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            """;
        }
    }

    /** The bar: inverted, divided from the page, sticky. */
    public record mpa_header() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inverted.class, Color.Surface.class), of(Inverted.class, Effect.Filter.class), of(Divider.class, Color.Edge.class), of(Divider.class, Shape.Rule.class)); }
        @Override public String body() { return """
            padding: 12px 28px;
            display: flex;
            align-items: center;
            gap: 20px;
            flex: 0 0 auto;
            position: sticky;
            top: 0;
            z-index: 50;
            @media print { & { position: static; } }
            """;
        }
    }

    /** The brand link: mark and word, home. */
    public record mpa_brand() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(OnInverted.class, Color.Ink.class), of(Link.class, Type.Decoration.class)); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 10px;
            flex: 0 0 auto;
            """;
        }
    }

    /** The mark beside the word: a primary square, until a site gives a logo. */
    public record mpa_brand_mark() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class)); }
        @Override public String body() { return """
            width: 12px;
            height: 12px;
            """;
        }
    }

    /** The house word, in the brand's own setting. */
    public record mpa_brand_word() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(OnInverted.class, Color.Ink.class), of(House.class, Type.Face.class), of(House.class, Type.Decoration.class), of(House.class, Type.Scale.class), of(House.class, Type.Treatment.class)); }
        @Override public String body() { return ""; }
    }

    /** The trail: muted on the bar, caption-sized. */
    public record mpa_crumbs() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(OnInvertedMuted.class, Color.Ink.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: 8px;
            min-width: 0;
            flex: 1 1 auto;
            """;
        }
    }

    public record mpa_crumb() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(OnInvertedMuted.class, Color.Ink.class), of(Link.class, Type.Decoration.class), of(Link.class, Motion.Ease.class)); }
        @Override public String body() { return "white-space: nowrap;"; }
    }

    public record mpa_crumb_sep() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** The app's slot: a reading column by default. */
    public record mpa_main() implements CssClass<MpaStyles>, InLayer<Layout> {
        @Override public String body() { return """
            flex: 1;
            max-width: 1280px;
            width: 100%;
            margin: 0 auto;
            padding: 32px 28px 64px;
            box-sizing: border-box;
            @media print { & { max-width: none; padding: 12px 0; } }
            """;
        }
    }

    // ── Preferences ───────────────────────────────────────────────────────────

    /** The anchor the menu hangs from; at the bar's end. */
    public record mpa_prefs() implements CssClass<MpaStyles> {
        @Override public String body() { return """
            position: relative;
            flex: 0 0 auto;
            margin-left: auto;
            """;
        }
    }

    /** The button on the bar: a control drawn in the bar's ink. */
    public record mpa_prefs_btn() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(OnInverted.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(OnInverted.class, Color.Ink.class), of(Caption.class, Type.Scale.class), of(Body.class, Type.Face.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
            font: inherit;
            background: transparent;
            padding: 5px 12px;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            """;
        }
    }

    public record mpa_prefs_btn_label() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(OnInvertedMuted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    /** Catches the click outside; carries no look. */
    public record mpa_prefs_scrim() implements CssClass<MpaStyles> {
        @Override public String body() { return """
            position: fixed;
            inset: 0;
            z-index: 60;
            """;
        }
    }

    /** The menu: a raised panel under the button, over everything. */
    public record mpa_prefs_menu() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Raised.class, Effect.Filter.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Overlay.class, Shape.Shadow.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class)); }
        @Override public String body() { return """
            position: absolute;
            right: 0;
            top: calc(100% + 8px);
            z-index: 61;
            width: 340px;
            max-width: calc(100vw - 32px);
            padding: 10px;
            box-sizing: border-box;
            display: flex;
            flex-direction: column;
            gap: 10px;
            """;
        }
    }

    /** A section's label: a kicker. */
    public record mpa_prefs_label() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "padding: 2px 6px;"; }
    }

    public record mpa_prefs_list() implements CssClass<MpaStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 2px;
            margin: 0;
            padding: 0;
            list-style: none;
            """;
        }
    }

    /** A row: selectable, marked by aria-selected. */
    public record mpa_prefs_item() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Selectable.class, Color.Surface.class), of(Selectable.class, Color.Ink.class), of(Selectable.class, Color.Edge.class), of(Selectable.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Selectable.class, Motion.Ease.class), of(Selectable.class, Affordance.Cursor.class), of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
            font: inherit;
            display: flex;
            align-items: baseline;
            gap: 8px;
            padding: 6px 8px;
            text-align: left;
            width: 100%;
            box-sizing: border-box;
            min-width: 0;
            """;
        }
    }

    /** A row's name: never squeezed. */
    public record mpa_prefs_name() implements CssClass<MpaStyles> {
        @Override public String body() { return "flex: 0 0 auto;"; }
    }

    /** Beside a row's name: the inspiration, or that these are the design's own colours; truncated before the name is. */
    public record mpa_prefs_note() implements CssClass<MpaStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Kicker.class, Type.Scale.class)); }
        @Override public String body() { return """
            margin-left: auto;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            flex: 0 1 auto;
            min-width: 0;
            """;
        }
    }

    /** The swatch dots beside a row. */
    public record mpa_prefs_dots() implements CssClass<MpaStyles> {
        @Override public String body() { return """
            display: inline-flex;
            gap: 3px;
            flex: 0 0 auto;
            align-self: center;
            """;
        }
    }

    /** One dot: its colour is DATA, the theme's own, set per dot by the module through the runtime var. */
    public record mpa_prefs_dot() implements CssClass<MpaStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--mpa-dot")); }
        @Override public String body() { return """
            width: 10px;
            height: 10px;
            border-radius: 50%;
            display: inline-block;
            background-color: var(--mpa-dot);
            """;
        }
    }

    @Override
    public List<CssClass<MpaStyles>> cssClasses() {
        return List.of(
                new mpa_page(), new mpa_root(), new mpa_header(),
                new mpa_brand(), new mpa_brand_mark(), new mpa_brand_word(),
                new mpa_crumbs(), new mpa_crumb(), new mpa_crumb_sep(), new mpa_main(),
                new mpa_prefs(), new mpa_prefs_btn(), new mpa_prefs_btn_label(), new mpa_prefs_scrim(),
                new mpa_prefs_menu(), new mpa_prefs_label(), new mpa_prefs_list(), new mpa_prefs_item(),
                new mpa_prefs_name(), new mpa_prefs_note(), new mpa_prefs_dots(), new mpa_prefs_dot());
    }
}
