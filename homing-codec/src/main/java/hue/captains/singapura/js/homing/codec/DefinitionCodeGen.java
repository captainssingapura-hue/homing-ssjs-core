package hue.captains.singapura.js.homing.codec;

/**
 * Ontology object — the function that translates an
 * {@link ObjectDefinition} from one language to another, emitting the
 * equivalent type definition as source code in the target language.
 *
 * <p>Without this, {@link FunctionsCodeGen}'s output would have nothing
 * to encode/decode against on the target side — the Transformation
 * Functions would refer to types that don't exist in the target
 * language. {@code DefinitionCodeGen} closes that loop: for each Object
 * Definition the framework wants to ship across a language seam, this
 * generator produces the matching type definition (a JS class, a
 * TypeScript interface, a Kotlin data class, …) in the target language.</p>
 *
 * <h2>Doctrine: CodeGen as Functions</h2>
 *
 * <p>As with {@link FunctionsCodeGen}, the interface accommodates
 * hand-written, automated, AI-assisted, or hybrid implementations
 * uniformly. The provenance is invisible at the contract boundary.</p>
 *
 * <p>Pure: same Object Definition → same source code, deterministically.
 * The source language is the framework's host (Java); the target
 * language is encoded in the implementation class
 * ({@code EcmaDefinitionCodeGen}, {@code TypeScriptDefinitionCodeGen}, …).</p>
 *
 * @since homing-codec ontology
 */
public interface DefinitionCodeGen {

    /**
     * Generate source code for the Object Definition's equivalent type
     * declaration in the target language.
     *
     * @param definition the Java-side Object Definition
     * @return source code in the target language, as a string
     */
    String generate(ObjectDefinition<?> definition);
}
