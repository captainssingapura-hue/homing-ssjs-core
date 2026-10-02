package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.site.Navigable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** How a page opens is the app's to say: in place unless its leaf says otherwise, and a restatement keeps it. */
class LeafTest {

    static final Navigable PAGE = q -> new HtmlPageContent("page");

    @Test
    void aPageOpensInPlace_unlessItsAppSaysOtherwise() {
        var leaf = Leaf.of(GraftTest.NotesCatalogue.INSTANCE, "Board", "", PAGE);
        assertEquals(Leaf.Opening.IN_PLACE, leaf.opens());
        var beside = leaf.opens(Leaf.Opening.NEW_TAB);
        assertEquals(Leaf.Opening.NEW_TAB, beside.opens());
        assertEquals(Leaf.Opening.NEW_TAB, beside.badge("WORKSPACE").icon("🧰").opens(), "a restatement keeps how it opens");
        assertEquals("board", beside.slug().value());
    }
}
