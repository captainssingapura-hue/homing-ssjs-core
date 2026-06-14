package hue.captains.singapura.js.homing.tree;

import hue.captains.singapura.js.homing.tree.dims.DepthValue;
import hue.captains.singapura.js.homing.tree.dims.NameValue;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeNodeJsonWriterTest {

    private record L0Node(Map<DimensionKey, DimensionValue> dimensions,
                          List<? extends TreeNode<?>> children)
            implements TreeNode<TreeLevel.L0> {
        @Override public TreeLevel.L0 level() { return TreeLevel.L0.INSTANCE; }
    }

    private record L1Node(Map<DimensionKey, DimensionValue> dimensions,
                          List<? extends TreeNode<?>> children)
            implements TreeNode<TreeLevel.L1> {
        @Override public TreeLevel.L1 level() { return TreeLevel.L1.INSTANCE; }
    }

    private static Map<DimensionKey, DimensionValue> dims(String label, int depth) {
        var m = new LinkedHashMap<DimensionKey, DimensionValue>();
        m.put(DisplayLabel.INSTANCE, new NameValue(label));
        m.put(LevelDepth.INSTANCE,   new DepthValue(depth));
        return m;
    }

    @Test
    void leafRoundTrip() {
        var leaf = new L1Node(dims("Alpha", 1), List.of());
        var json = new TreeNodeJsonWriter().write(leaf);
        assertEquals(
                "{\"level\":\"L1\",\"dimensions\":["
                        + "{\"key\":\"displayLabel\",\"valueTag\":\"name\",\"text\":\"Alpha\"},"
                        + "{\"key\":\"levelDepth\",\"valueTag\":\"depth\",\"text\":\"1\"}"
                        + "],\"children\":[]}",
                json);
    }

    @Test
    void nestedTwoLevels() {
        var leafA = new L1Node(dims("Alpha", 1), List.of());
        var leafB = new L1Node(dims("Beta",  1), List.of());
        var root  = new L0Node(dims("Root",  0), List.of(leafA, leafB));
        var json  = new TreeNodeJsonWriter().write(root);
        assertTrue(json.startsWith("{\"level\":\"L0\""), json);
        assertTrue(json.contains("\"text\":\"Alpha\""));
        assertTrue(json.contains("\"text\":\"Beta\""));
        assertTrue(json.contains("\"text\":\"Root\""));
    }

    @Test
    void stringEscapingHandlesQuotesAndControls() {
        var node = new L1Node(dims("He said \"hi\"\n", 1), List.of());
        var json = new TreeNodeJsonWriter().write(node);
        assertTrue(json.contains("\\\"hi\\\""), json);
        assertTrue(json.contains("\\n"),        json);
    }

    @Test
    void levelChainBelowResolvesCorrectly() {
        assertEquals(TreeLevel.L1.INSTANCE, TreeLevel.L0.INSTANCE.below().orElseThrow());
        assertEquals(TreeLevel.L8.INSTANCE, TreeLevel.L7.INSTANCE.below().orElseThrow());
        // RFC 0040: the cap moved from L8 to L18 — L8 now has a level below it.
        assertEquals(TreeLevel.L9.INSTANCE, TreeLevel.L8.INSTANCE.below().orElseThrow());
        assertTrue(TreeLevel.L18.INSTANCE.below().isEmpty());
    }
}
