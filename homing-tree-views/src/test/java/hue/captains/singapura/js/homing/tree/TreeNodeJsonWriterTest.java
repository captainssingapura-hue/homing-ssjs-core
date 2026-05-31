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

    private record L0Node(String id, Map<DimensionKey, DimensionValue> dimensions,
                          List<? extends TreeNode<?>> children)
            implements TreeNode<TreeLevel.L0> {
        @Override public TreeLevel.L0 level() { return TreeLevel.L0.INSTANCE; }
    }

    private record L1Node(String id, Map<DimensionKey, DimensionValue> dimensions,
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
        var leaf = new L1Node("a", dims("Alpha", 1), List.of());
        var json = new TreeNodeJsonWriter().write(leaf);
        assertEquals(
                "{\"level\":\"L1\",\"id\":\"a\",\"dimensions\":["
                        + "{\"key\":\"displayLabel\",\"valueTag\":\"name\",\"text\":\"Alpha\"},"
                        + "{\"key\":\"levelDepth\",\"valueTag\":\"depth\",\"text\":\"1\"}"
                        + "],\"children\":[]}",
                json);
    }

    @Test
    void nestedTwoLevels() {
        var leafA = new L1Node("a", dims("Alpha", 1), List.of());
        var leafB = new L1Node("b", dims("Beta",  1), List.of());
        var root  = new L0Node("r", dims("Root",  0), List.of(leafA, leafB));
        var json  = new TreeNodeJsonWriter().write(root);
        assertTrue(json.startsWith("{\"level\":\"L0\""), json);
        assertTrue(json.contains("\"id\":\"a\""));
        assertTrue(json.contains("\"id\":\"b\""));
        assertTrue(json.contains("\"text\":\"Root\""));
    }

    @Test
    void stringEscapingHandlesQuotesAndControls() {
        var node = new L1Node("x", dims("He said \"hi\"\n", 1), List.of());
        var json = new TreeNodeJsonWriter().write(node);
        assertTrue(json.contains("\\\"hi\\\""), json);
        assertTrue(json.contains("\\n"),        json);
    }

    @Test
    void levelChainBelowResolvesCorrectly() {
        assertEquals(TreeLevel.L1.INSTANCE, TreeLevel.L0.INSTANCE.below().orElseThrow());
        assertEquals(TreeLevel.L8.INSTANCE, TreeLevel.L7.INSTANCE.below().orElseThrow());
        assertTrue(TreeLevel.L8.INSTANCE.below().isEmpty());
    }
}
