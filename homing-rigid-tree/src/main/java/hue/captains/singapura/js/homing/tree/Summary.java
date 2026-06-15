package hue.captains.singapura.js.homing.tree;

/**
 * A short descriptive blurb for a node — universal across tree-kinds.
 * Part of the substrate's closed key vocabulary (permitted by
 * {@link DimensionKey}). Carried in the tree JSON so a navigation/detail
 * view can show a node's summary without a server round-trip — essential
 * for branch nodes (e.g. catalogues) that have no addressable content of
 * their own. The value is text (typically {@code NameValue}).
 *
 * <p>Descriptive, not a grouping axis — but universal in the same way
 * {@link DisplayLabel} is, so it belongs in the closed substrate set.</p>
 *
 * @since homing-tree-views v1
 */
public record Summary() implements DimensionKey {
    public static final Summary INSTANCE = new Summary();
    @Override public String tag() { return "summary"; }
}
