package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.LayoutNode;

/**
 * Hand-written codec for {@link LayoutNode}. Tagged-union dispatch on
 * {@code kind} like {@code WidgetLocation}, but with self-referential
 * recursion in the Split variant — the codec calls itself for
 * {@code first} and {@code second} sub-trees.
 */
public final class LayoutNodeJsFunctions implements FunctionsCodeGen {

    public static final LayoutNodeJsFunctions INSTANCE = new LayoutNodeJsFunctions();

    private LayoutNodeJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != LayoutNode.class) {
            throw new IllegalArgumentException(
                    "LayoutNodeJsFunctions only handles LayoutNode; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const LayoutNodeCodec = {
                transformTo(node) {
                    if (node instanceof LayoutNode.Leaf) {
                        return {
                            kind:   'Leaf',
                            paneId: PaneIdCodec.transformTo(node.paneId)
                        };
                    }
                    if (node instanceof LayoutNode.Split) {
                        return {
                            kind:        'Split',
                            orientation: OrientationCodec.transformTo(node.orientation),
                            ratio:       node.ratio,
                            first:       LayoutNodeCodec.transformTo(node.first),
                            second:      LayoutNodeCodec.transformTo(node.second)
                        };
                    }
                    throw new TypeError("LayoutNodeCodec.transformTo: not a LayoutNode variant");
                },
                transformFrom(wire) {
                    if (wire == null || typeof wire.kind !== 'string') {
                        throw new TypeError("LayoutNodeCodec.transformFrom: wire missing 'kind' discriminator");
                    }
                    switch (wire.kind) {
                        case 'Leaf':
                            return new LayoutNode.Leaf(PaneIdCodec.transformFrom(wire.paneId));
                        case 'Split':
                            return new LayoutNode.Split(
                                OrientationCodec.transformFrom(wire.orientation),
                                wire.ratio,
                                LayoutNodeCodec.transformFrom(wire.first),
                                LayoutNodeCodec.transformFrom(wire.second));
                        default:
                            throw new TypeError(
                                "LayoutNodeCodec.transformFrom: unknown kind '" + wire.kind + "'");
                    }
                }
            };
            """;
}
