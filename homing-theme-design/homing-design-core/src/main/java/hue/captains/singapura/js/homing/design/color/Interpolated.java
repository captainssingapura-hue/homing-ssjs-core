package hue.captains.singapura.js.homing.design.color;

/**
 * A colour a scale gives at a degree: a fixed point of a scale given a meaning of its own, so a style still names a
 * semantic colour and nothing else. Its scale is designed for its own scheme or one below, and core works the colour
 * out when a sheet is made, under the worn palettes.
 *
 * @param <S> the scheme it is in
 * @param <P> its branch
 */
public non-sealed interface Interpolated<S extends ColorScheme, P extends ColorBranch<? super S, ?>>
        extends ColorLeaf<S, P, LeafKind.InterpolatedKind>, SemanticColor<S> {

    /** The scale it is drawn from. */
    ColorScale<? super S, ?> scale();

    /** Where along the scale, from -1 to 1. */
    double degree();
}
