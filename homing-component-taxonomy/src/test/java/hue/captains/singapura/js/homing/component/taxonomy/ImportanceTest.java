package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The leaves a design sees and the precedence of their states (RFC 0067, appendix Two Classes per Leaf): a component and
 * a part at its role, each a leaf with states of its own; every state's level intrinsic, on the tree of states; a leaf's
 * override in place of it; two states of one level in the order the leaf lists them. And what the reader refuses.
 */
class ImportanceTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();
    private static final Taxonomy GEAR = READ.read(List.of(Ranked.Knob.INSTANCE, Ranked.Dial.INSTANCE, Ranked.Console.INSTANCE));

    @Test
    void theLevels_areSix_theMostImportantFirst_readOffTheTypes() {
        var imp = Importances.INSTANCE;
        assertEquals(List.of(Importance.Primary.class, Importance.Secondary.class, Importance.Tertiary.class,
                Importance.Quaternary.class, Importance.Quinary.class, Importance.Senary.class), imp.levels());
        assertEquals(Importance.Tertiary.class, imp.levelOf(Ranked.Armed.INSTANCE).orElseThrow());
        assertEquals(0, imp.rank(Importance.Primary.class));
        assertEquals(5, imp.rank(Importance.Senary.class));
        assertTrue(imp.levelOf(Ranked.Unranked.INSTANCE).isEmpty());
    }

    @Test
    void aLeafsPrecedence_theLeastImportantFirst_aTieInTheOrderListed() {
        assertEquals(List.of(Ranked.Faded.INSTANCE, Ranked.Pointed.INSTANCE, Ranked.Picked.INSTANCE, Ranked.Armed.INSTANCE, Ranked.Grabbed.INSTANCE),
                GEAR.precedence(Ranked.Knob.INSTANCE),
                "senary, quinary, then the two tertiary states as the knob lists them, then secondary: the last wins");
        assertEquals(List.of(Ranked.Grabbed.INSTANCE, Ranked.Armed.INSTANCE), GEAR.precedence(Ranked.Dial.INSTANCE),
                "the dial makes Armed the most important, for itself");
        assertEquals(Importance.Tertiary.class, Importances.INSTANCE.levelFor(Ranked.Knob.INSTANCE, Ranked.Armed.INSTANCE).orElseThrow(),
                "the dial's override is its own: the knob's Armed is still tertiary");
    }

    @Test
    void aCasting_isALeafForItsClass_itsStatesItsComponents_itsOwnerNeverInIt() {
        assertEquals(List.of("knob", "dial", "console", "knob_steer"), GEAR.castings().stream().map(Casting::token).toList(),
                "each component on its own, then each role a slot casts its component in: the knob as Steer, whoever owns it");
        Casting steer = GEAR.castings().get(3);
        assertEquals(Ranked.Knob.INSTANCE, steer.cast());
        assertEquals(GEAR.precedence(Ranked.Knob.INSTANCE), GEAR.precedence(steer.cast()), "a role adds no state: the knob's, in the knob's precedence");
    }

    @Test
    void refused_aStateOfNoLevel_aPseudoStateThatIsNone_andALeafThatListsBadly() {
        var e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(Ranked.Muddled.INSTANCE), List.of(),
                List.of(Ranked.Unranked.INSTANCE, Ranked.Wide.INSTANCE, Ranked.Colonless.INSTANCE), List.of()));
        Map<Rule, Long> rules = e.problems().stream().collect(Collectors.groupingBy(TaxonomyProblem::rule, Collectors.counting()));
        assertEquals(Map.of(Rule.STATE_UNRANKED, 1L, Rule.PSEUDO_BADLY, 2L, Rule.STATES_LISTED_BADLY, 3L), rules, e.getMessage());
        assertTrue(e.getMessage().contains("muddled lists Armed twice"), e.getMessage());
        assertTrue(e.getMessage().contains("muddled overrides the level of Picked, which it does not list"), e.getMessage());
        assertTrue(e.getMessage().contains("muddled overrides the level of Armed twice"), e.getMessage());
    }

    @Test
    void aPseudoState_saysItsPseudoClass() {
        assertEquals(":hover", ((Pseudo) Ranked.Pointed.INSTANCE).pseudoClass());
        assertEquals(List.of(Ranked.Pointed.INSTANCE), GEAR.precedence(Ranked.Knob.INSTANCE).stream().filter(s -> s instanceof Pseudo).toList());
        StateType<?> armed = Ranked.Armed.INSTANCE;
        assertFalse(armed instanceof Pseudo, "every other state is the application's");
    }
}
