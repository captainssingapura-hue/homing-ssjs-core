package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.rigid.RigidDoc;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDocNormalizer;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The typed code segment is a <b>mirror</b>, and these tests are what make that
 * word mean something: if a {@link TypedCodeSegment} ever serialised differently
 * from the {@link CodeSegment} it stands in for, migrating a doc one listing at
 * a time would silently change what readers see.
 */
class TypedCodeSegmentTest {

    private static final UUID ID = UUID.fromString("77777777-8888-4999-aaaa-bbbbbbbbbbbb");

    private static String json(Segment s) {
        var sb = new StringBuilder();
        SegmentJson.write(sb, s, "seg-0", x -> "");
        return sb.toString();
    }

    @Test
    void typedAndUntypedSerialiseIdentically() {
        for (CodeLanguage lang : CodeLanguage.ALL) {
            assertEquals(json(new CodeSegment("BODY", lang.tag())),
                         json(new TypedCodeSegment("BODY", lang)),
                         "wire shapes diverged for language '" + lang.tag() + "'");
        }
    }

    @Test
    void titleTravelsTheSameWay() {
        assertEquals(json(new CodeSegment("B", "java", Optional.of("Listing 1"))),
                     json(new TypedCodeSegment("B", CodeLanguage.JAVA, Optional.of("Listing 1"))));
    }

    @Test
    void mermaidReachesTheWireAsTheRendererExpectsIt() {
        // RFC 0059 Phase 2: renderCodeSegment branches on exactly this string.
        assertTrue(json(new TypedCodeSegment("flowchart LR\n A --> B", CodeLanguage.MERMAID))
                        .contains("\"language\":\"mermaid\""),
                "the one language a renderer branches on must reach it verbatim");
    }

    @Test
    void jsAndJavascriptCollapseToOneSpelling() {
        // 55 fences say js and 21 say javascript; a typed doc cannot do both.
        assertEquals("javascript", CodeLanguage.JAVASCRIPT.tag());
    }

    @Test
    void noneIsTheUnspecifiedLanguage() {
        assertEquals("", CodeLanguage.NONE.tag());
        assertEquals(json(new CodeSegment("B")), json(new TypedCodeSegment("B")));
    }

    @Test
    void otherRefusesABlankTag() {
        // Blank means unspecified, and NONE already says that. Two ways to spell
        // one state is the defect this type exists to remove.
        assertThrows(IllegalArgumentException.class, () -> CodeLanguage.other(" "));
        assertNotEquals(CodeLanguage.NONE, CodeLanguage.other("kotlin"));
    }

    @Test
    void asUntypedRoundTrips() {
        var typed = new TypedCodeSegment("B", CodeLanguage.BASH, Optional.of("T"));
        assertEquals(new CodeSegment("B", "bash", Optional.of("T")), typed.asUntyped());
    }

    @Test
    void aRigidDocCanHoldOne() {
        // The builder overload compiles and the segment survives normalisation --
        // TypedCodeSegment is Listable, so it is legal wherever CodeSegment is.
        RigidDoc doc = RigidDoc.root(ID, "Typed", "summary", "TEST")
                .l1("Section")
                    .code("flowchart LR\n  A --> B", CodeLanguage.MERMAID)
                .l1build()
                .build();
        String wire = DocTreeJsonWriter.INSTANCE.write(
                RigidDocNormalizer.INSTANCE.toDocTree(doc), ID.toString());
        assertTrue(wire.contains("\"language\":\"mermaid\""), wire);
    }
}
