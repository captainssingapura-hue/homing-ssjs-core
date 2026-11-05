package hue.captains.singapura.js.homing.studio.base.rigid;

import hue.captains.singapura.js.homing.studio.base.composed.DocTreeV2;
import hue.captains.singapura.js.homing.studio.base.composed.RigidNodeContent;
import hue.captains.singapura.js.homing.studio.base.composed.graph.RigidNode;
import hue.captains.singapura.js.homing.studio.base.composed.graph.RigidNodeNormalizer;
import hue.captains.singapura.js.homing.studio.base.composed.text.Title;
import hue.captains.singapura.js.homing.tree.NodeName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** The tree is built at the first request, then kept. */
class RigidDocV2TreeTest {

    private static final UUID ID = UUID.fromString("00000000-0000-4000-8000-000000000003");

    private static DocTreeV2 aTree() {
        return RigidNodeNormalizer.INSTANCE.toDocTree(ID,
                List.of(RigidNode.root("", new NodeName("root"), new Title("Root"))),
                body -> new RigidNodeContent());
    }

    private static RigidDocV2 docOver(Supplier<DocTreeV2> builder) {
        return new RigidDocV2(ID, "Rigid v2", "", "DOC", builder);
    }

    @Test
    void theTreeIsNotBuiltUntilItIsAskedFor() {
        var builds = new AtomicInteger();
        docOver(() -> { builds.incrementAndGet(); return aTree(); });
        assertEquals(0, builds.get());
    }

    @Test
    void theTreeIsBuiltOnceAndTheSameOneIsGivenAfter() {
        var builds = new AtomicInteger();
        var doc = docOver(() -> { builds.incrementAndGet(); return aTree(); });
        assertSame(doc.toDocTreeV2(), doc.toDocTreeV2());
        assertEquals(1, builds.get());
    }

    @Test
    void aBuildThatFailsKeepsNothingAndIsTriedAgain() {
        var builds = new AtomicInteger();
        var doc = docOver(() -> {
            if (builds.incrementAndGet() == 1) throw new IllegalStateException("not yet");
            return aTree();
        });
        assertThrows(IllegalStateException.class, doc::toDocTreeV2);
        assertEquals(aTree(), doc.toDocTreeV2());
        assertEquals(2, builds.get());
    }
}
