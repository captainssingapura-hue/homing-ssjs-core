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
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Semantic colours made CSS in a theme, when a sheet is made: a palettable colour as the palettes give it, a fixed one
 * as written, an interpolated one worked out along its scale. Core alone interpolates, by one rule: piecewise linear
 * between a scale's knots, in OKLab - rectangular, so the way between two colours never turns through a third hue. A
 * degree fixed in the tree is mixed when the sheet is made; a degree that moves is mixed by the browser, along the same
 * knots, from the {@code --extent} the element sets on itself, so the colour at 0.45 is the one the moving colour
 * passes at 0.45.
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
        ColorNode<?, ?, ?> node = switch (color) {
            case Palettable<?, ?> p -> p;
            case Fixed<?, ?> f -> f;
            case Interpolated<?, ?> i -> i;
            case CurrentColor<?, ?> k -> k;
        };
        if (!theme.colors().has(node)) throw new IllegalArgumentException(node.token() + " is in no colour tree read: its scheme's layer, or one below it, lists it");
        return switch (color) {
            case Palettable<?, ?> p -> theme.colors().resolve(theme.palette(), p).orElseThrow(() -> new IllegalArgumentException(
                    theme.palette().token() + " gives " + p.token() + " no colour, nor does any palette below it or a sub-palette")).css();
            case Fixed<?, ?> f -> f.color().css();
            case Interpolated<?, ?> i -> at(theme, i.scale(), i.degree());
            case CurrentColor<?, ?> k -> k.share() == 1 ? "currentColor" : "color-mix(in oklab, currentColor " + percent(k.share()) + ", transparent)";
        };
    }

    /** A paint, written as its target's declarations. */
    String body(Dressed theme, Target.Color target, Paint<?, ?> paint) {
        String property = target.properties().size() == 1 ? target.properties().iterator().next() : null;
        return switch (paint) {
            case Paint.With<?, ?> w -> property + ": " + css(theme, w.color()) + ";";
            case Paint.Inherited<?, ?> i -> target instanceof Target.Color.Scrollbar ? "scrollbar-color: inherit;" : property + ": inherit;";
            case Paint.Nothing<?, ?> n -> property + ": none;";
            case Paint.Typography<?, ?> t -> property + ": currentColor;";
            case Paint.Along<?, ?> a -> target instanceof Target.Color.Background ? ground(along(theme, a.scale())) : property + ": " + along(theme, a.scale()) + ";";
            case Paint.ThumbAndTrack<?> s -> "scrollbar-color: " + css(theme, s.thumb()) + " " + css(theme, s.track()) + ";";
            case Paint.Ground<?> g -> ground(css(theme, g.ground()));
            case Paint.Drawn<?> d -> String.join("\n",
                    "background-color: " + css(theme, d.ground()) + ";",
                    "--background-ramp-from: " + css(theme, d.rampFrom()) + ";",
                    "--background-ramp-via: " + css(theme, d.rampVia()) + ";",
                    "--background-ramp-to: " + css(theme, d.rampTo()) + ";",
                    "--background-mark: " + css(theme, d.mark()) + ";");
            case Paint.Sides<?> s -> "border-color: " + css(theme, s.top()) + " " + css(theme, s.right()) + " " + css(theme, s.bottom()) + " " + css(theme, s.left()) + ";";
            case Paint.Shadow<?> s -> shadow(css(theme, s.cast()), css(theme, s.lit()), css(theme, s.glow()));
            case Paint.Cast<?> c -> shadow(css(theme, c.cast()), CLEAR, CLEAR);
        };
    }

    /** A scale at a degree fixed when the sheet is made: a knot's colour, or the two either side of it mixed. */
    private String at(Dressed theme, ColorScale<?, ?> scale, double degree) {
        List<? extends ColorScale.Knot<?>> knots = scale.knots();
        for (int i = 0; i < knots.size(); i++) {
            ColorScale.Knot<?> k = knots.get(i);
            if (k.degree() == degree) return css(theme, (SemanticColor<?>) k.color());
            if (i > 0 && degree < k.degree()) {
                ColorScale.Knot<?> before = knots.get(i - 1);
                double share = (k.degree() - degree) / (k.degree() - before.degree());
                return "color-mix(in oklab, " + css(theme, (SemanticColor<?>) before.color()) + " " + percent(share) + ", " + css(theme, (SemanticColor<?>) k.color()) + ")";
            }
        }
        throw new IllegalArgumentException(scale.token() + " has no colour at " + degree + ": its knots run from -1 to 1");
    }

    /**
     * A scale along the element's degree: from the first knot, each next knot mixed in by how far the degree has come
     * between the two - none of it before, all of it after - so every segment but the one the degree is in is whole.
     */
    private String along(Dressed theme, ColorScale<?, ?> scale) {
        List<? extends ColorScale.Knot<?>> knots = scale.knots();
        String out = css(theme, (SemanticColor<?>) knots.get(0).color());
        for (int i = 1; i < knots.size(); i++) {
            double from = knots.get(i - 1).degree(), span = knots.get(i).degree() - from;
            String moved = from == 0 ? "var(" + Extent.VAR + ")"
                    : "var(" + Extent.VAR + ") " + (from < 0 ? "+ " + plain(-from) : "- " + plain(from));
            String share = span == 1 ? moved : "(" + moved + ") / " + plain(span);
            out = "color-mix(in oklab, " + css(theme, (SemanticColor<?>) knots.get(i).color()) + " calc(clamp(0, " + share + ", 1) * 100%), " + out + ")";
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
