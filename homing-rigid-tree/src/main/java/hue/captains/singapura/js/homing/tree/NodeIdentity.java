package hue.captains.singapura.js.homing.tree;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * What a vertex <b>is</b>, as opposed to where it sits — the link from a
 * {@link NormalizedNode} to the real content behind it (RFC 0053).
 *
 * <p>A tree is walked uniformly but assembled from heterogeneous subtrees: a
 * catalogue, a doc, a proxied studio, a crate listing. Each subtree brings its own
 * identity kind and its own resolver; grafting merges those resolvers by
 * <b>union</b>. That union is well-defined as a <i>function</i> only when the
 * domains are disjoint, which is why the contract below is what it is.</p>
 *
 * <h2>Identity must be intrinsically global</h2>
 *
 * <p>An implementation is anchored on something its producing subtree already owns
 * globally — a class, a navigable key, a uuid. <b>Never on position:</b> not "third
 * child of my parent", not a bare segment. Position is exactly what grafting
 * changes, so a positional identity would need rebasing at every graft and would
 * not survive its subtree being mounted somewhere else. Identity rides a graft
 * unchanged; only the path composes.</p>
 *
 * <p>Uniqueness must hold in the <b>largest</b> tree the subtree could ever join —
 * an umbrella deployment, not merely the studio it was authored in — and must be
 * stable whether the subtree is mounted standalone or grafted.</p>
 *
 * <h2>A collision is a law violation, not a merge failure</h2>
 *
 * <p>Two vertices claiming one identity is precisely "a navigable at two
 * positions", which RFC 0051 Law 1 forbids. So the merge does not need to resolve
 * collisions; it needs to <i>report</i> them. The law thereby stops being a
 * separate pass over a separate structure and becomes a property of building the
 * tree. It follows that the union is commutative: if merge order ever changes the
 * result, the disjointness contract has already been broken.</p>
 *
 * <h2>Open, not sealed — and why equality is the whole contract</h2>
 *
 * <p>This interface is deliberately <b>not</b> sealed. {@code CrateTreeGetAction}
 * already builds nodes from another module, so a closed set of node kinds is not
 * available even in principle. That matches the substrate's existing doctrine:
 * {@link DimensionKey}s are closed because they are the contract, while values are
 * open extensions. Identity is value-shaped, so it is open.</p>
 *
 * <p>Being a marker with no members, {@code equals} and {@code hashCode} <b>are</b>
 * the contract — the disjointness of every resolver union rests on them entirely.
 * Implementations must therefore be {@code record}s, or otherwise value-correct.
 * Since that cannot be made impossible here, it is enforced as a conformance rule
 * rather than left to convention.</p>
 *
 * @since RFC 0053
 */
public interface NodeIdentity extends ValueObject {
}
