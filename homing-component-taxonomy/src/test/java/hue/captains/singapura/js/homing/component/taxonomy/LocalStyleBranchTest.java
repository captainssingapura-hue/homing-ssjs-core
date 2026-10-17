package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Framed.Box;
import hue.captains.singapura.js.homing.component.taxonomy.Framed.Deep;
import hue.captains.singapura.js.homing.component.taxonomy.Framed.Edge;
import hue.captains.singapura.js.homing.component.taxonomy.Framed.Frame;
import hue.captains.singapura.js.homing.component.taxonomy.Framed.Holder;
import hue.captains.singapura.js.homing.component.taxonomy.Framed.Mark;
import hue.captains.singapura.js.homing.component.taxonomy.Framed.Partful;
import hue.captains.singapura.js.homing.component.taxonomy.Framed.Stray;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A local-style branch read (RFC 0067, appendix Virtual Leaves and Designs): its virtual leaves in the tree and
 * never cast, and what a local-style branch refuses to hold. How a concrete leaf's leaf and local style are read off
 * its declaration is the design side's: homing-design-local-style.
 */
class LocalStyleBranchTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();

    @Test
    void aLocalStyleBranchOfVirtualLeaves_isRead_andItsLeavesAreNeverCast() {
        Taxonomy t = READ.read(List.of(Box.INSTANCE, Edge.INSTANCE, Mark.INSTANCE));
        assertEquals(List.of(Box.INSTANCE, Edge.INSTANCE, Mark.INSTANCE), t.components(), "the virtual leaves are nodes of the tree");
        assertTrue(t.branches().contains(Frame.INSTANCE), "and so is their local-style branch");
        assertEquals(List.of(new Casting(Box.INSTANCE)), t.castings(), "a virtual leaf is never cast: a local style's concrete leaf of it is worn");
    }

    @Test
    void anOrdinaryComponentUnderALocalStyleBranch_isRefused() {
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
    void aBranchUnderALocalStyleBranch_isRefused() {
        assertRefused(Rule.BRANCH_IN_LOCAL_STYLE_BRANCH, Deep.INSTANCE);
    }

    private static void assertRefused(Rule rule, Component<?> declared) {
        var e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(declared)));
        assertEquals(List.of(rule), e.problems().stream().map(TaxonomyProblem::rule).toList(), e.getMessage());
    }
}
