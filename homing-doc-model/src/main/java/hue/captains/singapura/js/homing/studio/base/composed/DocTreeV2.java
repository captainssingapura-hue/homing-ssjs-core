package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.tree.NormalizedNode;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0039, name-path variant — the successor to {@link DocTree}. Same two parts:
 * a pure {@link NormalizedNode} structure tree plus a flat content seam. The one
 * difference, and the reason it exists, is the <b>key</b>: content is addressed by
 * a node's <b>name-path</b> — the {@code '/'}-joined chain of {@code NodeName}s
 * from the root ({@code ""} for the root, {@code "animals"} for its child,
 * {@code "animals/turtle"} for a grandchild) — not by the fragile child-index path
 * {@link DocTree} uses.
 *
 * <p>A name-path is a <b>stable</b> address: inserting or reordering siblings never
 * renumbers it, so anchors, deep-links, and the content lookup all survive edits.
 * Each structure node carries its {@link hue.captains.singapura.js.homing.tree.NodeKey}
 * dimension, so the client rebuilds the very same key while walking the structure.</p>
 *
 * <p>Structure and content stay apart exactly as the RFC prescribes — the node
 * knows nothing of content; content is looked up by name-path.</p>
 *
 * @param structure the nav-only structure tree (RFC 0040), each node carrying a {@code NodeKey}
 * @param providers content providers keyed by name-path ({@code ""} = root)
 * @since homing-studio-base — RFC 0039 name-path doc (RigidDocV2)
 */
public record DocTreeV2(NormalizedNode structure,
                        Map<String, ContentProvider> providers) {

    public DocTreeV2 {
        Objects.requireNonNull(structure, "DocTreeV2.structure");
        providers = Map.copyOf(providers);
    }

    /** The content provider at a node's name-path, if any ({@code ""} = root). */
    public Optional<ContentProvider> providerAt(String namePath) {
        return Optional.ofNullable(providers.get(namePath));
    }
}
