package hue.captains.singapura.js.homing.tree;

/**
 * Typed value for a node {@link Dimension}. Non-sealed — this is the
 * substrate's extension point. Tree-kind authors introduce a small
 * record per concrete dimension value (e.g. {@code CategoryValue},
 * {@code KindValue}) and the JSON writer uses the record's
 * {@link #tag()} as its discriminator on the wire.
 *
 * <p>Doctrine — Names Are Types: values are typed records, never raw
 * strings. The substrate keeps the keys closed (small vocabulary) and
 * the values open (long tail of per-tree-kind specifics) — the same
 * shape DomOpsParty uses (closed branch types; open content).</p>
 *
 * @since homing-tree-views v1
 */
public interface DimensionValue {

    /** Stable wire tag identifying the value's record kind. */
    String tag();

    /** Short string form, used for display when the renderer has no
     *  per-kind formatter. Implementations should keep this URL-safe
     *  and stable — adapter records typically just return the
     *  underlying String the value wraps. */
    String displayText();
}
