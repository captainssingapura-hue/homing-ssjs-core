package hue.captains.singapura.js.homing.base.lang;

import hue.captains.singapura.js.homing.base.lang.SingletonManager.Read;
import hue.captains.singapura.js.homing.base.lang.samples.Samples;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Bare;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Child;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Identity;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Kind;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Loose;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Pair;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Unit;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Unmarked;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Unset;
import hue.captains.singapura.js.homing.base.lang.samples.Samples.Wide;
import hue.captains.singapura.tao.ontology.Mutable;
import hue.captains.singapura.tao.ontology.enforcer.OntologyEnforcer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CachedSingletonManagerTest {

    /** A reader that counts how often it is asked about each type, and answers as the plain reader does. */
    private static final class Counting implements SingletonManager, Mutable {
        final Map<Class<?>, Integer> asked = new HashMap<>();

        @Override public Read read(Class<?> type) {
            asked.merge(type, 1, Integer::sum);
            return ReadSingleton.INSTANCE.read(type);
        }
    }

    /** A reader that says "not yet" the first time it is asked, and gives the instance after. */
    private static final class NotYetThenHeld implements SingletonManager, Mutable {
        int asked;

        @Override public Read read(Class<?> type) {
            asked++;
            return asked == 1 ? new Read.NotYet("not yet") : new Read.Held(Identity.INSTANCE);
        }
    }

    @Test
    void eachType_isAskedAboutOnce_itsInstanceKept_andItsRefusalKept() {
        var counting = new Counting();
        var cache = new CachedSingletonManager(counting);

        assertSame(Identity.INSTANCE, cache.of(Identity.class));
        assertSame(Identity.INSTANCE, cache.of(Identity.class));
        assertSame(Identity.INSTANCE, cache.of(Identity.class, StatelessSingleton.class));
        assertSame(Identity.INSTANCE, cache.find(Identity.class, Identity.class).orElseThrow());

        var first = assertThrows(IllegalStateException.class, () -> cache.of(Bare.class)).getMessage();
        var again = assertThrows(IllegalStateException.class, () -> cache.of(Bare.class)).getMessage();
        assertEquals(first, again);
        assertEquals(Optional.empty(), cache.find(Bare.class, Object.class));

        assertEquals(Map.of(Identity.class, 1, Bare.class, 1), counting.asked);
    }

    @Test
    void notThereYet_isNeverKept() {
        var reader = new NotYetThenHeld();
        var cache = new CachedSingletonManager(reader);

        assertEquals(Optional.empty(), cache.find(Identity.class, Identity.class));
        assertSame(Identity.INSTANCE, cache.of(Identity.class), "asked again, the type is read again");
        assertSame(Identity.INSTANCE, cache.of(Identity.class));
        assertEquals(2, reader.asked, "once for not yet, once for the instance it keeps");
    }

    @Test
    void answersAsTheReaderItWraps() {
        var cache = new CachedSingletonManager(ReadSingleton.INSTANCE);
        for (Class<?> c : List.of(Identity.class, Unit.class, Samples.SHY, Unmarked.class, Kind.class, Bare.class, Child.class,
                Loose.class, Wide.class, Pair.class, Unset.class))
            assertEquals(ReadSingleton.INSTANCE.read(c), cache.read(c), c.getName());
    }

    @Test
    void isMutable_byTheOntologysOwnCheck_andSoNoSingleton() {
        assertEquals(List.of(), new OntologyEnforcer().enforce(CachedSingletonManager.class).stream().map(v -> v.message()).toList());
        assertFalse(StatelessSingleton.class.isAssignableFrom(CachedSingletonManager.class));
        assertEquals(new Read.Refused(CachedSingletonManager.class.getName()
                        + " is not a StatelessSingleton: only a stateless singleton has one instance to give"),
                ReadSingleton.INSTANCE.read(CachedSingletonManager.class));
    }

    @Test
    void aCacheOverNoReader_isRefused() {
        assertEquals("reader", assertThrows(NullPointerException.class, () -> new CachedSingletonManager(null)).getMessage());
    }
}
