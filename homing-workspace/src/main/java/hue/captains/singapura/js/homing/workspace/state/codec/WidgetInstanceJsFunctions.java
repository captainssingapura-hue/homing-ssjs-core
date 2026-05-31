package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WidgetInstance;

/**
 * Hand-written codec for {@link WidgetInstance}. Composite encoder:
 * each typed field goes through its own codec. The {@code params} field
 * is polymorphic — its codec is looked up at encode and decode time via
 * {@code WidgetParamsCodecRegistry}, keyed by the sibling {@code kind}'s
 * underlying value.
 *
 * <p>Note the asymmetry in the decode path: the {@code kind} must be
 * decoded <i>first</i> so the params codec lookup has a key; the order
 * of construction is significant.</p>
 */
public final class WidgetInstanceJsFunctions implements FunctionsCodeGen {

    public static final WidgetInstanceJsFunctions INSTANCE = new WidgetInstanceJsFunctions();

    private WidgetInstanceJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WidgetInstance.class) {
            throw new IllegalArgumentException(
                    "WidgetInstanceJsFunctions only handles WidgetInstance; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const WidgetInstanceCodec = {
                transformTo(w) {
                    if (!(w instanceof WidgetInstance)) {
                        throw new TypeError("WidgetInstanceCodec.transformTo: expected WidgetInstance");
                    }
                    const paramsCodec = WidgetParamsCodecRegistry.get(w.kind.value);
                    return {
                        id:       WidgetInstanceIdCodec.transformTo(w.id),
                        kind:     WidgetKindCodec.transformTo(w.kind),
                        params:   paramsCodec.transformTo(w.params),
                        title:    WidgetTitleCodec.transformTo(w.title),
                        location: WidgetLocationCodec.transformTo(w.location)
                    };
                },
                transformFrom(wire) {
                    const kind = WidgetKindCodec.transformFrom(wire.kind);
                    const paramsCodec = WidgetParamsCodecRegistry.get(kind.value);
                    return new WidgetInstance(
                        WidgetInstanceIdCodec.transformFrom(wire.id),
                        kind,
                        paramsCodec.transformFrom(wire.params),
                        WidgetTitleCodec.transformFrom(wire.title),
                        WidgetLocationCodec.transformFrom(wire.location));
                }
            };
            """;
}
