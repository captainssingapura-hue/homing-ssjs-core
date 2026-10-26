package hue.captains.singapura.js.homing.base.lang;

import hue.captains.singapura.tao.ontology.Immutable;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Optional;

/**
 * Gives the one instance of a {@link StatelessSingleton}: the type's own {@code static final INSTANCE}, never a
 * supertype's, and never one made for the asking. A type without its instance is refused, in words that name the type
 * and what it lacks.
 *
 * <p>A manager says only what reading a type comes to ({@link #read}); how that is given or refused is the same for
 * every manager. There are two:</p>
 * <ul>
 *   <li>{@link ReadSingleton} reads a type each time it is asked, and keeps nothing - stateless, and a singleton
 *       itself;</li>
 *   <li>{@link CachedSingletonManager} keeps what each read came to, so a type is read once - stateful by design, and
 *       so no singleton: whoever wants the cache makes one and hands it on.</li>
 * </ul>
 */
public interface SingletonManager {

    /** What reading the type comes to: its instance, why it has none, or that it is not there yet. */
    Read read(Class<?> type);

    /** The type's one instance. */
    default <T extends StatelessSingleton> T of(Class<T> type) {
        return type.cast(instance(type));
    }

    /**
     * The type's one instance, as an {@code as}: for a caller that holds a class without its type - a type argument,
     * an enclosing class - and knows what its instance should be.
     */
    default <T> T of(Class<?> type, Class<T> as) {
        Object instance = instance(type);
        if (!as.isInstance(instance))
            throw new IllegalStateException(type.getName() + "'s INSTANCE is not a " + as.getName());
        return as.cast(instance);
    }

    /** The type's one instance, as an {@code as}; empty where {@link #of(Class, Class)} refuses. */
    default <T> Optional<T> find(Class<?> type, Class<T> as) {
        return read(type) instanceof Read.Held held && as.isInstance(held.instance()) ? Optional.of(as.cast(held.instance())) : Optional.empty();
    }

    private Object instance(Class<?> type) {
        return switch (read(type)) {
            case Read.Held held -> held.instance();
            case Read.Refused refused -> throw new IllegalStateException(refused.why());
            case Read.NotYet notYet -> throw new IllegalStateException(notYet.why());
        };
    }

    /** What reading a type comes to. */
    sealed interface Read extends Immutable {

        /** The type's one instance. */
        record Held(StatelessSingleton instance) implements Read {}

        /** Why the type has none, in words that name it: so it stays, however often it is asked. */
        record Refused(String why) implements Read, ValueObject {}

        /**
         * Its INSTANCE is null: asked for while the type is still being initialized, or never set. It may be there
         * when asked again, so it is never kept.
         */
        record NotYet(String why) implements Read, ValueObject {}
    }
}
