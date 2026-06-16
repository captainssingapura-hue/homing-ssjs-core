package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
 * carry their <i>own stable {@code url()}</i> rather than a leveled URL.</p>
 *
 * <p>Functional Object: stateless, one {@code INSTANCE}.</p>
 *
 * @since homing-studio-base — RFC 0039 rigid-tree doc
 */
public final class DocTreeJsonWriter {

    public static final DocTreeJsonWriter INSTANCE = new DocTreeJsonWriter();

    private DocTreeJsonWriter() {}

    private static final TreeNodeJsonWriter STRUCTURE = new TreeNodeJsonWriter();

    /** Resource-backed segments inline their own stable resource URL (RFC 0039). */
    private static final Function<Segment, String> STABLE_URL = s -> switch (s) {
        case SvgSegment v    -> v.doc().url();
        case TableSegment t  -> t.doc().url();
        case ImageSegment im -> im.doc().url();
        default              -> "";   // text-shaped kinds inline; no resource URL
    };

    /** Render the two-part payload: structure tree + content-by-path map. */
    public String write(DocTree tree) {
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
            writeLeafContent(sb, e.getValue().content(), key);
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
    private static void writeLeafContent(StringBuilder sb, LeafContent content, String key) {
        switch (content) {
            case ComposedLeaf bundle -> {
                sb.append('[');
                List<Segment> segs = bundle.contents();
                for (int i = 0; i < segs.size(); i++) {
                    if (i > 0) sb.append(',');
                    SegmentJson.write(sb, segs.get(i), "seg-" + key + "-" + i, STABLE_URL);
                }
                sb.append(']');
            }
        }
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
