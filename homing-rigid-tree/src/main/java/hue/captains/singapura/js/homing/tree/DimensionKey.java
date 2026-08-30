package hue.captains.singapura.js.homing.tree;

/**
 * Typed identifier for a node dimension — one of the substrate's grouping
 * axes. Sealed: the key vocabulary is the small, closed, universal set of
 * axes a tree can be labelled, grouped, or pivoted on. It is deliberately
 * <b>not</b> a downstream extension point — tree-kind authors extend the
 * open {@link DimensionValue} side instead, supplying new value shapes
 * under these existing keys.
 *
 * <p>What is left of the vocabulary. It carried six keys and a guess at what
 * trees are — "axes a tree can be labelled, grouped, or pivoted on". Nothing
 * ever pivoted or grouped; catalogues and crates now answer through a resolver
 * and project into {@link RowDisplay}, which is a fact about what a row draws
 * rather than a theory about trees. These two are the doc residue: one
 * structural, one descriptive, both due to follow.</p>
 *
 * <p>The remaining keys:</p>
 * <ul>
 *   <li>{@link DisplayLabel} — the human-readable label every node carries
 *       (value typically {@code NameValue}).</li>
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
        permits DisplayLabel, NodeKey {

    /** Stable wire tag for JSON serialisation (also the key name in JS). */
    String tag();
}
