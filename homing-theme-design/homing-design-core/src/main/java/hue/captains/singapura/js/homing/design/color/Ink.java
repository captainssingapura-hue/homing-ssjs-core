package hue.captains.singapura.js.homing.design.color;

/**
 * A share of the ink the element stands on: CSS's {@code currentColor}, at a share (RFC 0067, appendix Two Classes per
 * Leaf, D31). A colour the thing drawn does not care about - only how much of its holder's ink it takes: a separator's
 * line, the sign between a trail's steps, a scale's tick, a caption's words. What the element stands on gives it, never
 * a palette, so it follows its holder onto any ground. Worn where taking the holder's ink does not hinder the
 * composability of the thing described; a colour that carries a meaning of its own - the accent that says where the
 * keys are, a danger - is the tree's, of another kind.
 *
 * @param <S> the scheme it is in
 * @param <P> its branch
 */
public non-sealed interface Ink<S extends ColorScheme, P extends ColorBranch<? super S, ?>>
        extends ColorLeaf<S, P, LeafKind.InkKind>, SemanticColor<S> {

    /** How much of the ink: more than none, at most the ink whole, 1. */
    double share();
}
