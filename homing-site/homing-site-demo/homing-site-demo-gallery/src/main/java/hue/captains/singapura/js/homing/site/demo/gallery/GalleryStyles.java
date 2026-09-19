package hue.captains.singapura.js.homing.site.demo.gallery;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Display;
import static hue.captains.singapura.js.homing.design.Text.Kicker;
import static hue.captains.singapura.js.homing.design.Text.Lede;
import static hue.captains.singapura.js.homing.design.Text.Numeral;

/** The two apps' own classes — kicker, title, lede, count and two grids. Cards and buttons are the shared elements'. */
public record GalleryStyles() implements CssGroup<GalleryStyles> {

    public static final GalleryStyles INSTANCE = new GalleryStyles();

    public record ga_kicker() implements CssClass<GalleryStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(Kicker.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0 0 6px;"; }
    }

    public record ga_title() implements CssClass<GalleryStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Display.class, Type.Face.class), of(Display.class, Type.Decoration.class), of(Display.class, Type.Scale.class), of(Display.class, Type.Weight.class), of(Display.class, Type.Treatment.class), of(Display.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0 0 8px;"; }
    }

    public record ga_lede() implements CssClass<GalleryStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Lede.class, Color.Ink.class), of(Lede.class, Type.Scale.class), of(Lede.class, Type.Treatment.class)); }
        @Override public String body() { return "margin: 0 0 28px; max-width: 46rem;"; }
    }

    public record ga_cards() implements CssClass<GalleryStyles> {
        @Override public String body() { return """
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
            gap: 16px;
            """;
        }
    }

    public record ga_count() implements CssClass<GalleryStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Numeral.class, Type.Face.class), of(Numeral.class, Type.Scale.class), of(Numeral.class, Type.Weight.class), of(Display.class, Color.Ink.class)); }
        @Override public String body() { return """
            font-size: 96px;
            line-height: 1;
            margin: 24px 0;
            """;
        }
    }

    public record ga_buttons() implements CssClass<GalleryStyles> {
        @Override public String body() { return "display: flex; gap: 10px;"; }
    }

    @Override
    public List<CssClass<GalleryStyles>> cssClasses() {
        return List.of(new ga_kicker(), new ga_title(), new ga_lede(), new ga_cards(), new ga_count(), new ga_buttons());
    }
}
