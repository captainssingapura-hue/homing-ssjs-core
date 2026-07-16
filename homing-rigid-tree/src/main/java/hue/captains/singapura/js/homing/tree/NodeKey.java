package hue.captains.singapura.js.homing.tree;

/**
 * The stable, URL-safe identity a node carries independent of its position —
 * the wire key by which content is addressed in a name-path document (RigidDocV2
 * / RFC 0039). Part of the substrate's closed key vocabulary (permitted by
 * {@link DimensionKey}); lives in the {@code tree} package beside its siblings so
 * the sealed permits resolve without a named module.
 *
 * <p>Where {@link DisplayLabel} is the <i>human</i> heading (may repeat, may be
 * long), {@code NodeKey} is the <i>machine</i> identity — sibling-unique and
 * short — so a node's name-path (the chain of {@code NodeKey}s from the root) is
 * a stable address that survives reordering, unlike a child-index path. The value
 * is carried by a {@code DimensionValue} (typically {@code NameValue} wrapping a
 * validated {@code NodeName}).</p>
 *
 * @since homing-tree-views — RFC 0039 name-path identity
 */
public record NodeKey() implements DimensionKey {
    public static final NodeKey INSTANCE = new NodeKey();
    @Override public String tag() { return "nodeName"; }
}
