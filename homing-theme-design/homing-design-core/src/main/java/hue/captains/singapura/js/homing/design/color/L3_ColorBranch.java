package hue.captains.singapura.js.homing.design.color;

/**
 * A branch at the third level, under a second-level branch its scheme can see.
 *
 * @param <S> the scheme it is in
 * @param <P> its parent
 */
public non-sealed interface L3_ColorBranch<S extends ColorScheme, P extends L2_ColorBranch<? super S, ?>> extends ColorBranch<S, P> {}
