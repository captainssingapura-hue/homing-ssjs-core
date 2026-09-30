package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.studio.base.composed.ComposedDoc;
import hue.captains.singapura.js.homing.studio.base.composed.TextSegment;
import hue.captains.singapura.js.homing.studio.base.image.ImageDoc;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDoc;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDocV2;
import hue.captains.singapura.js.homing.studio.base.table.TableData;
import hue.captains.singapura.js.homing.studio.base.table.TableDoc;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A doc never decides how it is shown. One whose data is structure has no content of its
 * own - {@code contents()} and {@code contentType()} say so - and one whose data is text
 * hands its text over as it always has.
 */
class NoOwnContentTest {

    private static final UUID ID = UUID.nameUUIDFromBytes("no-own-content".getBytes());

    @Test
    void aDocWhoseDataIsStructureHasNoContentOfItsOwn() {
        List<Doc> structured = List.of(
                ComposedDoc.of(ID, "Composed", "", "DOC", List.of(new TextSegment("words"))),
                RigidDoc.root(ID, "Rigid", "", "DOC").l1("One").text("words").l1build().build(),
                new RigidDocV2(ID, "Rigid v2", "", "DOC", () -> { throw new AssertionError("the tree is never built for this"); }),
                new TableDoc(ID, "Table", "", TableData.fromCsv("a,b\n1,2")),
                ImageDoc.of("nowhere.png", "image/png", "An image"));
        for (Doc d : structured) {
            var e = assertThrows(NoOwnContentException.class, d::contents, d.getClass().getSimpleName());
            assertTrue(e.getMessage().contains("Function<Doc, Content>"), e.getMessage());
            assertThrows(NoOwnContentException.class, d::contentType, d.getClass().getSimpleName());
            assertThrows(UnsupportedOperationException.class, () -> d.contentsRootedAt(ID.toString(), List.of()),
                    "the leveled variant has nothing to root either");
        }
    }

    @Test
    void aDocWhoseDataIsTextHandsItsTextOver() {
        InlineDoc prose = new InlineDoc() {
            @Override public UUID uuid() { return ID; }
            @Override public String title() { return "Prose"; }
            @Override public String contents() { return "# Prose\n\nwords"; }
        };
        assertEquals("# Prose\n\nwords", prose.contents());
        assertEquals("text/markdown; charset=utf-8", prose.contentType());
    }

    @Test
    void contentIsMadeFromADoc_byAFunction() {
        java.util.function.Function<Doc, Content> outline = d -> new Content(d.title(), "text/plain; charset=utf-8");
        assertEquals(new Content("Composed", "text/plain; charset=utf-8"),
                outline.apply(ComposedDoc.of(ID, "Composed", "", "DOC", List.of())));
        assertThrows(NullPointerException.class, () -> new Content(null, "text/plain"));
    }
}
