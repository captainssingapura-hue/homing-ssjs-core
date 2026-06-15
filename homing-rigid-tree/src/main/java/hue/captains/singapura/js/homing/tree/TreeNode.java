package hue.captains.singapura.js.homing.tree;

import java.util.List;

/**
 * Immutable typed leveled node — the rigid tree substrate's atomic value.
 *
 * <p>Per-tree-kind adapter records implement {@code TreeNode<L>} for a
 * specific level; the type parameter pins the depth so the compiler
 * enforces that {@link #children()} returns nodes one level below.
 * Native shapes (catalogues, ComposedDoc segments, future tree-shaped
 * doc kinds) become trees by writing one tiny adapter record per level
 * — no per-kind JS, no schema duplication.</p>
 *
 * <p>Value-object discipline: equality is by content; a node and its
 * children list are immutable. The substrate gains all the usual
 * value-object benefits (caching, structural sharing, trivial equality
 * for diffing) for free.</p>
 *
 * <p>Names Are Types: {@link #dimensions()} is typed
 * ({@code Map<DimensionKey, DimensionValue>}); no raw-string keys or
 * stringly-typed values escape the contract.</p>
 *
 * @param <L> the level this node sits at
 * @since homing-tree-views v1
 */
public interface TreeNode<L extends TreeLevel> {

    /** The level singleton this node sits at — pins {@code L} at runtime. */
    L level();

    /**
     * Typed metadata, including {@code DisplayLabel} (the human-readable
     * label) and any per-tree-kind dimensions (e.g. category, kind, depth).
     * Keys and values are both typed — no raw strings on the contract.
     */
    java.util.Map<DimensionKey, DimensionValue> dimensions();

    /**
     * Children, one level below. Empty list = leaf node.
     *
     * <p>Returned as {@code List<? extends TreeNode<?>>} so adapter
     * implementations may parameterise their child type more precisely
     * while still satisfying the substrate contract.</p>
     */
    List<? extends TreeNode<?>> children();
}
