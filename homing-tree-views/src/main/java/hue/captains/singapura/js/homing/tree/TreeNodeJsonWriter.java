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
        if (root == null) throw new IllegalArgumentException("root");
        StringBuilder out = new StringBuilder(256);
        writeNode(root, out);
        return out.toString();
    }

    private void writeNode(TreeNode<?> node, StringBuilder out) {
        out.append('{');
        out.append("\"level\":\"").append(node.level().tag()).append('"');
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
            writeNode(child, out);
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
