package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.LayoutNode;

/**
 * Hand-written codec class for {@link LayoutNode} — sealed (Leaf | Split),
 * <b>recursive</b> in the Split variant. Mirrors WidgetLocation's
 * tagged-union shape, with the structural extension that Split children
 * are themselves LayoutNode values; the matching codec
 * ({@link LayoutNodeJsFunctions}) is self-referential.
 */
public final class LayoutNodeJsDefinition implements DefinitionCodeGen {

    public static final LayoutNodeJsDefinition INSTANCE = new LayoutNodeJsDefinition();

    private LayoutNodeJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != LayoutNode.class) {
            throw new IllegalArgumentException(
                    "LayoutNodeJsDefinition only handles LayoutNode; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const LayoutNode = {
                Leaf: class Leaf {
                    constructor(paneId) {
                        if (!(paneId instanceof PaneId)) {
                            throw new TypeError("LayoutNode.Leaf.paneId: expected PaneId");
                        }
                        this.paneId = paneId;
                        Object.freeze(this);
                    }
                },
                Split: class Split {
                    constructor(orientation, ratio, first, second) {
                        if (orientation !== Orientation.HORIZONTAL && orientation !== Orientation.VERTICAL) {
                            throw new TypeError("LayoutNode.Split.orientation: expected Orientation");
                        }
                        if (typeof ratio !== 'number' || !(ratio > 0.0 && ratio < 1.0)) {
                            throw new RangeError(
                                "LayoutNode.Split.ratio must be strictly between 0.0 and 1.0, got " + ratio);
                        }
                        if (!(first instanceof LayoutNode.Leaf) && !(first instanceof LayoutNode.Split)) {
                            throw new TypeError("LayoutNode.Split.first: expected LayoutNode");
                        }
                        if (!(second instanceof LayoutNode.Leaf) && !(second instanceof LayoutNode.Split)) {
                            throw new TypeError("LayoutNode.Split.second: expected LayoutNode");
                        }
                        this.orientation = orientation;
                        this.ratio = ratio;
                        this.first = first;
                        this.second = second;
                        Object.freeze(this);
                    }
                }
            };
            Object.freeze(LayoutNode);
            """;
}
