package hue.captains.singapura.js.homing.design.color;

/**
 * A branch at the top of the colour tree, in its scheme: {@code record Attention() implements L1_ColorBranch<Base>}.
 *
 * @param <S> the scheme it is in
 */
public non-sealed interface L1_ColorBranch<S extends ColorScheme> extends ColorBranch<S, ColorBranch.Top> {}
