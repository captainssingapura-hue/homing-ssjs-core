package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.AtMost;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Fixed;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Unbounded;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Varying;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentPartDSL.NeedsCount;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentPartDSL.NeedsRole;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Badge;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Heading;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Naming;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Tag;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Title;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The declaration language on its own, before anything is read: a part played by a component,
 * given a role and told how many, becomes a slot; the verbs say every shape of cardinality, one way
 * each; a count that is no cardinality is thrown where it is said; and a role is an identity that
 * knows nothing of the language.
 */
class DeclarationTest {

    private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;

    private static NeedsCount<Badge> tags() { return DSL.part(Badge.INSTANCE).as(Tag.INSTANCE); }

    @Test
    void aPartPlayed_givenARole_andToldHowMany_isASlot() {
        Slot<Heading> title = DSL.part(Heading.INSTANCE).as(Title.INSTANCE).one();
        assertEquals(new Slot<>(Title.INSTANCE, Heading.INSTANCE, new Fixed(1)), title);
        assertEquals("title → heading 1", title.toString());
        assertEquals(new NeedsRole<>(Heading.INSTANCE), DSL.part(Heading.INSTANCE), "played, and needing a role");
        assertEquals(new NeedsCount<>(Heading.INSTANCE, Title.INSTANCE), DSL.part(Heading.INSTANCE).as(Title.INSTANCE),
                "given a role, and needing a count");
    }

    @Test
    void theVerbs_everyShapeOfCardinality() {
        assertEquals(new Fixed(1), tags().one().cardinality());
        assertEquals(new Fixed(3), tags().exactly(3).cardinality());
        assertEquals(new Varying(0, new AtMost(1)), tags().optional().cardinality());
        assertEquals(new Varying(0, new AtMost(4)), tags().atMost(4).cardinality());
        assertEquals(new Varying(0, Unbounded.INSTANCE), tags().any().cardinality());
        assertEquals(new Varying(2, Unbounded.INSTANCE), tags().atLeast(2).cardinality());
        assertEquals(new Varying(2, new AtMost(12)), tags().between(2, 12).cardinality());
    }

    @Test
    void writtenAsMultiplicity() {
        assertEquals(List.of("1", "3", "0..1", "0..4", "0..*", "2..*", "2..12"),
                List.of(tags().one(), tags().exactly(3), tags().optional(), tags().atMost(4), tags().any(), tags().atLeast(2),
                        tags().between(2, 12)).stream().map(s -> s.cardinality().multiplicity()).toList());
    }

    @Test
    void twoVerbsThatSayOneThing_makeOneValue() {
        assertEquals(tags().optional(), tags().between(0, 1));
        assertEquals(tags().any(), tags().atLeast(0));
        assertEquals(tags().one(), tags().exactly(1));
    }

    @Test
    void howManyIsAllowed_andWhatIsRequired() {
        Cardinality optional = tags().optional().cardinality(), some = tags().atLeast(1).cardinality(), two = tags().exactly(2).cardinality();
        assertTrue(optional.allows(0) && optional.allows(1) && !optional.allows(2));
        assertTrue(some.allows(1) && some.allows(1000) && !some.allows(0));
        assertTrue(two.allows(2) && !two.allows(1) && !two.allows(3));
        assertEquals(0, optional.least(), "optional: not required");
        assertEquals(1, some.least(), "required: a least of one or more - not a flag of its own");
    }

    @Test
    void aCountThatIsNoCardinality_isThrownWhereItIsSaid() {
        assertEquals("0 is no count: a role played no times is no role",
                assertThrows(BadCardinality.class, () -> tags().exactly(0)).getMessage());
        assertEquals("3..1 is no range: the most is below the least",
                assertThrows(BadCardinality.class, () -> tags().between(3, 1)).getMessage());
        assertEquals("2..2 is no range: say exactly(2)",
                assertThrows(BadCardinality.class, () -> tags().between(2, 2)).getMessage());
        assertThrows(BadCardinality.class, () -> tags().atMost(0));
        assertThrows(BadCardinality.class, () -> tags().atLeast(-1));
        assertThrows(BadCardinality.class, () -> new Varying(1, null));
    }

    @Test
    void aRole_isAnIdentity_carryingNothing_andKnowingNothingOfTheLanguage() {
        for (Role<?> r : Sketch.CATALOGUE) {
            assertTrue(r.getClass().isRecord(), r + " is a record");
            assertEquals(0, r.getClass().getRecordComponents().length, r + " carries nothing: what plays it and how many are a slot's");
        }
        assertEquals(List.of("parent"), Arrays.stream(Role.class.getDeclaredMethods()).map(Method::getName).toList(),
                "a role names its parent, and nothing of how it is cast");
        assertEquals("title", Title.INSTANCE.name().value(), "named after its type");
        assertEquals(Naming.INSTANCE, Title.INSTANCE.parent());
        assertEquals(Title.INSTANCE, new Title(), "one role, however it is reached");
    }

    @Test
    void theLanguage_aStandaloneFunctionalObject() {
        assertTrue(ComponentPartDSL.class.isRecord());
        assertEquals(0, ComponentPartDSL.class.getRecordComponents().length, "it holds nothing");
        assertEquals(ComponentPartDSL.INSTANCE, new ComponentPartDSL());
    }

    @Test
    void whatComposes_isSeenInItsFields() {
        var taxonomy = new ReadTaxonomy().read(Sketch.DECLARED);
        for (Component<?> c : taxonomy.components()) {
            boolean holds = Arrays.stream(c.getClass().getDeclaredFields())
                    .anyMatch(f -> f.getType() == ComponentPartDSL.class && java.lang.reflect.Modifier.isStatic(f.getModifiers()));
            assertEquals(!taxonomy.partsOf(c).isEmpty(), holds,
                    c + (holds ? " holds the language and declares no parts" : " declares parts without holding the language"));
        }
    }

    @Test
    void theShapes_byType() {
        assertFalse(Slot.class.isAssignableFrom(NeedsRole.class), "a part given no role is not a slot");
        assertFalse(Slot.class.isAssignableFrom(NeedsCount.class), "a part told no count is not a slot");
        assertEquals(List.of(Fixed.class, Varying.class), List.of(Cardinality.class.getPermittedSubclasses()), "two cases, no third");
        assertEquals(List.of(AtMost.class, Unbounded.class), List.of(Cardinality.Bound.class.getPermittedSubclasses()));
        assertFalse(RoleBranch.class.isAssignableFrom(Role.class), "a role is not a branch: nothing is filed under it");
        assertFalse(ComponentNode.class.isAssignableFrom(RoleNode.class), "a role is no node of the design: it has no classes of its own");
    }
}
