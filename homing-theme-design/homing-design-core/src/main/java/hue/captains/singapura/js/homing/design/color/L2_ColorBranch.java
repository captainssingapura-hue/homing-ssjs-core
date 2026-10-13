package hue.captains.singapura.js.homing.design.color;

/**
 * A branch under a branch at the top, which its scheme can see: its parent's scheme is its own or one below it.
 *
 * @param <S> the scheme it is in
 * @param <P> its parent
 */
public non-sealed interface L2_ColorBranch<S extends ColorScheme, P extends L1_ColorBranch<? super S>> extends ColorBranch<S, P> {}
