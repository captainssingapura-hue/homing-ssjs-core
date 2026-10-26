package hue.captains.singapura.js.homing.base.lang.samples;

import hue.captains.singapura.js.homing.base.lang.ReadSingleton;
import hue.captains.singapura.js.homing.base.lang.StatelessSingleton;
import hue.captains.singapura.tao.ontology.Stateless;

import java.util.Optional;

/**
 * Sample types for the singleton managers' tests: stateless singletons kept as {@link StatelessSingleton} asks, and
 * each way of breaking its rule. A package of their own, so a manager reads them from outside, as it reads any
 * module's.
 */
public final class Samples {

    private Samples() {}

    // ---- kept as the rule asks

    /** A record, its one instance its own public static final INSTANCE. */
    public record Identity() implements StatelessSingleton {
        public static final Identity INSTANCE = new Identity();
    }

    /** A one-constant enum. */
    public enum Unit implements StatelessSingleton { INSTANCE }

    /** A class of this package alone, its INSTANCE too: read from outside the package all the same. */
    static final class Shy implements StatelessSingleton {
        static final Shy INSTANCE = new Shy();

        private Shy() {}
    }

    /** {@link Shy}'s class, for a test outside this package. */
    public static final Class<?> SHY = Shy.class;

    // ---- each way of breaking it

    /** Stateless, with an INSTANCE, but no stateless singleton: being stateless is not being one. */
    public record Unmarked() implements Stateless {
        public static final Unmarked INSTANCE = new Unmarked();
    }

    /** A kind of singleton, with no instance of its own. */
    public interface Kind extends StatelessSingleton {}

    /** A stateless singleton that declares no INSTANCE. */
    public record Bare() implements StatelessSingleton {}

    /** A parent with an instance of its own. */
    public static class Parent implements StatelessSingleton {
        public static final Parent INSTANCE = new Parent();
    }

    /** A child with none: its parent's INSTANCE is the parent's, though {@code getField} would hand it over. */
    public static final class Child extends Parent {}

    /** An INSTANCE that could be set again. */
    public record Loose() implements StatelessSingleton {
        public static Loose INSTANCE = new Loose();
    }

    /** An INSTANCE declared wider than the type. */
    public record Wide() implements StatelessSingleton {
        public static final StatelessSingleton INSTANCE = new Wide();
    }

    /** An enum of two constants: two instances, not one. */
    public enum Pair implements StatelessSingleton { INSTANCE, OTHER }

    /** An INSTANCE never set. */
    public record Unset() implements StatelessSingleton {
        public static final Unset INSTANCE = null;
    }

    /** Asks for its own instance while it is being initialized, before its INSTANCE is set. */
    public record Early() implements StatelessSingleton {
        public static final Optional<Early> SEEN = ReadSingleton.INSTANCE.find(Early.class, Early.class);
        public static final Early INSTANCE = new Early();
    }

    /** A singleton that cannot be made: its constructor refuses, so its initialization fails. */
    public record Broken() implements StatelessSingleton {
        public static final Broken INSTANCE = new Broken();

        public Broken {
            throw new IllegalStateException("Broken refuses to be made");
        }
    }
}
