package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WidgetInstanceId;

/** Hand-written codec for {@link WidgetInstanceId} — wire form is the UUID string. */
public final class WidgetInstanceIdJsFunctions implements FunctionsCodeGen {

    public static final WidgetInstanceIdJsFunctions INSTANCE = new WidgetInstanceIdJsFunctions();

    private WidgetInstanceIdJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WidgetInstanceId.class) {
            throw new IllegalArgumentException(
                    "WidgetInstanceIdJsFunctions only handles WidgetInstanceId; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const WidgetInstanceIdCodec = {
                transformTo(wid) {
                    if (!(wid instanceof WidgetInstanceId)) {
                        throw new TypeError("WidgetInstanceIdCodec.transformTo: expected WidgetInstanceId");
                    }
                    return wid.id;
                },
                transformFrom(wire) {
                    return new WidgetInstanceId(wire);
                }
            };
            """;
}
