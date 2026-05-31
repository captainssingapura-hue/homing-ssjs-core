package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.PaneId;

/**
 * The first concrete {@link DefinitionCodeGen} — a hand-written
 * implementation that emits the ECMAScript class definition for
 * {@link PaneId}.
 *
 * <p>Per the {@code CodeGen as Functions} doctrine, "hand-written" is
 * a valid implementation strategy: the function body produces source
 * code authored by a human, and the result conforms to the
 * {@link DefinitionCodeGen} contract identically to whatever an
 * automated generator would produce. Provenance is invisible at the
 * boundary.</p>
 *
 * <p>The generated class mirrors the Java {@link PaneId} record exactly:
 * one {@code value} field of type string, the same regex grammar
 * validation, frozen instance, {@code toString} returning the value.</p>
 *
 * @since codec POC — first hand-written Functional Object implementation
 */
public final class PaneIdJsDefinition implements DefinitionCodeGen {

    public static final PaneIdJsDefinition INSTANCE = new PaneIdJsDefinition();

    private PaneIdJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != PaneId.class) {
            throw new IllegalArgumentException(
                    "PaneIdJsDefinition only handles PaneId; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    /**
     * Hand-written ECMAScript class definition for {@link PaneId}.
     * Mirrors the Java record's compact-constructor validation: regex
     * grammar, non-null, frozen.
     */
    private static final String SOURCE = """
            class PaneId {
                constructor(value) {
                    if (typeof value !== 'string') {
                        throw new TypeError("PaneId.value: must be string, got " + typeof value);
                    }
                    if (!/^[A-Za-z0-9_-]+$/.test(value)) {
                        throw new RangeError(
                            "PaneId.value '" + value
                            + "' — only letters, digits, hyphen, underscore allowed");
                    }
                    this.value = value;
                    Object.freeze(this);
                }
                toString() { return this.value; }
            }
            """;
}
