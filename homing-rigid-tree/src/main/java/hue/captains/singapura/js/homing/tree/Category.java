package hue.captains.singapura.js.homing.tree;

/**
 * The grouping category a node belongs to — the primary axis a pivot/grid
 * view typically groups by. Part of the substrate's closed key vocabulary
 * (permitted by {@link DimensionKey}). The <i>key</i> is universal and
 * lives here; the <i>values</i> are open and supplied per tree-kind by
 * downstream modules (e.g. studio-base's {@code CategoryValue}).
 *
 * @since homing-tree-views v1
 */
public record Category() implements DimensionKey {
    public static final Category INSTANCE = new Category();
    @Override public String tag() { return "category"; }
}
