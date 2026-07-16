package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.tree.NormalizedNode;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
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

    /**
     * Build a {@code DocTree} from a structure tree whose content is keyed by the
     * {@link NormalizedNode} <b>itself</b>, deriving the canonical child-index
     * {@link #providers() path map} in a single walk of the structure.
     *
     * <p>This lets a builder bind content to the very node object it just created,
     * rather than hand-threading a child-index path that can silently drift out of
     * sync with the tree it is meant to address. The stored (and serialized) form
     * is path-keyed exactly as before — the path is <i>derived</i> here, never
     * authored.</p>
     *
     * <p>{@code byNode} is read by node <b>identity</b>: two structurally-equal
     * nodes are distinct positions and must not collapse, so an
     * {@link IdentityHashMap} (or any distinct-object keying) is required — this
     * factory re-keys defensively by identity regardless. Nodes absent from the
     * map are purely structural (a heading with no body).</p>
     *
     * @param structure the nav-only structure tree (RFC 0040)
     * @param byNode     content providers keyed by structure node identity
     */
    public static DocTree byNode(NormalizedNode structure,
                                 Map<NormalizedNode, ContentProvider> byNode) {
        Objects.requireNonNull(structure, "DocTree.structure");
        var identity = new IdentityHashMap<NormalizedNode, ContentProvider>(byNode);
        var providers = new LinkedHashMap<List<Integer>, ContentProvider>();
        derivePaths(structure, List.of(), identity, providers);
        return new DocTree(structure, providers);
    }

    /** Pre-order walk: bind each node's content (by identity) at its child-index path. */
    private static void derivePaths(NormalizedNode node, List<Integer> path,
                                    IdentityHashMap<NormalizedNode, ContentProvider> byNode,
                                    Map<List<Integer>, ContentProvider> out) {
        ContentProvider provider = byNode.get(node);   // identity lookup
        if (provider != null) out.put(path, provider);
        List<NormalizedNode> kids = node.children();
        for (int i = 0; i < kids.size(); i++) {
            var childPath = new ArrayList<Integer>(path.size() + 1);
            childPath.addAll(path);
            childPath.add(i);
            derivePaths(kids.get(i), List.copyOf(childPath), byNode, out);
        }
    }
}
