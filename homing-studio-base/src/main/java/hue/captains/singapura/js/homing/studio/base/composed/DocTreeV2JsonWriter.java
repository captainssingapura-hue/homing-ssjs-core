package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;

import java.util.Map;

/**
 * RFC 0039 (name-path variant) — serialises a {@link DocTreeV2} to the same
 * two-part {@code {structure, content}} wire shape as {@link DocTreeJsonWriter},
 * with one difference: the {@code content} map is keyed by each node's
 * <b>name-path</b> ({@code ""}, {@code "animals"}, {@code "animals/turtle"})
 * instead of its child-index path.
 *
 * <pre>{@code
 * {
 *   "structure": <NormalizedNode tree>,     // each node carries a "nodeName" dimension
 *   "content": {
 *     "animals":        [ { "kind":"paragraph", ... } ],
 *     "animals/turtle": { "caption":"…", "segments":[ … ] }
 *   }
 * }
 * }</pre>
 *
 * <p>Structure serialisation is unchanged — {@link TreeNodeJsonWriter} emits the
 * {@code NodeKey} dimension (tag {@code "nodeName"}) for free, so the client
 * rebuilds the same name-path while walking. The leaf/segment value shape is
 * <b>identical</b> to V1: this writer delegates to
 * {@link DocTreeJsonWriter#writeLeafContent} so captions, segment kinds, and the
 * {@code /doc-tree-content?path=<name-path>} resource URLs all match. Only the key
 * changes.</p>
 *
 * <p>Functional Object: stateless, one {@code INSTANCE}.</p>
 *
 * @since homing-studio-base — RFC 0039 name-path doc (RigidDocV2)
 */
public final class DocTreeV2JsonWriter {

    public static final DocTreeV2JsonWriter INSTANCE = new DocTreeV2JsonWriter();

    private DocTreeV2JsonWriter() {}

    private static final TreeNodeJsonWriter STRUCTURE = new TreeNodeJsonWriter();

    /** Render with no root id — resource-backed segments get an unrooted content URL. */
    public String write(DocTreeV2 tree) {
        return write(tree, "");
    }

    /**
     * Render rooted at {@code rootId} (the registered uuid of the doc being served)
     * so resource-backed segments resolve via {@code /doc-tree-content}.
     */
    public String write(DocTreeV2 tree, String rootId) {
        if (tree == null) throw new IllegalArgumentException("tree");
        var sb = new StringBuilder(512);
        sb.append("{\"structure\":");
        sb.append(STRUCTURE.write(tree.structure()));
        sb.append(",\"content\":{");
        boolean first = true;
        for (Map.Entry<String, ContentProvider> e : tree.providers().entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(ComposedDoc.jstr(e.getKey())).append(':');
            // Same leaf/segment wire shape as V1 — only the key (name-path) differs.
            DocTreeJsonWriter.writeLeafContent(sb, e.getValue().content(), e.getKey(), rootId);
        }
        sb.append("}}");
        return sb.toString();
    }
}
