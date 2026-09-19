package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.InLayer;
import hue.captains.singapura.js.homing.core.Layout;
import hue.captains.singapura.js.homing.core.Reset;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Box.Control;
import static hue.captains.singapura.js.homing.design.Brand.House;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Layer.Base;
import static hue.captains.singapura.js.homing.design.Layer.Inverted;
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
import static hue.captains.singapura.js.homing.design.Text.Link;

/**
 * The chrome's classes. Every colour, face, edge and motion is a design word
 * worn, never a value: the chrome looks like whatever design the page wears,
 * and it paints nothing of its own. The bodies hold only layout.
 *
 * <p>The words are the ones a page chrome needs and nothing more — the
 * inverted bar, the house word, the muted crumbs, a control for the
 * preferences button. A design that binds the studio's vocabulary binds all
 * of these; the preferences dialog wears its own.</p>
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

    /**
     * The slot as a full-bleed column: no reading width, no padding, its
     * children stacked and the last one free to fill it. An app that lays
     * itself out — a shell of panes — adds this to the slot it is given.
     */
    public record mpa_main_full() implements CssClass<MpaStyles>, InLayer<Layout> {
        @Override public String body() { return """
            max-width: none;
            padding: 0;
            display: flex;
            flex-direction: column;
            min-height: 0;
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

    @Override
    public List<CssClass<MpaStyles>> cssClasses() {
        return List.of(
                new mpa_page(), new mpa_root(), new mpa_header(),
                new mpa_brand(), new mpa_brand_mark(), new mpa_brand_word(),
                new mpa_crumbs(), new mpa_crumb(), new mpa_crumb_sep(), new mpa_main(), new mpa_main_full(),
                new mpa_prefs(), new mpa_prefs_btn(), new mpa_prefs_btn_label());
    }
}
