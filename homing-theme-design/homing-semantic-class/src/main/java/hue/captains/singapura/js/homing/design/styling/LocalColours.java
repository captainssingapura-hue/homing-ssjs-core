package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.Extent;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.CurrentColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.SemanticColor;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * What a colour assigned in a leaf's own CSS must say (RFC 0067, appendix Two Classes per Leaf, D13, D14) - the one value
 * the tree gives it, which the check holds each assignment to:
 *
 * <ul>
 *   <li>{@code --fixed-<leaf>}: the colour as declared;</li>
 *   <li>{@code --it-<leaf>}: its scale at its degree - a knot's variable, or the two either side mixed in OKLab;</li>
 *   <li>{@code --along-<scale>}: the scale by the element's {@code --extent}, from the first knot, each next mixed in
 *       by how far the degree has come between the two;</li>
 *   <li>{@code --current-<leaf>}: the colour the element is given, {@code currentColor}, at its share - the rest clear (D31, D32).</li>
 * </ul>
 *
 * Every anchor is a knot's variable, {@code --fp-} or {@code --fixed-}, never a literal colour: the palette's colours
 * stay the palette's. Core's one rule of interpolation, piecewise linear between the knots in OKLab, as a sheet made
 * from the walk wrote it.
 */
public record LocalColours() implements StatelessFunctionalObject {

    public static final LocalColours INSTANCE = new LocalColours();

    /** {@code #c62828}. */
    public String fixed(Fixed<?, ?> color) { return color.color().css(); }

    /** {@code color-mix(in oklab, currentColor 30%, transparent)}; the whole of it, {@code currentColor}. */
    public String current(CurrentColor<?, ?> color) {
        return color.share() == 1 ? "currentColor" : "color-mix(in oklab, currentColor " + percent(color.share()) + ", transparent)";
    }

    /** {@code color-mix(in oklab, var(--fixed-clear) 50%, var(--fp-danger))}. */
    public String interpolated(Interpolated<?, ?> color) {
        ColorScale<?, ?> scale = color.scale();
        double degree = color.degree();
        List<? extends ColorScale.Knot<?>> knots = scale.knots();
        for (int i = 0; i < knots.size(); i++) {
            ColorScale.Knot<?> k = knots.get(i);
            if (k.degree() == degree) return knot(k);
            if (i > 0 && degree < k.degree()) {
                ColorScale.Knot<?> before = knots.get(i - 1);
                double share = (k.degree() - degree) / (k.degree() - before.degree());
                return "color-mix(in oklab, " + knot(before) + " " + percent(share) + ", " + knot(k) + ")";
            }
        }
        throw new IllegalArgumentException(scale.token() + " has no colour at " + degree + ": its knots run from -1 to 1");
    }

    /** The scale along the element's degree: each knot after the first mixed in over the one before. */
    public String along(ColorScale<?, ?> scale) {
        List<? extends ColorScale.Knot<?>> knots = scale.knots();
        String out = knot(knots.get(0));
        for (int i = 1; i < knots.size(); i++) {
            double from = knots.get(i - 1).degree(), span = knots.get(i).degree() - from;
            String moved = from == 0 ? "var(" + Extent.VAR + ")"
                    : "var(" + Extent.VAR + ") " + (from < 0 ? "+ " + plain(-from) : "- " + plain(from));
            String share = span == 1 ? moved : "(" + moved + ") / " + plain(span);
            out = "color-mix(in oklab, " + knot(knots.get(i)) + " calc(clamp(0, " + share + ", 1) * 100%), " + out + ")";
        }
        return out;
    }

    /** The variables a scale's knots are named by, in rising degree: {@code --fp-safe}. */
    public List<String> anchors(ColorScale<?, ?> scale) {
        return scale.knots().stream().map(k -> ColorVariables.INSTANCE.of((SemanticColor<?>) k.color())).toList();
    }

    private static String knot(ColorScale.Knot<?> knot) { return "var(" + ColorVariables.INSTANCE.of((SemanticColor<?>) knot.color()) + ")"; }

    private static String percent(double share) {
        return BigDecimal.valueOf(share * 100).setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString() + "%";
    }

    /** A degree or a span, written plain - to six places, so 0.6 - 0.4 is 0.2 and not its double's tail. */
    private static String plain(double d) { return BigDecimal.valueOf(d).setScale(6, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString(); }
}
