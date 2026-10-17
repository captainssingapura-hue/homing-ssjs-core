package hue.captains.singapura.js.homing.design.color;

/**
 * A colour the worn palette gives: {@code record Accent() implements Palettable<BaseScheme, Attention>}. Every
 * palette of its scheme gives it, or a palette of a scheme above, or the sub-palette its extension supplies; how a
 * palette arrives at the concrete colour is the palette's own concern.
 *
 * @param <S> the scheme it is in
 * @param <P> its branch
 */
public non-sealed interface Palettable<S extends ColorScheme, P extends ColorBranch<? super S, ?>>
        extends ColorLeaf<S, P, LeafKind.PalettableKind>, SemanticColor<S> {}
