package hue.captains.singapura.js.homing.tree;

import java.util.Map;

/**
 * Canonical JSON serialiser for {@link TreeNode} — the universal contract
 * the JS renderer reads. Self-describing on the wire: every node carries
 * its {@code level} discriminator, every dimension carries its
 * {@code key}/{@code valueTag}, so the renderer can branch on shape
 * without prior knowledge of the source tree-kind.
 *
 * <p>Wire shape:</p>
 * <pre>{@code
 * {
 *   "level": "L1",
 *   "dimensions": [
 *     { "key": "displayLabel", "valueTag": "name",  "text":  "Animals" },
 *     { "key": "levelDepth",   "valueTag": "depth", "depth": 1 }
 *   ],
 *   "children": [ ... ]
 * }
 * }</pre>
 *
 * <p>Functional Object: stateless, single public method. The output is
 * stable for a given input (deterministic key ordering: dimensions emit
 * in iteration order; callers should pass a {@code LinkedHashMap} when
 * the wire order matters for tests).</p>
 *
 * @since homing-tree-views v1
 */
public final class TreeNodeJsonWriter {

    /** Render a node and its subtree to compact JSON. */
    public String write(TreeNode<?> root) {
        return write(root, RowDisplaySource.none());
    }

    /**
     * As above, asking {@code display} what each row should show (RFC 0053).
     *
     * <p>The display block travels BESIDE dimensions, not instead of them, for as
     * long as any producer still fills a dimension map. A client prefers the block
     * and falls back, so the two halves of the migration never have to land in the
     * same commit.</p>
     */
    public String write(TreeNode<?> root, RowDisplaySource display) {
        if (root == null) throw new IllegalArgumentException("root");
        if (display == null) throw new IllegalArgumentException("display");
        StringBuilder out = new StringBuilder(256);
        writeNode(root, display, out);
        return out.toString();
    }

    private void writeNode(TreeNode<?> node, RowDisplaySource display, StringBuilder out) {
        out.append('{');
        out.append("\"level\":\"").append(node.level().tag()).append('"');
        // RFC 0053 — the segment travels, so a client rebuilds the node's
        // name-path during the walk it already performs and addresses the node by
        // that rather than by its child index. An ordinal address silently moves
        // when a sibling is inserted or reordered; a name-path does not.
        out.append(",\"segment\":\"").append(node.segment().value()).append('"');
        RowDisplay row = display.displayOf(node);
        if (row != null) {
            out.append(",\"display\":{");
            out.append("\"label\":");  writeString(row.label(), out);
            out.append(",\"badge\":"); writeString(row.badge(), out);
            out.append(",\"note\":");  writeString(row.note(),  out);
            out.append(",\"kind\":");  writeString(row.kind(),  out);
            out.append("}");
        }
        out.append(",\"dimensions\":[");
        boolean first = true;
        for (Map.Entry<DimensionKey, DimensionValue> e : node.dimensions().entrySet()) {
            if (!first) out.append(',');
            first = false;
            writeDimension(e.getKey(), e.getValue(), out);
        }
        out.append(']');
        out.append(",\"children\":[");
        first = true;
        for (TreeNode<?> child : node.children()) {
            if (!first) out.append(',');
            first = false;
            writeNode(child, display, out);
        }
        out.append(']');
        out.append('}');
    }

    private void writeDimension(DimensionKey key, DimensionValue value, StringBuilder out) {
        out.append('{');
        out.append("\"key\":\"").append(key.tag()).append('"');
        out.append(",\"valueTag\":\"").append(value.tag()).append('"');
        // The substrate always carries displayText so the JS renderer can
        // surface any dimension as a label without per-kind knowledge.
        // Records that want richer wire data still inherit this floor.
        out.append(",\"text\":");
        writeString(value.displayText(), out);
        out.append('}');
    }

    private void writeString(String s, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
