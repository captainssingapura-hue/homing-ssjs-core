package hue.captains.singapura.js.homing.design.color;

/**
 * A colour a target may be painted in: a leaf of one of the colour kinds - palettable, fixed, interpolated or an ink - and never a scale.
 * A style names one for each colour a target writes, of any kind, and only the resolver tells the kinds apart. A style of a
 * scheme names a colour its scheme can see: {@code SemanticColor<? super S>}.
 *
 * @param <S> the scheme it is in
 */
public sealed interface SemanticColor<S extends ColorScheme> permits Palettable, Fixed, Interpolated, Ink {}
