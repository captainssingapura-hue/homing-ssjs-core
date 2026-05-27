package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WidgetInstance;

/**
 * Hand-written codec class for {@link WidgetInstance} — composite
 * with five typed fields, one of which ({@code params}) is polymorphic
 * via the {@code Widget._Param} interface. The JS class enforces field
 * types; the codec ({@link WidgetInstanceJsFunctions}) uses
 * {@code WidgetParamsCodecRegistry} to dispatch params encoding/decoding
 * by the widget's {@code kind}.
 */
public final class WidgetInstanceJsDefinition implements DefinitionCodeGen {

    public static final WidgetInstanceJsDefinition INSTANCE = new WidgetInstanceJsDefinition();

    private WidgetInstanceJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WidgetInstance.class) {
            throw new IllegalArgumentException(
                    "WidgetInstanceJsDefinition only handles WidgetInstance; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            class WidgetInstance {
                constructor(id, kind, params, title, location) {
                    if (!(id instanceof WidgetInstanceId)) {
                        throw new TypeError("WidgetInstance.id: expected WidgetInstanceId");
                    }
                    if (!(kind instanceof WidgetKind)) {
                        throw new TypeError("WidgetInstance.kind: expected WidgetKind");
                    }
                    if (params == null) {
                        throw new TypeError("WidgetInstance.params: required");
                    }
                    if (!(title instanceof WidgetTitle)) {
                        throw new TypeError("WidgetInstance.title: expected WidgetTitle");
                    }
                    if (!(location instanceof WidgetLocation.InPane)
                            && !(location instanceof WidgetLocation.InModal)) {
                        throw new TypeError("WidgetInstance.location: expected WidgetLocation variant");
                    }
                    this.id = id;
                    this.kind = kind;
                    this.params = params;
                    this.title = title;
                    this.location = location;
                    Object.freeze(this);
                }
            }
            """;
}
