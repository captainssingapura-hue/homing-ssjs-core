package hue.captains.singapura.js.homing.tree.dims;

import hue.captains.singapura.js.homing.tree.DimensionValue;

/**
 * Standard {@link DimensionValue} carrying a human-readable string —
 * the canonical pairing for {@link DisplayLabel}. Adapter records wrap
 * the native node's name/title in {@code NameValue} at the substrate
 * boundary; raw strings never leak past the adapter.
 *
 * @since homing-tree-views v1
 */
public record NameValue(String text) implements DimensionValue {
    public NameValue {
        if (text == null) throw new IllegalArgumentException("text");
    }
    @Override public String tag() { return "name"; }
    @Override public String displayText() { return text; }
}
