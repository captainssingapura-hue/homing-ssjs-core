package hue.captains.singapura.js.homing.tree;

import hue.captains.singapura.tao.ontology.FunctionalObject;

/**
 * The per-source-encoding seam (RFC 0040): one function that maps a native
 * spec into a {@link NormalizedNode} tree. <b>Context-free</b> — it always
 * builds a standalone tree rooted at {@code L0} and knows nothing of where the
 * result will be attached. Composition is {@link RigidTrees#shift}'s job, not
 * the normalizer's, so the normalized tree never picks up the attachment
 * substrate.
 *
 * @param <SPEC> the native source encoding (a catalogue, a ComposedDoc, …)
 * @since homing-rigid-tree (RFC 0040)
 */
@FunctionalInterface
public interface TreeNormalizer<SPEC> extends FunctionalObject {

    /** Normalize a native spec into a standalone tree rooted at {@code L0}. */
    NormalizedNode normalize(SPEC spec);
}
