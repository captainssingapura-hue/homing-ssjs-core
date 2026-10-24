package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A leaf the document wears (RFC 0071 port, 24 Oct): a branch says so for every leaf under it - the leaf is a style's rule
 * on the document's root, and nothing else. And what the reader refuses: a leaf with what an element has, one that plays
 * a part, a component that says it.
 */
class WornByTheDocumentTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();

    @Test
    void theDocumentWearsALeaf_whenABranchAboveSaysSo() {
        Taxonomy page = READ.read(List.of(Documents.Ground.INSTANCE, Documents.Plain.INSTANCE));
        assertTrue(page.wornByTheDocument(Documents.Ground.INSTANCE), "the sheet above it says so");
        assertFalse(page.wornByTheDocument(Documents.Plain.INSTANCE), "the frame says nothing");
        assertEquals(Optional.of(Documents.Sheet.INSTANCE), page.head(Documents.Ground.INSTANCE),
                "the sheet heads the ground's family too: its CSS in the sheet's file");
    }

    @Test
    void refused_whatAnElementHas_aPartPlayed_aComponentThatSaysIt() {
        var e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(Documents.Busy.INSTANCE, Documents.Box.INSTANCE, Documents.Bossy.INSTANCE)));
        Map<Rule, Long> rules = e.problems().stream().collect(Collectors.groupingBy(TaxonomyProblem::rule, Collectors.counting()));
        assertEquals(Map.of(Rule.DOCUMENT_LEAF_BADLY, 2L, Rule.DOCUMENT_LEAF_PLAYS_A_PART, 1L, Rule.WORN_BY_THE_DOCUMENT_NOT_A_BRANCH, 1L),
                rules, e.getMessage());
        assertTrue(e.getMessage().contains(" is worn by the document, and has rooms, values, axes: the document's leaf is a style's rule on its root"), e.getMessage());
        assertTrue(e.getMessage().contains(" is worn by the document, and has a role set: "), e.getMessage());
        assertTrue(e.getMessage().contains(", and the document wears it: a part is an element of its owner's, and the document's leaf has none"), e.getMessage());
        assertTrue(e.getMessage().contains(" is a component, and says the document wears it: only a branch does, for every leaf under it"), e.getMessage());
    }
}
