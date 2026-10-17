package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.Extent;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.CurrentColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.design.color.SemanticColor;
import hue.captains.singapura.js.homing.design.styling.ColorVariables;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

/**
 * Semantic colours made CSS in a theme, when a sheet is made: a palettable colour as the palettes give it, a fixed one
 * as written, an interpolated one worked out along its scale. Core alone interpolates, by one rule: piecewise linear
 * between a scale's knots, in OKLab - rectangular, so the way between two colours never turns through a third hue. A
 * degree fixed in the tree is mixed when the sheet is made; a degree that moves is mixed by the browser, along the same
 * knots, from the {@code --extent} the element sets on itself, so the colour at 0.45 is the one the moving colour
 * passes at 0.45.
 *
 * <p>In a style alone - a local style's sheet, the same in every palette - a palettable colour is written as its
 * palette's variable, {@code var(--fp-accent)}, which the palette's own sheet defines; a fixed colour as written, and an
 * interpolated one mixed from its knots so written. A switch of palette repaints it.</p>
 *
 * <p>A paint is written as its target's declarations, in one fixed order: its first property, then a ramp's stops from
 * the first, then a pattern's marks; a shadow's cast, lit and glow.</p>
 */
record RenderColor() implements StatelessFunctionalObject {

    static final RenderColor INSTANCE = new RenderColor();

    /** A colour property with nothing drawn in it. */
    private static final String CLEAR = "transparent";

    /** A semantic colour as CSS, in the theme's palettes; refused when the theme's colour tree has it not. */
    String css(Dressed theme, SemanticColor<?> color) {
        in(theme.colors(), color);
        return switch (color) {
            case Palettable<?, ?> p -> theme.colors().resolve(theme.palette(), p).orElseThrow(() -> new IllegalArgumentException(
                    theme.palette().token() + " gives " + p.token() + " no colour, nor does any palette below it or a sub-palette")).css();
            case Fixed<?, ?> f -> f.color().css();
            case Interpolated<?, ?> i -> at(c -> css(theme, c), i.scale(), i.degree());
            case CurrentColor<?, ?> k -> current(k);
        };
    }

    /**
     * A semantic colour as CSS in a style, the same in every palette: a palettable one as its palette's variable; refused
     * when the style's colour tree has it not.
     */
    String css(InStyle style, SemanticColor<?> color) {
        in(style.colours(), color);
        return switch (color) {
            case Palettable<?, ?> p -> "var(" + ColorVariables.INSTANCE.of(p) + ")";
            case Fixed<?, ?> f -> f.color().css();
            case Interpolated<?, ?> i -> at(c -> css(style, c), i.scale(), i.degree());
            case CurrentColor<?, ?> k -> current(k);
        };
    }

    /** A paint, written as its target's declarations, its colours in the theme's palettes. */
    String body(Dressed theme, Target.Color target, Paint<?, ?> paint) { return body(c -> css(theme, c), target, paint); }

    /** A paint, written as its target's declarations, its colours as a style writes them: the same in every palette. */
    String body(InStyle style, Target.Color target, Paint<?, ?> paint) { return body(c -> css(style, c), target, paint); }

    private String body(Function<SemanticColor<?>, String> css, Target.Color target, Paint<?, ?> paint) {
        String property = target.properties().size() == 1 ? target.properties().iterator().next() : null;
        return switch (paint) {
            case Paint.With<?, ?> w -> property + ": " + css.apply(w.color()) + ";";
            case Paint.Inherited<?, ?> i -> target instanceof Target.Color.Scrollbar ? "scrollbar-color: inherit;" : property + ": inherit;";
            case Paint.Nothing<?, ?> n -> property + ": none;";
            case Paint.Typography<?, ?> t -> property + ": currentColor;";
            case Paint.Along<?, ?> a -> target instanceof Target.Color.Background ? ground(along(css, a.scale())) : property + ": " + along(css, a.scale()) + ";";
            case Paint.ThumbAndTrack<?> s -> "scrollbar-color: " + css.apply(s.thumb()) + " " + css.apply(s.track()) + ";";
            case Paint.Ground<?> g -> ground(css.apply(g.ground()));
            case Paint.Drawn<?> d -> String.join("\n",
                    "background-color: " + css.apply(d.ground()) + ";",
                    "--background-ramp-from: " + css.apply(d.rampFrom()) + ";",
                    "--background-ramp-via: " + css.apply(d.rampVia()) + ";",
                    "--background-ramp-to: " + css.apply(d.rampTo()) + ";",
                    "--background-mark: " + css.apply(d.mark()) + ";");
            case Paint.Sides<?> s -> "border-color: " + css.apply(s.top()) + " " + css.apply(s.right()) + " " + css.apply(s.bottom()) + " " + css.apply(s.left()) + ";";
            case Paint.Shadow<?> s -> shadow(css.apply(s.cast()), css.apply(s.lit()), css.apply(s.glow()));
            case Paint.Cast<?> c -> shadow(css.apply(c.cast()), CLEAR, CLEAR);
        };
    }

    /** Refused when the colour tree has the colour not. */
    private static void in(ColorTree colours, SemanticColor<?> color) {
        ColorNode<?, ?, ?> node = switch (color) {
            case Palettable<?, ?> p -> p;
            case Fixed<?, ?> f -> f;
            case Interpolated<?, ?> i -> i;
            case CurrentColor<?, ?> k -> k;
        };
        if (!colours.has(node)) throw new IllegalArgumentException(node.token() + " is in no colour tree read: its scheme's layer, or one below it, lists it");
    }

    /** CSS's currentColor at a share, the rest clear. */
    private static String current(CurrentColor<?, ?> k) {
        return k.share() == 1 ? "currentColor" : "color-mix(in oklab, currentColor " + percent(k.share()) + ", transparent)";
    }

    /** A scale at a degree fixed when the sheet is made: a knot's colour, or the two either side of it mixed. */
    private String at(Function<SemanticColor<?>, String> css, ColorScale<?, ?> scale, double degree) {
        List<? extends ColorScale.Knot<?>> knots = scale.knots();
        for (int i = 0; i < knots.size(); i++) {
            ColorScale.Knot<?> k = knots.get(i);
            if (k.degree() == degree) return css.apply((SemanticColor<?>) k.color());
            if (i > 0 && degree < k.degree()) {
                ColorScale.Knot<?> before = knots.get(i - 1);
                double share = (k.degree() - degree) / (k.degree() - before.degree());
                return "color-mix(in oklab, " + css.apply((SemanticColor<?>) before.color()) + " " + percent(share) + ", " + css.apply((SemanticColor<?>) k.color()) + ")";
            }
        }
        throw new IllegalArgumentException(scale.token() + " has no colour at " + degree + ": its knots run from -1 to 1");
    }

    /**
     * A scale along the element's degree: from the first knot, each next knot mixed in by how far the degree has come
     * between the two - none of it before, all of it after - so every segment but the one the degree is in is whole.
     */
    private String along(Function<SemanticColor<?>, String> css, ColorScale<?, ?> scale) {
        List<? extends ColorScale.Knot<?>> knots = scale.knots();
        String out = css.apply((SemanticColor<?>) knots.get(0).color());
        for (int i = 1; i < knots.size(); i++) {
            double from = knots.get(i - 1).degree(), span = knots.get(i).degree() - from;
            String moved = from == 0 ? "var(" + Extent.VAR + ")"
                    : "var(" + Extent.VAR + ") " + (from < 0 ? "+ " + plain(-from) : "- " + plain(from));
            String share = span == 1 ? moved : "(" + moved + ") / " + plain(span);
            out = "color-mix(in oklab, " + css.apply((SemanticColor<?>) knots.get(i).color()) + " calc(clamp(0, " + share + ", 1) * 100%), " + out + ")";
        }
        return out;
    }

    /** A ground and nothing drawn on it: a ramp's stops and a pattern's marks clear. */
    private static String ground(String colour) {
        return String.join("\n", "background-color: " + colour + ";", "--background-ramp-from: " + CLEAR + ";",
                "--background-ramp-via: " + CLEAR + ";", "--background-ramp-to: " + CLEAR + ";", "--background-mark: " + CLEAR + ";");
    }

    private static String shadow(String cast, String lit, String glow) {
        return String.join("\n", "--box-shadow-cast: " + cast + ";", "--box-shadow-lit: " + lit + ";", "--box-shadow-glow: " + glow + ";");
    }

    private static String percent(double share) {
        return BigDecimal.valueOf(share * 100).setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString() + "%";
    }

    private static String plain(double d) { return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString(); }
}
