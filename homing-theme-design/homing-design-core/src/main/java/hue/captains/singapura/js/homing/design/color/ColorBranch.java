package hue.captains.singapura.js.homing.design.color;

/**
 * A branch of the colour tree: it groups, and is no colour. It is in one scheme, at one level, under one parent its
 * scheme can see - one whose scheme is its own or one below it - so a scheme's branches graft under the base's, and the
 * compiler refuses the base's under a scheme's. Its level is the interface it implements, and each level names the
 * level above as its parent's, so no chain of parents comes back.
 *
 * @param <S> the scheme it is in
 * @param <P> its parent: a branch its scheme can see, or {@link Top} for a branch at the top of the tree
 */
public sealed interface ColorBranch<S extends ColorScheme, P> extends ColorNode<S, P, ColorNode.BranchKind>
        permits L1_ColorBranch, L2_ColorBranch, L3_ColorBranch {

    /** The parent of a branch at the top of the tree: none. */
    final class Top {
        private Top() {}
    }
}
