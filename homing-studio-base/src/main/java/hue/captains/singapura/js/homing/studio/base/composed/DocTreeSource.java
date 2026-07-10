package hue.captains.singapura.js.homing.studio.base.composed;

/**
 * RFC 0039 — a {@link hue.captains.singapura.js.homing.studio.base.Doc Doc} that
 * supplies its <b>own</b> rigid-tree {@link DocTree}, bypassing the kind-based
 * normalizer dispatch in {@link DocTreeGetAction} / {@link DocTreeContentGetAction}.
 *
 * <p>The built-in doc kinds ({@code ComposedDoc}, {@code RigidDoc}, markdown) are
 * transformed to a {@link DocTree} by a fixed {@code instanceof} switch. A Doc
 * that instead <i>computes</i> its structure — e.g. by mirroring a catalogue tree,
 * possibly lazily — implements this interface, and the doc-tree endpoints call
 * {@link #toDocTree()} directly. This decouples doc-tree serving from the concrete
 * doc types and lets a self-referential mirror defer its build to request time.</p>
 */
public interface DocTreeSource {

    /** This doc's rigid-tree structure + content seam (RFC 0039). */
    DocTree toDocTree();
}
