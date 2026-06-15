package hue.captains.singapura.js.homing.tree;

/**
 * The node's zero-indexed depth from the tree root, exposed as a typed
 * dimension so pivot/grid transforms can group by depth without
 * special-casing it. Part of the substrate's closed key vocabulary
 * (permitted by {@link DimensionKey}). Its value is carried by
 * {@code DepthValue}.
 *
 * @since homing-tree-views v1
 */
public record LevelDepth() implements DimensionKey {
    public static final LevelDepth INSTANCE = new LevelDepth();
    @Override public String tag() { return "levelDepth"; }
}
