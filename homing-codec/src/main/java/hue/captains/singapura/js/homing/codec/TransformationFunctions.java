package hue.captains.singapura.js.homing.codec;

/**
 * Ontology object — the typed encode/decode pair for a single
 * {@link ObjectDefinition}.
 *
 * <p>A Functional Object (per the {@code Functional Objects} doctrine):
 * stateless, pure, two methods that compose into a round-trip. The pair
 * is inseparable — implementations satisfy:</p>
 *
 * <pre>{@code
 *     transformFrom(transformTo(t)) = t      for every t : T
 * }</pre>
 *
 * <p>Implementations are typically produced by
 * {@link FunctionsCodeGen} at build time; the source code that
 * {@code FunctionsCodeGen} emits, once loaded into a target runtime
 * (Java, ECMAScript, …), conforms to the shape this interface
 * declares.</p>
 *
 * <p>The wire-form type {@code W} is a type parameter, not a separate
 * ontology object — different wire formats (JSON string, CBOR bytes,
 * MessagePack bytes, custom binary) instantiate {@code W} differently
 * at use sites without changing the ontology.</p>
 *
 * @param <T> the typed Object's runtime type
 * @param <W> the wire form (e.g. {@link String} for textual, {@code byte[]} for binary)
 * @since homing-codec ontology
 */
public interface TransformationFunctions<T, W> {

    /** Encode a typed value into its wire form. */
    W transformTo(T value);

    /** Decode a wire form back into its typed value. */
    T transformFrom(W wire);
}
