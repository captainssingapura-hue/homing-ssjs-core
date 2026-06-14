package hue.captains.singapura.js.homing.tree;

/**
 * Graft — the one shared operation on the normalized layer (RFC 0040): a pure
 * recursive level shift. A sub-tree is normalized once as a standalone tree
 * rooted at {@code L0}; grafting it under a host re-levels every node by a
 * fixed delta. Nothing but the tree structure and the delta is required — no
 * spec, no re-normalization, no host context.
 *
 * <p>The shift is purely structural: it copies each node's dimensions
 * untouched and re-levels exactly one field ({@code level}), so a sub-tree
 * built once can be grafted, unchanged, at any depth. A shift whose result
 * would leave the rigid {@code L0..L18} range is a hard error (the cap is not
 * silently clamped — {@link TreeLevel#shifted(int)} throws).</p>
 *
 * @since homing-rigid-tree (RFC 0040)
 */
public final class RigidTrees {

    private RigidTrees() {}

    /** Re-level a whole subtree by {@code by} steps (negative shifts shallower). */
    public static NormalizedNode shift(NormalizedNode node, int by) {
        if (by == 0) return node;
        return new NormalizedNode(
                node.level().shifted(by),               // hard error if outside 0..18
                node.dimensions(),
                node.children().stream().map(c -> shift(c, by)).toList());
    }

    /** Re-level a subtree so its root sits at {@code target}. */
    public static NormalizedNode shiftedTo(NormalizedNode node, TreeLevel target) {
        return shift(node, target.depth() - node.level().depth());
    }

    /**
     * Graft a standalone sub-tree under a host node: the sub-tree's root lands
     * one level below {@code hostLevel}. Throws if the host is already at the
     * {@code L18} cap (no room for a child).
     */
    public static NormalizedNode graftUnder(NormalizedNode subtree, TreeLevel hostLevel) {
        TreeLevel childLevel = hostLevel.below().orElseThrow(() ->
                new IllegalArgumentException(
                        "cannot graft under " + hostLevel.tag()
                      + ": already at the L" + TreeLevel.MAX_DEPTH + " cap"));
        return shiftedTo(subtree, childLevel);
    }
}
