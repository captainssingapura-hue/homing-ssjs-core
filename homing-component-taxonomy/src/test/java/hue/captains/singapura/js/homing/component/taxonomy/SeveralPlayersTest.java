package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.AtMost;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Varying;
import hue.captains.singapura.js.homing.component.taxonomy.Pictured.Bar;
import hue.captains.singapura.js.homing.component.taxonomy.Pictured.Down;
import hue.captains.singapura.js.homing.component.taxonomy.Pictured.Header;
import hue.captains.singapura.js.homing.component.taxonomy.Pictured.Lead;
import hue.captains.singapura.js.homing.component.taxonomy.Pictured.Mark;
import hue.captains.singapura.js.homing.component.taxonomy.Pictured.Picture;
import hue.captains.singapura.js.homing.component.taxonomy.Pictured.Row;
import hue.captains.singapura.js.homing.component.taxonomy.Pictured.Sort;
import hue.captains.singapura.js.homing.component.taxonomy.Pictured.Star;
import hue.captains.singapura.js.homing.component.taxonomy.Pictured.Up;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Several components playing one role in one owner (RFC 0067, D52): one slot for the role, its cardinality the role's.
 * Named - one of these, the owner choosing which - or any under a branch: whatever the instance registers there.
 */
class SeveralPlayersTest {

    private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;

    private static Taxonomy read(Component<?>... declared) { return new ReadTaxonomy().read(List.of(declared)); }

    @Test
    void oneOfSeveral_onePartForTheRole_eachCastInIt() {
        Taxonomy t = read(Header.INSTANCE);
        assertEquals(List.of(new Part<>(Header.INSTANCE, Sort.INSTANCE, List.of(Up.INSTANCE, Down.INSTANCE), new Varying(0, new AtMost(1)))),
                t.partsOf(Header.INSTANCE), "one part, the up and the down its players, at most one at a time");
        assertTrue(t.partsOf(Header.INSTANCE).get(0).alternatives());
        assertTrue(t.components().containsAll(List.of(Up.INSTANCE, Down.INSTANCE)), "the alternatives come in with their owner");
        assertTrue(t.castings().containsAll(List.of(new Casting(Up.INSTANCE, Sort.INSTANCE), new Casting(Down.INSTANCE, Sort.INSTANCE))),
                "each cast in the role");
        assertEquals(List.of("header-sort", "picture", "mark", "root"),
                t.fallback(t.partsOf(Header.INSTANCE).get(0)).stream().map(ComponentNode::token).toList(),
                "the part answered as what its players share: the branch they are both under");
    }

    @Test
    void anyUnderABranch_everyComponentReadThere_aDownstreamsWithTheHouses() {
        Taxonomy t = read(Row.INSTANCE, Header.INSTANCE, Star.INSTANCE);
        var lead = t.partsOf(Row.INSTANCE).get(0);
        assertEquals(List.of(Star.INSTANCE, Up.INSTANCE, Down.INSTANCE), lead.bases(),
                "every picture read, in the order read, whoever brings it - the star declared, the header's two its parts");
        assertTrue(t.castings().contains(new Casting(Star.INSTANCE, Lead.INSTANCE)));
        assertEquals(List.of(Up.INSTANCE, Down.INSTANCE), read(Row.INSTANCE, Header.INSTANCE).partsOf(Row.INSTANCE).get(0).bases(),
                "without the star, the row leads with what is read");
        var none = assertThrows(RefusedTaxonomy.class, () -> read(Row.INSTANCE));
        assertTrue(none.problems().stream().anyMatch(p -> p.rule() == TaxonomyProblem.Rule.NO_BASE
                && p.toString().contains("Row.Lead is played by any Picture, and none is read")), none.problems().toString());
    }

    @Test
    void aChoiceOfOne_orOneNamedTwice_isNoChoice() {
        assertThrows(IllegalArgumentException.class, () -> DSL.part(Up.INSTANCE, Up.INSTANCE), "the same twice");
        var said = DSL.part(Up.INSTANCE, Down.INSTANCE).as(Sort.INSTANCE).optional();
        assertEquals("sort → up | down 0..1", said.toString());
        assertEquals("lead → any picture 0..1", DSL.anyOf(Picture.INSTANCE).as(Lead.INSTANCE).optional().toString());
        assertFalse(read(Header.INSTANCE).branches().isEmpty());
        assertTrue(read(Header.INSTANCE).branches().containsAll(List.of(Mark.INSTANCE, Picture.INSTANCE, Bar.INSTANCE)));
    }

    @Test
    void aModuleBringsItsComponentsAlternatives() {
        record Headers() implements ComponentModule {
            @Override public List<? extends Component<?>> components() { return List.of(Header.INSTANCE); }
        }
        var modules = List.of(new Headers());
        Taxonomy t = ComponentModules.INSTANCE.read(modules, List.of(), List.of(), List.of());
        var bringers = ComponentModules.INSTANCE.bringers(modules, t);
        assertTrue(bringers.get(Up.INSTANCE) instanceof Headers && bringers.get(Down.INSTANCE) instanceof Headers);
    }
}
