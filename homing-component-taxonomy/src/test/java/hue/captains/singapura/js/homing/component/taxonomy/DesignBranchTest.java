package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Designed.Bold;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.BoldMark;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.Box;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.Deep;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.DirectMark;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.Edge;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.Frame;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.Holder;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.Mark;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.Partful;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.Plain;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.PlainEdge;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.RawEdge;
import hue.captains.singapura.js.homing.component.taxonomy.Designed.Stray;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A design branch read (RFC 0067, appendix Virtual Leaves and Designs): its virtual leaves in the tree and
 * never cast, a concrete leaf's leaf and design read off its declaration, and what a design branch refuses
 * to hold.
 */
class DesignBranchTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();

    @Test
    void aDesignBranchOfVirtualLeaves_isRead_andItsLeavesAreNeverCast() {
        Taxonomy t = READ.read(List.of(Box.INSTANCE, Edge.INSTANCE, Mark.INSTANCE));
        assertEquals(List.of(Box.INSTANCE, Edge.INSTANCE, Mark.INSTANCE), t.components(), "the virtual leaves are nodes of the tree");
        assertTrue(t.branches().contains(Frame.INSTANCE), "and so is their design branch");
        assertEquals(List.of(new Casting(Box.INSTANCE)), t.castings(), "a virtual leaf is never cast: a design's concrete leaf of it is worn");
    }

    @Test
    void aConcreteLeafsLeafAndDesign_areReadOffItsDeclaration() {
        assertEquals(Edge.INSTANCE, new PlainEdge().virtual(), "the leaf, bound by the leaf's own interface");
        assertEquals(Plain.INSTANCE, new PlainEdge().design(), "the design, passed down through the leaf's interface");
        assertEquals(Mark.INSTANCE, new BoldMark().virtual());
        assertEquals(Bold.INSTANCE, new BoldMark().design());
        assertEquals(Mark.INSTANCE, new DirectMark().virtual(), "all three named directly reads alike");
        assertEquals(Plain.INSTANCE, new DirectMark().design());
        assertEquals("plain", Plain.INSTANCE.token(), "a design is named from its type");
    }

    @Test
    void aConcreteLeafThatNamesNoDesign_isRefusedInItsOwnTerms() {
        assertEquals(Edge.INSTANCE, new RawEdge().virtual(), "the leaf is the leaf's interface's to bind");
        var e = assertThrows(IllegalStateException.class, () -> new RawEdge().design());
        assertTrue(e.getMessage().contains("names no design") && e.getMessage().contains("raw"), e.getMessage());
    }

    @Test
    void anOrdinaryComponentUnderADesignBranch_isRefused() {
        assertRefused(Rule.NOT_VIRTUAL, Stray.INSTANCE);
    }

    @Test
    void aPartPlayedByAVirtualLeaf_isRefused() {
        assertRefused(Rule.VIRTUAL_PLAYS_A_PART, Holder.INSTANCE);
    }

    @Test
    void aVirtualLeafWithParts_isRefused() {
        assertRefused(Rule.VIRTUAL_PARTS, Partful.INSTANCE);
    }

    @Test
    void aBranchUnderADesignBranch_isRefused() {
        assertRefused(Rule.BRANCH_IN_DESIGN_BRANCH, Deep.INSTANCE);
    }

    private static void assertRefused(Rule rule, Component<?> declared) {
        var e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(declared)));
        assertEquals(List.of(rule), e.problems().stream().map(TaxonomyProblem::rule).toList(), e.getMessage());
    }
}
