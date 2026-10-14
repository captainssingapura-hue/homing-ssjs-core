package hue.captains.singapura.js.homing.design.color;

/**
 * CSS's {@code currentColor}, at a share (RFC 0067, appendix Two Classes per Leaf, D31, named by D32): the colour the
 * element is given, taken whole or in part. For what the thing drawn does not care about - only how much of the colour
 * it is given it takes: a separator's line, a scale's tick, a lamp at rest. What holds the element gives it, never a
 * palette, so it follows its holder onto any ground. Worn where taking the holder's colour does not hinder the
 * composability of the thing described; a colour that carries a meaning of its own - the accent that says where the
 * keys are, a danger, an ink - is the tree's, of another kind. It is CSS's, not the design's: an ink is a colour the
 * palette gives, in the tree's inks.
 *
 * @param <S> the scheme it is in
 * @param <P> its branch
 */
public non-sealed interface CurrentColor<S extends ColorScheme, P extends ColorBranch<? super S, ?>>
        extends ColorLeaf<S, P, LeafKind.CurrentColorKind>, SemanticColor<S> {

    /** How much of the colour it is given: more than none, at most the whole of it, 1. */
    double share();
}
