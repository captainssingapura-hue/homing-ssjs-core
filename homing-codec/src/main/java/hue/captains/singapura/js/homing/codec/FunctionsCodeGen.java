package hue.captains.singapura.js.homing.codec;

/**
 * Ontology object — the function that produces source code for a
 * {@link TransformationFunctions} implementation from an
 * {@link ObjectDefinition}.
 *
 * <h2>Doctrine: CodeGen as Functions</h2>
 *
 * <p>The interface doesn't care about provenance. Implementations may
 * be:</p>
 *
 * <ul>
 *   <li><b>Hand-written</b> — the function body is human-authored source
 *       code returned as a string; the simplest implementation strategy
 *       and the one this POC uses.</li>
 *   <li><b>Automated</b> — the function body walks an Object Definition's
 *       record components and templates JS / TS / Kotlin / Swift source
 *       deterministically.</li>
 *   <li><b>AI-assisted</b> — a model generates the function body; the
 *       result is still a {@code FunctionsCodeGen} from the interface's
 *       point of view.</li>
 *   <li><b>Hybrid</b> — automation produces a skeleton; humans fill in
 *       the tricky cases. Still one implementation of the interface.</li>
 * </ul>
 *
 * <p>All four flow through the same contract. The test discipline (round-trip
 * equivalence on the produced Transformation Functions) doesn't care which
 * provenance produced the source code.</p>
 *
 * <p>Pure: same Object Definition → same source code, deterministically.
 * The target language is encoded in the implementation class
 * ({@code EcmaFunctionsCodeGen}, {@code TypeScriptFunctionsCodeGen}, …),
 * not surfaced here.</p>
 *
 * @since homing-codec ontology
 */
public interface FunctionsCodeGen {

    /**
     * Generate source code that, when loaded into the target runtime,
     * implements {@link TransformationFunctions} for the given Object
     * Definition.
     *
     * @param definition the typed Object whose codec is to be generated
     * @return source code in the target language, as a string
     */
    String generate(ObjectDefinition<?> definition);
}
