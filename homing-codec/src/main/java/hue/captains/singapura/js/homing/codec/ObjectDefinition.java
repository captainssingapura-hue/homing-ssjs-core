package hue.captains.singapura.js.homing.codec;

import java.util.Objects;

/**
 * Ontology object — the typed source-of-truth in the codec sub-category.
 *
 * <p>An {@code ObjectDefinition<T>} names what is being encoded / decoded /
 * translated. In Java, it's a thin wrapper over the underlying type
 * (typically a record class or a sealed-of-records hierarchy). The wrapper
 * exists so the ontology has a typed first-class concept rather than
 * passing {@code Class<T>} around bare.</p>
 *
 * <p>What the other ontology objects do with it:</p>
 *
 * <ul>
 *   <li>{@link TransformationFunctions} is parameterised by {@code T} —
 *       the encode/decode pair operates on instances of the Object
 *       Definition's type.</li>
 *   <li>{@link FunctionsCodeGen} consumes an Object Definition and
 *       produces source code for the matching Transformation Functions.</li>
 *   <li>{@link DefinitionCodeGen} consumes an Object Definition and
 *       produces source code for the same Object Definition in a
 *       different target language.</li>
 * </ul>
 *
 * <p>Per the {@code Names Are Types} doctrine, this is a typed record —
 * never a raw {@code Class<?>} passed into codec APIs.</p>
 *
 * @param type the underlying Java {@link Class} for the type T
 * @param <T>  the type being defined
 * @since homing-codec ontology
 */
public record ObjectDefinition<T>(Class<T> type) {

    public ObjectDefinition {
        Objects.requireNonNull(type, "ObjectDefinition.type");
    }

    /** Convenience constructor. */
    public static <T> ObjectDefinition<T> of(Class<T> type) {
        return new ObjectDefinition<>(type);
    }

    /** Convenience — the human-readable name of the underlying type. */
    public String simpleName() {
        return type.getSimpleName();
    }
}
