package hue.captains.singapura.js.homing.design.color;

/**
 * A leaf of the colour tree: a colour that exists for a purpose, or a scale the colours of one purpose are drawn along.
 * Its meaning is universal across styles: a style says which colour a thing uses, never what a colour means. It sits
 * under a branch its scheme can see, and is of one kind.
 *
 * @param <S> the scheme it is in
 * @param <P> its branch
 * @param <K> its kind
 */
public sealed interface ColorLeaf<S extends ColorScheme, P extends ColorBranch<? super S, ?>, K extends LeafKind>
        extends ColorNode<S, P, K> permits Palettable, Fixed, Interpolated, CurrentColor, ColorScale {}
