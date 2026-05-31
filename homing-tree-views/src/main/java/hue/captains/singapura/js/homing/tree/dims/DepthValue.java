package hue.captains.singapura.js.homing.tree.dims;

import hue.captains.singapura.js.homing.tree.DimensionValue;

/**
 * Standard {@link DimensionValue} carrying a zero-indexed depth — the
 * canonical pairing for {@link LevelDepth}. Stored as an int and
 * stringified for {@code displayText()} so pivot/grid transforms can
 * cast back to numeric without parsing.
 *
 * @since homing-tree-views v1
 */
public record DepthValue(int depth) implements DimensionValue {
    public DepthValue {
        if (depth < 0) throw new IllegalArgumentException("depth must be non-negative: " + depth);
    }
    @Override public String tag() { return "depth"; }
    @Override public String displayText() { return Integer.toString(depth); }
}
