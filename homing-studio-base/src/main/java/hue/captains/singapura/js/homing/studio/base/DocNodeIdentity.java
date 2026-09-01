package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.tree.NamePath;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.tree.NodeIdentity;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The identity of a node <b>inside</b> a document: the doc's uuid, plus the node's
 * path within that doc ({@link NamePath#ROOT} for the doc's own root node).
 *
 * <p>Global because the uuid is. A doc's uuid is an authoring-time identity that no
 * placement or rename touches (RFC 0051's axiom appendix settles that this is its
 * real job), so pairing it with a within-doc path gives every interior node a name
 * that is unchanged wherever the doc is mounted. That is the property
 * {@link NodeIdentity} requires: the path <i>within</i> the source may be
 * structural, because the source is what the producer owns — what must never leak
 * in is the node's position in the <i>host</i> tree, since that is exactly what
 * grafting changes.</p>
 *
 * <p><b>Not navigable.</b> A doc's interior nodes are anchors within one page, not
 * pages of their own — RFC 0051 terminates breadcrumbs at the catalogue leaf. So
 * navigability is a property of an identity's KIND rather than of its presence: a
 * {@link hue.captains.singapura.js.homing.studio.base.app.NavKey} names a page, a
 * {@code DocNodeIdentity} names a position inside one.</p>
 *
 * @param doc    the owning document's uuid
 * @param within the node's path inside that document; {@code ROOT} is the doc itself
 * @since RFC 0053
 */
public record DocNodeIdentity(UUID doc, NamePath within) implements NodeIdentity {

    public DocNodeIdentity {
        Objects.requireNonNull(within, "DocNodeIdentity.within");
    }

    /** The identity of the document's own root node. */
    public static DocNodeIdentity root(UUID doc) {
        return new DocNodeIdentity(doc, NamePath.ROOT);
    }

    /** This identity's node, one segment deeper. */
    public DocNodeIdentity child(NodeName segment) {
        return new DocNodeIdentity(doc, within.then(segment));
    }

    // ── The legacy index-addressed shape ───────────────────────────────────

    /**
     * The identity of a node addressed by its <b>index</b> path within the doc,
     * rather than by a name-path — {@code ComposedDoc}, {@code RigidDoc} and
     * markdown docs are still keyed that way, since RFC 0039's name-path migration
     * has only reached {@code RigidDocV2}.
     *
     * <p>Honest rather than aspirational: the segments really are ordinals, and
     * pretending otherwise by slugging a heading would invent sibling-uniqueness
     * that the source does not guarantee. It still satisfies
     * {@link NodeIdentity}'s contract, because an index <i>within the doc</i> is
     * unchanged by grafting that doc anywhere — what may never leak in is position
     * in the HOST tree. It is merely fragile under editing the doc, which is
     * precisely what the name-path migration exists to fix.</p>
     *
     * <p>When that migration lands, these two factories are the whole of the
     * change: one grep names every site still on ordinals.</p>
     */
    public static DocNodeIdentity byIndex(UUID doc, List<Integer> path) {
        var segments = new java.util.ArrayList<NodeName>(path.size());
        for (Integer i : path) segments.add(new NodeName(String.valueOf(i)));
        return new DocNodeIdentity(doc, new NamePath(segments));
    }

    /** The segment of an index-addressed node: its ordinal, or {@code doc} at the root. */
    public static NodeName indexSegment(List<Integer> path) {
        return path.isEmpty()
                ? new NodeName("doc")
                : new NodeName(String.valueOf(path.get(path.size() - 1)));
    }
}
