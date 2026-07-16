package hue.captains.singapura.js.homing.tree;

/**
 * Typed identifier for a node dimension — one of the substrate's grouping
 * axes. Sealed: the key vocabulary is the small, closed, universal set of
 * axes a tree can be labelled, grouped, or pivoted on. It is deliberately
 * <b>not</b> a downstream extension point — tree-kind authors extend the
 * open {@link DimensionValue} side instead, supplying new value shapes
 * under these existing keys.
 *
 * <p>The closed vocabulary:</p>
 * <ul>
 *   <li>{@link DisplayLabel} — the human-readable label every node carries
 *       (value typically {@code NameValue}).</li>
 *   <li>{@link Summary} — a short descriptive blurb; lets a detail view
 *       show branch-node content without a round-trip (value text).</li>
 *   <li>{@link LevelDepth} — zero-indexed depth from the tree root
 *       (value {@code DepthValue}).</li>
 *   <li>{@link Category} — primary grouping category (value supplied per
 *       tree-kind, e.g. studio-base's {@code CategoryValue}).</li>
 *   <li>{@link Kind} — kind/type discriminator (value per tree-kind, e.g.
 *       studio-base's {@code KindValue}); doubles as the detail view's
 *       widget-registry dispatch key.</li>
 *   <li>{@link NodeKey} — the stable, URL-safe node identity (value typically
 *       {@code NameValue}); the segment from which a name-path address is
 *       built, distinct from the human {@link DisplayLabel}.</li>
 * </ul>
 *
 * <p>All permitted keys live in this same package so the sealed permits
 * resolve without a named module. This is intentional and structural:
 * keys are part of the closed contract, so they live with the contract;
 * values are open extensions, so they live in {@code tree.dims} (bundled)
 * or in downstream modules.</p>
 *
 * <p>Doctrine — Names Are Types: a key is a typed identifier, never a raw
 * string. Make It Impossible, Not Forbidden: the sealed permits make
 * inventing an ad-hoc key a compile error, not a convention.</p>
 *
 * @since homing-tree-views v1
 */
public sealed interface DimensionKey
        permits DisplayLabel, Summary, LevelDepth, Category, Kind, NodeKey {

    /** Stable wire tag for JSON serialisation (also the key name in JS). */
    String tag();
}
