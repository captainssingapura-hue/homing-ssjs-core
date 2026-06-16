package hue.captains.singapura.js.homing.studio.base.composed;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocTreeJsonWriterTest {

    private static ComposedDoc doc(String title, Segment... segs) {
        return new ComposedDoc(UUID.nameUUIDFromBytes(title.getBytes()),
                title, "", "", List.of(segs), List.of());
    }

    @Test
    void twoPartPayloadStructureAndContentByPath() {
        var d = doc("Doc",
                new TextSegment("intro", Optional.of("Intro")),
                new CodeSegment("x = 1", "java", Optional.of("Snippet")));
        String json = DocTreeJsonWriter.INSTANCE.write(ComposedDocNormalizer.INSTANCE.toDocTree(d));

        // ── part 1: structure (nav-only NormalizedNode tree) ──
        assertTrue(json.contains("\"structure\":{"), json);
        assertTrue(json.contains("\"level\":\"L0\""), json);
        assertTrue(json.contains("\"level\":\"L1\""), json);
        assertTrue(json.contains("\"text\":\"Intro\""), json);   // label dimension

        // ── part 2: content map keyed by canonical path — each value is the
        //    node's ComposedLeaf bundle (an ARRAY of segment objects, RFC 0041) ──
        assertTrue(json.contains("\"content\":{"), json);
        assertTrue(json.contains("\"0\":[{\"kind\":\"text\""), json);
        assertTrue(json.contains("\"1\":[{\"kind\":\"code\""), json);
        assertTrue(json.contains("\"language\":\"java\""), json);
    }

    @Test
    void embeddedSegmentKeyedByDeepPath() {
        var embedded = doc("Embedded", new TextSegment("deep", Optional.of("Deep")));
        var outer = doc("Outer",
                new TextSegment("top", Optional.of("Top")),
                new ComposedSegment(embedded));
        String json = DocTreeJsonWriter.INSTANCE.write(ComposedDocNormalizer.INSTANCE.toDocTree(outer));

        // the grafted embedded segment is content at the deep canonical path "1/0"
        assertTrue(json.contains("\"1/0\":[{\"kind\":\"text\""), json);
        // a ComposedSegment is STRUCTURE (a graft) — never serialized as content
        assertFalse(json.contains("\"kind\":\"composed\""), json);
    }
}
