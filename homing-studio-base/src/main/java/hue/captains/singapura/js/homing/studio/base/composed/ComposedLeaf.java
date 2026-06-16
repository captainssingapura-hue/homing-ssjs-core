package hue.captains.singapura.js.homing.studio.base.composed;

import java.util.List;
import java.util.Objects;

/**
 * RFC 0041 — a leaf's content: a flat, ordered bundle of {@link Segment}s. The
 * payload a {@link ContentProvider} yields for one node of a doc rigid tree.
 *
 * <p>This is the concept RFC 0041 named: the flatness that made a whole
 * {@code ComposedDoc} a poor <i>document</i> is exactly right for a <i>leaf</i>.
 * A node's content is a list of segments rendered into that node's body, below
 * the tree's structure — so the segments are content, never tree nodes, and
 * never reach the TOC. A {@code ComposedDoc} contributes a singleton bundle per
 * segment; a {@code RigidDoc} (RFC 0042) leaf contributes its whole bundle at
 * once.</p>
 *
 * <p>No {@link ComposedSegment} ever appears here — nesting is structure (a
 * graft), not content.</p>
 *
 * <p>One {@link LeafContent} kind among the (currently one) polymorphic
 * content payloads — the flat-bundle case. Other kinds (an external SVG, a bare
 * image) bind through the same provider seam without being a bundle.</p>
 *
 * @param contents the ordered segments rendered in this node's body
 * @since homing-studio-base — RFC 0041 structure vs content
 */
public record ComposedLeaf(List<Segment> contents) implements LeafContent {

    public ComposedLeaf {
        Objects.requireNonNull(contents, "ComposedLeaf.contents");
        contents = List.copyOf(contents);
    }
}
