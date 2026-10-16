package hue.captains.singapura.js.homing.design.color;

/**
 * A branch at the top of the colour tree, in its scheme: {@code record Attention() implements L1_ColorBranch<BaseScheme>}.
 *
 * @param <S> the scheme it is in
 */
public non-sealed interface L1_ColorBranch<S extends ColorScheme> extends ColorBranch<S, ColorBranch.Top> {

    /** At the top: its parent is the top. */
    @Override default ColorBranch.Top parent() { return ColorBranch.Top.INSTANCE; }
}
