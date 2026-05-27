package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WidgetKind;

/** Hand-written codec for {@link WidgetKind} — wire form is the underlying string. */
public final class WidgetKindJsFunctions implements FunctionsCodeGen {

    public static final WidgetKindJsFunctions INSTANCE = new WidgetKindJsFunctions();

    private WidgetKindJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WidgetKind.class) {
            throw new IllegalArgumentException(
                    "WidgetKindJsFunctions only handles WidgetKind; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const WidgetKindCodec = {
                transformTo(k) {
                    if (!(k instanceof WidgetKind)) {
                        throw new TypeError("WidgetKindCodec.transformTo: expected WidgetKind");
                    }
                    return k.value;
                },
                transformFrom(wire) {
                    return new WidgetKind(wire);
                }
            };
            """;
}
