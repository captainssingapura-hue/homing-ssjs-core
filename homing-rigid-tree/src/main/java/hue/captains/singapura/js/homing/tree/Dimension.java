package hue.captains.singapura.js.homing.tree;

/**
 * Typed (key, value) pair attached to a {@link TreeNode}. Constructed by
 * adapter records and emitted by the JSON writer as a self-describing
 * object {@code { "key": "<tag>", "valueTag": "<tag>", "...": ... }}.
 *
 * <p>Doctrine — Names Are Types: both key and value are typed; raw
 * strings never appear on the substrate boundary.</p>
 *
 * @since homing-tree-views v1
 */
public record Dimension(DimensionKey key, DimensionValue value) {
    public Dimension {
        if (key == null) throw new IllegalArgumentException("key");
        if (value == null) throw new IllegalArgumentException("value");
    }
}
