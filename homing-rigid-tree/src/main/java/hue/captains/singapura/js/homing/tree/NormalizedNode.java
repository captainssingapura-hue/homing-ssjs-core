package hue.captains.singapura.js.homing.tree;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The one canonical normalized node — the single shape every source encoding
 * maps to (via a {@link TreeNormalizer}) and the only shape the serializer and
 * renderer consume (RFC 0040).
 *
 * <p>Explicitly leveled: the node carries its {@link TreeLevel} as a runtime
 * value. Depth lives in {@link #level()} alone — never duplicated as a
 * dimension — so a graft (a pure recursive level shift, see {@link RigidTrees})
 * touches exactly one field per node. Immutable value object: dimensions are
 * order-preserving and unmodifiable; children are an immutable copy.</p>
 *
 * <p>Identity is purely positional (RFC 0040): a node carries no id — its
 * tree path is its identity, derived structurally by consumers. Only the
 * level, typed dimensions, and children travel.</p>
 *
 * @since homing-rigid-tree (RFC 0040)
 */
public record NormalizedNode(TreeLevel level,
                             Map<DimensionKey, DimensionValue> dimensions,
                             List<NormalizedNode> children)
        implements TreeNode<TreeLevel> {

    public NormalizedNode {
        Objects.requireNonNull(level, "level");
        dimensions = Collections.unmodifiableMap(
                new LinkedHashMap<>(dimensions == null ? Map.of() : dimensions));
        children = List.copyOf(children == null ? List.of() : children);
    }

    /** A leaf node — no children. */
    public static NormalizedNode leaf(TreeLevel level,
                                      Map<DimensionKey, DimensionValue> dimensions) {
        return new NormalizedNode(level, dimensions, List.of());
    }
}
