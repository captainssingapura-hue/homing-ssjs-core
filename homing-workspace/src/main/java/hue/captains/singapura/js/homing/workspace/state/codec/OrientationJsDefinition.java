package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.Orientation;

/**
 * Hand-written codec class for {@link Orientation} — the SplitPane
 * orientation enum. JS representation: a frozen namespace object with
 * two named members ({@code HORIZONTAL}, {@code VERTICAL}), each
 * carrying a {@code name} field that matches the Java enum constant
 * name. Wire form (in {@link OrientationJsFunctions}) is the name
 * string.
 */
public final class OrientationJsDefinition implements DefinitionCodeGen {

    public static final OrientationJsDefinition INSTANCE = new OrientationJsDefinition();

    private OrientationJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != Orientation.class) {
            throw new IllegalArgumentException(
                    "OrientationJsDefinition only handles Orientation; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const Orientation = Object.freeze({
                HORIZONTAL: Object.freeze({ name: 'HORIZONTAL' }),
                VERTICAL:   Object.freeze({ name: 'VERTICAL'   })
            });
            """;
}
