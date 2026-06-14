package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNode;

import java.util.List;
import java.util.Map;

/**
 * A node produced by {@link CatalogueTreeAdapter}. One record serves every
 * depth: the level singleton is carried as a field (typed {@link TreeLevel})
 * rather than via a per-level record type.
 *
 * <p>Why not per-level records (CatalogueNodeL0, L1, …)? The leveled
 * <i>types</i> earn their keep for <b>hand-written</b> trees, where the
 * compiler enforces that an L1's children are L2s. A catalogue's depth is a
 * <b>runtime</b> property, so a dynamic adapter cannot benefit from that
 * compile-time check — it would only pay the boilerplate. This record carries
 * the correct level singleton at runtime; the emitted JSON is identical to a
 * per-level-typed tree. Strong leveled typing remains available in the
 * substrate for the authors who can use it.</p>
 *
 * @param level    the runtime level singleton (L0 at the root, L1 below, …)
 * @param dimensions ordered typed metadata (LinkedHashMap for stable JSON)
 * @param kids      children, one level below; empty for leaves
 * @since homing-tree-views v1
 */
public record CatalogueTreeNode(
        TreeLevel level,
        Map<DimensionKey, DimensionValue> dimensions,
        List<CatalogueTreeNode> kids
) implements TreeNode<TreeLevel> {

    @Override public List<? extends TreeNode<?>> children() { return kids; }
}
