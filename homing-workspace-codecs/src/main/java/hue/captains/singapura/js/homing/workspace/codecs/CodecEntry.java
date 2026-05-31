package hue.captains.singapura.js.homing.workspace.codecs;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * One typed entry in the codec umbrella's manifest. Links a Java type to
 * the two stateless functional objects that emit its JS class definition
 * and codec functions.
 *
 * <p>The two-generator pair (Definition + Functions) is intentional —
 * it mirrors the existing {@code homing-codec} ontology split:
 * {@link DefinitionCodeGen} owns the class declaration; {@link
 * FunctionsCodeGen} owns the {@code transformTo}/{@code transformFrom}
 * round-trip codec. Different concerns, separately tested, sometimes
 * authored by different mechanisms — e.g., hand-written for the
 * structural records, reflective for the simple wrappers.</p>
 *
 * <p>By holding instances of both generators rather than class
 * references, the manifest is fully constructed at compile time — every
 * generator is reachable via its {@code INSTANCE} singleton. The
 * generator implementations themselves are stateless functional objects,
 * so caching, identity, and equality are all trivially correct.</p>
 *
 * @since RFC 0034 P2-prep Cycle A — codec umbrella manifest entry
 */
public record CodecEntry<T>(
        DefinitionCodeGen defGen,
        FunctionsCodeGen  fnGen,
        Class<T>          type
) implements StatelessFunctionalObject {

    public ObjectDefinition<T> definition() {
        return ObjectDefinition.of(type);
    }

    /** Convenience constructor when both generators are the same reflective default. */
    public static <T> CodecEntry<T> reflective(Class<T> type,
                                               DefinitionCodeGen defGen,
                                               FunctionsCodeGen  fnGen) {
        return new CodecEntry<>(defGen, fnGen, type);
    }

    /** Convenience constructor for hand-written codegen pairs. */
    public static <T> CodecEntry<T> manual(DefinitionCodeGen defGen,
                                           FunctionsCodeGen  fnGen,
                                           Class<T>          type) {
        return new CodecEntry<>(defGen, fnGen, type);
    }
}
