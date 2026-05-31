package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.PaneId;

/**
 * The first concrete {@link FunctionsCodeGen} — a hand-written
 * implementation that emits the ECMAScript Transformation Functions
 * (encode + decode pair) for {@link PaneId}.
 *
 * <p>Per the {@code CodeGen as Functions} doctrine, hand-written is one
 * valid implementation strategy among many (automated, AI-assisted,
 * hybrid). The contract is the same regardless of how the function
 * body was authored; the round-trip equivalence test doesn't know or
 * care which it was.</p>
 *
 * <h2>Wire form</h2>
 *
 * <p>For one-field wrapper types like {@link PaneId}, the wire form
 * IS the underlying value (a plain string here). No JSON envelope is
 * needed — the wire type {@code W} instantiates directly to the wrapped
 * type. This is the simplest case the ontology can express.</p>
 *
 * <p>Composite types (records with multiple fields, sealed hierarchies)
 * will produce a more elaborate JS object as their wire form; the
 * ontology accommodates both via the {@code <T, W>} type parameters on
 * {@link hue.captains.singapura.js.homing.codec.TransformationFunctions}.</p>
 *
 * @since codec POC — first hand-written Functional Object implementation
 */
public final class PaneIdJsFunctions implements FunctionsCodeGen {

    public static final PaneIdJsFunctions INSTANCE = new PaneIdJsFunctions();

    private PaneIdJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != PaneId.class) {
            throw new IllegalArgumentException(
                    "PaneIdJsFunctions only handles PaneId; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    /**
     * Hand-written ECMAScript Transformation Functions for {@link PaneId}.
     * Encodes to / decodes from the underlying string value. Round-trip
     * soundness: {@code transformFrom(transformTo(p)).value === p.value}.
     */
    private static final String SOURCE = """
            const PaneIdCodec = {
                transformTo(paneId) {
                    if (!(paneId instanceof PaneId)) {
                        throw new TypeError(
                            "PaneIdCodec.transformTo: expected PaneId instance");
                    }
                    return paneId.value;
                },
                transformFrom(wire) {
                    return new PaneId(wire);
                }
            };
            """;
}
