package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.graph.RigidNode;
import hue.captains.singapura.js.homing.studio.base.composed.graph.RigidNodeNormalizer;
import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.js.homing.studio.base.composed.text.Title;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The name-path wire shape: content keyed by nodeName-chain, and each structure
 * node carrying its {@code nodeName} dimension so the client rebuilds the key.
 */
class DocTreeV2JsonWriterTest {

    private static final Function<String, RigidNodeContent> CONTENT =
            body -> body.isEmpty() ? RigidNodeContent.of() : RigidNodeContent.of(ParagraphSegment.of(body));

    @Test
    void emitsNamePathKeysAndNodeNameDimension() {
        var root = RigidNode.root("", new NodeName("root"), new Title("Guide"));
        var animals = root.child("Warm.", new NodeName("animals"), new Title("Animals"));
        var turtle = animals.child("Slow.", new NodeName("turtle"), new Title("Turtle"));

        DocTreeV2 tree = RigidNodeNormalizer.INSTANCE.toDocTree(List.of(root, animals, turtle), CONTENT);
        String json = DocTreeV2JsonWriter.INSTANCE.write(tree, "root-uuid");

        // content keyed by the name-path chain, not child indices
        assertTrue(json.contains("\"animals\":["), json);
        assertTrue(json.contains("\"animals/turtle\":["), json);
        assertFalse(json.contains("\"0/0\""), "must not emit child-index keys");

        // structure carries the machine identity so the client can rebuild the key
        assertTrue(json.contains("\"key\":\"nodeName\""), json);
        assertTrue(json.contains("\"text\":\"turtle\""), json);

        // the two-part envelope is intact
        assertTrue(json.startsWith("{\"structure\":"), json);
        assertTrue(json.contains(",\"content\":{"), json);
    }
}
