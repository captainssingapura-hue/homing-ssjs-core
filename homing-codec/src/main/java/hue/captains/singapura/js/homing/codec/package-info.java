/**
 * The Meta-Function ontology — typed interfaces that name the four
 * objects involved in moving data across language seams via generated
 * code rather than runtime reflection.
 *
 * <h2>Four ontology objects</h2>
 *
 * <ol>
 *   <li>{@link hue.captains.singapura.js.homing.codec.ObjectDefinition}
 *       — the typed source-of-truth (a Java record or sealed type).</li>
 *   <li>{@link hue.captains.singapura.js.homing.codec.TransformationFunctions}
 *       — the pure encode/decode pair: {@code transformTo: T → W} and
 *       {@code transformFrom: W → T}. A Functional Object per the
 *       framework's doctrine.</li>
 *   <li>{@link hue.captains.singapura.js.homing.codec.FunctionsCodeGen}
 *       — {@code ObjectDefinition<T> → source code of TransformationFunctions<T, W>}.
 *       Generates the codec.</li>
 *   <li>{@link hue.captains.singapura.js.homing.codec.DefinitionCodeGen}
 *       — {@code ObjectDefinition<T> in language A → source code of ObjectDefinition<T> in language B}.
 *       Generates the type declaration in the target language so the
 *       generated codec has something to encode / decode against.</li>
 * </ol>
 *
 * <h2>Where implementations live</h2>
 *
 * <p>This module is interfaces only. Concrete codecs live with their
 * Value Objects — each {@code ObjectDefinition}'s codecs are
 * co-located with the Object Definition itself, in whatever module owns
 * that record. The pattern: meta-level contracts here, Value-Object-specific
 * implementations elsewhere.</p>
 *
 * <h2>What's deliberately NOT in the ontology</h2>
 *
 * <ul>
 *   <li><b>Wire format</b> — a type parameter on
 *       {@link hue.captains.singapura.js.homing.codec.TransformationFunctions},
 *       not a separate ontology object.</li>
 *   <li><b>Target language</b> — encoded in the implementation class of
 *       a {@code CodeGen}, not a separate object.</li>
 *   <li><b>Codec as a third object</b> — the codec IS the function pair,
 *       not a wrapper class.</li>
 * </ul>
 *
 * <h2>Doctrinal alignment</h2>
 *
 * <ul>
 *   <li><b>Names Are Types</b> — every ontology object is typed; no raw
 *       {@code Class<?>} or {@code String} signatures at the contract
 *       boundary.</li>
 *   <li><b>Functional Objects</b> — {@code TransformationFunctions}
 *       and the CodeGens are stateless pure functions, not stateful
 *       classes.</li>
 *   <li><b>CodeGen as Functions</b> — implementations may be hand-written,
 *       automated, AI-assisted, or hybrid; the interface doesn't privilege
 *       any one source. Provenance is invisible at the contract boundary.</li>
 *   <li><b>Codegen Over Reflection</b> (candidate doctrine) — codecs are
 *       generated at build time, not derived by runtime introspection.</li>
 * </ul>
 *
 * @since homing-codec ontology
 */
package hue.captains.singapura.js.homing.codec;
