package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Families of components (RFC 0067, appendix Two Classes per Leaf): a branch heads one by saying so, at one level; every
 * component under it, at any depth, is of it, and its castings come in the order its files rule them - each component on
 * its own, then in each role it is cast in. And what the reader refuses: a head under a head, a component that heads.
 */
class FamilyHeadTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();
    private static final Taxonomy SHOP = READ.read(List.of(Families.Saw.INSTANCE, Families.Lamp.INSTANCE,
            Families.Fuse.INSTANCE, Families.Pebble.INSTANCE));

    @Test
    void aBranchHeadsAFamily_bySayingSo_atAnyLevel() {
        assertEquals(List.of(Families.Gadget.INSTANCE, Families.Cutter.INSTANCE), SHOP.heads(),
                "the gadget at the first level, the cutter at the second, under tools that head none");
        assertEquals(Optional.of(Families.Cutter.INSTANCE), SHOP.head(Families.Saw.INSTANCE));
        assertEquals(Optional.of(Families.Gadget.INSTANCE), SHOP.head(Families.Fuse.INSTANCE),
                "the fuse two levels down, through a branch that heads none");
        assertEquals(Optional.empty(), SHOP.head(Families.Pebble.INSTANCE), "nothing above the pebble heads: it has no family");
    }

    @Test
    void aFamilysCastings_eachComponentOnItsOwn_thenInEachRole() {
        assertEquals(List.of("saw", "blade", "blade_edge"), SHOP.family(Families.Cutter.INSTANCE).stream().map(Casting::token).toList(),
                "the blade as Edge beside the blade, whoever casts it");
        assertEquals(List.of("lamp", "fuse"), SHOP.family(Families.Gadget.INSTANCE).stream().map(Casting::token).toList());
    }

    @Test
    void refused_aHeadUnderAHead_andAComponentThatHeads() {
        var e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(Families.Bulb.INSTANCE, Families.Bossy.INSTANCE)));
        Map<Rule, Long> rules = e.problems().stream().collect(Collectors.groupingBy(TaxonomyProblem::rule, Collectors.counting()));
        assertEquals(Map.of(Rule.FAMILY_HEAD_NESTED, 1L, Rule.FAMILY_HEAD_NOT_A_BRANCH, 1L), rules, e.getMessage());
        assertTrue(e.getMessage().contains("heads a family under"), e.getMessage());
        assertTrue(e.getMessage().contains("which heads one too: a family is headed at one level"), e.getMessage());
        assertTrue(e.getMessage().contains("is a component, and says it heads a family: only a branch does"), e.getMessage());
    }
}
