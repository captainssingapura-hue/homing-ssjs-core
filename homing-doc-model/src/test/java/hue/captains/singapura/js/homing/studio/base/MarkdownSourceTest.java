package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.studio.base.composed.ComposedDoc;
import hue.captains.singapura.js.homing.studio.base.composed.TextSegment;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDoc;
import hue.captains.singapura.js.homing.studio.base.table.TableData;
import hue.captains.singapura.js.homing.studio.base.table.TableDoc;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A markdown doc is known by its type: every markdown kind is a {@link MarkdownSource}, and
 * its contents are its markdown; a doc whose data is structure is not one.
 */
class MarkdownSourceTest {

    private static final UUID ID = UUID.nameUUIDFromBytes("markdown-source".getBytes());

    /** A markdown doc of the classpath kind, its text in the file named after the class. */
    @SuppressWarnings("deprecation")
    record OnTheClasspath() implements ClasspathMarkdownDoc {
        @Override public UUID uuid() { return ID; }
        @Override public String title() { return "On the classpath"; }
    }

    @SuppressWarnings("deprecation")
    record Inline() implements InlineDoc {
        @Override public UUID uuid() { return ID; }
        @Override public String title() { return "Inline"; }
        @Override public String contents() { return "# Inline\n\nWords."; }
    }

    @SuppressWarnings("deprecation")
    record AtAPath() implements ResourceMarkdownDoc {
        @Override public UUID uuid() { return ID; }
        @Override public String title() { return "At a path"; }
        @Override public String resourcePath() { return "nowhere.md"; }
    }

    @Test
    void everyMarkdownKindIsAMarkdownSource() {
        List<Doc> markdown = List.of(new MarkdownDoc(ID, "Plain", "Words."), new OnTheClasspath(), new Inline(), new AtAPath());
        for (Doc d : markdown) assertTrue(d instanceof MarkdownSource, d.getClass().getSimpleName());
        assertEquals("Words.", new MarkdownDoc(ID, "Plain", "Words.").contents(), "its contents are its markdown");
        assertEquals("# Inline\n\nWords.", new Inline().contents());
    }

    @Test
    void aDocWhoseDataIsStructureIsNotOne() {
        List<Doc> structured = List.of(
                ComposedDoc.of(ID, "Composed", "", "DOC", List.of(new TextSegment("words"))),
                RigidDoc.root(ID, "Rigid", "", "DOC").l1("One").text("words").l1build().build(),
                new TableDoc(ID, "Table", "", TableData.fromCsv("a,b\n1,2")));
        for (Doc d : structured) assertFalse(d instanceof MarkdownSource, d.getClass().getSimpleName());
    }
}
