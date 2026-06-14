package hue.captains.singapura.js.homing.tree;

/**
 * The kind/type discriminator of a node — a secondary grouping axis a
 * pivot/grid view can pivot on (e.g. doc vs catalogue, RFC vs doctrine).
 * Part of the substrate's closed key vocabulary (permitted by
 * {@link DimensionKey}). The <i>key</i> is universal and lives here; the
 * <i>values</i> are open and supplied per tree-kind by downstream modules
 * (e.g. studio-base's {@code KindValue}).
 *
 * @since homing-tree-views v1
 */
public record Kind() implements DimensionKey {
    public static final Kind INSTANCE = new Kind();
    @Override public String tag() { return "kind"; }
}
