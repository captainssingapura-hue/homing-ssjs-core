package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WidgetKind;

/** Hand-written codec class for {@link WidgetKind} — kebab-case registry key. */
public final class WidgetKindJsDefinition implements DefinitionCodeGen {

    public static final WidgetKindJsDefinition INSTANCE = new WidgetKindJsDefinition();

    private WidgetKindJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WidgetKind.class) {
            throw new IllegalArgumentException(
                    "WidgetKindJsDefinition only handles WidgetKind; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            class WidgetKind {
                constructor(value) {
                    if (typeof value !== 'string') {
                        throw new TypeError("WidgetKind.value: must be string");
                    }
                    if (!/^[A-Za-z0-9_-]+$/.test(value)) {
                        throw new RangeError(
                            "WidgetKind.value '" + value + "' — identifier-shaped required (letters, digits, hyphen, underscore)");
                    }
                    this.value = value;
                    Object.freeze(this);
                }
                toString() { return this.value; }
            }
            """;
}
