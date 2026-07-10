package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;

import java.util.List;
import java.util.Map;

/**
 * RFC 0039 — serialises a {@link DocTree} to the doc rigid-tree wire payload.
 * The shape is two parts, exactly as the model prescribes:
 *
 * <pre>{@code
 * {
 *   "structure": <NormalizedNode tree>,        // nav-only, via TreeNodeJsonWriter
 *   "content": {
 *     "0":   [ { "kind":"text", ... } ],          // keyed by a node's CANONICAL PATH;
 *     "1":   [ { "kind":"code", ... } ],          //   value is the node's ComposedLeaf
 *     "1/0": [ { "kind":"markdown", ... },        //   bundle — an ARRAY of segments
 *              { "kind":"code", ... } ]           //   (a RigidDoc leaf may hold several)
 *   }
 * }
 * }</pre>
 *
 * <p><b>Part 1 — structure only.</b> The pure {@link TreeNodeJsonWriter} output
 * for the structure tree; carries no body, only level + dimensions + children.</p>
 *
 * <p><b>Part 2 — content by canonical path.</b> A flat map whose key is the
 * node's child-index path joined by {@code '/'} (root = {@code ""}, its first
 * child = {@code "0"}, a grafted embed's first child = {@code "1/0"}). This is
 * the same positional identity the renderer derives while walking the structure,
 * so it joins the two parts with no stamped id. Each value is the segment's
 * content object ({@link SegmentJson}); resource-backed kinds (svg/table/image)
 * carry a {@code /doc-tree-content?id=<root>&path=<key>&seg=<i>} URL — served by
 * {@link DocTreeContentGetAction}, which returns the embedded doc's <i>own
 * bytes</i> so the composed doc loads it as a regular resource and applies its
 * own figure/caption styling (rather than the resource's standalone viewer app,
 * whose {@code url()} would serve an HTML page the renderers can't consume).</p>
 *
 * <p>Functional Object: stateless, one {@code INSTANCE}.</p>
 *
 * @since homing-studio-base — RFC 0039 rigid-tree doc
 */
public final class DocTreeJsonWriter {

    public static final DocTreeJsonWriter INSTANCE = new DocTreeJsonWriter();

    private DocTreeJsonWriter() {}

    private static final TreeNodeJsonWriter STRUCTURE = new TreeNodeJsonWriter();

    /**
     * Render the two-part payload with no root id — resource-backed segments get
     * an unrooted content URL. Retained for callers (and tests) that have no root
     * uuid; production callers use {@link #write(DocTree, String)} so inline
     * resources resolve.
     */
    public String write(DocTree tree) {
        return write(tree, "");
    }

    /**
     * Render the two-part payload rooted at {@code rootId} — the registered uuid
     * of the doc being served. Resource-backed segments (svg/table/image) carry a
     * {@code /doc-tree-content?id=<rootId>&path=<key>&seg=<i>} URL so the client
     * fetches the embedded doc's own bytes ({@link DocTreeContentGetAction}).
     */
    public String write(DocTree tree, String rootId) {
        if (tree == null) throw new IllegalArgumentException("tree");
        var sb = new StringBuilder(512);
        sb.append("{\"structure\":");
        sb.append(STRUCTURE.write(tree.structure()));
        sb.append(",\"content\":{");
        boolean first = true;
        for (Map.Entry<List<Integer>, ContentProvider> e : tree.providers().entrySet()) {
            if (!first) sb.append(',');
            first = false;
            String key = pathKey(e.getKey());
            sb.append(ComposedDoc.jstr(key)).append(':');
            writeLeafContent(sb, e.getValue().content(), key, rootId);
        }
        sb.append("}}");
        return sb.toString();
    }

    /**
     * Serialize one node's {@link LeafContent} — an exhaustive switch over the
     * sealed type, so a new leaf-content kind cannot be added without a wire
     * shape being chosen here (and a matching client renderer added). Today the
     * only kind is {@link ComposedLeaf}, emitted as an array of segment objects.
     */
    private static void writeLeafContent(StringBuilder sb, LeafContent content, String key, String rootId) {
        switch (content) {
            case ComposedLeaf bundle -> {
                var caption = bundle.caption();
                if (caption.isPresent()) {
                    // Object form (only when a caption is set): { caption, segments }.
                    // Caption-less nodes keep the legacy array form — the extra field
                    // is optional, so the front-end supports both.
                    sb.append("{\"caption\":").append(ComposedDoc.jstr(caption.get().raw()))
                      .append(",\"segments\":");
                    writeSegments(sb, bundle.contents(), key, rootId);
                    sb.append('}');
                } else {
                    writeSegments(sb, bundle.contents(), key, rootId);
                }
            }
        }
    }

    private static void writeSegments(StringBuilder sb, List<Segment> segs, String key, String rootId) {
        sb.append('[');
        for (int i = 0; i < segs.size(); i++) {
            if (i > 0) sb.append(',');
            final int segIndex = i;
            // Resource-backed segments (svg/table/image) resolve to the embedded
            // doc's own bytes via /doc-tree-content; the closure is invoked by
            // SegmentJson only for those kinds.
            SegmentJson.write(sb, segs.get(i), "seg-" + key + "-" + i,
                    s -> inlineResourceUrl(rootId, key, segIndex));
        }
        sb.append(']');
    }

    /** The raw-content URL for a resource-backed inline segment (svg / table / image). */
    private static String inlineResourceUrl(String rootId, String pathKey, int seg) {
        return "/doc-tree-content?id=" + rootId + "&path=" + pathKey + "&seg=" + seg;
    }

    /** Canonical path key: child-index path joined by {@code '/'} ({@code ""} for root). */
    static String pathKey(List<Integer> path) {
        var sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append('/');
            sb.append(path.get(i));
        }
        return sb.toString();
    }
}
