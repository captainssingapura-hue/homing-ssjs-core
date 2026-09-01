package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.tree.NodeIdentity;
import hue.captains.singapura.js.homing.tree.NodeResolver;
import hue.captains.singapura.js.homing.tree.RowDisplay;
import hue.captains.singapura.js.homing.tree.RowDisplaySource;
import hue.captains.singapura.js.homing.tree.NormalizedNode;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A catalogue as the two halves RFC 0053 separates: a pure structure tree, and
 * the details its vertices resolve to.
 *
 * <p>The same shape the doc side already uses — {@code DocTreeV2} is a structure
 * tree plus a content seam — arriving one level up. Both are produced by one walk,
 * so the structure and its answers cannot drift; building them separately is
 * exactly how a catalogue came to have four derivations.</p>
 *
 * <p><b>Keyed by identity, not by path.</b> That is the difference from the seam
 * withdrawn in the identity appendix: a path changes when a subtree is grafted, so
 * a path-keyed seam needs rebasing at every mount. An identity rides a graft
 * unchanged, which is why the grafted subtree's details merge into this map
 * without translation — the resolver union happening at build time, with
 * disjointness as its precondition.</p>
 *
 * @param structure the pure tree — segment, identity, children
 * @param details   what each vertex looks like, and for a leaf what opens it
 * @since RFC 0053
 */
public record CatalogueTree(NormalizedNode structure, Map<NodeIdentity, ListingDetails> details) {

    public CatalogueTree {
        Objects.requireNonNull(structure, "CatalogueTree.structure");
        details = Map.copyOf(details);
    }

    /** The details as a resolver, for merging with other subtrees answers. */
    public NodeResolver<ListingDetails> resolver() {
        Map<NodeIdentity, ListingDetails> map = details;
        return identity -> Optional.ofNullable(map.get(identity));
    }
    /**
     * The details narrowed to what a tree ROW draws (RFC 0053).
     *
     * <p>This is the projection direction the substrate depends on: a catalogue
     * keeps its own richly typed answer — illustration plus, for a leaf, the
     * binding that opens it — and hands the renderer four strings. The renderer
     * never learns what a {@code ListingDetails} is, and the icon simply does not
     * travel, because a row has nowhere to put one.</p>
     *
     * <p>Answers {@code null} for a node it does not own, which is the writer's
     * signal to fall back rather than an error: a grafted subtree from another
     * family is expected to be unknown here.</p>
     */
    public RowDisplaySource rowDisplay() {
        Map<NodeIdentity, ListingDetails> map = details;
        return node -> {
            if (!(node instanceof NormalizedNode n)) return null;
            ListingDetails d = map.get(n.identity());
            if (d == null) return null;
            Illustration i = d.illustration();
            return new RowDisplay(i.label(), i.badge(), i.summary(), i.kind());
        };
    }

}
