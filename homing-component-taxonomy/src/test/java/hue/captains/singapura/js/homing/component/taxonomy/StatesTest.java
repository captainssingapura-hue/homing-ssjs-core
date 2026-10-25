package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tree of states, read with the taxonomy: the types given and those the components have, their
 * branches through their parents, each type of state with its
 * values, the one at rest first; and what is refused - a name twice within a tree, a type whose
 * values do not hold together - with every node meaning something.
 */
class StatesTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();

    @Test
    void theTypesGiven_thenThoseTheComponentsHave_theirBranchesThroughTheirParents() {
        Taxonomy t = READ.read(List.of(Signals.Lamp.INSTANCE), List.of(), List.of(Signals.Lit.INSTANCE));
        assertEquals(List.of(Signals.Lit.INSTANCE, Signals.Enabled.INSTANCE, Signals.Glow.INSTANCE), t.states().types(),
                "the reader's list first, then those only a component has - kept whether or not a component has them");
        assertEquals(List.of(Signals.Availability.INSTANCE), t.states().branches());
        assertEquals(List.of(Signals.Availability.INSTANCE, Signals.Lit.INSTANCE), t.states().children(StateRoot.INSTANCE), "branches first");
    }

    @Test
    void aTypeOfState_hasItsValues_theOneAtRestFirst() {
        assertEquals(List.of(StateValue.TRUE, StateValue.FALSE), Signals.Enabled.INSTANCE.values(), "a flag true at rest");
        assertEquals(List.of(StateValue.FALSE, StateValue.TRUE), Signals.Lit.INSTANCE.values(), "a flag false at rest, as most are");
        assertEquals(List.of("off", "dim", "bright"), Signals.Glow.INSTANCE.values().stream().map(StateValue::token).toList());
        assertThrows(IllegalArgumentException.class, () -> new StateValue("Bright"), "a value is named in lower case");
    }

    @Test
    void aValue_isAnsweredByItsDesignWord_establishedOrTheTypesAndItsOwn() {
        assertEquals("disabled", Signals.Enabled.INSTANCE.word(StateValue.FALSE), "an established word, declared");
        assertEquals("glow-dim", Signals.Glow.INSTANCE.word(new StateValue("dim")), "none established: the type's name and the value's");
        assertEquals("glow-bright", Signals.Glow.INSTANCE.word(new StateValue("bright")));
        assertEquals("lit-true", Signals.Lit.INSTANCE.word(StateValue.TRUE));
        assertThrows(IllegalArgumentException.class, () -> Signals.Enabled.INSTANCE.word(StateValue.TRUE), "the rest is a look, not a word");
        assertThrows(IllegalArgumentException.class, () -> Signals.Glow.INSTANCE.word(StateValue.TRUE), "a value the type lacks");
    }

    @Test
    void refused_oneDesignWordForTwoValues_andAWordForARest() {
        var twice = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(), List.of(),
                List.of(Signals.Enabled.INSTANCE, BrokenSignals.Off.INSTANCE)));
        assertEquals(Set.of(Rule.DESIGN_WORD_TWICE), twice.problems().stream().map(TaxonomyProblem::rule).collect(Collectors.toSet()), twice.getMessage());
        var rest = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(), List.of(), List.of(BrokenSignals.Quiet.INSTANCE)));
        assertEquals(Set.of(Rule.BAD_STATE_VALUES), rest.problems().stream().map(TaxonomyProblem::rule).collect(Collectors.toSet()), rest.getMessage());
    }

    @Test
    void everyNode_meansSomething_theRootsToo() {
        Taxonomy t = READ.read(List.of(Signals.Lamp.INSTANCE), List.of(), List.of(Signals.Lit.INSTANCE));
        assertTrue(t.meaning(StateRoot.INSTANCE).markdown().startsWith("Any type of state"));
        assertEquals("How brightly it shines.", t.meaning(Signals.Glow.INSTANCE).markdown());
    }

    @Test
    void refused_aNameTwiceWithinATree_andValuesThatDoNotHoldTogether() {
        var e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(), List.of(),
                List.of(BrokenSignals.Single.INSTANCE, BrokenSignals.Twice.INSTANCE, BrokenSignals.Glow.INSTANCE, Signals.Glow.INSTANCE)));
        Set<Rule> rules = e.problems().stream().map(TaxonomyProblem::rule).collect(Collectors.toSet());
        assertEquals(Set.of(Rule.BAD_STATE_VALUES, Rule.TREE_NAME_TWICE), rules, e.getMessage());
        assertEquals(3, e.problems().size(), "one value only, a value twice, and two glows: " + e.problems());
    }

    @Test
    void aTreeCannotBeMade_withANameTwice() {
        assertThrows(RefusedTaxonomy.class, () -> new StateTree(List.of(), List.of(BrokenSignals.Glow.INSTANCE, Signals.Glow.INSTANCE)));
        assertThrows(RefusedTaxonomy.class, () -> new StateTree(List.of(), List.of(BrokenSignals.Single.INSTANCE)));
    }

    @Test
    void withoutStates_theTreeIsEmpty() {
        Taxonomy t = READ.read(Sketch.DECLARED, Sketch.CATALOGUE);
        assertEquals(List.of(), t.states().types());
    }
}
