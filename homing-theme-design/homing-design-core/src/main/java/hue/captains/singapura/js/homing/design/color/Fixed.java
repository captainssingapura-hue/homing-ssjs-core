package hue.captains.singapura.js.homing.design.color;

/**
 * A colour its declaration gives, the same under every palette: a mine's red in a classic minesweeper. Rare, and
 * explicit where it is used, since a fixed colour opts out of every palette.
 *
 * @param <S> the scheme it is in
 * @param <P> its branch
 */
public non-sealed interface Fixed<S extends ColorScheme, P extends ColorBranch<? super S, ?>>
        extends ColorLeaf<S, P, LeafKind.FixedKind>, SemanticColor<S> {

    /** The colour, as written. */
    ConcreteColor color();
}
