package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WidgetTitle;

/** Hand-written codec for {@link WidgetTitle}. */
public final class WidgetTitleJsFunctions implements FunctionsCodeGen {

    public static final WidgetTitleJsFunctions INSTANCE = new WidgetTitleJsFunctions();

    private WidgetTitleJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WidgetTitle.class) {
            throw new IllegalArgumentException(
                    "WidgetTitleJsFunctions only handles WidgetTitle; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const WidgetTitleCodec = {
                transformTo(t) {
                    if (!(t instanceof WidgetTitle)) {
                        throw new TypeError("WidgetTitleCodec.transformTo: expected WidgetTitle");
                    }
                    return t.value;
                },
                transformFrom(wire) {
                    return new WidgetTitle(wire);
                }
            };
            """;
}
