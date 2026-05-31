/**
 * ECMAScript-target implementations of the {@code homing-codec} ontology.
 *
 * <p>Provides two concrete code generators:</p>
 *
 * <ul>
 *   <li>{@link hue.captains.singapura.js.homing.codec.ecma.EcmaDefinitionCodeGen}
 *       — emits a JS class declaration mirroring a Java record's shape.</li>
 *   <li>{@link hue.captains.singapura.js.homing.codec.ecma.EcmaFunctionsCodeGen}
 *       — emits a JS {@code TransformationFunctions} implementation
 *       (a class with static {@code transformTo} and {@code transformFrom}
 *       methods) for the same Object Definition.</li>
 * </ul>
 *
 * <p>Both are stateless Functional Objects (per the framework's Functional
 * Objects doctrine) and reflect on the {@code ObjectDefinition}'s underlying
 * Java {@code Class} at <i>generation time</i> — never at runtime in the
 * generated JS. The generated JS contains no reflection; only explicit
 * field accesses derived from the Java record's components.</p>
 *
 * <p>"Codegen as Functions" — the same instance can be invoked at build
 * time (typically via a Maven {@code exec} task in a downstream module)
 * or at request time if a consumer ever wants runtime generation. The
 * interface contract is identical either way.</p>
 *
 * @since homing-codec ontology — ECMAScript target
 */
package hue.captains.singapura.js.homing.codec.ecma;
