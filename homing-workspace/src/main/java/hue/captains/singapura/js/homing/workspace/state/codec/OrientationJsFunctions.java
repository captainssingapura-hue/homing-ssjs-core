package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.Orientation;

/** Hand-written codec for {@link Orientation} — wire form is the enum constant name. */
public final class OrientationJsFunctions implements FunctionsCodeGen {

    public static final OrientationJsFunctions INSTANCE = new OrientationJsFunctions();

    private OrientationJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != Orientation.class) {
            throw new IllegalArgumentException(
                    "OrientationJsFunctions only handles Orientation; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const OrientationCodec = {
                transformTo(o) {
                    if (o !== Orientation.HORIZONTAL && o !== Orientation.VERTICAL) {
                        throw new TypeError("OrientationCodec.transformTo: expected Orientation enum value");
                    }
                    return o.name;
                },
                transformFrom(wire) {
                    if (typeof wire !== 'string') {
                        throw new TypeError("OrientationCodec.transformFrom: expected string");
                    }
                    const o = Orientation[wire];
                    if (o === undefined || typeof o !== 'object') {
                        throw new RangeError("OrientationCodec.transformFrom: unknown Orientation '" + wire + "'");
                    }
                    return o;
                }
            };
            """;
}
