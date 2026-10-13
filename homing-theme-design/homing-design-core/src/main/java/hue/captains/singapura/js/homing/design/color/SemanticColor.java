package hue.captains.singapura.js.homing.design.color;

/**
 * A colour a slot may hold: a leaf of one of the colour kinds - palettable, fixed or interpolated - and never a scale.
 * A style names one for each slot it answers, of any kind, and only the resolver tells the kinds apart. A style of a
 * scheme names a colour its scheme can see: {@code SemanticColor<? super S>}.
 *
 * @param <S> the scheme it is in
 */
public sealed interface SemanticColor<S extends ColorScheme> permits Palettable, Fixed, Interpolated {}
