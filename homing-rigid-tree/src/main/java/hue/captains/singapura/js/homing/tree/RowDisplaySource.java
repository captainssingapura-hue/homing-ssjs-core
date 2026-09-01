package hue.captains.singapura.js.homing.tree;

/**
 * How a serializer asks what a node's row should show (RFC 0053). A named type
 * rather than a bare {@code Function} — Names Are Types, and a writer that takes
 * "some function of a node" says nothing about which question it is asking.
 *
 * <p>The projection direction is the point. A family resolves its own richly
 * typed answer by identity — {@code ListingDetails} for a catalogue, a crate's
 * own for crates — and narrows it here. The substrate is handed a row, never a
 * domain type, so it stays free of every family at once.</p>
 *
 * @since RFC 0053
 */
@FunctionalInterface
public interface RowDisplaySource {

    /** The row for this node, or {@code null} to fall back to the node itself. */
    RowDisplay displayOf(TreeNode<?> node);

    /** Nothing to add: every node speaks for itself. */
    static RowDisplaySource none() { return node -> null; }
}
