package hue.captains.singapura.js.homing.tree;

/**
 * The human-readable label every node carries. Part of the substrate's
 * closed key vocabulary (permitted by {@link DimensionKey}). Lives in the
 * {@code tree} package, beside {@code DimensionKey}, so the sealed permits
 * resolve without a named module. The label's value is carried by a
 * {@code DimensionValue} (typically {@code NameValue}).
 *
 * @since homing-tree-views v1
 */
public record DisplayLabel() implements DimensionKey {
    public static final DisplayLabel INSTANCE = new DisplayLabel();
    @Override public String tag() { return "displayLabel"; }
}
