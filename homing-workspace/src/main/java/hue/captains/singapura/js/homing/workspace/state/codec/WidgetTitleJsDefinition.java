package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WidgetTitle;

/** Hand-written codec class for {@link WidgetTitle} — free-text display label. */
public final class WidgetTitleJsDefinition implements DefinitionCodeGen {

    public static final WidgetTitleJsDefinition INSTANCE = new WidgetTitleJsDefinition();

    private WidgetTitleJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WidgetTitle.class) {
            throw new IllegalArgumentException(
                    "WidgetTitleJsDefinition only handles WidgetTitle; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            class WidgetTitle {
                constructor(value) {
                    if (typeof value !== 'string') {
                        throw new TypeError("WidgetTitle.value: must be string");
                    }
                    this.value = value;
                    Object.freeze(this);
                }
                toString() { return this.value; }
            }
            """;
}
