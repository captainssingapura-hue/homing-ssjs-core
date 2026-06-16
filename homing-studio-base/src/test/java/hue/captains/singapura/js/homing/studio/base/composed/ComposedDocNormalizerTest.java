package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.dims.NameValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComposedDocNormalizerTest {

    private static ComposedDoc doc(String title, Segment... segs) {
        return new ComposedDoc(UUID.nameUUIDFromBytes(title.getBytes()),
                title, "", "", List.of(segs), List.of());
    }

    private static String label(NormalizedNode n) {
        return ((NameValue) n.dimensions().get(DisplayLabel.INSTANCE)).text();
    }

    @Test
    void flatDocToStructureAndProviders() {
        var d = doc("Doc",
                new TextSegment("intro body", Optional.of("Intro")),
                new CodeSegment("x = 1", "java", Optional.of("Snippet")));
        DocTree t = ComposedDocNormalizer.INSTANCE.toDocTree(d);

        // ── structure: L0 root, two L1 leaves, labelled by segment title ──
        assertEquals(TreeLevel.L0.INSTANCE, t.structure().level());
        assertEquals("Doc", label(t.structure()));
        assertEquals(2, t.structure().children().size());
        assertEquals(TreeLevel.L1.INSTANCE, t.structure().children().get(0).level());
        assertEquals("Intro",   label(t.structure().children().get(0)));
        assertEquals("Snippet", label(t.structure().children().get(1)));

        // ── content seam: providers keyed by child-index path; each yields a
        //    polymorphic LeafContent (RFC 0041) — here a singleton ComposedLeaf
        //    bundle holding the one segment ──
        var p0 = t.providerAt(List.of(0)).orElseThrow().content();
        var p1 = t.providerAt(List.of(1)).orElseThrow().content();
        assertInstanceOf(ComposedLeaf.class, p0);
        assertInstanceOf(ComposedLeaf.class, p1);
        assertInstanceOf(TextSegment.class, ((ComposedLeaf) p0).contents().get(0));
        assertInstanceOf(CodeSegment.class, ((ComposedLeaf) p1).contents().get(0));
        // the root is purely structural — no provider
        assertTrue(t.providerAt(List.of()).isEmpty());
    }

    @Test
    void composedSegmentGraftsEmbeddedDoc() {
        var embedded = doc("Embedded", new TextSegment("deep", Optional.of("Deep")));
        var outer = doc("Outer",
                new TextSegment("top", Optional.of("Top")),
                new ComposedSegment(embedded));
        DocTree t = ComposedDocNormalizer.INSTANCE.toDocTree(outer);

        assertEquals(2, t.structure().children().size());

        // the embed node is the grafted embedded-doc root, one level below the host
        NormalizedNode embedNode = t.structure().children().get(1);
        assertEquals(TreeLevel.L1.INSTANCE, embedNode.level());
        assertEquals("Embedded", label(embedNode));
        assertEquals(1, embedNode.children().size());

        // the embedded segment shifted one level deeper (L2) by the graft
        NormalizedNode deep = embedNode.children().get(0);
        assertEquals(TreeLevel.L2.INSTANCE, deep.level());
        assertEquals("Deep", label(deep));

        // its provider re-keys under the embed's path: [1, 0]
        var deepLeaf = t.providerAt(List.of(1, 0)).orElseThrow().content();
        assertInstanceOf(ComposedLeaf.class, deepLeaf);
        assertInstanceOf(TextSegment.class, ((ComposedLeaf) deepLeaf).contents().get(0));
        // the embed node itself is structural — no provider
        assertTrue(t.providerAt(List.of(1)).isEmpty());
    }
}
