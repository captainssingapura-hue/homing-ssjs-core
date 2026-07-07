package hue.captains.singapura.js.homing.color.studio;

import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ColorGroup taxonomy adapts into a NormalizedNode tree that serialises to
 * the wire shape the JS TreeRenderer consumes.
 */
class ColorTreeTest {

    @Test
    void rootHasTheFourNatureBranches() {
        NormalizedNode root = ColorTree.build();
        assertEquals(4, root.children().size(), "Expressive / Encoding / Identity / Structural");
    }

    @Test
    void serialisesToTreeRendererWireShape() {
        String json = new TreeNodeJsonWriter().write(ColorTree.build());
        assertTrue(json.startsWith("{"));
        assertTrue(json.contains("\"level\":\"L0\""));
        assertTrue(json.contains("displayLabel"));
        assertTrue(json.contains("Affective"));   // a group branch
        assertTrue(json.contains("serene"));       // an affective leaf
        assertTrue(json.contains("danger"));       // a symbolic leaf
        assertTrue(json.contains("surface"));      // a structural leaf
    }
}
