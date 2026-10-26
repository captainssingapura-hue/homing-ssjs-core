package hue.captains.singapura.js.homing.base.lang;

import hue.captains.singapura.js.homing.base.lang.SingletonManager.Read;
import hue.captains.singapura.js.homing.base.lang.samples.Samples;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Bare;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Broken;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Child;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Early;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Identity;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Kind;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Loose;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Pair;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Parent;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Unit;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Unmarked;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Unset;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Wide;
import hue.captains.singapura.tao.ontology.enforcer.OntologyEnforcer;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadSingletonTest {

    private static final ReadSingleton READ = ReadSingleton.INSTANCE;

    @Test
    void aRecord_aOneConstantEnum_andTheReaderItself_giveTheirInstance() {
        assertSame(Identity.INSTANCE, READ.of(Identity.class));
        assertSame(Unit.INSTANCE, READ.of(Unit.class));
        assertSame(ReadSingleton.INSTANCE, READ.of(ReadSingleton.class));
    }

    @Test
    void askedAgain_theSameInstance() {
        assertSame(READ.of(Identity.class), READ.of(Identity.class));
        assertSame(READ.of(Identity.class), READ.of(Identity.class, Identity.class));
        assertSame(READ.of(Identity.class), READ.find(Identity.class, Identity.class).orElseThrow());
    }

    @Test
    void aClassOfAnotherPackage_isReadAllTheSame() {
        StatelessSingleton shy = READ.of(Samples.SHY, StatelessSingleton.class);
        assertEquals(Samples.SHY, shy.getClass());
    }

    @Test
    void asAnAs_orRefusedNamingWhatItIsNot() {
        assertSame(Identity.INSTANCE, READ.of(Identity.class, StatelessSingleton.class));
        var refused = assertThrows(IllegalStateException.class, () -> READ.of(Identity.class, CharSequence.class));
        assertEquals(Identity.class.getName() + "'s INSTANCE is not a java.lang.CharSequence", refused.getMessage());
        assertEquals(Optional.empty(), READ.find(Identity.class, CharSequence.class));
    }

    @Test
    void eachWayOfBreakingTheRule_isRefusedInWords_andFindIsEmpty() {
        var expected = new LinkedHashMap<Class<?>, String>();
        expected.put(Unmarked.class, Unmarked.class.getName() + " is not a StatelessSingleton: only a stateless singleton has one instance to give");
        expected.put(Kind.class, Kind.class.getName() + " is an interface: only a concrete type has an instance of its own");
        expected.put(Pair.class, Pair.class.getName() + " is an enum of 2 constants: a singleton has one");
        expected.put(Bare.class, Bare.class.getName() + " declares no INSTANCE of its own: a stateless singleton declares its one instance"
                + " as a static final INSTANCE, and a supertype's is not its own");
        expected.put(Child.class, Child.class.getName() + " declares no INSTANCE of its own: a stateless singleton declares its one instance"
                + " as a static final INSTANCE, and a supertype's is not its own");
        expected.put(Loose.class, Loose.class.getName() + "'s INSTANCE is not static final");
        expected.put(Wide.class, Wide.class.getName() + "'s INSTANCE is declared as " + StatelessSingleton.class.getName() + ", not " + Wide.class.getName());
        for (Map.Entry<Class<?>, String> e : expected.entrySet()) {
            assertEquals(new Read.Refused(e.getValue()), READ.read(e.getKey()));
            var refused = assertThrows(IllegalStateException.class, () -> READ.of(e.getKey(), Object.class), e.getKey().getName());
            assertEquals(e.getValue(), refused.getMessage());
            assertEquals(Optional.empty(), READ.find(e.getKey(), Object.class), e.getKey().getName());
        }
    }

    @Test
    void anInstanceNeverSet_isNotThereYet() {
        String why = Unset.class.getName() + "'s INSTANCE is null: asked for while " + Unset.class.getName() + " is still being initialized, or never set";
        assertEquals(new Read.NotYet(why), READ.read(Unset.class));
        assertEquals(why, assertThrows(IllegalStateException.class, () -> READ.of(Unset.class)).getMessage());
        assertEquals(Optional.empty(), READ.find(Unset.class, Object.class));
    }

    @Test
    void aChild_neverGetsItsParentsInstance_thoughGetFieldWouldHandItOver() {
        assertDoesNotThrow(() -> Child.class.getField("INSTANCE"), "getField finds the parent's INSTANCE on the child");
        assertSame(Parent.INSTANCE, READ.of(Parent.class));
        assertInstanceOf(Read.Refused.class, READ.read(Child.class));
    }

    @Test
    void askedForWhileItIsBeingInitialized_notThereYet_andThereOnceItIsMade() {
        assertEquals(Optional.empty(), Early.SEEN, "Early asked for itself before its INSTANCE was set");
        assertSame(Early.INSTANCE, READ.of(Early.class));
    }

    @Test
    void readAgain_aSingletonThatCannotBeMade_saysLess() {
        var first = assertThrows(IllegalStateException.class, () -> READ.of(Broken.class)).getMessage();
        var again = assertThrows(IllegalStateException.class, () -> READ.of(Broken.class)).getMessage();
        assertTrue(first.startsWith(Broken.class.getName() + " failed to initialize, so it has no INSTANCE: "), first);
        assertTrue(first.endsWith("Broken refuses to be made"), first);
        // The second time, the class says only that it could not be initialized: why a cache keeps a refusal.
        assertTrue(again.startsWith(Broken.class.getName() + " cannot be loaded, so it has no INSTANCE: java.lang.NoClassDefFoundError"), again);
    }

    @Test
    void theReaderAndTheKind_passTheOntologysOwnCheck() {
        var enforcer = new OntologyEnforcer();
        for (Class<?> c : List.of(ReadSingleton.class, StatelessSingleton.class))
            assertEquals(List.of(), enforcer.enforce(c).stream().map(v -> v.message()).toList(), c.getName());
    }
}
