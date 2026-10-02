package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.tree.NormalizedNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0039 — the output of transforming a doc into a rigid tree: the pure
 * <b>structure</b> (an RFC 0040 {@link NormalizedNode} tree, nav-only) paired
 * with the <b>content</b> seam (a position-keyed map of {@link ContentProvider}s).
 * Structure and content are kept apart exactly as the RFC prescribes — the
 * node knows nothing of content; content is looked up by a node's child-index
 * path.
 *
 * <p>A node with no entry in {@link #providers()} is purely structural (a
 * section heading with no body); a node with an entry is a content leaf (or a
 * section that also has intro content). The path key is the same child-index
 * path the renderer holds for a node — the positional identity RFC 0040
 * established.</p>
 *
 * @param structure the nav-only structure tree (RFC 0040)
 * @param providers content providers keyed by a node's child-index path
 * @since homing-studio-base — RFC 0039 rigid-tree doc
 */
public record DocTree(NormalizedNode structure,
                      Map<List<Integer>, ContentProvider> providers) {

    public DocTree {
        Objects.requireNonNull(structure, "DocTree.structure");
        providers = Map.copyOf(providers);
    }

    /** The content provider at a node's child-index path, if any. */
    public Optional<ContentProvider> providerAt(List<Integer> path) {
        return Optional.ofNullable(providers.get(path));
    }
}
