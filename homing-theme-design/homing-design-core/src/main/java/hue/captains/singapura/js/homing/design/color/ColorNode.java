package hue.captains.singapura.js.homing.design.color;

import hue.captains.singapura.js.homing.design.Trees;

/**
 * A node of the colour tree (RFC 0067, appendix Semantic Colours and Flavours): a branch, which groups, or a leaf, a
 * colour or a scale. Its scheme, its parent and its kind are type arguments of this one interface, which a class cannot
 * implement twice with different arguments: a node in two schemes, under two parents, of two kinds - a branch that is
 * also a leaf, a leaf both palettable and fixed - does not compile. A reader reads all three off the declaration.
 *
 * <p>A node is a stateless record; its type is its meaning. Names are unique across the tree, as a node's token is its
 * name: {@code AccentEdge} is {@code accent-edge}.</p>
 *
 * @param <S> the scheme it is in
 * @param <P> its parent
 * @param <K> its kind
 */
public sealed interface ColorNode<S extends ColorScheme, P, K extends ColorNode.Kind> permits ColorBranch, ColorLeaf {

    /** {@code accent-edge}, from its type. */
    default String token() { return Trees.kebab(getClass().getSimpleName()); }

    /** What a node is: a branch, or a leaf of one of the leaf kinds. */
    sealed interface Kind permits BranchKind, LeafKind {}

    /** A branch: it groups, and is no colour. */
    record BranchKind() implements Kind {}
}
