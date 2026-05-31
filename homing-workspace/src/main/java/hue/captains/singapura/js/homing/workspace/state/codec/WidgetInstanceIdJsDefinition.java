package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WidgetInstanceId;

/**
 * Hand-written {@link DefinitionCodeGen} for
 * {@link WidgetInstanceId} — UUID-backed wrapper.
 * Validation grammar matches Java's {@code UUID.fromString} input shape.
 */
public final class WidgetInstanceIdJsDefinition implements DefinitionCodeGen {

    public static final WidgetInstanceIdJsDefinition INSTANCE = new WidgetInstanceIdJsDefinition();

    private WidgetInstanceIdJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WidgetInstanceId.class) {
            throw new IllegalArgumentException(
                    "WidgetInstanceIdJsDefinition only handles WidgetInstanceId; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            class WidgetInstanceId {
                constructor(id) {
                    if (typeof id !== 'string') {
                        throw new TypeError("WidgetInstanceId.id: must be string, got " + typeof id);
                    }
                    if (!/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(id)) {
                        throw new RangeError("WidgetInstanceId.id '" + id + "' is not a valid UUID");
                    }
                    this.id = id;
                    Object.freeze(this);
                }
                toString() { return this.id; }
            }
            """;
}
