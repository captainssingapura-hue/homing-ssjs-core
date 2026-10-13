package hue.captains.singapura.js.homing.design.color;

import java.util.List;

/**
 * A colour scale: a range of colours designed for a scheme, the extent a colour moves along. Three knots or more, each
 * a concrete colour - palettable or fixed, never interpolated, so no scale is built of another - that its scheme can
 * see, each at a degree it names: strictly rising, the first at -1 and the last at 1, so every degree has a colour.
 * Between two knots core interpolates, by one rule. A scale is no colour a slot holds; a style answers a slot along it
 * - live, its degree set on the element - or names one of its interpolated colours.
 *
 * @param <S> the scheme it is in
 * @param <P> its branch
 */
public non-sealed interface ColorScale<S extends ColorScheme, P extends ColorBranch<? super S, ?>>
        extends ColorLeaf<S, P, LeafKind.ScaleKind> {

    /** Its knots, in rising degree. */
    List<Knot<S>> knots();

    /**
     * A key colour of a scale, at its degree.
     *
     * @param color  a concrete colour the scale's scheme can see
     * @param degree from -1 to 1
     */
    record Knot<S extends ColorScheme>(ColorLeaf<? super S, ?, ? extends LeafKind.ConcreteKind> color, double degree) {
        public Knot {
            if (color == null) throw new IllegalArgumentException("a knot has a colour");
            if (!(degree >= -1 && degree <= 1)) throw new IllegalArgumentException(color.token() + ": a knot's degree is from -1 to 1, not " + degree);
        }
    }
}
